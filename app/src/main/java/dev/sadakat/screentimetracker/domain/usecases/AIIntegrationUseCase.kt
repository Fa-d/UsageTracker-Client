package dev.sadakat.screentimetracker.domain.usecases

import dev.sadakat.screentimetracker.core.presentation.ui.ai.components.AIInsight
import dev.sadakat.screentimetracker.core.presentation.ui.ai.components.AIInsightSeverity
import dev.sadakat.screentimetracker.core.presentation.ui.ai.components.AIInsightType
import dev.sadakat.screentimetracker.data.local.entities.AppSessionEvent
import dev.sadakat.screentimetracker.data.local.entities.DailyAppSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Integration layer between main app and AI module.
 * Handles data conversion and AI feature availability.
 */
class AIIntegrationUseCase @Inject constructor() {

    data class AIRecommendation(
        val id: String,
        val title: String,
        val description: String,
        val type: RecommendationType,
        val targetValue: Float,
        val reasoning: String,
        val successProbability: Float
    )

    enum class RecommendationType {
        DAILY_LIMIT,
        APP_LIMIT,
        BREAK_FREQUENCY,
        BEDTIME_LIMIT,
        FOCUS_SESSION
    }

    data class WellnessAlert(
        val severity: AlertSeverity,
        val title: String,
        val message: String,
        val actionLabel: String?,
        val urgent: Boolean = false
    )

    enum class AlertSeverity {
        INFO, WARNING, CRITICAL
    }

    suspend fun generateAIInsights(
        sessionEvents: List<AppSessionEvent>,
        dailySummaries: List<DailyAppSummary>
    ): List<AIInsight> {

        // Check if AI module is available
        if (!isAIModuleAvailable()) {
            return generateFallbackInsights(sessionEvents, dailySummaries)
        }

        try {
            // Convert app data to AI format and call AI module
            val aiUsageData = convertToAIFormat(sessionEvents, dailySummaries)
            return callAIModuleForInsights(aiUsageData)
        } catch (e: Exception) {
            // Fallback to rule-based insights if AI fails
            return generateFallbackInsights(sessionEvents, dailySummaries)
        }
    }

    suspend fun generateGoalRecommendations(
        sessionEvents: List<AppSessionEvent>,
        dailySummaries: List<DailyAppSummary>
    ): List<AIRecommendation> {

        if (!isAIModuleAvailable()) {
            return generateRuleBasedRecommendations(sessionEvents, dailySummaries)
        }

        try {
            val aiUsageData = convertToAIFormat(sessionEvents, dailySummaries)
            return callAIModuleForRecommendations(aiUsageData)
        } catch (e: Exception) {
            return generateRuleBasedRecommendations(sessionEvents, dailySummaries)
        }
    }

    suspend fun checkWellnessAlerts(
        sessionEvents: List<AppSessionEvent>,
        currentSessionDuration: Long = 0L
    ): List<WellnessAlert> {

        if (!isAIModuleAvailable()) {
            return generateRuleBasedAlerts(sessionEvents, currentSessionDuration)
        }

        try {
            val aiUsageData = convertToAIFormat(sessionEvents, emptyList())
            return callAIModuleForWellnessAlerts(aiUsageData, currentSessionDuration)
        } catch (e: Exception) {
            return generateRuleBasedAlerts(sessionEvents, currentSessionDuration)
        }
    }

    private fun isAIModuleAvailable(): Boolean {
        // This would check if AI module is loaded and ready
        // For now, return false to use fallback implementations
        return false
    }

    private fun convertToAIFormat(
        sessionEvents: List<AppSessionEvent>,
        dailySummaries: List<DailyAppSummary>
    ): Any {
        // Convert main app data structures to AI module format
        // This would use reflection or interface to call AI module
        return mapOf(
            "sessionEvents" to sessionEvents,
            "dailySummaries" to dailySummaries,
            "currentTime" to System.currentTimeMillis()
        )
    }

    private suspend fun callAIModuleForInsights(aiUsageData: Any): List<AIInsight> {
        // This would dynamically load and call the AI module
        // For now, return empty list
        return emptyList()
    }

    private suspend fun callAIModuleForRecommendations(aiUsageData: Any): List<AIRecommendation> {
        // This would dynamically load and call the AI module
        return emptyList()
    }

    private suspend fun callAIModuleForWellnessAlerts(
        aiUsageData: Any,
        currentSessionDuration: Long
    ): List<WellnessAlert> {
        // This would dynamically load and call the AI module
        return emptyList()
    }

    private fun generateFallbackInsights(
        sessionEvents: List<AppSessionEvent>,
        dailySummaries: List<DailyAppSummary>
    ): List<AIInsight> {

        val insights = mutableListOf<AIInsight>()

        // Calculate total usage today
        val today = System.currentTimeMillis()
        val todayStart = today - (today % (24 * 60 * 60 * 1000))
        val todayUsage = sessionEvents
            .filter { it.startTimeMillis >= todayStart }
            .sumOf { it.durationMillis }.toFloat() / (1000 * 60 * 60) // Hours

        if (todayUsage > 6f) {
            insights.add(AIInsight(
                id = "high_usage_${System.currentTimeMillis()}",
                title = "High Usage Alert",
                description = "You've used your phone for ${todayUsage.toInt()} hours today",
                type = AIInsightType.WARNING,
                confidence = 0.9f,
                severity = AIInsightSeverity.WARNING
            ))
        }

        // Check for usage patterns
        val weeklyUsage = dailySummaries.takeLast(7)
        if (weeklyUsage.size >= 7) {
            val thisWeekTotal = weeklyUsage.sumOf { it.totalDurationMillis } / (1000 * 60 * 60)
            if (thisWeekTotal > 35) { // More than 5 hours daily average
                insights.add(AIInsight(
                    id = "weekly_trend_${System.currentTimeMillis()}",
                    title = "Weekly Usage Trend",
                    description = "Your weekly usage is ${thisWeekTotal}h, averaging ${thisWeekTotal/7}h per day",
                    type = AIInsightType.USAGE_PATTERN,
                    confidence = 0.8f,
                    severity = if (thisWeekTotal > 56) AIInsightSeverity.WARNING else AIInsightSeverity.INFO
                ))
            }
        }

        return insights
    }

    private fun generateRuleBasedRecommendations(
        sessionEvents: List<AppSessionEvent>,
        dailySummaries: List<DailyAppSummary>
    ): List<AIRecommendation> {

        val recommendations = mutableListOf<AIRecommendation>()

        // Calculate average daily usage
        val avgDailyUsage = if (dailySummaries.isNotEmpty()) {
            dailySummaries.sumOf { it.totalDurationMillis }.toFloat() /
            (dailySummaries.size * 1000 * 60 * 60) // Hours
        } else 0f

        if (avgDailyUsage > 4f) {
            val targetUsage = avgDailyUsage * 0.8f // 20% reduction
            recommendations.add(AIRecommendation(
                id = "daily_limit_${System.currentTimeMillis()}",
                title = "Set Daily Usage Limit",
                description = "Reduce daily usage to ${targetUsage.toInt()} hours",
                type = RecommendationType.DAILY_LIMIT,
                targetValue = targetUsage,
                reasoning = "Your current daily average of ${avgDailyUsage.toInt()}h is above recommended levels",
                successProbability = when {
                    avgDailyUsage - targetUsage <= 1f -> 0.8f
                    avgDailyUsage - targetUsage <= 2f -> 0.6f
                    else -> 0.4f
                }
            ))
        }

        // Find most used app for app limit recommendation
        val topApp = dailySummaries
            .groupBy { it.packageName }
            .mapValues { it.value.sumOf { summary -> summary.totalDurationMillis } }
            .maxByOrNull { it.value }

        topApp?.let { (packageName, totalDuration) ->
            val dailyMinutes = totalDuration.toFloat() / (dailySummaries.size * 1000 * 60)
            if (dailyMinutes > 60f) { // More than 1 hour daily
                recommendations.add(AIRecommendation(
                    id = "app_limit_${System.currentTimeMillis()}",
                    title = "Limit ${packageName.substringAfterLast('.')}",
                    description = "Set a ${(dailyMinutes * 0.7f).toInt()} minute daily limit",
                    type = RecommendationType.APP_LIMIT,
                    targetValue = dailyMinutes * 0.7f,
                    reasoning = "This app accounts for ${(dailyMinutes / (avgDailyUsage * 60) * 100).toInt()}% of your usage",
                    successProbability = 0.7f
                ))
            }
        }

        return recommendations
    }

    private fun generateRuleBasedAlerts(
        sessionEvents: List<AppSessionEvent>,
        currentSessionDuration: Long
    ): List<WellnessAlert> {

        val alerts = mutableListOf<WellnessAlert>()

        // Check for extended session
        if (currentSessionDuration > 2 * 60 * 60 * 1000) { // More than 2 hours
            alerts.add(WellnessAlert(
                severity = AlertSeverity.WARNING,
                title = "Extended Session",
                message = "You've been using your phone for over 2 hours. Consider taking a break.",
                actionLabel = "Take Break",
                urgent = true
            ))
        }

        // Check for late night usage
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        if (currentHour >= 22 || currentHour <= 6) {
            alerts.add(WellnessAlert(
                severity = AlertSeverity.INFO,
                title = "Late Night Usage",
                message = "Using your phone late at night may affect sleep quality",
                actionLabel = "Set Bedtime"
            ))
        }

        return alerts
    }

    fun createInsightsFlow(
        sessionEventsFlow: Flow<List<AppSessionEvent>>,
        dailySummariesFlow: Flow<List<DailyAppSummary>>
    ): Flow<List<AIInsight>> = flow {
        sessionEventsFlow.collect { sessions ->
            dailySummariesFlow.collect { summaries ->
                emit(generateAIInsights(sessions, summaries))
            }
        }
    }
}