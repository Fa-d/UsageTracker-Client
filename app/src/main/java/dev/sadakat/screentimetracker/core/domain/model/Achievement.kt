package dev.sadakat.screentimetracker.core.domain.model

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
    val category: AchievementCategory,
    val targetValue: Int,
    val currentProgress: Int = 0,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val requirements: List<AchievementRequirement> = emptyList(),
    val tier: AchievementTier = AchievementTier.BRONZE
) {
    init {
        require(targetValue > 0) { "Target value must be positive" }
        require(currentProgress >= 0) { "Current progress cannot be negative" }
        require(currentProgress <= targetValue) { "Progress cannot exceed target" }
    }

    val progressPercentage: Float
        get() = if (targetValue > 0) (currentProgress.toFloat() / targetValue * 100).coerceAtMost(100f) else 0f

    val isCompleted: Boolean
        get() = currentProgress >= targetValue

    val canUnlock: Boolean
        get() = isCompleted && !isUnlocked

    fun addProgress(amount: Int): Achievement {
        require(amount >= 0) { "Progress amount must be positive" }
        val newProgress = (currentProgress + amount).coerceAtMost(targetValue)
        return copy(currentProgress = newProgress)
    }

    fun unlock(): Achievement {
        require(canUnlock) { "Achievement cannot be unlocked yet" }
        return copy(
            isUnlocked = true,
            unlockedAt = System.currentTimeMillis()
        )
    }

    fun reset(): Achievement {
        return copy(
            currentProgress = 0,
            isUnlocked = false,
            unlockedAt = null
        )
    }

    fun getRemainingProgress(): Int {
        return (targetValue - currentProgress).coerceAtLeast(0)
    }

    companion object {
        fun createStreakAchievement(days: Int, tier: AchievementTier = AchievementTier.BRONZE): Achievement {
            return Achievement(
                id = "streak_${days}d_${tier.name.lowercase()}",
                name = "${days}-Day Wellness Streak",
                description = "Maintain good digital wellness for $days consecutive days",
                emoji = when (tier) {
                    AchievementTier.BRONZE -> "🥉"
                    AchievementTier.SILVER -> "🥈"
                    AchievementTier.GOLD -> "🥇"
                    AchievementTier.PLATINUM -> "💎"
                },
                category = AchievementCategory.STREAK,
                targetValue = days,
                tier = tier
            )
        }

        fun createLimitAchievement(days: Int): Achievement {
            return Achievement(
                id = "limit_respected_${days}d",
                name = "Limit Respecter",
                description = "Stay within app limits for $days consecutive days",
                emoji = "⏰",
                category = AchievementCategory.DISCIPLINE,
                targetValue = days
            )
        }
    }
}

enum class AchievementCategory(val displayName: String, val color: String) {
    STREAK("Streak", "#F59E0B"),
    MINDFUL("Mindfulness", "#10B981"),
    FOCUS("Focus", "#3B82F6"),
    DISCIPLINE("Discipline", "#8B5CF6"),
    BALANCE("Balance", "#06B6D4"),
    PRODUCTIVITY("Productivity", "#84CC16"),
    WELLNESS("Wellness", "#EF4444")
}

enum class AchievementTier(val displayName: String, val multiplier: Float) {
    BRONZE("Bronze", 1.0f),
    SILVER("Silver", 1.5f),
    GOLD("Gold", 2.0f),
    PLATINUM("Platinum", 3.0f)
}

data class AchievementRequirement(
    val type: RequirementType,
    val value: Int,
    val description: String
)

enum class RequirementType {
    CONSECUTIVE_DAYS,
    TOTAL_SESSIONS,
    SCREEN_TIME_UNDER,
    UNLOCKS_UNDER,
    WELLNESS_SCORE_OVER,
    APP_CATEGORY_LIMIT
}