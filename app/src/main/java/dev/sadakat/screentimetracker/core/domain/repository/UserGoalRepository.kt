package dev.sadakat.screentimetracker.core.domain.repository

import dev.sadakat.screentimetracker.core.domain.model.UserGoal
import dev.sadakat.screentimetracker.core.domain.model.GoalType
import dev.sadakat.screentimetracker.core.domain.model.TimeRange
import kotlinx.coroutines.flow.Flow

interface UserGoalRepository {

    /**
     * Saves a new user goal
     */
    suspend fun saveGoal(goal: UserGoal)

    /**
     * Updates an existing goal
     */
    suspend fun updateGoal(goal: UserGoal)

    /**
     * Deletes a goal by ID
     */
    suspend fun deleteGoal(goalId: String)

    /**
     * Gets a goal by ID
     */
    suspend fun getGoalById(goalId: String): UserGoal?

    /**
     * Gets all active goals for the user
     */
    suspend fun getActiveGoals(): List<UserGoal>

    /**
     * Gets all goals (active and inactive)
     */
    suspend fun getAllGoals(): List<UserGoal>

    /**
     * Gets goals by type
     */
    suspend fun getGoalsByType(type: GoalType): List<UserGoal>

    /**
     * Gets completed goals within a time range
     */
    suspend fun getCompletedGoals(timeRange: TimeRange): List<UserGoal>

    /**
     * Gets overdue goals
     */
    suspend fun getOverdueGoals(): List<UserGoal>

    /**
     * Observes active goals changes
     */
    fun observeActiveGoals(): Flow<List<UserGoal>>

    /**
     * Observes a specific goal
     */
    fun observeGoal(goalId: String): Flow<UserGoal?>

    /**
     * Updates goal progress
     */
    suspend fun updateGoalProgress(goalId: String, progress: Long)

    /**
     * Marks a goal as completed
     */
    suspend fun completeGoal(goalId: String, completedAt: Long = System.currentTimeMillis())

    /**
     * Resets goal progress
     */
    suspend fun resetGoalProgress(goalId: String)

    /**
     * Gets goal achievement statistics
     */
    suspend fun getGoalStats(timeRange: TimeRange): GoalStatistics

    /**
     * Gets goal completion history
     */
    suspend fun getGoalCompletionHistory(timeRange: TimeRange): List<GoalCompletion>

    /**
     * Archives completed goals older than specified date
     */
    suspend fun archiveCompletedGoals(beforeDate: Long)

    /**
     * Gets goal performance analytics
     */
    suspend fun getGoalPerformanceAnalytics(timeRange: TimeRange): GoalPerformanceAnalytics
}

data class GoalStatistics(
    val totalGoals: Int,
    val completedGoals: Int,
    val activeGoals: Int,
    val overdueGoals: Int,
    val completionRate: Float,
    val averageCompletionTime: Long,
    val goalsByType: Map<GoalType, Int>,
    val completionsByType: Map<GoalType, Int>
)

data class GoalCompletion(
    val goal: UserGoal,
    val completedAt: Long,
    val timeTaken: Long,
    val progressHistory: List<ProgressPoint>
)

data class ProgressPoint(
    val timestamp: Long,
    val progress: Long,
    val progressPercentage: Float
)

data class GoalPerformanceAnalytics(
    val consistencyScore: Float,
    val improvementTrend: String,
    val bestPerformingGoalTypes: List<GoalType>,
    val strugglingGoalTypes: List<GoalType>,
    val recommendedGoalAdjustments: List<GoalAdjustment>
)

data class GoalAdjustment(
    val goalId: String,
    val currentTarget: Long,
    val suggestedTarget: Long,
    val reason: String,
    val confidence: Float
)