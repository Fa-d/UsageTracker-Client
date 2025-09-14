package dev.sadakat.screentimetracker.domain.wellness.usecases

import dev.sadakat.screentimetracker.core.common.model.WellnessScore
import dev.sadakat.screentimetracker.domain.wellness.repository.WellnessRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject

class GetWellnessScoreUseCase @Inject constructor(
    private val repository: WellnessRepository
) {
    fun getTodaysScore(): Flow<WellnessScore?> {
        val today = getTodayStart()
        return repository.getAllWellnessScores().map { scores ->
            scores.find { it.date == today }?.let { entity ->
                mapToCommonModel(entity)
            }
        }
    }

    fun getScoresInRange(startDate: Long, endDate: Long): Flow<List<WellnessScore>> {
        return repository.getWellnessScoresInRange(startDate, endDate).map { entities ->
            entities.map { mapToCommonModel(it) }
        }
    }

    suspend fun getScoreForDate(date: Long): WellnessScore? {
        val startOfDay = getStartOfDay(date)
        return repository.getWellnessScoreForDate(startOfDay)?.let { entity ->
            mapToCommonModel(entity)
        }
    }

    private fun mapToCommonModel(entity: dev.sadakat.screentimetracker.core.database.entities.WellnessScore): WellnessScore {
        val level = try {
            dev.sadakat.screentimetracker.core.common.model.WellnessLevel.valueOf(entity.level.uppercase())
        } catch (e: Exception) {
            dev.sadakat.screentimetracker.core.common.model.WellnessLevel.fromScore(entity.totalScore)
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

    private fun getTodayStart(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}