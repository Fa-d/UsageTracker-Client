package dev.sadakat.screentimetracker.core.presentation.ui.focus.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sadakat.screentimetracker.data.local.entities.FocusSession
import dev.sadakat.screentimetracker.domain.usecases.FocusSessionManagerUseCase
import dev.sadakat.screentimetracker.domain.usecases.GetAllLimitedAppsUseCase
import dev.sadakat.screentimetracker.domain.usecases.UserPreferencesUseCase
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FocusModeViewModel @Inject constructor(
    private val focusSessionManagerUseCase: FocusSessionManagerUseCase,
    private val getAllLimitedAppsUseCase: GetAllLimitedAppsUseCase,
    private val userPreferencesUseCase: UserPreferencesUseCase,
    private val appLogger: AppLogger
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusModeState())
    val uiState: StateFlow<FocusModeState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "FocusModeViewModel"
    }

    init {
        loadFocusModeState()
        observeFocusSessions()
    }

    private fun loadFocusModeState() {
        viewModelScope.launch {
            try {
                // Check if there's an active focus session
                val isActive = focusSessionManagerUseCase.isSessionActive()
                val currentDuration = if (isActive) {
                    focusSessionManagerUseCase.getCurrentSessionDuration()
                } else 0L

                // Load blocked apps from limited apps - this will be populated from flow observation

                _uiState.value = _uiState.value.copy(
                    isActive = isActive,
                    currentSessionDuration = currentDuration,
                    isLoading = false
                )

                // Load user preferences for default duration
                val preferences = userPreferencesUseCase.getUserPreferencesOnce()
                _uiState.value = _uiState.value.copy(
                    selectedDurationMinutes = preferences.defaultFocusDurationMinutes ?: 25
                )

            } catch (e: Exception) {
                appLogger.e(TAG, "Error loading focus mode state", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load focus mode state"
                )
            }
        }
    }

    private fun observeFocusSessions() {
        viewModelScope.launch {
            combine(
                focusSessionManagerUseCase.getAllFocusSessions(),
                getAllLimitedAppsUseCase()
            ) { sessions, limitedApps ->
                _uiState.value = _uiState.value.copy(
                    recentSessions = sessions.take(5),
                    blockedApps = limitedApps.map { it.packageName }
                )
            }.collect {}
        }
    }

    fun toggleFocusMode() {
        viewModelScope.launch {
            try {
                if (_uiState.value.isActive) {
                    // Stop current session
                    val success = focusSessionManagerUseCase.completeFocusSession(
                        wasSuccessful = true,
                        interruptionCount = _uiState.value.interruptionCount
                    )
                    if (success) {
                        _uiState.value = _uiState.value.copy(
                            isActive = false,
                            currentSessionDuration = 0L,
                            interruptionCount = 0
                        )
                        appLogger.i(TAG, "Focus session stopped successfully")
                    }
                } else {
                    // Show duration selection dialog
                    _uiState.value = _uiState.value.copy(showDurationDialog = true)
                }
            } catch (e: Exception) {
                appLogger.e(TAG, "Error toggling focus mode", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to toggle focus mode"
                )
            }
        }
    }

    fun startFocusSession(durationMinutes: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    showDurationDialog = false
                )

                val sessionId = focusSessionManagerUseCase.startFocusSession(
                    durationMinutes = durationMinutes,
                    appsToBlock = _uiState.value.blockedApps
                )

                // Save user preference for future use
                userPreferencesUseCase.updateDefaultFocusDuration(durationMinutes)

                _uiState.value = _uiState.value.copy(
                    isActive = true,
                    selectedDurationMinutes = durationMinutes,
                    targetDurationMillis = durationMinutes * 60 * 1000L,
                    currentSessionId = sessionId,
                    isLoading = false
                )

                appLogger.i(TAG, "Focus session started: $durationMinutes minutes")

                // Start tracking duration
                startDurationTracking()

            } catch (e: Exception) {
                appLogger.e(TAG, "Error starting focus session", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to start focus session"
                )
            }
        }
    }

    private fun startDurationTracking() {
        viewModelScope.launch {
            while (_uiState.value.isActive) {
                try {
                    val currentDuration = focusSessionManagerUseCase.getCurrentSessionDuration()
                    _uiState.value = _uiState.value.copy(
                        currentSessionDuration = currentDuration
                    )

                    // Check if session should auto-complete
                    if (currentDuration >= _uiState.value.targetDurationMillis) {
                        completeFocusSession(wasSuccessful = true)
                        break
                    }

                    kotlinx.coroutines.delay(1000) // Update every second
                } catch (e: Exception) {
                    appLogger.e(TAG, "Error tracking session duration", e)
                    break
                }
            }
        }
    }

    fun completeFocusSession(wasSuccessful: Boolean) {
        viewModelScope.launch {
            try {
                val success = focusSessionManagerUseCase.completeFocusSession(
                    wasSuccessful = wasSuccessful,
                    interruptionCount = _uiState.value.interruptionCount
                )

                if (success) {
                    _uiState.value = _uiState.value.copy(
                        isActive = false,
                        currentSessionDuration = 0L,
                        currentSessionId = null,
                        interruptionCount = 0
                    )
                    appLogger.i(TAG, "Focus session completed. Success: $wasSuccessful")
                }
            } catch (e: Exception) {
                appLogger.e(TAG, "Error completing focus session", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to complete focus session"
                )
            }
        }
    }

    fun recordInterruption() {
        _uiState.value = _uiState.value.copy(
            interruptionCount = _uiState.value.interruptionCount + 1
        )
    }

    fun updateSelectedDuration(minutes: Int) {
        _uiState.value = _uiState.value.copy(selectedDurationMinutes = minutes)
    }

    fun dismissDurationDialog() {
        _uiState.value = _uiState.value.copy(showDurationDialog = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun isAppBlocked(packageName: String): Boolean {
        return _uiState.value.isActive && _uiState.value.blockedApps.contains(packageName)
    }
}

data class FocusModeState(
    val isActive: Boolean = false,
    val isLoading: Boolean = true,
    val currentSessionId: Long? = null,
    val selectedDurationMinutes: Int = 25, // Default Pomodoro duration
    val targetDurationMillis: Long = 0L,
    val currentSessionDuration: Long = 0L,
    val interruptionCount: Int = 0,
    val blockedApps: List<String> = emptyList(),
    val recentSessions: List<FocusSession> = emptyList(),
    val showDurationDialog: Boolean = false,
    val error: String? = null
)