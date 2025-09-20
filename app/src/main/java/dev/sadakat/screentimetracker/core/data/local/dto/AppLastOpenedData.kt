package dev.sadakat.screentimetracker.core.data.local.dto

// Data class for last opened timestamp data
data class AppLastOpenedData(
    val packageName: String,
    val lastOpenedTimestamp: Long
)