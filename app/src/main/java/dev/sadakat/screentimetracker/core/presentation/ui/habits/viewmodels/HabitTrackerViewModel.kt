package dev.sadakat.screentimetracker.core.presentation.ui.habits.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sadakat.screentimetracker.data.local.entities.HabitTracker
import dev.sadakat.screentimetracker.domain.usecases.HabitTrackerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HabitUiState(
    val isLoading: Boolean = false,
    val todaysHabits: List<HabitTracker> = emptyList(),
    val completionCelebration: CompletionCelebration? = null,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val habitStats: Map<String, HabitTrackerUseCase.HabitStats> = emptyMap()
)

data class CompletionCelebration(
    val habitName: String,
    val streak: Int,
    val isNewRecord: Boolean
)

data class CustomHabitData(
    val name: String = "",
    val description: String = "",
    val emoji: String = "✨"
)

@HiltViewModel
class HabitTrackerViewModel @Inject constructor(
    private val habitTrackerUseCase: HabitTrackerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitUiState(isLoading = true))
    val uiState: StateFlow<HabitUiState> = _uiState.asStateFlow()

    private val _customHabitData = MutableStateFlow(CustomHabitData())
    val customHabitData: StateFlow<CustomHabitData> = _customHabitData.asStateFlow()

    init {
        loadTodaysHabits()
        initializeHabits()
        checkHabitsAutomatically()
    }

    private fun initializeHabits() {
        viewModelScope.launch {
            try {
                habitTrackerUseCase.initializeDigitalWellnessHabits()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to initialize habits: ${e.localizedMessage}"
                )
            }
        }
    }

    /**
     * NEW: Automatically check and complete habits based on user behavior
     */
    fun checkHabitsAutomatically() {
        viewModelScope.launch {
            try {
                habitTrackerUseCase.checkAndCompleteHabitsAutomatically()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to check habits automatically: ${e.localizedMessage}"
                )
            }
        }
    }

    private fun loadTodaysHabits() {
        viewModelScope.launch {
            habitTrackerUseCase.getTodaysHabits()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load habits: ${exception.localizedMessage}"
                    )
                }
                .collect { habits ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        todaysHabits = habits,
                        error = null
                    )
                }
        }
    }

    /**
     * Manual habit completion - mainly for manual override/edge cases
     * Most habits should be completed automatically by checkHabitsAutomatically()
     */
    fun completeHabit(habitId: String) {
        viewModelScope.launch {
            try {
                val habit = _uiState.value.todaysHabits.find { it.habitId == habitId }
                if (habit != null && !habit.isCompleted) {
                    val success = habitTrackerUseCase.completeHabit(habitId)
                    if (success) {
                        // Show celebration animation
                        val newStreak = habit.currentStreak + 1
                        val isNewRecord = newStreak > habit.bestStreak
                        
                        _uiState.value = _uiState.value.copy(
                            completionCelebration = CompletionCelebration(
                                habitName = habit.habitName,
                                streak = newStreak,
                                isNewRecord = isNewRecord
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to complete habit: ${e.localizedMessage}"
                )
            }
        }
    }

    fun dismissCelebration() {
        _uiState.value = _uiState.value.copy(completionCelebration = null)
    }

    fun loadHabitStats(habitId: String, days: Int = 30) {
        viewModelScope.launch {
            try {
                val stats = habitTrackerUseCase.getHabitStats(habitId, days)
                val currentStats = _uiState.value.habitStats.toMutableMap()
                currentStats[habitId] = stats
                _uiState.value = _uiState.value.copy(habitStats = currentStats)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load habit stats: ${e.localizedMessage}"
                )
            }
        }
    }

    fun showCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = true)
    }

    fun hideCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = false)
        _customHabitData.value = CustomHabitData()
    }

    fun updateCustomHabitName(name: String) {
        _customHabitData.value = _customHabitData.value.copy(name = name)
    }

    fun updateCustomHabitDescription(description: String) {
        _customHabitData.value = _customHabitData.value.copy(description = description)
    }

    fun updateCustomHabitEmoji(emoji: String) {
        _customHabitData.value = _customHabitData.value.copy(emoji = emoji)
    }

    fun createCustomHabit() {
        val habitData = _customHabitData.value
        if (habitData.name.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Habit name cannot be empty")
            return
        }

        viewModelScope.launch {
            try {
                habitTrackerUseCase.createCustomHabit(
                    habitName = habitData.name,
                    description = habitData.description,
                    emoji = habitData.emoji
                )
                hideCreateDialog()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to create habit: ${e.localizedMessage}"
                )
            }
        }
    }

    fun getTodayProgress(): Pair<Int, Int> {
        val habits = _uiState.value.todaysHabits
        val completed = habits.count { it.isCompleted }
        return Pair(completed, habits.size)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetHabitStreak(habitId: String) {
        viewModelScope.launch {
            try {
                habitTrackerUseCase.resetHabitStreak(habitId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to reset streak: ${e.localizedMessage}"
                )
            }
        }
    }
}