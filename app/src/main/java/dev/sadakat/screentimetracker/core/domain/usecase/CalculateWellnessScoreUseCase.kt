package dev.sadakat.screentimetracker.core.domain.usecase

import dev.sadakat.screentimetracker.core.domain.model.WellnessScore
import dev.sadakat.screentimetracker.core.domain.model.TimeRange
import dev.sadakat.screentimetracker.core.domain.repository.ScreenTimeRepository
import dev.sadakat.screentimetracker.core.domain.repository.UserGoalRepository
import dev.sadakat.screentimetracker.core.domain.service.WellnessCalculationService

class CalculateWellnessScoreUseCase(
    private val screenTimeRepository: ScreenTimeRepository,
    private val userGoalRepository: UserGoalRepository,
    private val wellnessService: WellnessCalculationService
) {
    suspend operator fun invoke(timeRange: TimeRange): WellnessScore {
        val metrics = screenTimeRepository.getScreenTimeMetrics(timeRange)
        val goals = userGoalRepository.getActiveGoals()

        return wellnessService.calculateWellnessScore(metrics, goals)
    }

    suspend fun calculateAndSave(timeRange: TimeRange): WellnessScore {
        val score = invoke(timeRange)
        screenTimeRepository.saveWellnessScore(score)
        return score
    }
}