package dev.sadakat.screentimetracker.core.domain.usecase

import dev.sadakat.screentimetracker.core.domain.model.ScreenTimeMetrics
import dev.sadakat.screentimetracker.core.domain.model.WellnessScore
import dev.sadakat.screentimetracker.core.domain.model.Achievement
import dev.sadakat.screentimetracker.core.domain.model.UserGoal
import dev.sadakat.screentimetracker.core.domain.model.TimeRange
import dev.sadakat.screentimetracker.core.domain.repository.ScreenTimeRepository
import dev.sadakat.screentimetracker.core.domain.repository.UserGoalRepository
import dev.sadakat.screentimetracker.core.domain.service.WellnessCalculationService
import dev.sadakat.screentimetracker.core.domain.service.AchievementService

class GetDashboardDataUseCase(
    private val screenTimeRepository: ScreenTimeRepository,
    private val userGoalRepository: UserGoalRepository,
    private val wellnessService: WellnessCalculationService,
    private val achievementService: AchievementService
) {
    suspend operator fun invoke(timeRange: TimeRange): DashboardData {
        val metrics = screenTimeRepository.getScreenTimeMetrics(timeRange)
        val goals = userGoalRepository.getActiveGoals()
        val wellnessScore = wellnessService.calculateWellnessScore(metrics, goals)
        val achievements = achievementService.getAlmostUnlockedAchievements(
            // TODO: Get achievements from repository
            emptyList()
        )

        return DashboardData(
            metrics = metrics,
            wellnessScore = wellnessScore,
            achievements = achievements,
            goals = goals,
            recommendations = wellnessService.generateWellnessRecommendations(wellnessScore, metrics)
        )
    }
}

data class DashboardData(
    val metrics: ScreenTimeMetrics,
    val wellnessScore: WellnessScore,
    val achievements: List<Achievement>,
    val goals: List<UserGoal>,
    val recommendations: List<dev.sadakat.screentimetracker.core.domain.service.WellnessRecommendation>
)