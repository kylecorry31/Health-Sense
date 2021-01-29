package com.kylecorry.healthsense.medicine.infrastructure

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kylecorry.healthsense.medicine.domain.Frequency
import com.kylecorry.healthsense.medicine.domain.Medicine
import com.kylecorry.healthsense.medicine.domain.MedicineFoodRequirement
import com.kylecorry.healthsense.medicine.domain.TimeOfDay
import java.time.DayOfWeek

@Entity(tableName = "medicine")
class MedicineEntity(
    val name: String,
    val sunday: Boolean,
    val monday: Boolean,
    val tuesday: Boolean,
    val wednesday: Boolean,
    val thursday: Boolean,
    val friday: Boolean,
    val saturday: Boolean,
    val morning: Boolean,
    val midday: Boolean,
    val evening: Boolean,
    val night: Boolean,
    val isReminding: Boolean = false,
    val dosage: String?,
    val foodRequirement: Int?
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0

    fun toMedicine(): Medicine {
        val days = mutableListOf<DayOfWeek>()
        if (sunday) days.add(DayOfWeek.SUNDAY)
        if (monday) days.add(DayOfWeek.MONDAY)
        if (tuesday) days.add(DayOfWeek.TUESDAY)
        if (wednesday) days.add(DayOfWeek.WEDNESDAY)
        if (thursday) days.add(DayOfWeek.THURSDAY)
        if (friday) days.add(DayOfWeek.FRIDAY)
        if (saturday) days.add(DayOfWeek.SATURDAY)

        val times = mutableListOf<TimeOfDay>()
        if (morning) times.add(TimeOfDay.Morning)
        if (midday) times.add(TimeOfDay.Midday)
        if (evening) times.add(TimeOfDay.Evening)
        if (night) times.add(TimeOfDay.Bedtime)

        return Medicine(
            id,
            name,
            Frequency(days, times),
            isReminding,
            dosage,
            if (foodRequirement == null) null else MedicineFoodRequirement.values()
                .first { it.id == foodRequirement })
    }

    companion object {
        fun from(med: Medicine): MedicineEntity {
            return MedicineEntity(
                med.name,
                sunday = med.frequency.days.contains(DayOfWeek.SUNDAY),
                monday = med.frequency.days.contains(DayOfWeek.MONDAY),
                tuesday = med.frequency.days.contains(DayOfWeek.TUESDAY),
                wednesday = med.frequency.days.contains(DayOfWeek.WEDNESDAY),
                thursday = med.frequency.days.contains(DayOfWeek.THURSDAY),
                friday = med.frequency.days.contains(DayOfWeek.FRIDAY),
                saturday = med.frequency.days.contains(DayOfWeek.SATURDAY),
                morning = med.frequency.times.contains(TimeOfDay.Morning),
                midday = med.frequency.times.contains(TimeOfDay.Midday),
                evening = med.frequency.times.contains(TimeOfDay.Evening),
                night = med.frequency.times.contains(TimeOfDay.Bedtime),
                med.isReminding,
                med.dosage,
                med.foodRequirement?.id
            ).also {
                it.id = med.id
            }
        }
    }


}