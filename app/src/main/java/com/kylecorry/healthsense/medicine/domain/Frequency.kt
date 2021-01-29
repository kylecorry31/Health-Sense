package com.kylecorry.healthsense.medicine.domain

import java.time.DayOfWeek

data class Frequency(val days: List<DayOfWeek>, val times: List<TimeOfDay>)
