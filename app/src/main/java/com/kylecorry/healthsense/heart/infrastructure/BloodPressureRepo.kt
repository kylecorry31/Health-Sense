package com.kylecorry.healthsense.heart.infrastructure

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.kylecorry.healthsense.heart.domain.BloodPressureReading
import com.kylecorry.healthsense.persistence.AppDatabase

class BloodPressureRepo private constructor(private val context: Context) {

    private val bloodPressureDao by lazy {
        AppDatabase.getInstance(context.applicationContext).bloodPressureDao()
    }

    fun getAll(): LiveData<List<BloodPressureReading>> {
        return Transformations.map(bloodPressureDao.getAll()) {
            it.map { reading -> reading.toReading() }.sortedByDescending { reading -> reading.time }
        }
    }

    suspend fun get(): List<BloodPressureReading> {
        return bloodPressureDao.get().map { it.toReading() }
    }

    suspend fun delete(reading: BloodPressureReading) {
        return bloodPressureDao.delete(BloodPressureReadingEntity.from(reading))
    }

    suspend fun add(reading: BloodPressureReading) {
        if (reading.id == 0L) {
            bloodPressureDao.insert(BloodPressureReadingEntity.from(reading))
        } else {
            bloodPressureDao.update(BloodPressureReadingEntity.from(reading))
        }
    }

    companion object {
        private var instance: BloodPressureRepo? = null

        @Synchronized
        fun getInstance(context: Context): BloodPressureRepo {
            if (instance == null) {
                instance = BloodPressureRepo(context.applicationContext)
            }
            return instance!!
        }
    }

}