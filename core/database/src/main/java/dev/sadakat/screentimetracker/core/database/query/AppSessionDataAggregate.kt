package dev.sadakat.screentimetracker.core.database.query

// Data class for aggregated session data
data class AppSessionDataAggregate(
    val packageName: String,
    val totalDuration: Long,
    val sessionCount: Int
)