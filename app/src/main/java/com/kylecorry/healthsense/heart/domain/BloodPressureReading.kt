package com.kylecorry.healthsense.heart.domain

import java.time.Instant

data class BloodPressureReading(val id: Long, val pressure: BloodPressure, val time: Instant)
