package dev.sadakat.screentimetracker.domain.model

data class WellnessAlert(
    val id: String,
    val title: String,
    val message: String,
    val alertType: WellnessAlertType,
    val severity: AlertSeverity,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val actionRequired: Boolean = false,
    val relatedData: Map<String, Any> = emptyMap()
)

enum class WellnessAlertType {
    EXCESSIVE_USAGE,
    SLEEP_DISRUPTION,
    POOR_POSTURE_WARNING,
    BREAK_NEEDED,
    GOAL_MISSED,
    STREAK_BROKEN,
    HEALTH_CONCERN
}

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL,
    URGENT
}