package dev.sadakat.screentimetracker.ui.timerestrictions.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sadakat.screentimetracker.core.data.local.TimeRestriction
import dev.sadakat.screentimetracker.domain.usecases.TimeRestrictionManagerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class TimeRestrictionsViewModel @Inject constructor(
    private val timeRestrictionManagerUseCase: TimeRestrictionManagerUseCase
) : ViewModel() {

    private val _uiState = mutableStateOf(TimeRestrictionsUiState())
    val uiState: State<TimeRestrictionsUiState> = _uiState

    private val _uiEvents = MutableSharedFlow<TimeRestrictionsUiEvent>()
    val uiEvents: SharedFlow<TimeRestrictionsUiEvent> = _uiEvents.asSharedFlow()

    init {
        loadTimeRestrictions()
        loadActiveRestrictions()
    }

    private fun loadTimeRestrictions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                timeRestrictionManagerUseCase.getAllTimeRestrictions().collectLatest { restrictions ->
                    _uiState.value = _uiState.value.copy(
                        restrictions = restrictions,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load restrictions: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun loadActiveRestrictions() {
        viewModelScope.launch {
            try {
                timeRestrictionManagerUseCase.getActiveTimeRestrictions().collectLatest { activeRestrictions ->
                    _uiState.value = _uiState.value.copy(
                        activeRestrictions = activeRestrictions
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load active restrictions: ${e.message}"
                )
            }
        }
    }

    fun createDefaultRestrictions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingDefaults = true)
            try {
                timeRestrictionManagerUseCase.createDefaultTimeRestrictions()
                _uiEvents.emit(TimeRestrictionsUiEvent.ShowSuccess("Default restrictions created successfully"))
                _uiState.value = _uiState.value.copy(isCreatingDefaults = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to create default restrictions: ${e.message}",
                    isCreatingDefaults = false
                )
            }
        }
    }

    fun toggleRestriction(restriction: TimeRestriction) {
        viewModelScope.launch {
            try {
                timeRestrictionManagerUseCase.updateRestrictionEnabled(
                    restriction.id, 
                    !restriction.isEnabled
                )
                _uiEvents.emit(
                    TimeRestrictionsUiEvent.ShowSuccess(
                        "${restriction.name} ${if (restriction.isEnabled) "disabled" else "enabled"}"
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to toggle restriction: ${e.message}"
                )
            }
        }
    }

    fun createCustomRestriction(
        name: String,
        description: String,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        selectedApps: List<String>,
        selectedDays: List<Int>,
        allowEmergencyApps: Boolean = true,
        showNotifications: Boolean = true
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingCustom = true)
            try {
                val startTimeMinutes = startHour * 60 + startMinute
                val endTimeMinutes = endHour * 60 + endMinute
                
                timeRestrictionManagerUseCase.createCustomRestriction(
                    name = name,
                    description = description,
                    startTimeMinutes = startTimeMinutes,
                    endTimeMinutes = endTimeMinutes,
                    blockedApps = selectedApps,
                    daysOfWeek = selectedDays,
                    allowEmergencyApps = allowEmergencyApps,
                    showNotifications = showNotifications
                )
                
                _uiEvents.emit(TimeRestrictionsUiEvent.ShowSuccess("Custom restriction '$name' created successfully"))
                _uiEvents.emit(TimeRestrictionsUiEvent.CloseCreateDialog)
                _uiState.value = _uiState.value.copy(isCreatingCustom = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to create custom restriction: ${e.message}",
                    isCreatingCustom = false
                )
            }
        }
    }

    fun checkAppBlocked(packageName: String) {
        viewModelScope.launch {
            try {
                val isBlocked = timeRestrictionManagerUseCase.isAppBlockedByTimeRestriction(packageName)
                _uiState.value = _uiState.value.copy(
                    lastCheckedApp = packageName to isBlocked
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to check app restriction: ${e.message}"
                )
            }
        }
    }

    fun getCurrentActiveRestrictions() {
        viewModelScope.launch {
            try {
                val currentActive = timeRestrictionManagerUseCase.getCurrentActiveRestrictions()
                _uiState.value = _uiState.value.copy(
                    currentActiveRestrictions = currentActive
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to get current active restrictions: ${e.message}"
                )
            }
        }
    }

    fun showCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true)
    }

    fun hideCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun getRestrictionStatusPreview(restriction: TimeRestriction): RestrictionStatusPreview {
        val calendar = Calendar.getInstance()
        val currentTimeMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1

        val restrictionDays = restriction.daysOfWeek.split(",").mapNotNull { it.toIntOrNull() }
        val isActiveDay = restrictionDays.contains(dayOfWeek)
        
        val isActiveTime = if (restriction.startTimeMinutes <= restriction.endTimeMinutes) {
            // Same day restriction
            currentTimeMinutes in restriction.startTimeMinutes..restriction.endTimeMinutes
        } else {
            // Overnight restriction
            currentTimeMinutes >= restriction.startTimeMinutes || currentTimeMinutes <= restriction.endTimeMinutes
        }

        val isCurrentlyActive = restriction.isEnabled && isActiveDay && isActiveTime

        val nextChangeMinutes = if (isCurrentlyActive) {
            restriction.endTimeMinutes
        } else if (isActiveDay && currentTimeMinutes < restriction.startTimeMinutes) {
            restriction.startTimeMinutes
        } else {
            null // Calculate next active day
        }

        return RestrictionStatusPreview(
            restriction = restriction,
            isCurrentlyActive = isCurrentlyActive,
            nextChangeTimeMinutes = nextChangeMinutes,
            timeUntilChange = nextChangeMinutes?.let { 
                if (it > currentTimeMinutes) it - currentTimeMinutes 
                else (24 * 60) - currentTimeMinutes + it
            }
        )
    }

    fun formatTime(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return String.format("%02d:%02d", hours, minutes)
    }

    fun formatTimeUntil(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return when {
            hours > 0 -> "${hours}h ${mins}m"
            else -> "${mins}m"
        }
    }
}

data class TimeRestrictionsUiState(
    val restrictions: List<TimeRestriction> = emptyList(),
    val activeRestrictions: List<TimeRestriction> = emptyList(),
    val currentActiveRestrictions: List<TimeRestriction> = emptyList(),
    val isLoading: Boolean = false,
    val isCreatingDefaults: Boolean = false,
    val isCreatingCustom: Boolean = false,
    val showCreateDialog: Boolean = false,
    val error: String? = null,
    val lastCheckedApp: Pair<String, Boolean>? = null
)

sealed class TimeRestrictionsUiEvent {
    object CloseCreateDialog : TimeRestrictionsUiEvent()
    data class ShowSuccess(val message: String) : TimeRestrictionsUiEvent()
    data class NavigateToAppSelection(val restrictionId: Long) : TimeRestrictionsUiEvent()
}

data class RestrictionStatusPreview(
    val restriction: TimeRestriction,
    val isCurrentlyActive: Boolean,
    val nextChangeTimeMinutes: Int?,
    val timeUntilChange: Int?
)