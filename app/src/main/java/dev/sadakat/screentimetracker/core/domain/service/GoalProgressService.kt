package dev.sadakat.screentimetracker.core.domain.service

import dev.sadakat.screentimetracker.core.domain.model.UserGoal
import dev.sadakat.screentimetracker.core.domain.model.ScreenTimeMetrics
import dev.sadakat.screentimetracker.core.domain.model.TimeRange
import dev.sadakat.screentimetracker.core.domain.model.GoalType

interface GoalProgressService {

    /**
     * Updates goal progress based on current metrics
     */
    fun updateGoalProgress(
        goals: List<UserGoal>,
        metrics: ScreenTimeMetrics
    ): List<UserGoal>

    /**
     * Calculates progress for a specific goal based on metrics
     */
    fun calculateGoalProgress(
        goal: UserGoal,
        metrics: ScreenTimeMetrics
    ): Long

    /**
     * Checks if any goals have been completed
     */
    fun checkCompletedGoals(
        goals: List<UserGoal>,
        metrics: ScreenTimeMetrics
    ): List<UserGoal>

    /**
     * Identifies goals that are at risk of not being met
     */
    fun identifyAtRiskGoals(
        goals: List<UserGoal>,
        timeRange: TimeRange
    ): List<GoalRisk>

    /**
     * Generates goal recommendations based on current progress and patterns
     */
    fun generateGoalRecommendations(
        currentGoals: List<UserGoal>,
        metrics: ScreenTimeMetrics,
        historicalData: List<ScreenTimeMetrics>
    ): List<GoalRecommendation>

    /**
     * Calculates the overall goal achievement rate for a user
     */
    fun calculateGoalAchievementRate(
        goals: List<UserGoal>,
        timeRange: TimeRange
    ): Float

    /**
     * Suggests optimal target values for new goals based on historical performance
     */
    fun suggestOptimalTargets(
        goalType: GoalType,
        historicalMetrics: List<ScreenTimeMetrics>
    ): GoalTargetSuggestion

    /**
     * Validates if a goal is realistic and achievable
     */
    fun validateGoalRealism(
        goal: UserGoal,
        historicalMetrics: List<ScreenTimeMetrics>
    ): GoalValidationResult
}

data class GoalRisk(
    val goal: UserGoal,
    val riskLevel: RiskLevel,
    val reason: String,
    val suggestedActions: List<String>
)

enum class RiskLevel {
    LOW, MEDIUM, HIGH, CRITICAL
}

data class GoalRecommendation(
    val title: String,
    val description: String,
    val suggestedGoal: UserGoal,
    val reasoning: String,
    val confidence: Float // 0-1 scale
)

data class GoalTargetSuggestion(
    val conservativeTarget: Long,
    val moderateTarget: Long,
    val ambitiousTarget: Long,
    val reasoning: String,
    val confidenceLevel: Float
)

data class GoalValidationResult(
    val isRealistic: Boolean,
    val confidence: Float,
    val issues: List<String>,
    val suggestions: List<String>
)