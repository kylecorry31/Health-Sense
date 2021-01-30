package com.kylecorry.healthsense.heart.ui

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.kylecorry.healthsense.heart.infrastructure.CameraHeartRateSensor

class HeartFragment : Fragment() {

    private var _binding: FragmentHeartBinding? = null
    private val binding get() = _binding!!

    private val heartService = HeartService()

    private val bloodPressureRepo by lazy { BloodPressureRepo.getInstance(requireContext()) }
    private val formatService by lazy { FormatService(requireContext()) }

    private lateinit var heartChart: HeartBeatChart

    private val heartRateSensor by lazy { CameraHeartRateSensor(requireContext(), viewLifecycleOwner) }
    private var monitoring = false

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
            if (PermissionUtils.hasPermission(requireContext(), Manifest.permission.CAMERA) && !monitoring){
                monitoring = true
                binding.bpm.text = getString(R.string.calculating)
                heartChart.plot(listOf())
                heartRateSensor.start(this::onHeartRateUpdate)
            } else {
                monitoring = false
                binding.bpm.text = ""
                heartChart.plot(listOf())
                heartRateSensor.stop(this::onHeartRateUpdate)
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

    override fun onPause() {
        super.onPause()
        monitoring = false
        heartRateSensor.stop(this::onHeartRateUpdate)
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

    private fun onHeartRateUpdate(): Boolean {
        heartChart.plot(heartRateSensor.pulseWave.map { it.second }, heartRateSensor.heartBeats.map { peak -> heartRateSensor.pulseWave.indexOfFirst { it.first == peak } })
        if (heartRateSensor.bpm != 0){
            binding.bpm.text = getString(R.string.bpm, heartRateSensor.bpm.toString())
        } else {
            binding.bpm.text = ""
        }
        return true
    }



}