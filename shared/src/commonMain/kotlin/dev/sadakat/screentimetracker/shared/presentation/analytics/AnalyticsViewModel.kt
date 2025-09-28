package dev.sadakat.screentimetracker.shared.presentation.analytics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val coroutineScope: CoroutineScope
) {
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalyticsData()
    }

    private fun loadAnalyticsData() {
        coroutineScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
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
                    ),
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun selectTimeRange(timeRange: TimeRange) {
        _uiState.value = _uiState.value.copy(selectedTimeRange = timeRange)
        loadAnalyticsData()
    }

    fun refresh() {
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