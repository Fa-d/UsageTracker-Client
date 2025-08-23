package dev.sadakat.screentimetracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
