package com.example.screentimetracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyAppSummaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(summaries: List<DailyAppSummary>) // For batch insert from worker

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(summary: DailyAppSummary) // For single insert if needed

    // Get all summaries for a specific date
    @Query("SELECT * FROM daily_app_summary WHERE dateMillis = :dateMillis ORDER BY totalDurationMillis DESC")
    fun getSummariesForDate(dateMillis: Long): Flow<List<DailyAppSummary>>

    // Get summaries for a specific package over a date range (for trends)
    @Query("SELECT * FROM daily_app_summary WHERE packageName = :packageName AND dateMillis >= :startDateMillis AND dateMillis <= :endDateMillis ORDER BY dateMillis ASC")
    fun getSummariesForAppInRange(packageName: String, startDateMillis: Long, endDateMillis: Long): Flow<List<DailyAppSummary>>

    // Get all summaries within a date range, good for overview charts
    @Query("SELECT * FROM daily_app_summary WHERE dateMillis >= :startDateMillis AND dateMillis <= :endDateMillis ORDER BY dateMillis ASC, totalDurationMillis DESC")
    fun getAllSummariesInRange(startDateMillis: Long, endDateMillis: Long): Flow<List<DailyAppSummary>>
}
