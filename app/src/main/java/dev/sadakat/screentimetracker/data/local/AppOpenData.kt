package dev.sadakat.screentimetracker.data.local

// Data class for app open count information  
data class AppOpenData(
    val packageName: String,
    val openCount: Int,
    val lastOpenedTimestamp: Long = 0L
)
