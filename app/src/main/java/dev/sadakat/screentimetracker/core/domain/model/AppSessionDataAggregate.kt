package dev.sadakat.screentimetracker.core.domain.model

/**
 * Legacy domain model for app session data aggregate - temporary for compatibility
 */
data class AppSessionDataAggregate(
    val packageName: String,
    val totalDuration: Long,
    val sessionCount: Int,
    val lastOpenedTimestamp: Long,
    val totalDurationMillis: Long
)