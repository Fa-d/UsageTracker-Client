package dev.sadakat.screentimetracker.core.domain.service

import dev.sadakat.screentimetracker.core.domain.model.Achievement
import dev.sadakat.screentimetracker.core.domain.model.AchievementCategory
import dev.sadakat.screentimetracker.core.domain.model.AchievementTier
import dev.sadakat.screentimetracker.core.domain.model.ScreenTimeMetrics
import dev.sadakat.screentimetracker.core.domain.model.UserGoal
import dev.sadakat.screentimetracker.core.domain.model.WellnessScore
import dev.sadakat.screentimetracker.core.domain.model.TimeRange

interface AchievementService {

    /**
     * Updates achievement progress based on current metrics and goals
     */
    fun updateAchievementProgress(
        achievements: List<Achievement>,
        metrics: ScreenTimeMetrics,
        goals: List<UserGoal>,
        wellnessScore: WellnessScore
    ): List<Achievement>

    /**
     * Checks for newly unlocked achievements
     */
    fun checkUnlockedAchievements(
        achievements: List<Achievement>
    ): List<Achievement>

    /**
     * Generates default achievements for a new user
     */
    fun generateDefaultAchievements(): List<Achievement>

    /**
     * Calculates progress for a specific achievement
     */
    fun calculateAchievementProgress(
        achievement: Achievement,
        metrics: ScreenTimeMetrics,
        goals: List<UserGoal>,
        wellnessScore: WellnessScore
    ): Int

    /**
     * Gets achievements that are close to being unlocked
     */
    fun getAlmostUnlockedAchievements(
        achievements: List<Achievement>,
        threshold: Float = 0.8f
    ): List<Achievement>

    /**
     * Suggests new achievements based on user behavior patterns
     */
    fun suggestPersonalizedAchievements(
        currentAchievements: List<Achievement>,
        historicalMetrics: List<ScreenTimeMetrics>,
        userPreferences: AchievementPreferences
    ): List<Achievement>

    /**
     * Calculates achievement statistics for a user
     */
    fun calculateAchievementStats(
        achievements: List<Achievement>
    ): AchievementStats

    /**
     * Validates if an achievement is appropriate for the user's current level
     */
    fun validateAchievementDifficulty(
        achievement: Achievement,
        userLevel: Int,
        historicalMetrics: List<ScreenTimeMetrics>
    ): AchievementValidation

    /**
     * Creates milestone achievements based on user progress
     */
    fun createMilestoneAchievements(
        category: AchievementCategory,
        userProgress: UserProgressData
    ): List<Achievement>
}

data class AchievementPreferences(
    val preferredCategories: List<AchievementCategory>,
    val difficultyLevel: DifficultyLevel,
    val focusAreas: List<String>,
    val motivationStyle: MotivationStyle
)

enum class DifficultyLevel {
    BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
}

enum class MotivationStyle {
    COMPETITIVE, PROGRESSIVE, SUPPORTIVE, CHALLENGING
}

data class AchievementStats(
    val totalAchievements: Int,
    val unlockedAchievements: Int,
    val completionRate: Float,
    val averageProgressTime: Long,
    val favoriteCategory: AchievementCategory?,
    val currentStreak: Int,
    val longestStreak: Int
)

data class AchievementValidation(
    val isAppropriate: Boolean,
    val difficulty: DifficultyLevel,
    val estimatedTimeToComplete: Long,
    val recommendations: List<String>
)

data class UserProgressData(
    val currentLevel: Int,
    val totalExperience: Long,
    val averageWellnessScore: Float,
    val consistencyRating: Float,
    val strongestAreas: List<String>,
    val improvementAreas: List<String>
)