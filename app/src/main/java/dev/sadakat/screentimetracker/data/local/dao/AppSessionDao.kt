package dev.sadakat.screentimetracker.data.local.dao

import androidx.room.Dao
import dev.sadakat.screentimetracker.data.local.entities.*
// REMOVED: import dev.sadakat.screentimetracker.data.local.dto.*
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.sadakat.screentimetracker.data.local.dto.AppLastOpenedData
import dev.sadakat.screentimetracker.data.local.dto.AppSessionDataAggregate
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppSession(session: AppSessionEvent)

    // Get all sessions for a specific package within a time range
    @Query("SELECT * FROM app_session_events WHERE packageName = :packageName AND startTimeMillis >= :startTime AND endTimeMillis <= :endTime ORDER BY startTimeMillis DESC")
    fun getSessionsForAppInRange(packageName: String, startTime: Long, endTime: Long): Flow<List<AppSessionEvent>>

    // Get all sessions within a time range, ordered by start time
    @Query("SELECT * FROM app_session_events WHERE startTimeMillis >= :startTime AND endTimeMillis <= :endTime ORDER BY startTimeMillis DESC")
    fun getAllSessionsInRange(startTime: Long, endTime: Long): Flow<List<AppSessionEvent>>

    // Get all sessions (potentially for debugging or export)
    @Query("SELECT * FROM app_session_events ORDER BY startTimeMillis DESC")
    fun getAllSessions(): Flow<List<AppSessionEvent>>

    // Get total duration for a specific package within a time range
    @Query("SELECT SUM(durationMillis) FROM app_session_events WHERE packageName = :packageName AND startTimeMillis >= :startTime AND endTimeMillis <= :endTime")
    fun getTotalDurationForAppInRange(packageName: String, startTime: Long, endTime: Long): Flow<Long?> // Long? because SUM can be null if no rows

    // Export methods
    @Query("SELECT * FROM app_session_events ORDER BY startTimeMillis DESC")
    suspend fun getAllAppSessionEventsForExport(): List<AppSessionEvent>

    // Get total duration for all apps within a time range (total screen time from app sessions)
    @Query("SELECT SUM(durationMillis) FROM app_session_events WHERE startTimeMillis >= :startTime AND endTimeMillis <= :endTime")
    fun getTotalScreenTimeFromSessionsInRange(startTime: Long, endTime: Long): Flow<Long?>

    // Get aggregated data: package name, total duration, and open count (number of sessions) for a given day
    // This query is an sadakat of what might be used by the DailyAggregationWorker
    @Query("SELECT packageName, SUM(durationMillis) as totalDuration, COUNT(id) as sessionCount FROM app_session_events WHERE startTimeMillis >= :dayStartMillis AND startTimeMillis < :dayEndMillis GROUP BY packageName")
    fun getAggregatedSessionDataForDay(dayStartMillis: Long, dayEndMillis: Long): Flow<List<AppSessionDataAggregate>>

    // Get the latest end time for each package within a time range
    @Query("SELECT packageName, MAX(endTimeMillis) as lastOpenedTimestamp FROM app_session_events WHERE startTimeMillis >= :startTime AND endTimeMillis <= :endTime GROUP BY packageName")
    fun getLastOpenedTimestampsForAppsInRange(startTime: Long, endTime: Long): Flow<List<AppLastOpenedData>>
    
    // Suspend version for progressive limits calculation
    @Query("SELECT * FROM app_session_events WHERE packageName = :packageName AND startTimeMillis >= :startTime AND endTimeMillis <= :endTime ORDER BY startTimeMillis DESC")
    suspend fun getSessionsForAppInRangeOnce(packageName: String, startTime: Long, endTime: Long): List<AppSessionEvent>
}


