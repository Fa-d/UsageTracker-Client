package dev.sadakat.screentimetracker.core.database.query

// Data class for last opened timestamp data
data class AppLastOpenedData(
    val packageName: String,
    val lastOpenedTimestamp: Long
)