package com.kylecorry.healthsense.medicine.domain

data class Medicine(val id: Long, val name: String, val frequency: Frequency, val isReminding: Boolean = false, val dosage: String?, val foodRequirement: MedicineFoodRequirement?)
