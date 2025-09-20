package dev.sadakat.screentimetracker.core.presentation.ui.dashboard.viewmodels
import dev.sadakat.screentimetracker.core.domain.usecase.CalculateWellnessScoreUseCase
import dev.sadakat.screentimetracker.core.domain.usecase.GetDashboardDataUseCase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sadakat.screentimetracker.domain.usecases.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.sadakat.screentimetracker.core.domain.model.WellnessLevel
import dev.sadakat.screentimetracker.core.domain.model.WellnessScore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuickActionsUiState(
    val isFocusModeActive: Boolean = false,
    val isEmergencyBlockActive: Boolean = false,
    val activeTimerMinutes: Int = 0,
    val todayUsageHours: String = "0h",
    val wellnessScore: Int = 0,
    val isBreakReminderActive: Boolean = false
)

@HiltViewModel
class QuickActionsViewModel @Inject constructor(
    private val focusSessionManagerUseCase: FocusSessionManagerUseCase,
    private val calculateWellnessScoreUseCase: CalculateWellnessScoreUseCase,
    private val getDashboardDataUseCase: GetDashboardDataUseCase
) : ViewModel() {
    
    private val _isFocusModeActive = MutableStateFlow(false)
    private val _isEmergencyBlockActive = MutableStateFlow(false)
    private val _activeTimerMinutes = MutableStateFlow(0)
    private val _isBreakReminderActive = MutableStateFlow(false)
    
    val uiState: StateFlow<QuickActionsUiState> = combine(
        _isFocusModeActive,
        _isEmergencyBlockActive,
        _activeTimerMinutes,
        _isBreakReminderActive,
getDashboardDataUseCase.observeDashboardData()
    ) { focusMode, emergencyBlock, timer, breakReminder, dashboardData ->
        val wellnessScore = dashboardData.wellnessScore
        
        QuickActionsUiState(
            isFocusModeActive = focusMode,
            isEmergencyBlockActive = emergencyBlock,
            activeTimerMinutes = timer,
            todayUsageHours = formatUsageTime(dashboardData.totalScreenTimeToday),
            wellnessScore = wellnessScore.overall,
            isBreakReminderActive = breakReminder
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = QuickActionsUiState()
    )
    
    fun toggleFocusMode() {
        viewModelScope.launch {
            val isCurrentlyActive = _isFocusModeActive.value
            if (isCurrentlyActive) {
                focusSessionManagerUseCase.cancelCurrentFocusSession()
                _isFocusModeActive.value = false
            } else {
                focusSessionManagerUseCase.startFocusSession(
                    durationMinutes = 25, // Default pomodoro
                    appsToBlock = emptyList() // Will be handled by use case
                )
                _isFocusModeActive.value = true
            }
        }
    }
    
    fun triggerEmergencyBlock() {
        viewModelScope.launch {
            // Implement emergency block logic
            _isEmergencyBlockActive.value = true
            
            // Auto-disable after 30 minutes
            kotlinx.coroutines.delay(30 * 60 * 1000L)
            _isEmergencyBlockActive.value = false
        }
    }
    
    fun startActivityTimer(minutes: Int) {
        viewModelScope.launch {
            _activeTimerMinutes.value = minutes
            
            // Countdown timer
            repeat(minutes) { minute ->
                kotlinx.coroutines.delay(60 * 1000L) // 1 minute
                _activeTimerMinutes.value = minutes - (minute + 1)
            }
            
            // Timer completed
            _activeTimerMinutes.value = 0
            // TODO: Show completion notification
        }
    }
    
    fun startBreakReminder() {
        viewModelScope.launch {
            _isBreakReminderActive.value = true
            
            // 10-minute break reminder
            kotlinx.coroutines.delay(10 * 60 * 1000L)
            _isBreakReminderActive.value = false
            
            // TODO: Show break completion notification
        }
    }
    
    fun refreshWellnessScore() {
        viewModelScope.launch {
            calculateWellnessScoreUseCase(System.currentTimeMillis())
        }
    }
    
    private fun formatUsageTime(millis: Long): String {
        val hours = millis / (60 * 60 * 1000)
        val minutes = (millis % (60 * 60 * 1000)) / (60 * 1000)
        
        return when {
            hours > 0 -> "${hours}h"
            minutes > 0 -> "${minutes}m"
            else -> "0m"
        }
    }
}