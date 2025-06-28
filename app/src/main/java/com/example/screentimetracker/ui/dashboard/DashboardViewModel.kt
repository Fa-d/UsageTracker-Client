package com.example.screentimetracker.ui.dashboard

import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.screentimetracker.domain.usecases.GetDashboardDataUseCase
import com.example.screentimetracker.domain.usecases.GetHistoricalDataUseCase
import com.example.screentimetracker.domain.usecases.GetAppSessionEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val getHistoricalDataUseCase: GetHistoricalDataUseCase, // New UseCase
    private val getAppSessionEventsUseCase: GetAppSessionEventsUseCase,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardState(isLoading = true))
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    private val _timelineEvents = MutableStateFlow<List<com.example.screentimetracker.data.local.AppSessionEvent>>(emptyList())
    val timelineEvents: StateFlow<List<com.example.screentimetracker.data.local.AppSessionEvent>> = _timelineEvents.asStateFlow()

    fun loadData() { // Removed suspend, as flow collection is main async part
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val todayDataFlow = getDashboardDataUseCase()
            val historicalDataFlow = getHistoricalDataUseCase() // Default is last 7 days

            todayDataFlow.combine(historicalDataFlow) { todayData, historicalData ->
                Pair(todayData, historicalData)
            }
            .catch { exception ->
                Log.e("DashboardViewModel", "Error loading dashboard data", exception)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load dashboard data: ${exception.localizedMessage ?: "Unknown error"}"
                )
            }
            .collect { (todayData, historicalData) ->
                val appUsageUIModels = todayData.appDetailsToday.map { detail ->
                    AppUsageUIModel(
                        packageName = detail.packageName,
                        appName = getAppName(detail.packageName),
                        openCount = detail.sessionCount,
                        lastOpenedTimestamp = detail.lastOpenedTimestamp,
                        totalDurationMillisToday = detail.totalDurationMillis
                    )
                }

                val avgScreenTime = if (historicalData.appSummaries.isNotEmpty()) {
                    // Calculate average daily screen time from historical app summaries
                    // This sums up total duration for each day, then averages these daily totals.
                    historicalData.appSummaries
                        .groupBy { it.dateMillis } // Group by day
                        .mapValues { entry -> entry.value.sumOf { it.totalDurationMillis } } // Sum durations for each day
                        .values.average().toLong() // Average of daily sums
                } else 0L

                val avgUnlocks = if (historicalData.unlockSummaries.isNotEmpty()) {
                    historicalData.unlockSummaries.map { it.unlockCount }.average().toInt()
                } else 0

                _uiState.value = DashboardState(
                    isLoading = false,
                    totalScreenUnlocksToday = todayData.totalScreenUnlocksToday,
                    appUsagesToday = appUsageUIModels,
                    totalScreenTimeTodayMillis = todayData.totalScreenTimeFromSessionsToday,
                    historicalAppSummaries = historicalData.appSummaries,
                    historicalUnlockSummaries = historicalData.unlockSummaries,
                    averageDailyScreenTimeMillisLastWeek = avgScreenTime,
                    averageDailyUnlocksLastWeek = avgUnlocks,
                    error = null
                )
            }
        }
    }

    internal fun getAppName(packageName: String): String {
        return try {
            val pm = application.packageManager
            val applicationInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            pm.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w("DashboardViewModel", "App name not found for package: $packageName")
            packageName // Fallback to package name
        }
    }

    fun loadTimelineEvents(startTime: Long, endTime: Long) {
        viewModelScope.launch {
            getAppSessionEventsUseCase(startTime, endTime)
                .catch { exception ->
                    Log.e("DashboardViewModel", "Error loading timeline events", exception)
                    // Handle error, maybe update a separate error state for timeline
                }
                .collect { events ->
                    _timelineEvents.value = events
                }
        }
    }
}
