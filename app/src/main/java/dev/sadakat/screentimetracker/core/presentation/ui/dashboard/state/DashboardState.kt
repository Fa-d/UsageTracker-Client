package dev.sadakat.screentimetracker.core.presentation.ui.dashboard.state

import dev.sadakat.screentimetracker.core.data.local.entities.DailyAppSummary // Import if using directly
import dev.sadakat.screentimetracker.core.data.local.entities.DailyScreenUnlockSummary // Import if using directly

// Data class to represent an app's usage info for the UI for the CURRENT DAY
data class AppUsageUIModel(
    val packageName: String,
    val appName: String,
    val openCount: Int, // Open count for today
    val lastOpenedTimestamp: Long, // Last opened time for today
    val totalDurationMillisToday: Long = 0L // New: Total duration for today
)

data class DashboardState(
    val isLoading: Boolean = true,
    val error: String? = null,

    // Today's Data
    val totalScreenUnlocksToday: Int = 0,
    val appUsagesToday: List<AppUsageUIModel> = emptyList(), // Renamed from appUsages for clarity
    val totalScreenTimeTodayMillis: Long = 0L, // New: Overall screen time for today from sessions

    // Historical Data (e.g., last 7 days)
    val historicalAppSummaries: List<DailyAppSummary> = emptyList(), // Or List<HistoricalAppUsageUIModel>
    val historicalUnlockSummaries: List<DailyScreenUnlockSummary> = emptyList(), // Or List<HistoricalUnlockUIModel>

    val averageDailyScreenTimeMillisLastWeek: Long = 0L, // Example processed insight
    val averageDailyUnlocksLastWeek: Int = 0 // Example processed insight
)
