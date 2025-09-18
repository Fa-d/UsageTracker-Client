package dev.sadakat.screentimetracker.core.domain.model

data class AppSession(
    val packageName: String,
    val appName: String,
    val timeRange: TimeRange,
    val category: AppCategory,
    val sessionType: SessionType = SessionType.REGULAR
) {
    val durationMillis: Long
        get() = timeRange.durationMillis()

    fun isLongSession(thresholdMillis: Long = 30 * 60 * 1000L): Boolean {
        return durationMillis >= thresholdMillis
    }

    fun isProductive(): Boolean = category.isProductive

    fun overlaps(other: AppSession): Boolean {
        return timeRange.overlaps(other.timeRange)
    }
}

enum class SessionType {
    REGULAR,
    FOCUS_SESSION,
    MINDFULNESS_BREAK,
    PRODUCTIVE_WORK
}

data class AppCategory(
    val name: String,
    val isProductive: Boolean,
    val defaultLimitMinutes: Int = 0,
    val color: String = "#6366F1"
) {
    companion object {
        val SOCIAL_MEDIA = AppCategory("Social Media", false, 60, "#EF4444")
        val PRODUCTIVITY = AppCategory("Productivity", true, 0, "#10B981")
        val ENTERTAINMENT = AppCategory("Entertainment", false, 120, "#F59E0B")
        val EDUCATION = AppCategory("Education", true, 0, "#3B82F6")
        val COMMUNICATION = AppCategory("Communication", true, 0, "#8B5CF6")
        val GAMES = AppCategory("Games", false, 90, "#F97316")
        val HEALTH_FITNESS = AppCategory("Health & Fitness", true, 0, "#06B6D4")
        val SHOPPING = AppCategory("Shopping", false, 30, "#EC4899")
        val NEWS = AppCategory("News", true, 45, "#6B7280")
        val UTILITIES = AppCategory("Utilities", true, 0, "#84CC16")
        val UNCATEGORIZED = AppCategory("Uncategorized", false, 0, "#6B7280")

        fun getDefaultCategories(): List<AppCategory> = listOf(
            SOCIAL_MEDIA, PRODUCTIVITY, ENTERTAINMENT, EDUCATION,
            COMMUNICATION, GAMES, HEALTH_FITNESS, SHOPPING, NEWS, UTILITIES
        )
    }
}