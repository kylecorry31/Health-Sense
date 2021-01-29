package com.kylecorry.healthsense.medicine.infrastructure

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicine")
    fun getAll(): LiveData<List<MedicineEntity>>

    @Query("SELECT * FROM medicine")
    suspend fun get(): List<MedicineEntity>

    @Delete
    suspend fun delete(medicine: MedicineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medicine: MedicineEntity): Long

    @Update
    suspend fun update(medicine: MedicineEntity)
}