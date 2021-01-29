package com.kylecorry.healthsense.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kylecorry.healthsense.heart.infrastructure.BloodPressureDao
import com.kylecorry.healthsense.heart.infrastructure.BloodPressureReadingEntity
import com.kylecorry.healthsense.medicine.infrastructure.MedicineDao
import com.kylecorry.healthsense.medicine.infrastructure.MedicineEntity

@Database(
    entities = [BloodPressureReadingEntity::class, MedicineEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bloodPressureDao(): BloodPressureDao
    abstract fun medicineDao(): MedicineDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "health_sense")
                .build()
        }
    }
}