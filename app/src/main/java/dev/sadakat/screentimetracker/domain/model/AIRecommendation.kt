package dev.sadakat.screentimetracker.domain.model

data class AIRecommendation(
    val id: String,
    val title: String,
    val description: String,
    val type: RecommendationType,
    val priority: Priority,
    val actionLabel: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isActionable: Boolean = false,
    val context: Map<String, Any> = emptyMap()
)

enum class RecommendationType {
    USAGE_REDUCTION,
    BREAK_REMINDER,
    FOCUS_SESSION,
    APP_LIMIT,
    WELLNESS_CHECK,
    HABIT_FORMATION
}

enum class Priority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}