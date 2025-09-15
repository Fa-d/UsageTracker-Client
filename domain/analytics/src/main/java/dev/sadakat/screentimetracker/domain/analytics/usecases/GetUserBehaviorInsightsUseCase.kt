package dev.sadakat.screentimetracker.domain.analytics.usecases

import dev.sadakat.screentimetracker.domain.analytics.repository.AnalyticsRepository
import dev.sadakat.screentimetracker.domain.analytics.repository.UserBehaviorInsights
import android.util.Log
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserBehaviorInsightsUseCase @Inject constructor(
    private val repository: AnalyticsRepository
) {
    companion object {
        private const val TAG = "GetUserBehaviorInsightsUseCase"
    }

    suspend operator fun invoke(days: Int = 30): Result<UserBehaviorInsights> {
        return try {
            val insights = repository.getUserBehaviorInsights(days)
            Log.i(TAG, "Retrieved user behavior insights for $days days - Focus score: ${insights.focusScore}")
            Result.success(insights)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user behavior insights", e)
            Result.failure(e)
        }
    }

    suspend fun getWeeklyBehaviorInsights(): Result<UserBehaviorInsights> {
        return invoke(7)
    }

    suspend fun getMonthlyBehaviorInsights(): Result<UserBehaviorInsights> {
        return invoke(30)
    }

    suspend fun getQuarterlyBehaviorInsights(): Result<UserBehaviorInsights> {
        return invoke(90)
    }

    fun getPickupAnalytics(): Flow<dev.sadakat.screentimetracker.domain.analytics.repository.PickupAnalytics> {
        return repository.getPickupAnalyticsFlow()
    }

    suspend fun analyzeBehaviorPatterns(insights: UserBehaviorInsights): BehaviorAnalysis {
        return try {
            BehaviorAnalysis(
                focusLevel = categorizeFocusLevel(insights.focusScore),
                multitaskingLevel = categorizeMultitaskingLevel(insights.multitaskingTendency),
                wellbeingLevel = categorizeWellbeingLevel(insights.digitalWellbeingScore),
                habitualBehavior = analyzeHabitualBehavior(insights.habitualApps),
                impulsiveBehavior = analyzeImpulsiveBehavior(insights.impulsiveUsagePattern),
                overallAssessment = generateOverallAssessment(insights),
                recommendations = insights.behaviorRecommendations
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to analyze behavior patterns", e)
            BehaviorAnalysis(
                focusLevel = FocusLevel.UNKNOWN,
                multitaskingLevel = MultitaskingLevel.UNKNOWN,
                wellbeingLevel = WellbeingLevel.UNKNOWN,
                habitualBehavior = "Unable to analyze",
                impulsiveBehavior = "Unable to analyze",
                overallAssessment = "Analysis failed",
                recommendations = listOf("Please try again later")
            )
        }
    }

    private fun categorizeFocusLevel(score: Float): FocusLevel {
        return when {
            score >= 0.8f -> FocusLevel.EXCELLENT
            score >= 0.6f -> FocusLevel.GOOD
            score >= 0.4f -> FocusLevel.MODERATE
            score >= 0.2f -> FocusLevel.POOR
            else -> FocusLevel.VERY_POOR
        }
    }

    private fun categorizeMultitaskingLevel(tendency: Float): MultitaskingLevel {
        return when {
            tendency >= 0.8f -> MultitaskingLevel.VERY_HIGH
            tendency >= 0.6f -> MultitaskingLevel.HIGH
            tendency >= 0.4f -> MultitaskingLevel.MODERATE
            tendency >= 0.2f -> MultitaskingLevel.LOW
            else -> MultitaskingLevel.VERY_LOW
        }
    }

    private fun categorizeWellbeingLevel(score: Float): WellbeingLevel {
        return when {
            score >= 0.8f -> WellbeingLevel.EXCELLENT
            score >= 0.6f -> WellbeingLevel.GOOD
            score >= 0.4f -> WellbeingLevel.MODERATE
            score >= 0.2f -> WellbeingLevel.CONCERNING
            else -> WellbeingLevel.POOR
        }
    }

    private fun analyzeHabitualBehavior(habitualApps: List<String>): String {
        return when {
            habitualApps.isEmpty() -> "No clear habitual patterns detected"
            habitualApps.size <= 2 -> "Strong habitual patterns with ${habitualApps.size} primary app(s)"
            habitualApps.size <= 5 -> "Moderate habitual patterns across ${habitualApps.size} apps"
            else -> "Diverse habitual patterns across ${habitualApps.size} apps"
        }
    }

    private fun analyzeImpulsiveBehavior(impulsivePattern: List<dev.sadakat.screentimetracker.domain.analytics.repository.ImpulsiveUsage>): String {
        return when {
            impulsivePattern.isEmpty() -> "No significant impulsive usage detected"
            impulsivePattern.size <= 2 -> "Low impulsive behavior detected"
            impulsivePattern.size <= 5 -> "Moderate impulsive behavior patterns"
            else -> "High frequency of impulsive app usage"
        }
    }

    private fun generateOverallAssessment(insights: UserBehaviorInsights): String {
        val focusScore = insights.focusScore
        val wellbeingScore = insights.digitalWellbeingScore
        val multitaskingTendency = insights.multitaskingTendency

        return when {
            focusScore >= 0.7f && wellbeingScore >= 0.7f ->
                "Excellent digital habits with strong focus and wellbeing"
            focusScore >= 0.5f && wellbeingScore >= 0.5f ->
                "Good digital habits with room for improvement"
            focusScore < 0.3f || wellbeingScore < 0.3f ->
                "Digital habits need attention and improvement"
            multitaskingTendency > 0.8f ->
                "High multitasking tendency may be affecting focus"
            else ->
                "Mixed digital behavior patterns with potential for optimization"
        }
    }
}

data class BehaviorAnalysis(
    val focusLevel: FocusLevel,
    val multitaskingLevel: MultitaskingLevel,
    val wellbeingLevel: WellbeingLevel,
    val habitualBehavior: String,
    val impulsiveBehavior: String,
    val overallAssessment: String,
    val recommendations: List<String>
)

enum class FocusLevel {
    EXCELLENT, GOOD, MODERATE, POOR, VERY_POOR, UNKNOWN
}

enum class MultitaskingLevel {
    VERY_HIGH, HIGH, MODERATE, LOW, VERY_LOW, UNKNOWN
}

enum class WellbeingLevel {
    EXCELLENT, GOOD, MODERATE, CONCERNING, POOR, UNKNOWN
}