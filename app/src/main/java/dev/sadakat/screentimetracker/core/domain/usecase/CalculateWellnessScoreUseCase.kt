package dev.sadakat.screentimetracker.core.domain.usecase
import dev.sadakat.screentimetracker.core.domain.model.WellnessScore
import dev.sadakat.screentimetracker.core.domain.model.TimeRange
import dev.sadakat.screentimetracker.core.domain.repository.ScreenTimeRepository
import dev.sadakat.screentimetracker.core.domain.repository.UserGoalRepository
import dev.sadakat.screentimetracker.core.domain.service.WellnessCalculationService

/**
 * Clean architecture version of CalculateWellnessScoreUseCase.
 * Delegates complex business logic to the WellnessCalculationService.
 */
class CalculateWellnessScoreUseCase(
    private val screenTimeRepository: ScreenTimeRepository,
    private val userGoalRepository: UserGoalRepository,
    private val wellnessService: WellnessCalculationService
) {
    /**
     * Calculates wellness score for a specific date
     */
    suspend operator fun invoke(
        date: Long,
        forceRecalculate: Boolean = false
    ): WellnessScore {
        val timeRange = TimeRange.fromTimestamp(date)

        // Check if we have a recent score and don't need to recalculate
        if (!forceRecalculate) {
            val existingScore = screenTimeRepository.getLatestWellnessScore()
            if (existingScore != null && !shouldRecalculate(existingScore)) {
                return existingScore
            }
        }

        // Get current data
        val metrics = screenTimeRepository.getScreenTimeMetrics(timeRange)
        val goals = userGoalRepository.getActiveGoals()

        // Calculate using domain service
        val wellnessScore = wellnessService.calculateWellnessScoreForPeriod(metrics, goals, timeRange)

        // Save calculated score
        screenTimeRepository.saveWellnessScore(wellnessScore)

        return wellnessScore
    }

    /**
     * Calculates wellness score for today
     */
    suspend fun calculateForToday(forceRecalculate: Boolean = false): WellnessScore {
        return invoke(System.currentTimeMillis(), forceRecalculate)
    }

    /**
     * Calculates wellness score for a specific time range
     */
    suspend fun calculateForRange(timeRange: TimeRange): WellnessScore {
        val metrics = screenTimeRepository.getScreenTimeMetrics(timeRange)
        val goals = userGoalRepository.getActiveGoals()

        return wellnessService.calculateWellnessScoreForPeriod(metrics, goals, timeRange)
    }

    /**
     * Calculates and compares wellness score with previous periods
     */
    suspend fun calculateWithTrend(date: Long): WellnessScoreWithTrend {
        val currentScore = invoke(date)

        // Get previous scores for trend analysis
        val previousDays = 7 // Last 7 days
        val previousScores = mutableListOf<WellnessScore>()

        for (i in 1..previousDays) {
            val previousDate = date - (i * 24 * 60 * 60 * 1000L)
            val previousRange = TimeRange.fromTimestamp(previousDate)
            try {
                val scores = screenTimeRepository.getWellnessScores(previousRange)
                scores.lastOrNull()?.let { previousScores.add(it) }
            } catch (e: Exception) {
                // Continue if we can't get historical data
            }
        }

        val trend = wellnessService.calculateWellnessTrend(currentScore, previousScores)

        return WellnessScoreWithTrend(
            currentScore = currentScore,
            trend = trend,
            previousScores = previousScores
        )
    }

    /**
     * Batch calculates wellness scores for multiple dates
     */
    suspend fun calculateBatch(dates: List<Long>): List<WellnessScore> {
        return dates.map { date ->
            try {
                invoke(date, forceRecalculate = false)
            } catch (e: Exception) {
                // Return default score for failed calculations
                WellnessScore.default()
            }
        }
    }

    private fun shouldRecalculate(existingScore: WellnessScore): Boolean {
        val currentTime = System.currentTimeMillis()
        val timeSinceCalculation = currentTime - existingScore.calculatedAt

        // Recalculate every 30 minutes for dynamic updates during the day
        val recalculationInterval = 30 * 60 * 1000L // 30 minutes

        return timeSinceCalculation > recalculationInterval
    }
}

/**
 * Extended to include helper method for TimeRange creation
 */
private fun TimeRange.Companion.fromTimestamp(timestamp: Long): TimeRange {
    val calendar = java.util.Calendar.getInstance()
    calendar.timeInMillis = timestamp
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
    calendar.set(java.util.Calendar.MINUTE, 0)
    calendar.set(java.util.Calendar.SECOND, 0)
    calendar.set(java.util.Calendar.MILLISECOND, 0)
    val startOfDay = calendar.timeInMillis

    calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
    calendar.add(java.util.Calendar.MILLISECOND, -1)
    val endOfDay = calendar.timeInMillis

    return TimeRange(startOfDay, endOfDay)
}

/**
 * Wellness score with trend analysis
 */
data class WellnessScoreWithTrend(
    val currentScore: WellnessScore,
    val trend: dev.sadakat.screentimetracker.core.domain.service.WellnessTrend,
    val previousScores: List<WellnessScore>
) {
    val isImproving: Boolean
        get() = trend.direction == dev.sadakat.screentimetracker.core.domain.service.TrendDirection.IMPROVING

    val isStable: Boolean
        get() = trend.direction == dev.sadakat.screentimetracker.core.domain.service.TrendDirection.STABLE

    val isDeclining: Boolean
        get() = trend.direction == dev.sadakat.screentimetracker.core.domain.service.TrendDirection.DECLINING

    val averagePreviousScore: Float
        get() = if (previousScores.isNotEmpty()) {
            previousScores.map { it.overall }.average().toFloat()
        } else 0f

    val improvementFromAverage: Int
        get() = currentScore.overall - averagePreviousScore.toInt()
}