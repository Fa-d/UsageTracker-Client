package dev.sadakat.screentimetracker.core.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import dev.sadakat.screentimetracker.core.database.model.AppUsageEntity

/**
 * Data Access Object for app usage events
 */
@Dao
interface AppUsageDao {

    @Query("SELECT * FROM app_usage_events ORDER BY timestamp DESC")
    fun getAllUsageEvents(): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage_events WHERE packageName = :packageName ORDER BY timestamp DESC")
    fun getUsageEventsForApp(packageName: String): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage_events WHERE year = :year AND dayOfYear = :dayOfYear ORDER BY timestamp ASC")
    suspend fun getUsageEventsForDay(year: Int, dayOfYear: Int): List<AppUsageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageEvent(event: AppUsageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageEvents(events: List<AppUsageEntity>)

    @Delete
    suspend fun deleteUsageEvent(event: AppUsageEntity)

    @Query("DELETE FROM app_usage_events WHERE year < :year OR (year = :year AND dayOfYear < :dayOfYear)")
    suspend fun deleteOldUsageEvents(year: Int, dayOfYear: Int)
}