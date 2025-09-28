package dev.sadakat.screentimetracker.shared.presentation.goals

import kotlinx.serialization.Serializable

@Serializable
data class GoalsUiState(
    val goals: List<Goal> = emptyList(),
    val filteredGoals: List<Goal> = emptyList(),
    val selectedCategory: GoalCategory = GoalCategory.ALL,
    val weeklyProgress: Float = 0f,
    val showCreateGoalDialog: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@Serializable
data class Goal(
    val id: String,
    val title: String,
    val description: String,
    val category: GoalCategory,
    val targetValue: Float?,
    val currentValue: Float,
    val unit: String,
    val dueDate: String,
    val isCompleted: Boolean
) {
    val progress: Float
        get() = if (targetValue != null && targetValue > 0) {
            (currentValue / targetValue).coerceIn(0f, 1f)
        } else {
            0f
        }
}

@Serializable
enum class GoalCategory(val displayName: String) {
    ALL("All"),
    SCREEN_TIME("Screen Time"),
    BREAKS("Breaks"),
    MINDFULNESS("Mindfulness"),
    PICKUPS("Pickups"),
    SLEEP("Sleep")
}