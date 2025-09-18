<<<<<<<< HEAD:core/database/src/main/java/dev/sadakat/screentimetracker/core/database/dao/DailyScreenUnlockSummaryDao.kt
package dev.sadakat.screentimetracker.core.database.dao
========
package dev.sadakat.screentimetracker.data.local.dao
import dev.sadakat.screentimetracker.data.local.entities.*
>>>>>>>> origin/detached3:backup_src_20250918_005155/main/java/dev/sadakat/screentimetracker/data/local/dao/DailyScreenUnlockSummaryDao.kt

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.sadakat.screentimetracker.core.database.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyScreenUnlockSummaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(summaries: List<DailyScreenUnlockSummary>) // For batch insert from worker

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(summary: DailyScreenUnlockSummary)

    // Get summary for a specific date
    @Query("SELECT * FROM daily_screen_unlock_summary WHERE dateMillis = :dateMillis")
    fun getSummaryForDate(dateMillis: Long): Flow<DailyScreenUnlockSummary?> // Nullable if no entry for date

    // Get summaries for a date range (for trends)
    @Query("SELECT * FROM daily_screen_unlock_summary WHERE dateMillis >= :startDateMillis AND dateMillis <= :endDateMillis ORDER BY dateMillis ASC")
    fun getSummariesInRange(startDateMillis: Long, endDateMillis: Long): Flow<List<DailyScreenUnlockSummary>>

    // Export methods
    @Query("SELECT * FROM daily_screen_unlock_summary ORDER BY dateMillis ASC")
    suspend fun getAllDailyScreenUnlockSummariesForExport(): List<DailyScreenUnlockSummary>
}
