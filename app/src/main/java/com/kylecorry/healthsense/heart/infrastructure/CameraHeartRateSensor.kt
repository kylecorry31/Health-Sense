package com.kylecorry.healthsense.heart.infrastructure

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.media.Image
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.kylecorry.healthsense.heart.domain.LowPassFilter
import com.kylecorry.trailsensecore.infrastructure.sensors.AbstractSensor
import java.io.ByteArrayOutputStream
import java.time.Duration
import java.time.Instant
import kotlin.math.roundToInt

class CameraHeartRateSensor(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) : AbstractSensor() {

    private val readings = mutableListOf<Pair<Instant, Float>>()
    private val maxReadingInterval = Duration.ofSeconds(10)
    private val alpha = 0.5f
    private var filter = LowPassFilter(alpha, 0f)
    private var _bpm = 0

    private var cameraProvider: ProcessCameraProvider? = null

    val pulseWave: List<Pair<Instant, Float>>
        get() = readings.toList()

    var peaks = mutableListOf<Instant>()

    val bpm: Int
        get() = _bpm

    override val hasValidReading: Boolean
        get() = true

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun startImpl() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(200, 200))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context), { image ->
                val bitmap = image.image?.toBitmap()
                if (bitmap != null) {
                    var averageR = 0f
                    val total = bitmap.width * bitmap.height.toFloat()
                    for (w in 0 until bitmap.width) {
                        for (h in 0 until bitmap.height) {
                            averageR += Color.red(bitmap.getPixel(w, h)) / total
                        }
                    }

                    if (averageR > 100) {
                        if (readings.isEmpty()) {
                            filter = LowPassFilter(alpha, averageR)
                            readings.add(Pair(Instant.now(), averageR))
                        } else {
                            readings.add(Pair(Instant.now(), filter.filter(averageR)))
                        }

                        while (Duration.between(
                                readings.first().first,
                                readings.last().first
                            ) > maxReadingInterval
                        ) {
                            readings.removeAt(0)
                        }

                        calculateHeartRate()
                        notifyListeners()
                    }

                }
                image.close()
            })

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val camera =
                cameraProvider?.bindToLifecycle(lifecycleOwner, cameraSelector, imageAnalysis)
            val controls = camera?.cameraControl
            controls?.enableTorch(true)
            controls?.setExposureCompensationIndex(0)
            controls?.cancelFocusAndMetering()

        }, ContextCompat.getMainExecutor(context))
    }

    private fun calculateHeartRate() {
        val dt = Duration.between(readings.first().first, readings.last().first)

        if (dt < Duration.ofSeconds(5)) {
            _bpm = 0
            return
        }

        val beats = countPeaks(readings, Duration.ofMillis(400))
        _bpm = (beats / (dt.toMillis() / 1000f / 60f)).roundToInt()
    }

    private fun countPeaks(
        readings: List<Pair<Instant, Float>>,
        minDuration: Duration = Duration.ofMillis(300)
    ): Int {
        var lastTime = Instant.MIN
        peaks.clear()
        for (i in 1 until readings.size - 1) {
            val r1 = readings[i - 1].second
            val r2 = readings[i].second
            val r3 = readings[i + 1].second

            if (maxOf(r1, r2, r3) == r2 && Duration.between(
                    lastTime,
                    readings[i].first
                ) > minDuration
            ) {
                lastTime = readings[i].first
                peaks.add(lastTime)
            }
        }

        return peaks.size
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

    override fun stopImpl() {
        cameraProvider?.unbindAll()
        cameraProvider = null
        readings.clear()
    }
}