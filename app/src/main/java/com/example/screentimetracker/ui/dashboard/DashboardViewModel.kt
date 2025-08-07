package com.example.screentimetracker.ui.dashboard

import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.screentimetracker.domain.usecases.GetDashboardDataUseCase
import com.example.screentimetracker.domain.usecases.GetHistoricalDataUseCase
import com.example.screentimetracker.domain.usecases.HistoricalData
import com.example.screentimetracker.domain.usecases.GetAppSessionEventsUseCase
import com.example.screentimetracker.domain.usecases.GetAchievementsUseCase
import com.example.screentimetracker.domain.usecases.CalculateWellnessScoreUseCase
import com.example.screentimetracker.domain.usecases.InitializeAchievementsUseCase
import com.example.screentimetracker.domain.usecases.ChallengeManagerUseCase
import com.example.screentimetracker.domain.usecases.FocusSessionManagerUseCase
import com.example.screentimetracker.domain.usecases.WeeklyInsightsUseCase
import com.example.screentimetracker.domain.model.Achievement
import com.example.screentimetracker.domain.model.WellnessScore
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
    private val getHistoricalDataUseCase: GetHistoricalDataUseCase,
    private val getAppSessionEventsUseCase: GetAppSessionEventsUseCase,
    private val getAchievementsUseCase: GetAchievementsUseCase,
    private val calculateWellnessScoreUseCase: CalculateWellnessScoreUseCase,
    private val initializeAchievementsUseCase: InitializeAchievementsUseCase,
    val challengeManagerUseCase: ChallengeManagerUseCase,
    val focusSessionManagerUseCase: FocusSessionManagerUseCase,
    val weeklyInsightsUseCase: WeeklyInsightsUseCase,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardState(isLoading = true))
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    private val _timelineEvents = MutableStateFlow<List<com.example.screentimetracker.data.local.AppSessionEvent>>(emptyList())
    val timelineEvents: StateFlow<List<com.example.screentimetracker.data.local.AppSessionEvent>> = _timelineEvents.asStateFlow()

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private val _wellnessScore = MutableStateFlow<WellnessScore?>(null)
    val wellnessScore: StateFlow<WellnessScore?> = _wellnessScore.asStateFlow()

    init {
        // Initialize achievements first
        viewModelScope.launch {
            try {
                initializeAchievementsUseCase()
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error initializing achievements", e)
            }
        }

        // Load dashboard data, achievements, and wellness score
        viewModelScope.launch {
            combine(
                getDashboardDataUseCase(),
                getHistoricalDataUseCase(),
                getAchievementsUseCase()
            ) { todayData, historicalData, achievements ->
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
                    historicalData.appSummaries
                        .groupBy { it.dateMillis } // Group by day
                        .mapValues { entry -> entry.value.sumOf { it.totalDurationMillis } } // Sum durations for each day
                        .values.average().toLong() // Average of daily sums
                } else 0L

                val avgUnlocks = if (historicalData.unlockSummaries.isNotEmpty()) {
                    historicalData.unlockSummaries.map { it.unlockCount }.average().toInt()
                } else 0

                // Update achievements state
                _achievements.value = achievements

                // Calculate wellness score for today
                try {
                    val todayWellnessScore = calculateWellnessScoreUseCase(System.currentTimeMillis())
                    _wellnessScore.value = todayWellnessScore
                } catch (e: Exception) {
                    Log.e("DashboardViewModel", "Error calculating wellness score", e)
                }

                _uiState.value.copy(
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
            }.catch { exception ->
                Log.e("DashboardViewModel", "Error loading dashboard data", exception)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load dashboard data: ${exception.localizedMessage ?: "Unknown error"}"
                )
            }.collect { newState ->
                _uiState.value = newState
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
        Log.d("DashboardViewModel", "Loading timeline events from $startTime to $endTime")
        viewModelScope.launch {
            getAppSessionEventsUseCase(startTime, endTime)
                .catch { exception ->
                    Log.e("DashboardViewModel", "Error loading timeline events", exception)
                    // Handle error, maybe update a separate error state for timeline
                }
                .collect { events ->
                    Log.d("DashboardViewModel", "Received ${events.size} timeline events.")
                    _timelineEvents.value = events
                }
        }
    }
}
