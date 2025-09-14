package dev.sadakat.screentimetracker.domain.wellness.usecases

import dev.sadakat.screentimetracker.core.common.model.WellnessLevel
import dev.sadakat.screentimetracker.core.common.model.WellnessScore
import dev.sadakat.screentimetracker.core.database.entities.WellnessScore as WellnessScoreEntity
import dev.sadakat.screentimetracker.domain.wellness.repository.WellnessRepository
import android.util.Log
import javax.inject.Inject

class CalculateWellnessScoreUseCase @Inject constructor(
    private val repository: WellnessRepository
) {
    companion object {
        private const val TAG = "CalculateWellnessScore"
    }

    suspend operator fun invoke(date: Long, forceRecalculate: Boolean = false): WellnessScore {
        val startOfDay = getStartOfDay(date)
        val existingScore = repository.getWellnessScoreForDate(startOfDay)

        // Check if we should recalculate based on time or force flag
        val shouldRecalculate = forceRecalculate || shouldRecalculateScore(existingScore)

        return if (existingScore != null && !shouldRecalculate) {
            mapToCommonModel(existingScore)
        } else {
            calculateNewScore(startOfDay)
        }
    }

    private suspend fun calculateNewScore(startOfDay: Long): WellnessScore {
        try {
            // Simplified scoring for now - could be made more sophisticated
            val timeLimitScore = calculateTimeLimitScore(startOfDay)
            val focusSessionScore = calculateFocusSessionScore(startOfDay)
            val breaksScore = calculateBreaksScore(startOfDay)
            val sleepHygieneScore = calculateSleepHygieneScore(startOfDay)

            // Weighted average: Time Limits (30%), Focus (25%), Breaks (20%), Sleep (25%)
            val totalScore = (
                timeLimitScore * 0.30 +
                focusSessionScore * 0.25 +
                breaksScore * 0.20 +
                sleepHygieneScore * 0.25
            ).toInt().coerceIn(0, 100)

            val level = WellnessLevel.fromScore(totalScore)

            val scoreEntity = WellnessScoreEntity(
                date = startOfDay,
                totalScore = totalScore,
                timeLimitScore = timeLimitScore,
                focusSessionScore = focusSessionScore,
                breaksScore = breaksScore,
                sleepHygieneScore = sleepHygieneScore,
                level = level.name.lowercase(),
                calculatedAt = System.currentTimeMillis()
            )

            // Save calculated score to database
            repository.insertWellnessScore(scoreEntity)

            return mapToCommonModel(scoreEntity)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to calculate wellness score", e)

            // Return a default score on error
            val defaultLevel = WellnessLevel.DIGITAL_SPROUT
            return WellnessScore(
                date = startOfDay,
                totalScore = 50,
                timeLimitScore = 50,
                focusSessionScore = 50,
                breaksScore = 50,
                sleepHygieneScore = 50,
                level = defaultLevel,
                calculatedAt = System.currentTimeMillis()
            )
        }
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun shouldRecalculateScore(existingScore: WellnessScoreEntity?): Boolean {
        if (existingScore == null) return true

        val currentTime = System.currentTimeMillis()
        val timeSinceCalculation = currentTime - existingScore.calculatedAt

        // Recalculate every 30 minutes for dynamic updates during the day
        val recalculationInterval = 30 * 60 * 1000L // 30 minutes

        return timeSinceCalculation > recalculationInterval
    }

    private fun mapToCommonModel(entity: WellnessScoreEntity): WellnessScore {
        val level = try {
            WellnessLevel.valueOf(entity.level.uppercase())
        } catch (e: Exception) {
            WellnessLevel.fromScore(entity.totalScore)
        }

        return WellnessScore(
            date = entity.date,
            totalScore = entity.totalScore,
            timeLimitScore = entity.timeLimitScore,
            focusSessionScore = entity.focusSessionScore,
            breaksScore = entity.breaksScore,
            sleepHygieneScore = entity.sleepHygieneScore,
            level = level,
            calculatedAt = entity.calculatedAt
        )
    }

    // Simplified scoring methods - could be enhanced with actual data integration
    private suspend fun calculateTimeLimitScore(startOfDay: Long): Int {
        // TODO: Integrate with goals and tracking data
        return 75 // Placeholder score
    }

    private suspend fun calculateFocusSessionScore(startOfDay: Long): Int {
        // TODO: Integrate with focus session data
        return 80 // Placeholder score
    }

    private suspend fun calculateBreaksScore(startOfDay: Long): Int {
        // TODO: Integrate with session and unlock data
        return 70 // Placeholder score
    }

    private suspend fun calculateSleepHygieneScore(startOfDay: Long): Int {
        // TODO: Integrate with evening/bedtime usage data
        return 85 // Placeholder score
    }
}