package com.kylecorry.healthsense.ui

import android.content.Context
import android.text.format.DateUtils
import com.kylecorry.healthsense.R
import com.kylecorry.healthsense.heart.domain.BloodPressure
import com.kylecorry.healthsense.heart.domain.BloodPressureCategory
import java.time.Instant

class FormatService(private val context: Context) {

    fun formatPressure(pressure: BloodPressure): String {
        return "${pressure.systolic} / ${pressure.diastolic}"
    }

    fun formatDateTime(instant: Instant): String {
        return DateUtils.formatDateTime(
            context, instant.toEpochMilli(),
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_SHOW_TIME
        )
    }

    fun formatPressureCategory(classification: BloodPressureCategory): String {
        return when (classification) {
            BloodPressureCategory.Normal -> context.getString(R.string.normal)
            BloodPressureCategory.Elevated -> context.getString(R.string.elevated)
            BloodPressureCategory.Hypertension1 -> context.getString(R.string.high_blood_pressure_stage_1)
            BloodPressureCategory.Hypertension2 -> context.getString(R.string.high_blood_pressure_stage_2)
            BloodPressureCategory.HypertensiveCrisis -> context.getString(R.string.extreme_high_blood_pressure)
            BloodPressureCategory.Hypotension -> context.getString(R.string.low_blood_pressure)
        }
    }

}