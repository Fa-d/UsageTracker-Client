package dev.sadakat.screentimetracker.core.data.mapper

import dev.sadakat.screentimetracker.core.domain.model.*
import dev.sadakat.screentimetracker.core.data.local.entities.Achievement as AchievementEntity

/**
 * Bidirectional mapper between Achievement domain models and data entities.
 */
class AchievementDataMapper {

    // ==================== Domain to Data Entity Mapping ====================

    fun mapToAchievementEntity(achievement: Achievement): AchievementEntity {
        return AchievementEntity(
            achievementId = achievement.id,
            name = achievement.name,
            description = achievement.description,
            emoji = achievement.emoji,
            category = mapCategoryToString(achievement.category),
            targetValue = achievement.targetValue,
            currentProgress = achievement.currentProgress,
            isUnlocked = achievement.isUnlocked,
            unlockedDate = achievement.unlockedAt
        )
    }

    // ==================== Data Entity to Domain Model Mapping ====================

    fun mapToAchievement(entity: AchievementEntity): Achievement {
        return Achievement(
            id = entity.achievementId,
            name = entity.name,
            description = entity.description,
            emoji = entity.emoji,
            category = mapStringToCategory(entity.category),
            targetValue = entity.targetValue,
            currentProgress = entity.currentProgress,
            isUnlocked = entity.isUnlocked,
            unlockedAt = entity.unlockedDate,
            requirements = emptyList(), // Would need separate mapping if stored
            tier = AchievementTier.BRONZE // Default tier since not stored in entity
        )
    }

    // ==================== Batch Mapping Functions ====================

    fun mapToAchievementEntities(achievements: List<Achievement>): List<AchievementEntity> {
        return achievements.map { mapToAchievementEntity(it) }
    }

    fun mapToAchievements(entities: List<AchievementEntity>): List<Achievement> {
        return entities.map { mapToAchievement(it) }
    }

    // ==================== Type Mapping Helpers ====================

    private fun mapCategoryToString(category: AchievementCategory): String {
        return when (category) {
            AchievementCategory.STREAK -> "STREAK"
            AchievementCategory.MINDFUL -> "MINDFUL"
            AchievementCategory.FOCUS -> "FOCUS"
            AchievementCategory.DISCIPLINE -> "CLEANER" // Map to existing category
            AchievementCategory.BALANCE -> "WARRIOR" // Map to existing category
            AchievementCategory.PRODUCTIVITY -> "EARLY_BIRD" // Map to existing category
            AchievementCategory.WELLNESS -> "DIGITAL_SUNSET" // Map to existing category
        }
    }

    private fun mapStringToCategory(categoryString: String): AchievementCategory {
        return when (categoryString) {
            "STREAK" -> AchievementCategory.STREAK
            "MINDFUL" -> AchievementCategory.MINDFUL
            "FOCUS" -> AchievementCategory.FOCUS
            "CLEANER" -> AchievementCategory.DISCIPLINE
            "WARRIOR" -> AchievementCategory.BALANCE
            "EARLY_BIRD" -> AchievementCategory.PRODUCTIVITY
            "DIGITAL_SUNSET" -> AchievementCategory.WELLNESS
            else -> AchievementCategory.STREAK // Default fallback
        }
    }

    private fun mapTierToString(tier: AchievementTier): String {
        return when (tier) {
            AchievementTier.BRONZE -> "bronze"
            AchievementTier.SILVER -> "silver"
            AchievementTier.GOLD -> "gold"
            AchievementTier.PLATINUM -> "platinum"
        }
    }

    private fun mapStringToTier(tierString: String): AchievementTier {
        return when (tierString.lowercase()) {
            "bronze" -> AchievementTier.BRONZE
            "silver" -> AchievementTier.SILVER
            "gold" -> AchievementTier.GOLD
            "platinum" -> AchievementTier.PLATINUM
            else -> AchievementTier.BRONZE // Default fallback
        }
    }

    // ==================== Achievement Statistics Mapping ====================

    fun mapToAchievementStatistics(
        entities: List<AchievementEntity>
    ): dev.sadakat.screentimetracker.core.domain.repository.AchievementStatistics {
        val totalAchievements = entities.size
        val unlockedAchievements = entities.count { it.isUnlocked }
        val completionRate = if (totalAchievements > 0) {
            unlockedAchievements.toFloat() / totalAchievements
        } else 0f

        val achievementsByCategory = entities.groupBy { mapStringToCategory(it.category) }
            .mapValues { it.value.size }

        val unlockedByCategory = entities.filter { it.isUnlocked }
            .groupBy { mapStringToCategory(it.category) }
            .mapValues { it.value.size }

        val achievementsByTier = mapOf(AchievementTier.BRONZE to entities.size)
        val unlockedByTier = mapOf(AchievementTier.BRONZE to unlockedAchievements)

        // Calculate streaks (simplified)
        val currentStreak = calculateCurrentStreak(entities)
        val longestStreak = calculateLongestStreak(entities)

        val lastUnlockDate = entities.filter { it.isUnlocked && it.unlockedDate != null }
            .maxByOrNull { it.unlockedDate!! }?.unlockedDate

        return dev.sadakat.screentimetracker.core.domain.repository.AchievementStatistics(
            totalAchievements = totalAchievements,
            unlockedAchievements = unlockedAchievements,
            completionRate = completionRate,
            achievementsByCategory = achievementsByCategory,
            unlockedByCategory = unlockedByCategory,
            achievementsByTier = achievementsByTier,
            unlockedByTier = unlockedByTier,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            lastUnlockDate = lastUnlockDate
        )
    }

    // ==================== Achievement Analytics Mapping ====================

    fun mapToAchievementAnalytics(
        entities: List<AchievementEntity>,
        timeRange: TimeRange
    ): dev.sadakat.screentimetracker.core.domain.repository.AchievementAnalytics {
        val unlockedInRange = entities.filter { entity ->
            entity.isUnlocked &&
            entity.unlockedDate != null &&
            timeRange.contains(entity.unlockedDate)
        }

        val unlockFrequency = unlockedInRange.groupBy { mapStringToCategory(it.category) }
            .mapValues { categoryGroup ->
                categoryGroup.value.size.toFloat() / timeRange.durationDays()
            }

        val averageTimeToUnlock = unlockedInRange.groupBy { mapStringToCategory(it.category) }
            .mapValues { categoryGroup ->
                // This would need creation date to calculate properly
                7 * 24 * 60 * 60 * 1000L // Default to 7 days
            }

        val mostPopularCategories = unlockFrequency.entries
            .sortedByDescending { it.value }
            .map { it.key }

        val completionTrends = AchievementCategory.values().map { category ->
            dev.sadakat.screentimetracker.core.domain.repository.CompletionTrend(
                category = category,
                trend = "stable", // Would need historical data to calculate
                changeRate = 0f
            )
        }

        val completionRate = if (entities.isNotEmpty()) {
            entities.count { it.isUnlocked }.toFloat() / entities.size
        } else 0f

        val userEngagementMetrics = dev.sadakat.screentimetracker.core.domain.repository.UserEngagementMetrics(
            dailyAchievementChecks = 1.0f, // Would need tracking data
            averageProgressPerDay = 2.0f, // Would need tracking data
            engagementScore = completionRate,
            motivationLevel = when {
                completionRate >= 0.8f -> "high"
                completionRate >= 0.5f -> "medium"
                else -> "low"
            }
        )

        return dev.sadakat.screentimetracker.core.domain.repository.AchievementAnalytics(
            unlockFrequency = unlockFrequency,
            averageTimeToUnlock = averageTimeToUnlock,
            mostPopularCategories = mostPopularCategories,
            completionTrends = completionTrends,
            userEngagementMetrics = userEngagementMetrics
        )
    }

    // ==================== Helper Functions ====================

    private fun calculateCurrentStreak(entities: List<AchievementEntity>): Int {
        // Simplified streak calculation - would need more sophisticated logic
        val recentUnlocks = entities.filter { it.isUnlocked && it.unlockedDate != null }
            .sortedByDescending { it.unlockedDate!! }

        var streak = 0
        val oneDayMillis = 24 * 60 * 60 * 1000L
        var lastUnlockDate = System.currentTimeMillis()

        for (achievement in recentUnlocks) {
            val daysDiff = (lastUnlockDate - achievement.unlockedDate!!) / oneDayMillis
            if (daysDiff <= 1) {
                streak++
                lastUnlockDate = achievement.unlockedDate
            } else {
                break
            }
        }

        return streak
    }

    private fun calculateLongestStreak(entities: List<AchievementEntity>): Int {
        // Simplified longest streak calculation
        val unlockedAchievements = entities.filter { it.isUnlocked && it.unlockedDate != null }
            .sortedBy { it.unlockedDate!! }

        var longestStreak = 0
        var currentStreak = 0
        val oneDayMillis = 24 * 60 * 60 * 1000L
        var previousUnlockDate: Long? = null

        for (achievement in unlockedAchievements) {
            if (previousUnlockDate == null) {
                currentStreak = 1
            } else {
                val daysDiff = (achievement.unlockedDate!! - previousUnlockDate) / oneDayMillis
                if (daysDiff <= 1) {
                    currentStreak++
                } else {
                    longestStreak = maxOf(longestStreak, currentStreak)
                    currentStreak = 1
                }
            }
            previousUnlockDate = achievement.unlockedDate
        }

        return maxOf(longestStreak, currentStreak)
    }

    // ==================== Validation Helpers ====================

    fun validateAchievement(achievement: Achievement): Boolean {
        return achievement.id.isNotBlank() &&
               achievement.name.isNotBlank() &&
               achievement.description.isNotBlank() &&
               achievement.targetValue > 0 &&
               achievement.currentProgress >= 0 &&
               achievement.currentProgress <= achievement.targetValue
    }

    fun validateAchievementEntity(entity: AchievementEntity): Boolean {
        return entity.achievementId.isNotBlank() &&
               entity.name.isNotBlank() &&
               entity.description.isNotBlank() &&
               entity.targetValue > 0 &&
               entity.currentProgress >= 0
    }
}