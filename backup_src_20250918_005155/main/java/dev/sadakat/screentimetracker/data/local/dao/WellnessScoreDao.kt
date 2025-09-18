<<<<<<<< HEAD:core/database/src/main/java/dev/sadakat/screentimetracker/core/database/dao/WellnessScoreDao.kt
package dev.sadakat.screentimetracker.core.database.dao
========
package dev.sadakat.screentimetracker.data.local.dao
import dev.sadakat.screentimetracker.data.local.entities.*
>>>>>>>> origin/detached3:backup_src_20250918_005155/main/java/dev/sadakat/screentimetracker/data/local/dao/WellnessScoreDao.kt

import androidx.room.*
import dev.sadakat.screentimetracker.core.database.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WellnessScoreDao {
    @Query("SELECT * FROM wellness_scores ORDER BY date DESC")
    fun getAllWellnessScores(): Flow<List<WellnessScore>>

    @Query("SELECT * FROM wellness_scores WHERE date = :date")
    suspend fun getWellnessScoreForDate(date: Long): WellnessScore?

    @Query("SELECT * FROM wellness_scores ORDER BY date DESC LIMIT 1")
    suspend fun getLatestWellnessScore(): WellnessScore?

    @Query("SELECT * FROM wellness_scores WHERE date >= :startDate ORDER BY date ASC LIMIT :limit")
    suspend fun getWellnessScoresFromDate(startDate: Long, limit: Int): List<WellnessScore>

    @Query("SELECT AVG(totalScore) FROM wellness_scores WHERE date >= :startDate")
    suspend fun getAverageScoreFromDate(startDate: Long): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWellnessScore(wellnessScore: WellnessScore)

    @Update
    suspend fun updateWellnessScore(wellnessScore: WellnessScore)

    @Query("DELETE FROM wellness_scores WHERE date < :cutoffDate")
    suspend fun deleteOldWellnessScores(cutoffDate: Long)

    // Export methods
    @Query("SELECT * FROM wellness_scores ORDER BY date ASC")
    suspend fun getAllWellnessScoresForExport(): List<WellnessScore>
}