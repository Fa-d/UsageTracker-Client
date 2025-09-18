package dev.sadakat.screentimetracker.core.domain.usecase

import dev.sadakat.screentimetracker.core.domain.model.ScreenTimeMetrics
import dev.sadakat.screentimetracker.core.domain.model.WellnessScore
import dev.sadakat.screentimetracker.core.domain.model.Achievement
import dev.sadakat.screentimetracker.core.domain.model.UserGoal
import dev.sadakat.screentimetracker.core.domain.model.TimeRange
import dev.sadakat.screentimetracker.core.domain.model.AppSession
import dev.sadakat.screentimetracker.core.domain.repository.ScreenTimeRepository
import dev.sadakat.screentimetracker.core.domain.repository.UserGoalRepository
import dev.sadakat.screentimetracker.core.domain.repository.AchievementRepository
import dev.sadakat.screentimetracker.core.domain.service.WellnessCalculationService
import dev.sadakat.screentimetracker.core.domain.service.AchievementService
import dev.sadakat.screentimetracker.core.domain.service.WellnessRecommendation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Clean architecture version of GetDashboardDataUseCase.
 * This replaces the old version that had data layer dependencies.
 */
class GetCleanDashboardDataUseCase(
    private val screenTimeRepository: ScreenTimeRepository,
    private val userGoalRepository: UserGoalRepository,
    private val achievementRepository: AchievementRepository,
    private val wellnessService: WellnessCalculationService,
    private val achievementService: AchievementService
) {
    suspend operator fun invoke(timeRange: TimeRange = TimeRange.today()): CleanDashboardData {
        val metrics = screenTimeRepository.getScreenTimeMetrics(timeRange)
        val goals = userGoalRepository.getActiveGoals()
        val achievements = achievementRepository.getAlmostCompletedAchievements(0.8f)

        val wellnessScore = wellnessService.calculateWellnessScore(metrics, goals)
        val recommendations = wellnessService.generateWellnessRecommendations(wellnessScore, metrics)
        val almostUnlockedAchievements = achievementService.getAlmostUnlockedAchievements(achievements, 0.8f)

        return CleanDashboardData(
            screenTimeMetrics = metrics,
            wellnessScore = wellnessScore,
            activeGoals = goals,
            almostUnlockedAchievements = almostUnlockedAchievements,
            recommendations = recommendations,
            appUsageSummary = screenTimeRepository.getMostUsedApps(timeRange, 10),
            usagePatterns = screenTimeRepository.getUsagePatterns(timeRange)
        )
    }

    fun observeDashboardData(timeRange: TimeRange = TimeRange.today()): Flow<CleanDashboardData> {
        return combine(
            screenTimeRepository.observeScreenTimeMetrics(timeRange),
            userGoalRepository.observeActiveGoals(),
            achievementRepository.observeAchievements()
        ) { metrics, goals, achievements ->
            val wellnessScore = wellnessService.calculateWellnessScore(metrics, goals)
            val recommendations = wellnessService.generateWellnessRecommendations(wellnessScore, metrics)
            val almostUnlocked = achievements.filter { !it.isUnlocked && it.progressPercentage >= 80f }

            CleanDashboardData(
                screenTimeMetrics = metrics,
                wellnessScore = wellnessScore,
                activeGoals = goals,
                almostUnlockedAchievements = almostUnlocked,
                recommendations = recommendations,
                appUsageSummary = emptyList(), // TODO: Make this observable
                usagePatterns = null // TODO: Make this observable
            )
        }
    }
}

/**
 * Clean dashboard data representation using pure domain models
 */
data class CleanDashboardData(
    val screenTimeMetrics: ScreenTimeMetrics,
    val wellnessScore: WellnessScore,
    val activeGoals: List<UserGoal>,
    val almostUnlockedAchievements: List<Achievement>,
    val recommendations: List<WellnessRecommendation>,
    val appUsageSummary: List<dev.sadakat.screentimetracker.core.domain.repository.AppUsageSummary>,
    val usagePatterns: dev.sadakat.screentimetracker.core.domain.repository.UsagePatterns?
) {
    val totalScreenTimeToday: Long
        get() = screenTimeMetrics.totalScreenTimeMillis

    val totalUnlocksToday: Int
        get() = screenTimeMetrics.unlockCount

    val wellnessLevel: String
        get() = screenTimeMetrics.wellnessScore.wellnessLevel.displayName

    val isWellnessHealthy: Boolean
        get() = wellnessScore.isHealthy()

    val needsAttention: Boolean
        get() = wellnessScore.needsImprovement()

    val topAppsToday: List<AppSession>
        get() = screenTimeMetrics.appSessions.sortedByDescending { it.durationMillis }.take(5)

    val productivityRatio: Float
        get() = if (screenTimeMetrics.totalScreenTimeMillis > 0) {
            screenTimeMetrics.getProductiveTimeMillis().toFloat() / screenTimeMetrics.totalScreenTimeMillis
        } else 0f
}