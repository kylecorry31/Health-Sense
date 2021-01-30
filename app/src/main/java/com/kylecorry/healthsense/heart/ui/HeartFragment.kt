package com.kylecorry.healthsense.heart.ui

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.*
import android.media.Image
import android.os.Bundle
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.kylecorry.healthsense.R
import com.kylecorry.healthsense.databinding.FragmentHeartBinding
import com.kylecorry.healthsense.databinding.ListItemBloodPressureBinding
import com.kylecorry.healthsense.heart.domain.BloodPressure
import com.kylecorry.healthsense.heart.domain.BloodPressureReading
import com.kylecorry.healthsense.heart.domain.HeartService
import com.kylecorry.healthsense.heart.infrastructure.BloodPressureRepo
import com.kylecorry.healthsense.ui.FormatService
import com.kylecorry.trailsensecore.infrastructure.system.PermissionUtils
import com.kylecorry.trailsensecore.infrastructure.system.UiUtils
import com.kylecorry.trailsensecore.infrastructure.view.ListView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.kylecorry.healthsense.heart.domain.LowPassFilter
import java.io.ByteArrayOutputStream

class HeartFragment : Fragment() {

    private var _binding: FragmentHeartBinding? = null
    private val binding get() = _binding!!

    private val heartService = HeartService()

    private val bloodPressureRepo by lazy { BloodPressureRepo.getInstance(requireContext()) }
    private val formatService by lazy { FormatService(requireContext()) }

    private lateinit var heartChart: HeartBeatChart

    private val maxHeartValues = 100
    private var filter = LowPassFilter(0.5f, 0f)
    private val heartValues = mutableListOf<Float>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHeartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        heartChart = HeartBeatChart(binding.heartChart, UiUtils.color(requireContext(), R.color.colorPrimary))

        val listView = ListView<BloodPressureReading>(
            binding.bloodPressureHistory,
            R.layout.list_item_blood_pressure
        ) { readingView, reading ->
            val itemBinding = ListItemBloodPressureBinding.bind(readingView)
            itemBinding.pressure.text = formatService.formatPressure(reading.pressure)
            itemBinding.time.text = formatService.formatDateTime(reading.time)
            val classification = heartService.classifyBloodPressure(reading.pressure)
            itemBinding.pressureReadingClassification.text = formatService.formatPressureCategory(classification)
            readingView.setOnLongClickListener {
                UiUtils.alertWithCancel(
                    requireContext(),
                    getString(R.string.delete_reading),
                    formatService.formatDateTime(reading.time),
                    getString(R.string.dialog_ok),
                    getString(R.string.dialog_cancel)
                ) { cancelled ->
                    if (!cancelled) {
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                bloodPressureRepo.delete(reading)
                            }
                        }
                    }
                }
                true
            }
        }

        binding.heartRateBtn.setOnClickListener {
            PermissionUtils.requestPermissions(requireActivity(), listOf(Manifest.permission.CAMERA), 123)
            if (PermissionUtils.hasPermission(requireContext(), Manifest.permission.CAMERA)){
                monitorHeartRate()
            }
        }

        bloodPressureRepo.getAll().observe(viewLifecycleOwner, Observer { listView.setData(it) })

        binding.systolicEdit.addTextChangedListener {
            update()
        }

        binding.diastolicEdit.addTextChangedListener {
            update()
        }

        binding.logBpBtn.setOnClickListener {
            val systolic = binding.systolicEdit.text.toString().toIntOrNull()
            val diastolic = binding.diastolicEdit.text.toString().toIntOrNull()

            if (systolic != null && diastolic != null) {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        bloodPressureRepo.add(
                            BloodPressureReading(
                                0,
                                BloodPressure(systolic, diastolic),
                                Instant.now()
                            )
                        )
                    }
                }
                binding.systolicEdit.setText("")
                binding.diastolicEdit.setText("")
                update()
            }
        }

        update()
    }

    private fun update() {
        val systolic = binding.systolicEdit.text.toString().toIntOrNull()
        val diastolic = binding.diastolicEdit.text.toString().toIntOrNull()

        if (systolic == null || diastolic == null) {
            binding.pressureClassification.text = ""
            binding.logBpBtn.isEnabled = false
            return
        }

        binding.logBpBtn.isEnabled = true
        val classification = heartService.classifyBloodPressure(BloodPressure(systolic, diastolic))
        binding.pressureClassification.text = formatService.formatPressureCategory(classification)

    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun monitorHeartRate(){
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(200, 200))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), { image ->
                val bitmap = image.image?.toBitmap()
                if (bitmap != null) {
                    var averageR = 0f
                    val total = bitmap.width * bitmap.height.toFloat()
                    for (w in 0 until bitmap.width){
                        for (h in 0 until bitmap.height){
                            averageR += Color.red(bitmap.getPixel(w, h)) / total
                        }
                    }

                    if (heartValues.isEmpty()){
                        filter = LowPassFilter(0.5f, averageR)
                        heartValues.add(averageR)
                    } else {
                        heartValues.add(filter.filter(averageR))
                    }

                    if (heartValues.size > maxHeartValues){
                        heartValues.removeAt(0)
                    }
                    heartChart.plot(heartValues)
                }
                image.close()
            })

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val camera = cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, imageAnalysis)
            val controls = camera.cameraControl
            controls.enableTorch(true)
            controls.setExposureCompensationIndex(0)
            controls.cancelFocusAndMetering()

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun Image.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer // Y
        val uBuffer = planes[1].buffer // U
        val vBuffer = planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

}