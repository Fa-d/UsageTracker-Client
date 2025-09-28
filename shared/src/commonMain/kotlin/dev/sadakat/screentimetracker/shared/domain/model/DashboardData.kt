package dev.sadakat.screentimetracker.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DashboardData(
    val totalScreenTimeMs: Long,
    val totalScreenTimeFormatted: String,
    val pickupsToday: Int,
    val wellnessScore: Float,
    val topApps: List<AppUsageInfo>,
    val isLoading: Boolean = false
)

@Serializable
data class UsageSession(
    val id: String,
    val packageName: String,
    val appName: String,
    val startTime: Long,
    val endTime: Long,
    val durationMs: Long
)

@Serializable
data class Goal(
    val id: String,
    val title: String,
    val description: String,
    val targetValue: Long,
    val currentValue: Long,
    val unit: GoalUnit,
    val type: GoalType,
    val isCompleted: Boolean = false,
    val createdAt: Long,
    val deadline: Long? = null
)

@Serializable
enum class GoalUnit {
    MINUTES,
    HOURS,
    PICKUPS,
    APPS
}

@Serializable
enum class GoalType {
    REDUCE_SCREEN_TIME,
    REDUCE_PICKUPS,
    LIMIT_APP_TIME,
    WELLNESS_SCORE
}