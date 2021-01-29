package com.kylecorry.healthsense.heart.infrastructure

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kylecorry.healthsense.heart.domain.BloodPressure
import com.kylecorry.healthsense.heart.domain.BloodPressureReading
import java.time.Instant

@Entity(tableName = "blood_pressure")
class BloodPressureReadingEntity(
    @ColumnInfo(name = "systolic") val systolic: Int,
    @ColumnInfo(name = "diastolic") val diastolic: Int,
    @ColumnInfo(name = "time") val time: Long
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0

    fun toReading(): BloodPressureReading {
        return BloodPressureReading(
            id,
            BloodPressure(systolic, diastolic),
            Instant.ofEpochMilli(time)
        )
    }

    companion object {
        fun from(reading: BloodPressureReading): BloodPressureReadingEntity {
            return BloodPressureReadingEntity(
                reading.pressure.systolic,
                reading.pressure.diastolic,
                reading.time.toEpochMilli()
            ).also {
                it.id = reading.id
            }
        }
    }

}