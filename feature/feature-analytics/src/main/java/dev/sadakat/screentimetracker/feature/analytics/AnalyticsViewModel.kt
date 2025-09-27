package dev.sadakat.screentimetracker.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    // TODO: Add repositories when available
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalyticsData()
    }

    private fun loadAnalyticsData() {
        viewModelScope.launch {
            // TODO: Load actual data from repositories
            _uiState.value = AnalyticsUiState(
                selectedTimeRange = TimeRange.WEEK,
                totalUsageTime = "31h 45m",
                averageDailyUsage = "4h 32m",
                totalPickups = 469,
                averagePickups = 67,
                usageTrendsData = generateMockTrendsData(),
                appUsageBreakdown = listOf(
                    AppUsageBreakdown("Instagram", "8h 30m", 0.27f, "+15%"),
                    AppUsageBreakdown("WhatsApp", "6h 15m", 0.20f, "-5%"),
                    AppUsageBreakdown("YouTube", "5h 45m", 0.18f, "+22%"),
                    AppUsageBreakdown("Chrome", "4h 20m", 0.14f, "+8%"),
                    AppUsageBreakdown("Maps", "2h 30m", 0.08f, "-12%"),
                    AppUsageBreakdown("Others", "4h 25m", 0.13f, "+3%")
                )
            )
        }
    }

    fun selectTimeRange(timeRange: TimeRange) {
        _uiState.value = _uiState.value.copy(selectedTimeRange = timeRange)
        loadAnalyticsData()
    }

    private fun generateMockTrendsData(): List<UsageTrendData> {
        return listOf(
            UsageTrendData("Mon", 4.2f),
            UsageTrendData("Tue", 3.8f),
            UsageTrendData("Wed", 5.1f),
            UsageTrendData("Thu", 4.7f),
            UsageTrendData("Fri", 6.2f),
            UsageTrendData("Sat", 5.9f),
            UsageTrendData("Sun", 4.3f)
        )
    }
}

data class AnalyticsUiState(
    val selectedTimeRange: TimeRange = TimeRange.WEEK,
    val totalUsageTime: String = "0h 0m",
    val averageDailyUsage: String = "0h 0m",
    val totalPickups: Int = 0,
    val averagePickups: Int = 0,
    val usageTrendsData: List<UsageTrendData> = emptyList(),
    val appUsageBreakdown: List<AppUsageBreakdown> = emptyList(),
    val isLoading: Boolean = false
)

enum class TimeRange(val label: String) {
    TODAY("Today"),
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year")
}

data class UsageTrendData(
    val label: String,
    val hours: Float
)

data class AppUsageBreakdown(
    val appName: String,
    val usageTime: String,
    val percentage: Float,
    val change: String
)