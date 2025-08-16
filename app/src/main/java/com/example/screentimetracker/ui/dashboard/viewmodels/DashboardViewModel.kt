package com.example.screentimetracker.ui.dashboard.viewmodels

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
import com.example.screentimetracker.ui.dashboard.state.DashboardState
import com.example.screentimetracker.ui.dashboard.state.AppUsageUIModel
import com.example.screentimetracker.data.repository.DigitalPetRepository
import com.example.screentimetracker.data.local.DigitalPet
import com.example.screentimetracker.data.local.PetStats
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
    private val digitalPetRepository: DigitalPetRepository,
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

    private val _digitalPet = MutableStateFlow<DigitalPet?>(null)
    val digitalPet: StateFlow<DigitalPet?> = _digitalPet.asStateFlow()

    private val _petStats = MutableStateFlow<PetStats?>(null)
    val petStats: StateFlow<PetStats?> = _petStats.asStateFlow()

    init {
        // Initialize achievements first
        viewModelScope.launch {
            try {
                initializeAchievementsUseCase()
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error initializing achievements", e)
            }
        }

        // Initialize digital pet if needed
        viewModelScope.launch {
            try {
                createDefaultPetIfNeeded()
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error initializing digital pet", e)
            }
        }

        // Load dashboard data, achievements, wellness score, and digital pet
        viewModelScope.launch {
            combine(
                getDashboardDataUseCase(),
                getHistoricalDataUseCase(),
                getAchievementsUseCase(),
                digitalPetRepository.getPet()
            ) { todayData, historicalData, achievements, digitalPet ->
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

                // Calculate wellness score for today (dynamic calculation)
                try {
                    val todayWellnessScore = calculateWellnessScoreUseCase(System.currentTimeMillis(), forceRecalculate = false)
                    _wellnessScore.value = todayWellnessScore
                } catch (e: Exception) {
                    Log.e("DashboardViewModel", "Error calculating wellness score", e)
                }

                // Update digital pet state and wellness
                _digitalPet.value = digitalPet
                if (digitalPet != null) {
                    try {
                        val stats = digitalPetRepository.getPetStats(digitalPet)
                        _petStats.value = stats
                        
                        // Update pet wellness with dynamic wellness score if enough time has passed
                        if (digitalPetRepository.shouldUpdatePetWellness()) {
                            val currentState = _uiState.value.copy(
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
                            
                            // Update pet with current wellness score for dynamic response
                            val updatedPet = digitalPetRepository.updatePetWithWellness(
                                currentState, 
                                wellnessScore = _wellnessScore.value
                            )
                            _digitalPet.value = updatedPet
                            val updatedStats = digitalPetRepository.getPetStats(updatedPet)
                            _petStats.value = updatedStats
                        }
                    } catch (e: Exception) {
                        Log.e("DashboardViewModel", "Error calculating pet stats", e)
                    }
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

    fun updatePetWithCurrentWellness() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val updatedPet = digitalPetRepository.updatePetWithWellness(
                    currentState,
                    wellnessScore = _wellnessScore.value
                )
                _digitalPet.value = updatedPet
                val stats = digitalPetRepository.getPetStats(updatedPet)
                _petStats.value = stats
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error updating pet wellness", e)
            }
        }
    }

    fun interactWithPet() {
        viewModelScope.launch {
            try {
                val updatedPet = digitalPetRepository.petInteraction()
                if (updatedPet != null) {
                    _digitalPet.value = updatedPet
                    val stats = digitalPetRepository.getPetStats(updatedPet)
                    _petStats.value = stats
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error interacting with pet", e)
            }
        }
    }

    fun feedPet() {
        viewModelScope.launch {
            try {
                val updatedPet = digitalPetRepository.feedPet()
                if (updatedPet != null) {
                    _digitalPet.value = updatedPet
                    val stats = digitalPetRepository.getPetStats(updatedPet)
                    _petStats.value = stats
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error feeding pet", e)
            }
        }
    }

    fun createDefaultPetIfNeeded() {
        viewModelScope.launch {
            try {
                if (digitalPetRepository.isFirstTimeUser()) {
                    val newPet = digitalPetRepository.createDefaultPet()
                    _digitalPet.value = newPet
                    val stats = digitalPetRepository.getPetStats(newPet)
                    _petStats.value = stats
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error creating default pet", e)
            }
        }
    }

    fun refreshPetWellness() {
        viewModelScope.launch {
            try {
                updatePetWithCurrentWellness()
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error refreshing pet wellness", e)
            }
        }
    }
    
    fun refreshWellnessScore() {
        viewModelScope.launch {
            try {
                val todayWellnessScore = calculateWellnessScoreUseCase(System.currentTimeMillis(), forceRecalculate = true)
                _wellnessScore.value = todayWellnessScore
                
                // Update pet with the new wellness score for immediate response
                updatePetWithCurrentWellness()
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error refreshing wellness score", e)
            }
        }
    }
}
