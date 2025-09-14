package dev.sadakat.screentimetracker.domain.wellness.repository

import dev.sadakat.screentimetracker.core.database.entities.WellnessScore as WellnessScoreEntity
import dev.sadakat.screentimetracker.core.common.model.WellnessScore
import kotlinx.coroutines.flow.Flow

interface WellnessRepository {
    // --- Wellness Score CRUD Methods ---
    suspend fun insertWellnessScore(score: WellnessScoreEntity)
    suspend fun updateWellnessScore(score: WellnessScoreEntity)
    suspend fun getWellnessScoreForDate(date: Long): WellnessScoreEntity?
    fun getAllWellnessScores(): Flow<List<WellnessScoreEntity>>
    fun getWellnessScoresInRange(startDate: Long, endDate: Long): Flow<List<WellnessScoreEntity>>

    // --- Statistics Methods ---
    suspend fun getAverageWellnessScore(days: Int): Double
    suspend fun getWellnessTrend(days: Int): WellnessTrend
    suspend fun getBestWellnessStreak(): Int
    suspend fun getCurrentWellnessStreak(): Int
}

data class WellnessTrend(
    val trend: TrendDirection,
    val changePercentage: Float,
    val periodDays: Int
)

enum class TrendDirection {
    IMPROVING, DECLINING, STABLE
}