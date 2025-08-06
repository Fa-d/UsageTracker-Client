package com.example.screentimetracker.data.local

// Data class for last opened timestamp data
data class AppLastOpenedData(
    val packageName: String,
    val lastOpenedTimestamp: Long
)