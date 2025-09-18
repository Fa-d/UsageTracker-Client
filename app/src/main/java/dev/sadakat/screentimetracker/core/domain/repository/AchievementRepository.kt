package dev.sadakat.screentimetracker.core.domain.repository

import dev.sadakat.screentimetracker.core.domain.model.Achievement
import dev.sadakat.screentimetracker.core.domain.model.AchievementCategory
import dev.sadakat.screentimetracker.core.domain.model.AchievementTier
import dev.sadakat.screentimetracker.core.domain.model.TimeRange
import kotlinx.coroutines.flow.Flow

interface AchievementRepository {

    /**
     * Saves a new achievement
     */
    suspend fun saveAchievement(achievement: Achievement)

    /**
     * Updates an existing achievement
     */
    suspend fun updateAchievement(achievement: Achievement)

    /**
     * Updates achievement progress
     */
    suspend fun updateAchievementProgress(achievementId: String, progress: Int)

    /**
     * Unlocks an achievement
     */
    suspend fun unlockAchievement(achievementId: String, unlockedAt: Long = System.currentTimeMillis())

    /**
     * Gets an achievement by ID
     */
    suspend fun getAchievementById(achievementId: String): Achievement?

    /**
     * Gets all achievements
     */
    suspend fun getAllAchievements(): List<Achievement>

    /**
     * Gets unlocked achievements
     */
    suspend fun getUnlockedAchievements(): List<Achievement>

    /**
     * Gets locked (not yet unlocked) achievements
     */
    suspend fun getLockedAchievements(): List<Achievement>

    /**
     * Gets achievements by category
     */
    suspend fun getAchievementsByCategory(category: AchievementCategory): List<Achievement>

    /**
     * Gets achievements by tier
     */
    suspend fun getAchievementsByTier(tier: AchievementTier): List<Achievement>

    /**
     * Gets recently unlocked achievements
     */
    suspend fun getRecentlyUnlockedAchievements(timeRange: TimeRange): List<Achievement>

    /**
     * Gets achievements close to completion (above threshold)
     */
    suspend fun getAlmostCompletedAchievements(progressThreshold: Float = 0.8f): List<Achievement>

    /**
     * Observes all achievements
     */
    fun observeAchievements(): Flow<List<Achievement>>

    /**
     * Observes unlocked achievements
     */
    fun observeUnlockedAchievements(): Flow<List<Achievement>>

    /**
     * Observes a specific achievement
     */
    fun observeAchievement(achievementId: String): Flow<Achievement?>

    /**
     * Gets achievement statistics
     */
    suspend fun getAchievementStatistics(): AchievementStatistics

    /**
     * Gets achievement unlock history
     */
    suspend fun getAchievementHistory(timeRange: TimeRange): List<AchievementUnlock>

    /**
     * Resets achievement progress (for testing or special events)
     */
    suspend fun resetAchievementProgress(achievementId: String)

    /**
     * Deletes an achievement
     */
    suspend fun deleteAchievement(achievementId: String)

    /**
     * Bulk updates achievement progress
     */
    suspend fun updateMultipleAchievements(updates: List<AchievementProgressUpdate>)

    /**
     * Gets achievement completion patterns and analytics
     */
    suspend fun getAchievementAnalytics(timeRange: TimeRange): AchievementAnalytics
}

data class AchievementStatistics(
    val totalAchievements: Int,
    val unlockedAchievements: Int,
    val completionRate: Float,
    val achievementsByCategory: Map<AchievementCategory, Int>,
    val unlockedByCategory: Map<AchievementCategory, Int>,
    val achievementsByTier: Map<AchievementTier, Int>,
    val unlockedByTier: Map<AchievementTier, Int>,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastUnlockDate: Long?
)

data class AchievementUnlock(
    val achievement: Achievement,
    val unlockedAt: Long,
    val progressHistory: List<ProgressSnapshot>
)

data class ProgressSnapshot(
    val timestamp: Long,
    val progress: Int,
    val progressPercentage: Float
)

data class AchievementProgressUpdate(
    val achievementId: String,
    val newProgress: Int
)

data class AchievementAnalytics(
    val unlockFrequency: Map<AchievementCategory, Float>,
    val averageTimeToUnlock: Map<AchievementCategory, Long>,
    val mostPopularCategories: List<AchievementCategory>,
    val completionTrends: List<CompletionTrend>,
    val userEngagementMetrics: UserEngagementMetrics
)

data class CompletionTrend(
    val category: AchievementCategory,
    val trend: String, // "improving", "stable", "declining"
    val changeRate: Float
)

data class UserEngagementMetrics(
    val dailyAchievementChecks: Float,
    val averageProgressPerDay: Float,
    val engagementScore: Float,
    val motivationLevel: String
)