<<<<<<<< HEAD:core/database/src/main/java/dev/sadakat/screentimetracker/core/database/dao/ScreenUnlockDao.kt
package dev.sadakat.screentimetracker.core.database.dao
========
package dev.sadakat.screentimetracker.data.local.dao
import dev.sadakat.screentimetracker.data.local.entities.*
>>>>>>>> origin/detached3:backup_src_20250918_005155/main/java/dev/sadakat/screentimetracker/data/local/dao/ScreenUnlockDao.kt

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.sadakat.screentimetracker.core.database.entities.ScreenUnlockEvent
import dev.sadakat.screentimetracker.core.database.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreenUnlockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnlockEvent(event: ScreenUnlockEvent)

    @Query("SELECT * FROM screen_unlock_events ORDER BY timestamp DESC")
    fun getAllUnlockEvents(): Flow<List<ScreenUnlockEvent>>

    @Query("SELECT COUNT(id) FROM screen_unlock_events WHERE timestamp >= :sinceTimestamp")
    fun getUnlockCountSince(sinceTimestamp: Long): Flow<Int>

    @Query("SELECT COUNT(id) FROM screen_unlock_events WHERE timestamp >= :dayStartMillis AND timestamp < :dayEndMillis")
    fun getUnlockCountForDay(dayStartMillis: Long, dayEndMillis: Long): Flow<Int>

    // Export methods
    @Query("SELECT * FROM screen_unlock_events ORDER BY timestamp ASC")
    suspend fun getAllScreenUnlockEventsForExport(): List<ScreenUnlockEvent>
}
