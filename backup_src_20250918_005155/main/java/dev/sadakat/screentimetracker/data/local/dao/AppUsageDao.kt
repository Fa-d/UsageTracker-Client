<<<<<<<< HEAD:core/database/src/main/java/dev/sadakat/screentimetracker/core/database/dao/AppUsageDao.kt
package dev.sadakat.screentimetracker.core.database.dao
========
package dev.sadakat.screentimetracker.data.local.dao
>>>>>>>> origin/detached3:backup_src_20250918_005155/main/java/dev/sadakat/screentimetracker/data/local/dao/AppUsageDao.kt

import androidx.room.Dao
import dev.sadakat.screentimetracker.data.local.entities.*
import dev.sadakat.screentimetracker.data.local.dto.*
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.sadakat.screentimetracker.core.database.entities.*
import dev.sadakat.screentimetracker.core.database.model.AppUsageEntity
import dev.sadakat.screentimetracker.core.database.query.AppOpenData
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppUsageEvent(event: AppUsageEntity)

    // Example query: Get all usage events for a specific package
    @Query("SELECT * FROM app_usage_events WHERE packageName = :packageName ORDER BY timestamp DESC")
    fun getUsageEventsForApp(packageName: String): Flow<List<AppUsageEntity>>

    // Example query: Get all app open events (could be refined)
    @Query("SELECT * FROM app_usage_events WHERE eventType = 'open' ORDER BY timestamp DESC")
    fun getAllAppOpenEvents(): Flow<List<AppUsageEntity>>

    // More specific queries for dashboard will be needed, e.g., counts per app
    @Query("SELECT packageName, COUNT(id) as openCount, 0 as lastOpenedTimestamp FROM app_usage_events WHERE eventType = 'open' AND timestamp >= :sinceTimestamp GROUP BY packageName")
    fun getAppOpenCountsSince(sinceTimestamp: Long): Flow<List<AppOpenData>>

    // Export methods
    @Query("SELECT * FROM app_usage_events ORDER BY timestamp DESC")
    suspend fun getAllAppUsageEventsForExport(): List<AppUsageEntity>
}


