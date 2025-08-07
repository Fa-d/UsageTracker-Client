package com.example.screentimetracker.ui.dashboard.state

import androidx.compose.ui.graphics.Color

// Data models
data class TodayStats(
    val screenTime: String,
    val unlocks: Int,
    val appOpens: Int,
    val goalProgress: Int
)

data class WeeklyData(
    val day: String,
    val hours: Float
)

data class CategoryData(
    val name: String,
    val packageName: String = "",
    val value: Long, // Changed to Long for duration in milliseconds
    val color: Color,
    val time: String,
    val openCount: Int = 0
)

data class AppUsageData(
    val app: String,
    val category: String,
    val time: String,
    val opens: Int,
    val icon: String
)

data class TimelineEntry(
    val time: String,
    val app: String,
    val duration: String,
    val category: String
)

