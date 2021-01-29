package com.kylecorry.healthsense.medicine.infrastructure

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.kylecorry.healthsense.medicine.domain.Medicine
import com.kylecorry.healthsense.persistence.AppDatabase

class MedicineRepo private constructor(private val context: Context) {

    private val medicineDao by lazy {
        AppDatabase.getInstance(context.applicationContext).medicineDao()
    }

    fun getAll(): LiveData<List<Medicine>> {
        return Transformations.map(medicineDao.getAll()) {
            it.map { med -> med.toMedicine() }
        }
    }

    suspend fun get(): List<Medicine> {
        return medicineDao.get().map { it.toMedicine() }
    }

    suspend fun delete(med: Medicine) {
        return medicineDao.delete(MedicineEntity.from(med))
    }

    suspend fun add(med: Medicine) {
        if (med.id == 0L) {
            medicineDao.insert(MedicineEntity.from(med))
        } else {
            medicineDao.update(MedicineEntity.from(med))
        }
    }

    companion object {
        private var instance: MedicineRepo? = null

        @Synchronized
        fun getInstance(context: Context): MedicineRepo {
            if (instance == null) {
                instance = MedicineRepo(context.applicationContext)
            }
            return instance!!
        }
    }

}