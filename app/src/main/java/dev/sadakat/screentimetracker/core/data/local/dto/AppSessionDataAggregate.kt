package dev.sadakat.screentimetracker.core.data.local.dto

// Data class for aggregated session data
data class AppSessionDataAggregate(
    val packageName: String,
    val totalDuration: Long,
    val sessionCount: Int
)
