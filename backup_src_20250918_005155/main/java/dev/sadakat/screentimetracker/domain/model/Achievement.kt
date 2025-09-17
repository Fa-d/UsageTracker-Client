package dev.sadakat.screentimetracker.domain.model

data class Achievement(
    val achievementId: String,
    val name: String,
    val description: String,
    val emoji: String,
    val category: AchievementCategory,
    val targetValue: Int,
    val isUnlocked: Boolean = false,
    val unlockedDate: Long? = null,
    val currentProgress: Int = 0
) {
    val progressPercentage: Float
        get() = if (targetValue > 0) (currentProgress.toFloat() / targetValue * 100).coerceAtMost(100f) else 0f
}

enum class AchievementCategory {
    STREAK,
    MINDFUL,
    FOCUS,
    CLEANER,
    WARRIOR,
    EARLY_BIRD,
    DIGITAL_SUNSET
}