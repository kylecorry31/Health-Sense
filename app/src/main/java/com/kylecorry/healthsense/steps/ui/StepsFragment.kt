package com.kylecorry.healthsense.steps.ui

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.math.MathUtils
import com.kylecorry.healthsense.BoundFragment
import com.kylecorry.healthsense.databinding.FragmentStepsBinding
import com.kylecorry.healthsense.steps.infrastructure.PedometerService
import com.kylecorry.trailsensecore.infrastructure.persistence.Cache
import com.kylecorry.trailsensecore.infrastructure.system.PermissionUtils
import com.kylecorry.trailsensecore.infrastructure.time.Intervalometer

class StepsFragment : BoundFragment<FragmentStepsBinding>() {

    private val cache by lazy { Cache(requireContext()) }

    private val intervalometer = Intervalometer {
        val steps = cache.getInt("steps") ?: 0
        val progress = MathUtils.clamp(100 * (steps / stepGoal.toFloat()), 0f, 100f).toInt()
        binding.stepProgress.progress = progress
        binding.steps.text = "$steps"
    }

    override fun generateBinding(
        layoutInflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentStepsBinding {
        return FragmentStepsBinding.inflate(layoutInflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q && !PermissionUtils.hasPermission(
                requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        ) {
            PermissionUtils.requestPermissions(
                requireActivity(),
                listOf(Manifest.permission.ACTIVITY_RECOGNITION),
                PERMISSION_REQUEST_CODE
            )
        } else {
            PedometerService.start(requireContext())
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && PermissionUtils.hasPermission(
                requireContext(),
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        ) {
            PedometerService.start(requireContext())
        }
    }

    override fun onResume() {
        super.onResume()
        intervalometer.interval(1000)
    }

    override fun onPause() {
        super.onPause()
        intervalometer.stop()
    }

    companion object {
        const val stepGoal = 5000
        const val PERMISSION_REQUEST_CODE = 2
    }

}