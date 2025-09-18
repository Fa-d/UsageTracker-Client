package dev.sadakat.screentimetracker.core.domain.usecase

import dev.sadakat.screentimetracker.core.domain.model.Achievement
import dev.sadakat.screentimetracker.core.domain.model.TimeRange
import dev.sadakat.screentimetracker.core.domain.repository.AchievementRepository
import dev.sadakat.screentimetracker.core.domain.repository.ScreenTimeRepository
import dev.sadakat.screentimetracker.core.domain.repository.UserGoalRepository
import dev.sadakat.screentimetracker.core.domain.service.AchievementService
import dev.sadakat.screentimetracker.core.domain.service.WellnessCalculationService

class ProcessAchievementsUseCase(
    private val achievementRepository: AchievementRepository,
    private val screenTimeRepository: ScreenTimeRepository,
    private val userGoalRepository: UserGoalRepository,
    private val achievementService: AchievementService,
    private val wellnessService: WellnessCalculationService
) {
    suspend fun processDaily(): List<Achievement> {
        val timeRange = TimeRange.today()
        val metrics = screenTimeRepository.getScreenTimeMetrics(timeRange)
        val goals = userGoalRepository.getActiveGoals()
        val wellnessScore = wellnessService.calculateWellnessScore(metrics, goals)
        val achievements = achievementRepository.getAllAchievements()

        // Update progress
        val updatedAchievements = achievementService.updateAchievementProgress(
            achievements, metrics, goals, wellnessScore
        )

        // Check for unlocks
        val newlyUnlocked = achievementService.checkUnlockedAchievements(updatedAchievements)

        // Save updates
        updatedAchievements.forEach { achievement ->
            achievementRepository.updateAchievement(achievement)
        }

        return newlyUnlocked
    }

    suspend fun initializeAchievements() {
        val defaultAchievements = achievementService.generateDefaultAchievements()
        defaultAchievements.forEach { achievement ->
            achievementRepository.saveAchievement(achievement)
        }
    }

    suspend fun getAlmostUnlocked(threshold: Float = 0.8f): List<Achievement> {
        val achievements = achievementRepository.getLockedAchievements()
        return achievementService.getAlmostUnlockedAchievements(achievements, threshold)
    }

    suspend fun getRecentlyUnlocked(timeRange: TimeRange): List<Achievement> {
        return achievementRepository.getRecentlyUnlockedAchievements(timeRange)
    }
}