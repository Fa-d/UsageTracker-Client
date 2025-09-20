package dev.sadakat.screentimetracker.core.data.local.dto

// Data class for app open count information  
data class AppOpenData(
    val packageName: String,
    val openCount: Int,
    val lastOpenedTimestamp: Long = 0L
)
