package com.kylecorry.healthsense.heart.domain

interface IHeartService {
    fun classifyBloodPressure(pressure: BloodPressure): BloodPressureCategory
    fun classifyPulseOxygen(percent: Float): PulseOxygenCategory
}