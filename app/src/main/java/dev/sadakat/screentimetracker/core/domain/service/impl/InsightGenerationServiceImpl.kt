package dev.sadakat.screentimetracker.core.domain.service.impl

import dev.sadakat.screentimetracker.core.domain.model.ScreenTimeMetrics
import dev.sadakat.screentimetracker.core.domain.model.WellnessScore
import dev.sadakat.screentimetracker.core.domain.model.UserGoal
import dev.sadakat.screentimetracker.core.domain.model.AppSession
import dev.sadakat.screentimetracker.core.domain.model.GoalType
import dev.sadakat.screentimetracker.core.domain.model.GoalUnit
import dev.sadakat.screentimetracker.core.domain.service.*
import dev.sadakat.screentimetracker.core.domain.service.GoalRecommendation as GoalProgressRecommendation
import dev.sadakat.screentimetracker.core.domain.service.WellnessRecommendation as WellnessCalculationRecommendation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Implementation of insight generation service with pure business logic.
 * Contains algorithms for analyzing usage patterns and generating recommendations.
 */
class InsightGenerationServiceImpl : InsightGenerationService {

    companion object {
        private const val MIN_SDK_VERSION = 24
        private const val MIN_RAM_MB = 2048L
        private const val INSIGHT_CONFIDENCE_THRESHOLD = 0.7f
        private const val PATTERN_ANALYSIS_MIN_DAYS = 7
        private const val HIGH_USAGE_THRESHOLD_HOURS = 6
        private const val BREAK_RECOMMENDATION_THRESHOLD_MINUTES = 90
    }

    override suspend fun generateScreenTimeInsights(
        metrics: ScreenTimeMetrics,
        historicalData: List<ScreenTimeMetrics>
    ): List<Insight> {
        val insights = mutableListOf<Insight>()

        // High usage insight
        val dailyHours = TimeUnit.MILLISECONDS.toHours(metrics.totalScreenTimeMillis)
        if (dailyHours >= HIGH_USAGE_THRESHOLD_HOURS) {
            insights.add(
                Insight(
                    id = "high_usage_${System.currentTimeMillis()}",
                    type = InsightType.USAGE_PATTERN,
                    title = "High Screen Time Detected",
                    description = "You've used your device for $dailyHours hours today. Consider taking breaks.",
                    confidence = 0.9f,
                    priority = InsightPriority.HIGH,
                    actionable = true,
                    suggestedActions = listOf(
                        "Set app time limits",
                        "Enable break reminders",
                        "Try a digital detox"
                    ),
                    relevantMetric = "totalScreenTimeMillis"
                )
            )
        }

        // High unlock frequency insight
        if (metrics.unlockCount > 100) {
            insights.add(
                Insight(
                    id = "high_unlocks_${System.currentTimeMillis()}",
                    type = InsightType.ATTENTION_MANAGEMENT,
                    title = "Frequent Device Checking",
                    description = "You unlocked your device ${metrics.unlockCount} times today. This may indicate distracted usage.",
                    confidence = 0.85f,
                    priority = InsightPriority.MEDIUM,
                    actionable = true,
                    suggestedActions = listOf(
                        "Turn off non-essential notifications",
                        "Practice mindful phone usage",
                        "Set specific times for checking messages"
                    ),
                    relevantMetric = "unlockCount"
                )
            )
        }

        return insights
    }

    override suspend fun generateWellnessRecommendations(
        currentScore: WellnessScore,
        historicalScores: List<WellnessScore>
    ): List<WellnessCalculationRecommendation> {
        val recommendations = mutableListOf<WellnessCalculationRecommendation>()

        // Low wellness score recommendations
        if (currentScore.overall < 50) {
            recommendations.add(
                WellnessCalculationRecommendation(
                    title = "Reduce Daily Screen Time",
                    description = "Your wellness score indicates high screen time usage. Try reducing daily usage by 20%.",
                    priority = RecommendationPriority.HIGH,
                    actionType = RecommendationAction.REDUCE_SCREEN_TIME,
                    estimatedImpact = 8
                )
            )
        }

        // Focus improvement recommendation
        if (currentScore.focusSessionScore < 40) {
            recommendations.add(
                WellnessCalculationRecommendation(
                    title = "Start Focus Sessions",
                    description = "Your focus score could improve. Try using focus sessions to build concentration.",
                    priority = RecommendationPriority.MEDIUM,
                    actionType = RecommendationAction.START_FOCUS_SESSION,
                    estimatedImpact = 7
                )
            )
        }

        return recommendations
    }

    override suspend fun generateGoalRecommendations(
        metrics: ScreenTimeMetrics,
        currentGoals: List<UserGoal>
    ): List<GoalProgressRecommendation> {
        val recommendations = mutableListOf<GoalProgressRecommendation>()

        // High usage goal recommendation
        val dailyUsageHours = TimeUnit.MILLISECONDS.toHours(metrics.totalScreenTimeMillis)
        if (dailyUsageHours > 6 && currentGoals.none { it.type == GoalType.SCREEN_TIME_LIMIT }) {
            val suggestedTarget = TimeUnit.HOURS.toMillis(dailyUsageHours - 1)
            val suggestedGoal = UserGoal(
                id = "",
                title = "Reduce Daily Screen Time",
                description = "Limit daily screen time to ${dailyUsageHours - 1} hours",
                type = GoalType.SCREEN_TIME_LIMIT,
                targetValue = suggestedTarget,
                currentProgress = 0,
                unit = GoalUnit.MILLISECONDS,
                deadline = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7),
                isActive = true
            )

            recommendations.add(
                GoalProgressRecommendation(
                    title = "Reduce Daily Screen Time",
                    description = "Current usage is high. Reducing by 1 hour daily is achievable.",
                    suggestedGoal = suggestedGoal,
                    reasoning = "Current daily usage: $dailyUsageHours hours",
                    confidence = 0.85f
                )
            )
        }

        return recommendations
    }

    override suspend fun predictUsagePatterns(
        historicalMetrics: List<ScreenTimeMetrics>
    ): UsagePrediction {
        if (historicalMetrics.isEmpty()) {
            return UsagePrediction(
                predictedDailyUsage = 0L,
                predictedPeakHours = emptyList(),
                mostLikelyApps = emptyList(),
                confidence = 0f,
                predictionHorizonDays = 0
            )
        }

        val avgDailyUsage = historicalMetrics
            .map { it.totalScreenTimeMillis }
            .average()
            .toLong()

        return UsagePrediction(
            predictedDailyUsage = avgDailyUsage,
            predictedPeakHours = listOf(9, 13, 19), // Common peak hours
            mostLikelyApps = emptyList(), // Would need app-specific data
            confidence = if (historicalMetrics.size >= 7) 0.8f else 0.5f,
            predictionHorizonDays = 7
        )
    }

    override suspend fun generateCoachingTips(
        wellnessScore: WellnessScore,
        recentBehaviorPatterns: BehaviorPatterns
    ): List<CoachingTip> {
        val tips = mutableListOf<CoachingTip>()

        if (wellnessScore.overall < 60) {
            tips.add(
                CoachingTip(
                    category = CoachingCategory.HABIT_BUILDING,
                    tip = "Start with small changes. Try reducing screen time by 15 minutes daily.",
                    reason = "Gradual changes are more sustainable than dramatic shifts",
                    difficulty = RecommendationDifficulty.EASY,
                    estimatedBenefit = "Improved focus and sleep quality"
                )
            )
        }

        if (recentBehaviorPatterns.consistencyScore < 0.5f) {
            tips.add(
                CoachingTip(
                    category = CoachingCategory.MOTIVATION,
                    tip = "Set specific times for device usage and stick to them.",
                    reason = "Consistency helps build healthy digital habits",
                    difficulty = RecommendationDifficulty.MEDIUM,
                    estimatedBenefit = "Better self-control and routine"
                )
            )
        }

        return tips
    }

    override suspend fun analyzeUsagePatterns(
        appSessions: List<AppSession>,
        timeWindowDays: Int
    ): BehaviorPatterns {
        if (appSessions.isEmpty()) {
            return BehaviorPatterns(
                dailyUsagePattern = emptyMap(),
                weeklyPattern = emptyMap(),
                appSwitchingFrequency = 0f,
                longestFocusPeriods = emptyList(),
                mostDistractiveTimes = emptyList(),
                consistencyScore = 0f,
                impulsiveUsageEvents = 0
            )
        }

        // Analyze daily usage patterns
        val dailyPattern = appSessions
            .groupBy { session ->
                val hour = java.util.Calendar.getInstance().apply {
                    timeInMillis = session.timeRange.startMillis
                }.get(java.util.Calendar.HOUR_OF_DAY)
                hour
            }
            .mapValues { (_, sessions) ->
                sessions.sumOf { it.durationMillis }
            }

        return BehaviorPatterns(
            dailyUsagePattern = dailyPattern,
            weeklyPattern = emptyMap(), // Would need week analysis
            appSwitchingFrequency = calculateSwitchingFrequency(appSessions),
            longestFocusPeriods = findLongestFocusPeriods(appSessions),
            mostDistractiveTimes = findDistractiveTimes(appSessions),
            consistencyScore = calculateConsistencyScore(appSessions),
            impulsiveUsageEvents = countImpulsiveEvents(appSessions)
        )
    }

    override fun checkInsightAvailability(
        deviceRequirements: DeviceRequirements,
        userPreferences: InsightPreferences
    ): InsightAvailability {
        val capabilities = mutableSetOf<InsightCapability>()
        val limitations = mutableListOf<String>()

        // Check device capabilities
        if (deviceRequirements.sdkVersion >= MIN_SDK_VERSION) {
            capabilities.add(InsightCapability.BASIC_INSIGHTS)
            capabilities.add(InsightCapability.PATTERN_ANALYSIS)
        } else {
            limitations.add("Android API level ${deviceRequirements.sdkVersion} is too low. Minimum required: $MIN_SDK_VERSION")
        }

        if (deviceRequirements.totalRamMb >= MIN_RAM_MB) {
            capabilities.add(InsightCapability.ADVANCED_RECOMMENDATIONS)
        } else {
            limitations.add("Insufficient RAM. Advanced features require at least ${MIN_RAM_MB}MB")
        }

        // Check user preferences
        if (userPreferences.insightsEnabled) {
            if (userPreferences.privacyLevel == PrivacyLevel.ENHANCED ||
                userPreferences.privacyLevel == PrivacyLevel.FULL) {
                capabilities.add(InsightCapability.PREDICTIVE_MODELING)
            }
        } else {
            limitations.add("Insights are disabled in user preferences")
        }

        return InsightAvailability(
            available = capabilities.isNotEmpty() && userPreferences.insightsEnabled,
            capabilities = capabilities,
            limitations = limitations,
            recommendedUpgrades = if (limitations.isNotEmpty()) {
                listOf("Enable insights in settings", "Update to newer Android version")
            } else emptyList()
        )
    }

    override fun observeInsightAvailability(
        deviceRequirements: DeviceRequirements,
        userPreferencesFlow: Flow<InsightPreferences>
    ): Flow<InsightAvailability> {
        return userPreferencesFlow.combine(flow { emit(deviceRequirements) }) { preferences, device ->
            checkInsightAvailability(device, preferences)
        }
    }

    // Helper methods
    private fun calculateSwitchingFrequency(sessions: List<AppSession>): Float {
        if (sessions.size < 2) return 0f
        val appSwitches = sessions.zipWithNext { current, next ->
            current.packageName != next.packageName
        }.count { it }
        return appSwitches.toFloat() / sessions.size
    }

    private fun findLongestFocusPeriods(sessions: List<AppSession>): List<Long> {
        return sessions
            .filter { it.durationMillis > TimeUnit.MINUTES.toMillis(20) }
            .map { it.durationMillis }
            .sortedDescending()
            .take(5)
    }

    private fun findDistractiveTimes(sessions: List<AppSession>): List<Int> {
        return sessions
            .filter { it.durationMillis < TimeUnit.MINUTES.toMillis(2) }
            .map { session ->
                java.util.Calendar.getInstance().apply {
                    timeInMillis = session.timeRange.startMillis
                }.get(java.util.Calendar.HOUR_OF_DAY)
            }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }
    }

    private fun calculateConsistencyScore(sessions: List<AppSession>): Float {
        if (sessions.isEmpty()) return 0f

        val dailyUsage = sessions
            .groupBy { session ->
                java.util.Calendar.getInstance().apply {
                    timeInMillis = session.timeRange.startMillis
                }.get(java.util.Calendar.DAY_OF_YEAR)
            }
            .mapValues { (_, daySessions) ->
                daySessions.sumOf { it.durationMillis }
            }
            .values

        if (dailyUsage.size < 2) return 1f

        val mean = dailyUsage.average()
        val variance = dailyUsage.map { (it - mean) * (it - mean) }.average()
        val stdDev = sqrt(variance)

        // Lower coefficient of variation = higher consistency
        return if (mean > 0) {
            1f - (stdDev / mean).toFloat().coerceIn(0f, 1f)
        } else 0f
    }

    private fun countImpulsiveEvents(sessions: List<AppSession>): Int {
        return sessions.count { session ->
            session.durationMillis < TimeUnit.MINUTES.toMillis(1) &&
            sessions.any { other ->
                other != session &&
                abs(other.timeRange.startMillis - session.timeRange.startMillis) < TimeUnit.MINUTES.toMillis(10) &&
                other.packageName != session.packageName
            }
        }
    }
}