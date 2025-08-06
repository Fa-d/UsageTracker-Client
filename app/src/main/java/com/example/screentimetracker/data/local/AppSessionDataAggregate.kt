package com.example.screentimetracker.data.local

// Data class for aggregated session data
data class AppSessionDataAggregate(
    val packageName: String,
    val totalDuration: Long,
    val sessionCount: Int
)
