package dev.sadakat.screentimetracker.shared.presentation.analytics

import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsUiState(
    val selectedTimeRange: TimeRange = TimeRange.WEEK,
    val totalUsageTime: String = "0h 0m",
    val averageDailyUsage: String = "0h 0m",
    val totalPickups: Int = 0,
    val averagePickups: Int = 0,
    val usageTrendsData: List<UsageTrendData> = emptyList(),
    val appUsageBreakdown: List<AppUsageBreakdown> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@Serializable
enum class TimeRange(val label: String) {
    TODAY("Today"),
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year")
}

@Serializable
data class UsageTrendData(
    val label: String,
    val hours: Float
)

@Serializable
data class AppUsageBreakdown(
    val appName: String,
    val usageTime: String,
    val percentage: Float,
    val change: String
)