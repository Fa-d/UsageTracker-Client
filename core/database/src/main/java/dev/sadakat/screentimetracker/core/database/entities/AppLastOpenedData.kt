package dev.sadakat.screentimetracker.core.database.entities

// Data class for last opened timestamp data
data class AppLastOpenedData(
    val packageName: String,
    val lastOpenedTimestamp: Long
)