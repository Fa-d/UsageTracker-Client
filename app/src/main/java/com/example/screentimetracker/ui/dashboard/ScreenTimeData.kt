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

// Mock data
val todayStats = TodayStats(
    screenTime = "4h 32m",
    unlocks = 87,
    appOpens = 142,
    goalProgress = 68
)

val weeklyData = listOf(
    WeeklyData("Mon", 3.5f),
    WeeklyData("Tue", 4.2f),
    WeeklyData("Wed", 5.1f),
    WeeklyData("Thu", 4.8f),
    WeeklyData("Fri", 6.2f),
    WeeklyData("Sat", 7.1f),
    WeeklyData("Sun", 4.5f)
)

val categoryData = listOf(
    CategoryData("Social", 35, Color(0xFFFF6B6B), "1h 35m"),
    CategoryData("Entertainment", 25, Color(0xFF4ECDC4), "1h 8m"),
    CategoryData("Productivity", 20, Color(0xFF45B7D1), "54m"),
    CategoryData("Games", 15, Color(0xFF96CEB4), "41m"),
    CategoryData("Other", 5, Color(0xFFFFEAA7), "15m")
)

val appUsageData = listOf(
    AppUsageData("Instagram", "Social", "45m", 23, "\uD83D\uDCF7"),
    AppUsageData("YouTube", "Entertainment", "38m", 12, "\uD83D\uDCFA"),
    AppUsageData("WhatsApp", "Social", "32m", 45, "\uD83D\uDCAC"),
    AppUsageData("Spotify", "Entertainment", "28m", 8, "\uD83C\uDFB5"),
    AppUsageData("Chrome", "Productivity", "25m", 15, "\uD83C\uDF10")
)

val timelineData = listOf(
    TimelineEntry("09:00", "Messages", "5m", "Communication"),
    TimelineEntry("09:15", "Instagram", "12m", "Social"),
    TimelineEntry("10:30", "Slack", "8m", "Productivity"),
    TimelineEntry("11:00", "YouTube", "15m", "Entertainment"),
    TimelineEntry("12:00", "Maps", "3m", "Navigation"),
    TimelineEntry("14:30", "WhatsApp", "7m", "Communication")
)

