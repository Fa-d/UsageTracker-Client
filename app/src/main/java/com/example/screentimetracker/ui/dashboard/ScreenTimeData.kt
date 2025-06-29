package com.example.screentimetracker.ui.dashboard

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
    val value: Int,
    val color: Color,
    val time: String
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

