package dev.sadakat.screentimetracker.core.domain.model

/**
 * Legacy domain model for app open data - temporary for compatibility
 */
data class AppOpenData(
    val packageName: String,
    val openCount: Int,
    val lastOpenedTimestamp: Long,
    val totalDurationMillis: Long
)