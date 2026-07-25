package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Query("SELECT * FROM step_records WHERE dateString = :date ORDER BY hour ASC")
    fun getStepsForDate(date: String): Flow<List<StepRecord>>

    @Query("SELECT * FROM step_records WHERE dateString = :date ORDER BY hour ASC")
    suspend fun getStepsForDateSync(date: String): List<StepRecord>

    @Query("SELECT * FROM step_records WHERE dateString BETWEEN :startDate AND :endDate ORDER BY dateString ASC, hour ASC")
    fun getStepsBetweenDates(startDate: String, endDate: String): Flow<List<StepRecord>>

    @Query("SELECT * FROM step_records WHERE dateString BETWEEN :startDate AND :endDate ORDER BY dateString ASC, hour ASC")
    suspend fun getStepsBetweenDatesSync(startDate: String, endDate: String): List<StepRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStepRecord(record: StepRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<StepRecord>)

    @Query("SELECT * FROM step_records WHERE dateString = :date AND hour = :hour LIMIT 1")
    suspend fun getRecordByDateAndHour(date: String, hour: Int): StepRecord?

    @Query("SELECT COUNT(*) FROM step_records")
    suspend fun getRecordCount(): Int

    @Query("DELETE FROM step_records")
    suspend fun deleteAllSteps()
}
