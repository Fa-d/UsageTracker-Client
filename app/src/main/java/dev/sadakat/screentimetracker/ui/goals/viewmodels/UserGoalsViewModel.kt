package dev.sadakat.screentimetracker.ui.goals.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sadakat.screentimetracker.data.local.UserGoal
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import dev.sadakat.screentimetracker.domain.usecases.SmartGoalSettingUseCase
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class UserGoalsViewModel @Inject constructor(
    private val repository: TrackerRepository,
    private val appLogger: AppLogger
) : ViewModel() {
    
    companion object {
        private const val TAG = "UserGoalsViewModel"
    }

    private val _uiState = mutableStateOf(UserGoalsUiState())
    val uiState: State<UserGoalsUiState> = _uiState

    init {
        loadActiveGoals()
        calculateCurrentProgress()
    }

    private fun loadActiveGoals() {
        viewModelScope.launch {
            try {
                repository.getActiveGoals()
                    .catch { e ->
                        appLogger.e(TAG, "Error loading active goals", e)
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to load goals: ${e.message}",
                            isLoading = false
                        )
                    }
                    .collect { goals ->
                        _uiState.value = _uiState.value.copy(
                            activeGoals = goals,
                            dailyScreenTimeGoal = goals.find { it.goalType == SmartGoalSettingUseCase.DAILY_SCREEN_TIME },
                            unlockFrequencyGoal = goals.find { it.goalType == SmartGoalSettingUseCase.UNLOCK_FREQUENCY },
                            isLoading = false,
                            error = null
                        )
                    }
            } catch (e: Exception) {
                appLogger.e(TAG, "Failed to load goals", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load goals: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun calculateCurrentProgress() {
        viewModelScope.launch {
            try {
                val today = System.currentTimeMillis()
                val dayStart = today - (today % TimeUnit.DAYS.toMillis(1))
                val dayEnd = dayStart + TimeUnit.DAYS.toMillis(1)

                // Get today's data for progress calculation
                combine(
                    repository.getTotalScreenTimeFromSessionsInRange(dayStart, dayEnd),
                    repository.getUnlockCountForDayFlow(dayStart, dayEnd)
                ) { screenTime, unlockCount ->
                    _uiState.value = _uiState.value.copy(
                        currentScreenTime = screenTime ?: 0L,
                        currentUnlockCount = unlockCount
                    )
                }.catch { e ->
                    appLogger.e(TAG, "Error calculating progress", e)
                }.collect { }
            } catch (e: Exception) {
                appLogger.e(TAG, "Failed to calculate progress", e)
            }
        }
    }

    fun createGoal(goalType: String, targetValue: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isCreatingGoal = true, error = null)
                
                val goal = UserGoal(
                    goalType = goalType,
                    targetValue = targetValue,
                    deadline = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30) // 30-day goal
                )
                
                repository.insertGoal(goal)
                appLogger.i(TAG, "Created goal: $goalType with target $targetValue")
                
                _uiState.value = _uiState.value.copy(isCreatingGoal = false, showGoalCreationDialog = false)
            } catch (e: Exception) {
                appLogger.e(TAG, "Failed to create goal", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to create goal: ${e.message}",
                    isCreatingGoal = false
                )
            }
        }
    }

    fun createDailyScreenTimeGoal(targetHours: Int) {
        val targetMillis = TimeUnit.HOURS.toMillis(targetHours.toLong())
        createGoal(SmartGoalSettingUseCase.DAILY_SCREEN_TIME, targetMillis)
    }

    fun createUnlockFrequencyGoal(maxUnlocks: Int) {
        createGoal(SmartGoalSettingUseCase.UNLOCK_FREQUENCY, maxUnlocks.toLong())
    }

    fun updateGoalProgress(goalId: Long, progress: Long) {
        viewModelScope.launch {
            try {
                repository.updateGoalProgress(goalId, progress)
                appLogger.i(TAG, "Updated goal progress: $goalId -> $progress")
            } catch (e: Exception) {
                appLogger.e(TAG, "Failed to update goal progress", e)
            }
        }
    }

    fun refreshGoals() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        loadActiveGoals()
        calculateCurrentProgress()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun showGoalCreationDialog() {
        _uiState.value = _uiState.value.copy(showGoalCreationDialog = true)
    }

    fun dismissGoalCreationDialog() {
        _uiState.value = _uiState.value.copy(showGoalCreationDialog = false)
    }

    fun formatDuration(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
    }

    fun getScreenTimeProgress(): Float {
        val goal = _uiState.value.dailyScreenTimeGoal
        val current = _uiState.value.currentScreenTime
        return if (goal != null && goal.targetValue > 0) {
            (current.toFloat() / goal.targetValue.toFloat()).coerceAtMost(1f)
        } else 0f
    }

    fun getUnlockProgress(): Float {
        val goal = _uiState.value.unlockFrequencyGoal
        val current = _uiState.value.currentUnlockCount
        return if (goal != null && goal.targetValue > 0) {
            (current.toFloat() / goal.targetValue.toFloat()).coerceAtMost(1f)
        } else 0f
    }

    data class UserGoalsUiState(
        val activeGoals: List<UserGoal> = emptyList(),
        val dailyScreenTimeGoal: UserGoal? = null,
        val unlockFrequencyGoal: UserGoal? = null,
        val currentScreenTime: Long = 0L,
        val currentUnlockCount: Int = 0,
        val isLoading: Boolean = true,
        val isCreatingGoal: Boolean = false,
        val showGoalCreationDialog: Boolean = false,
        val error: String? = null
    )
}