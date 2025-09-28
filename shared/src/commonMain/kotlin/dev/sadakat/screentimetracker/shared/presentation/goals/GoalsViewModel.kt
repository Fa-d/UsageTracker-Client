package dev.sadakat.screentimetracker.shared.presentation.goals

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GoalsViewModel(
    private val coroutineScope: CoroutineScope
) {
    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    init {
        loadGoals()
    }

    private fun loadGoals() {
        coroutineScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // TODO: Load actual data from repositories
                val mockGoals = listOf(
                    Goal(
                        id = "1",
                        title = "Reduce daily screen time",
                        description = "Limit screen time to 4 hours per day",
                        category = GoalCategory.SCREEN_TIME,
                        targetValue = 4f,
                        currentValue = 5.2f,
                        unit = "hours",
                        dueDate = "Dec 31",
                        isCompleted = false
                    ),
                    Goal(
                        id = "2",
                        title = "Take regular breaks",
                        description = "Take a 5-minute break every hour",
                        category = GoalCategory.BREAKS,
                        targetValue = 8f,
                        currentValue = 5f,
                        unit = "breaks",
                        dueDate = "Daily",
                        isCompleted = false
                    ),
                    Goal(
                        id = "3",
                        title = "Mindfulness practice",
                        description = "Practice mindfulness for 10 minutes daily",
                        category = GoalCategory.MINDFULNESS,
                        targetValue = 10f,
                        currentValue = 10f,
                        unit = "minutes",
                        dueDate = "Daily",
                        isCompleted = true
                    ),
                    Goal(
                        id = "4",
                        title = "Reduce phone pickups",
                        description = "Keep phone pickups under 50 per day",
                        category = GoalCategory.PICKUPS,
                        targetValue = 50f,
                        currentValue = 67f,
                        unit = "pickups",
                        dueDate = "Daily",
                        isCompleted = false
                    ),
                    Goal(
                        id = "5",
                        title = "Digital sunset",
                        description = "No phone use after 9 PM",
                        category = GoalCategory.SLEEP,
                        targetValue = null,
                        currentValue = 0f,
                        unit = "",
                        dueDate = "Daily",
                        isCompleted = false
                    )
                )

                _uiState.value = _uiState.value.copy(
                    goals = mockGoals,
                    weeklyProgress = 0.68f,
                    isLoading = false
                )
                updateFilteredGoals()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun selectCategory(category: GoalCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        updateFilteredGoals()
    }

    fun toggleGoalCompletion(goalId: String) {
        val updatedGoals = _uiState.value.goals.map { goal ->
            if (goal.id == goalId) {
                goal.copy(isCompleted = !goal.isCompleted)
            } else {
                goal
            }
        }
        _uiState.value = _uiState.value.copy(goals = updatedGoals)
        updateFilteredGoals()
    }

    fun editGoal(goalId: String) {
        // TODO: Implement goal editing
    }

    fun deleteGoal(goalId: String) {
        val updatedGoals = _uiState.value.goals.filter { it.id != goalId }
        _uiState.value = _uiState.value.copy(goals = updatedGoals)
        updateFilteredGoals()
    }

    fun showCreateGoalDialog() {
        _uiState.value = _uiState.value.copy(showCreateGoalDialog = true)
    }

    fun hideCreateGoalDialog() {
        _uiState.value = _uiState.value.copy(showCreateGoalDialog = false)
    }

    fun createGoal(title: String, description: String, category: GoalCategory) {
        val newGoal = Goal(
            id = kotlin.random.Random.nextLong().toString(),
            title = title,
            description = description,
            category = category,
            targetValue = null,
            currentValue = 0f,
            unit = "",
            dueDate = "Set due date",
            isCompleted = false
        )

        val updatedGoals = _uiState.value.goals + newGoal
        _uiState.value = _uiState.value.copy(
            goals = updatedGoals,
            showCreateGoalDialog = false
        )
        updateFilteredGoals()
    }

    fun refresh() {
        loadGoals()
    }

    private fun updateFilteredGoals() {
        val filteredGoals = if (_uiState.value.selectedCategory == GoalCategory.ALL) {
            _uiState.value.goals
        } else {
            _uiState.value.goals.filter { it.category == _uiState.value.selectedCategory }
        }
        _uiState.value = _uiState.value.copy(filteredGoals = filteredGoals)
    }
}