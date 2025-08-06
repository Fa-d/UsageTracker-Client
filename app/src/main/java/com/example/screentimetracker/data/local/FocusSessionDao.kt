package com.example.screentimetracker.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    fun getAllFocusSessions(): Flow<List<FocusSession>>

    @Query("SELECT * FROM focus_sessions WHERE startTime >= :startTime AND endTime <= :endTime ORDER BY startTime DESC")
    fun getFocusSessionsInRange(startTime: Long, endTime: Long): Flow<List<FocusSession>>

    @Query("SELECT * FROM focus_sessions WHERE DATE(startTime/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch')")
    suspend fun getFocusSessionsForDate(date: Long): List<FocusSession>

    @Query("SELECT COUNT(*) FROM focus_sessions WHERE wasSuccessful = 1 AND startTime >= :startTime")
    suspend fun getSuccessfulSessionsCount(startTime: Long): Int

    @Query("SELECT AVG(actualDurationMillis) FROM focus_sessions WHERE wasSuccessful = 1 AND startTime >= :startTime")
    suspend fun getAverageFocusSessionDuration(startTime: Long): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(focusSession: FocusSession): Long

    @Update
    suspend fun updateFocusSession(focusSession: FocusSession)

    @Query("UPDATE focus_sessions SET endTime = :endTime, actualDurationMillis = :actualDuration, wasSuccessful = :wasSuccessful, interruptionCount = :interruptionCount WHERE id = :id")
    suspend fun completeFocusSession(id: Long, endTime: Long, actualDuration: Long, wasSuccessful: Boolean, interruptionCount: Int)

    @Delete
    suspend fun deleteFocusSession(focusSession: FocusSession)
}