package com.kylecorry.healthsense.heart.infrastructure

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BloodPressureDao {
    @Query("SELECT * FROM blood_pressure")
    fun getAll(): LiveData<List<BloodPressureReadingEntity>>

    @Query("SELECT * FROM blood_pressure")
    suspend fun get(): List<BloodPressureReadingEntity>

    @Delete
    suspend fun delete(bp: BloodPressureReadingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bp: BloodPressureReadingEntity): Long

    @Update
    suspend fun update(bp: BloodPressureReadingEntity)
}