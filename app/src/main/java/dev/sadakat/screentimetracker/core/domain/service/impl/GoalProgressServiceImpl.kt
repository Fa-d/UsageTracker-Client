package dev.sadakat.screentimetracker.core.domain.service.impl

import dev.sadakat.screentimetracker.core.domain.model.UserGoal
import dev.sadakat.screentimetracker.core.domain.model.ScreenTimeMetrics
import dev.sadakat.screentimetracker.core.domain.model.TimeRange
import dev.sadakat.screentimetracker.core.domain.model.GoalType
import dev.sadakat.screentimetracker.core.domain.service.GoalProgressService
import dev.sadakat.screentimetracker.core.domain.service.GoalRisk
import dev.sadakat.screentimetracker.core.domain.service.RiskLevel
import dev.sadakat.screentimetracker.core.domain.service.GoalRecommendation
import dev.sadakat.screentimetracker.core.domain.service.GoalTargetSuggestion
import dev.sadakat.screentimetracker.core.domain.service.GoalValidationResult
import kotlin.math.max
import kotlin.math.min

class GoalProgressServiceImpl : GoalProgressService {

    override fun updateGoalProgress(
        goals: List<UserGoal>,
        metrics: ScreenTimeMetrics
    ): List<UserGoal> {
        return goals.map { goal ->
            val progress = calculateGoalProgress(goal, metrics)
            goal.addProgress(progress - goal.currentProgress)
        }
    }

    override fun calculateGoalProgress(
        goal: UserGoal,
        metrics: ScreenTimeMetrics
    ): Long {
        return when (goal.type) {
            GoalType.SCREEN_TIME_LIMIT -> {
                // For limit goals, progress is "staying within limit"
                if (metrics.totalScreenTimeMillis <= goal.targetValue) goal.targetValue else 0
            }
            GoalType.UNLOCK_LIMIT -> {
                // For unlock limits, progress is "staying within limit"
                if (metrics.unlockCount <= goal.targetValue) goal.targetValue else 0
            }
            GoalType.PRODUCTIVE_TIME -> {
                // Progress is actual productive time
                min(metrics.getProductiveTimeMillis(), goal.targetValue)
            }
            GoalType.FOCUS_SESSIONS -> {
                // Count focus sessions (simplified - would need access to focus session data)
                0L // TODO: Implement with focus session repository
            }
            GoalType.APP_LIMIT -> {
                // Check if specific app usage is within limit
                0L // TODO: Implement with app-specific data
            }
            GoalType.WELLNESS_STREAK -> {
                // Wellness streaks are calculated differently
                0L // TODO: Implement with wellness streak tracking
            }
            GoalType.DIGITAL_DETOX -> {
                // Digital detox progress tracking
                0L // TODO: Implement with detox session tracking
            }
            GoalType.SLEEP_HYGIENE -> {
                // Sleep hygiene goal progress
                0L // TODO: Implement with sleep pattern tracking
            }
        }
    }

    override fun checkCompletedGoals(
        goals: List<UserGoal>,
        metrics: ScreenTimeMetrics
    ): List<UserGoal> {
        return updateGoalProgress(goals, metrics).filter { it.isCompleted && !it.isCompleted }
    }

    override fun identifyAtRiskGoals(
        goals: List<UserGoal>,
        timeRange: TimeRange
    ): List<GoalRisk> {
        val risks = mutableListOf<GoalRisk>()

        goals.forEach { goal ->
            val risk = assessGoalRisk(goal, timeRange)
            if (risk.riskLevel != RiskLevel.LOW) {
                risks.add(risk)
            }
        }

        return risks.sortedByDescending { it.riskLevel.ordinal }
    }

    override fun generateGoalRecommendations(
        currentGoals: List<UserGoal>,
        metrics: ScreenTimeMetrics,
        historicalData: List<ScreenTimeMetrics>
    ): List<GoalRecommendation> {
        val recommendations = mutableListOf<GoalRecommendation>()

        // Analyze current performance
        val averageScreenTime = historicalData.map { it.totalScreenTimeMillis }.average()
        val averageUnlocks = historicalData.map { it.unlockCount }.average()
        val averageProductivity = historicalData.map { it.getProductiveTimeMillis() }.average()

        // Screen time goal recommendation
        if (currentGoals.none { it.type == GoalType.SCREEN_TIME_LIMIT }) {
            val suggestedLimit = (averageScreenTime * 0.9).toLong() // 10% reduction
            recommendations.add(
                GoalRecommendation(
                    title = "Daily Screen Time Goal",
                    description = "Set a daily screen time limit to improve digital wellness",
                    suggestedGoal = UserGoal.screenTimeLimit(suggestedLimit),
                    reasoning = "Based on your average usage patterns, this target is achievable",
                    confidence = 0.8f
                )
            )
        }

        // Unlock limit recommendation
        if (currentGoals.none { it.type == GoalType.UNLOCK_LIMIT }) {
            val suggestedLimit = (averageUnlocks * 0.85).toInt() // 15% reduction
            recommendations.add(
                GoalRecommendation(
                    title = "Phone Unlock Limit",
                    description = "Reduce phone unlocks to improve focus",
                    suggestedGoal = UserGoal.unlockLimit(suggestedLimit),
                    reasoning = "Fewer unlocks correlate with better focus and wellness",
                    confidence = 0.7f
                )
            )
        }

        // Productivity goal recommendation
        if (currentGoals.none { it.type == GoalType.PRODUCTIVE_TIME }) {
            val suggestedTarget = (averageProductivity * 1.2).toLong() // 20% increase
            recommendations.add(
                GoalRecommendation(
                    title = "Productive Time Goal",
                    description = "Increase time spent on productive applications",
                    suggestedGoal = UserGoal.productivityGoal((suggestedTarget / (60 * 60 * 1000)).toInt()),
                    reasoning = "Increasing productive app usage can improve overall life satisfaction",
                    confidence = 0.75f
                )
            )
        }

        return recommendations.sortedByDescending { it.confidence }
    }

    override fun calculateGoalAchievementRate(
        goals: List<UserGoal>,
        timeRange: TimeRange
    ): Float {
        if (goals.isEmpty()) return 0f

        val completedGoals = goals.count { it.isCompleted }
        return completedGoals.toFloat() / goals.size
    }

    override fun suggestOptimalTargets(
        goalType: GoalType,
        historicalMetrics: List<ScreenTimeMetrics>
    ): GoalTargetSuggestion {
        if (historicalMetrics.isEmpty()) {
            return getDefaultTargetSuggestion(goalType)
        }

        return when (goalType) {
            GoalType.SCREEN_TIME_LIMIT -> {
                val averageScreenTime = historicalMetrics.map { it.totalScreenTimeMillis }.average()
                val conservative = (averageScreenTime * 0.95).toLong()
                val moderate = (averageScreenTime * 0.85).toLong()
                val ambitious = (averageScreenTime * 0.7).toLong()

                GoalTargetSuggestion(
                    conservativeTarget = conservative,
                    moderateTarget = moderate,
                    ambitiousTarget = ambitious,
                    reasoning = "Based on your historical screen time patterns",
                    confidenceLevel = 0.85f
                )
            }
            GoalType.UNLOCK_LIMIT -> {
                val averageUnlocks = historicalMetrics.map { it.unlockCount }.average()
                val conservative = (averageUnlocks * 0.9).toLong()
                val moderate = (averageUnlocks * 0.8).toLong()
                val ambitious = (averageUnlocks * 0.65).toLong()

                GoalTargetSuggestion(
                    conservativeTarget = conservative,
                    moderateTarget = moderate,
                    ambitiousTarget = ambitious,
                    reasoning = "Based on your historical unlock patterns",
                    confidenceLevel = 0.8f
                )
            }
            GoalType.PRODUCTIVE_TIME -> {
                val averageProductiveTime = historicalMetrics.map { it.getProductiveTimeMillis() }.average()
                val conservative = (averageProductiveTime * 1.1).toLong()
                val moderate = (averageProductiveTime * 1.25).toLong()
                val ambitious = (averageProductiveTime * 1.5).toLong()

                GoalTargetSuggestion(
                    conservativeTarget = conservative,
                    moderateTarget = moderate,
                    ambitiousTarget = ambitious,
                    reasoning = "Based on your productive app usage patterns",
                    confidenceLevel = 0.75f
                )
            }
            else -> getDefaultTargetSuggestion(goalType)
        }
    }

    override fun validateGoalRealism(
        goal: UserGoal,
        historicalMetrics: List<ScreenTimeMetrics>
    ): GoalValidationResult {
        if (historicalMetrics.isEmpty()) {
            return GoalValidationResult(
                isRealistic = true,
                confidence = 0.5f,
                issues = listOf("Insufficient historical data"),
                suggestions = listOf("Start with conservative targets")
            )
        }

        return when (goal.type) {
            GoalType.SCREEN_TIME_LIMIT -> validateScreenTimeGoal(goal, historicalMetrics)
            GoalType.UNLOCK_LIMIT -> validateUnlockGoal(goal, historicalMetrics)
            GoalType.PRODUCTIVE_TIME -> validateProductivityGoal(goal, historicalMetrics)
            else -> GoalValidationResult(
                isRealistic = true,
                confidence = 0.6f,
                issues = emptyList(),
                suggestions = listOf("Monitor progress and adjust as needed")
            )
        }
    }

    private fun assessGoalRisk(goal: UserGoal, timeRange: TimeRange): GoalRisk {
        val progressPercent = goal.progressPercentage
        val timeElapsed = if (goal.deadline != null) {
            val totalTime = goal.deadline - goal.createdAt
            val elapsed = System.currentTimeMillis() - goal.createdAt
            elapsed.toFloat() / totalTime
        } else {
            val daysSinceCreated = (System.currentTimeMillis() - goal.createdAt) / (24 * 60 * 60 * 1000)
            daysSinceCreated.toFloat() / 30 // Assume 30-day cycle
        }

        val expectedProgress = timeElapsed * 100
        val progressGap = expectedProgress - progressPercent

        val (riskLevel, reason, actions) = when {
            progressGap > 30 -> Triple(
                RiskLevel.HIGH,
                "Significantly behind expected progress",
                listOf("Consider adjusting target", "Identify barriers", "Create action plan")
            )
            progressGap > 15 -> Triple(
                RiskLevel.MEDIUM,
                "Moderately behind expected progress",
                listOf("Review current approach", "Set daily reminders")
            )
            progressGap > 5 -> Triple(
                RiskLevel.LOW,
                "Slightly behind expected progress",
                listOf("Monitor closely", "Stay motivated")
            )
            else -> Triple(
                RiskLevel.LOW,
                "On track or ahead",
                listOf("Maintain current approach")
            )
        }

        return GoalRisk(goal, riskLevel, reason, actions)
    }

    private fun getDefaultTargetSuggestion(goalType: GoalType): GoalTargetSuggestion {
        return when (goalType) {
            GoalType.SCREEN_TIME_LIMIT -> GoalTargetSuggestion(
                conservativeTarget = 6 * 60 * 60 * 1000L, // 6 hours
                moderateTarget = 4 * 60 * 60 * 1000L, // 4 hours
                ambitiousTarget = 2 * 60 * 60 * 1000L, // 2 hours
                reasoning = "General recommendations for screen time limits",
                confidenceLevel = 0.6f
            )
            GoalType.UNLOCK_LIMIT -> GoalTargetSuggestion(
                conservativeTarget = 80L,
                moderateTarget = 50L,
                ambitiousTarget = 30L,
                reasoning = "General recommendations for unlock limits",
                confidenceLevel = 0.6f
            )
            else -> GoalTargetSuggestion(
                conservativeTarget = 100L,
                moderateTarget = 150L,
                ambitiousTarget = 200L,
                reasoning = "Default target suggestions",
                confidenceLevel = 0.5f
            )
        }
    }

    private fun validateScreenTimeGoal(goal: UserGoal, historicalMetrics: List<ScreenTimeMetrics>): GoalValidationResult {
        val averageScreenTime = historicalMetrics.map { it.totalScreenTimeMillis }.average()
        val reduction = (averageScreenTime - goal.targetValue) / averageScreenTime

        return when {
            reduction > 0.5 -> GoalValidationResult(
                isRealistic = false,
                confidence = 0.8f,
                issues = listOf("Target represents more than 50% reduction from current usage"),
                suggestions = listOf("Consider a more gradual approach", "Set intermediate milestones")
            )
            reduction > 0.3 -> GoalValidationResult(
                isRealistic = true,
                confidence = 0.6f,
                issues = listOf("Ambitious target requiring significant behavior change"),
                suggestions = listOf("Create specific action plan", "Track progress daily")
            )
            else -> GoalValidationResult(
                isRealistic = true,
                confidence = 0.9f,
                issues = emptyList(),
                suggestions = listOf("Achievable target based on current patterns")
            )
        }
    }

    private fun validateUnlockGoal(goal: UserGoal, historicalMetrics: List<ScreenTimeMetrics>): GoalValidationResult {
        val averageUnlocks = historicalMetrics.map { it.unlockCount }.average()
        val reduction = (averageUnlocks - goal.targetValue) / averageUnlocks

        return when {
            reduction > 0.4 -> GoalValidationResult(
                isRealistic = false,
                confidence = 0.8f,
                issues = listOf("Target represents more than 40% reduction in unlocks"),
                suggestions = listOf("Start with 20% reduction", "Focus on notification management")
            )
            else -> GoalValidationResult(
                isRealistic = true,
                confidence = 0.85f,
                issues = emptyList(),
                suggestions = listOf("Realistic unlock reduction target")
            )
        }
    }

    private fun validateProductivityGoal(goal: UserGoal, historicalMetrics: List<ScreenTimeMetrics>): GoalValidationResult {
        val averageProductiveTime = historicalMetrics.map { it.getProductiveTimeMillis() }.average()
        val increase = (goal.targetValue - averageProductiveTime) / averageProductiveTime

        return when {
            increase > 1.0 -> GoalValidationResult(
                isRealistic = false,
                confidence = 0.7f,
                issues = listOf("Target represents more than 100% increase in productive time"),
                suggestions = listOf("Set more gradual increases", "Focus on habit formation")
            )
            else -> GoalValidationResult(
                isRealistic = true,
                confidence = 0.8f,
                issues = emptyList(),
                suggestions = listOf("Achievable productivity increase")
            )
        }
    }
}