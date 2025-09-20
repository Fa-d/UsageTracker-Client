package dev.sadakat.screentimetracker.core.domain.service

import dev.sadakat.screentimetracker.core.domain.model.ScreenTimeMetrics
import dev.sadakat.screentimetracker.core.domain.model.WellnessScore
import dev.sadakat.screentimetracker.core.domain.model.UserGoal
import dev.sadakat.screentimetracker.core.domain.model.AppSession
import kotlinx.coroutines.flow.Flow

/**
 * Domain service for generating insights and recommendations based on user data.
 * Contains pure business logic for AI-powered insights without framework dependencies.
 */
interface InsightGenerationService {

    /**
     * Generates personalized insights based on screen time metrics
     */
    suspend fun generateScreenTimeInsights(
        metrics: ScreenTimeMetrics,
        historicalData: List<ScreenTimeMetrics>
    ): List<Insight>

    /**
     * Generates wellness improvement recommendations
     */
    suspend fun generateWellnessRecommendations(
        currentScore: WellnessScore,
        historicalScores: List<WellnessScore>
    ): List<WellnessRecommendation>

    /**
     * Generates goal recommendations based on usage patterns
     */
    suspend fun generateGoalRecommendations(
        metrics: ScreenTimeMetrics,
        currentGoals: List<UserGoal>
    ): List<GoalRecommendation>

    /**
     * Predicts future usage patterns
     */
    suspend fun predictUsagePatterns(
        historicalMetrics: List<ScreenTimeMetrics>
    ): UsagePrediction

    /**
     * Generates personalized coaching tips
     */
    suspend fun generateCoachingTips(
        wellnessScore: WellnessScore,
        recentBehaviorPatterns: BehaviorPatterns
    ): List<CoachingTip>

    /**
     * Analyzes app usage trends and patterns
     */
    suspend fun analyzeUsagePatterns(
        appSessions: List<AppSession>,
        timeWindowDays: Int = 30
    ): BehaviorPatterns

    /**
     * Checks if insight generation is available based on requirements
     */
    fun checkInsightAvailability(
        deviceRequirements: DeviceRequirements,
        userPreferences: InsightPreferences
    ): InsightAvailability

    /**
     * Creates a flow for monitoring insight availability
     */
    fun observeInsightAvailability(
        deviceRequirements: DeviceRequirements,
        userPreferencesFlow: Flow<InsightPreferences>
    ): Flow<InsightAvailability>
}

/**
 * Represents an actionable insight generated from user data
 */
data class Insight(
    val id: String,
    val type: InsightType,
    val title: String,
    val description: String,
    val confidence: Float, // 0.0 to 1.0
    val priority: InsightPriority,
    val actionable: Boolean,
    val suggestedActions: List<String> = emptyList(),
    val relevantMetric: String? = null,
    val generatedAt: Long = System.currentTimeMillis()
)

/**
 * Types of insights that can be generated
 */
enum class InsightType {
    USAGE_PATTERN,
    TIME_WASTE,
    PRODUCTIVITY_OPPORTUNITY,
    WELLNESS_IMPROVEMENT,
    GOAL_OPTIMIZATION,
    HABIT_FORMATION,
    ATTENTION_MANAGEMENT
}

enum class InsightPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}


/**
 * Usage predictions for future behavior
 */
data class UsagePrediction(
    val predictedDailyUsage: Long,
    val predictedPeakHours: List<Int>,
    val mostLikelyApps: List<String>,
    val confidence: Float,
    val predictionHorizonDays: Int
)

/**
 * Personalized coaching tips
 */
data class CoachingTip(
    val category: CoachingCategory,
    val tip: String,
    val reason: String,
    val difficulty: RecommendationDifficulty,
    val estimatedBenefit: String
)

enum class CoachingCategory {
    HABIT_BUILDING,
    MOTIVATION,
    TECHNIQUE,
    MINDSET,
    ENVIRONMENT,
    TOOLS
}

enum class RecommendationDifficulty {
    EASY,
    MEDIUM,
    HARD,
    EXPERT
}

/**
 * Analyzed behavior patterns from usage data
 */
data class BehaviorPatterns(
    val dailyUsagePattern: Map<Int, Long>, // Hour -> Average usage millis
    val weeklyPattern: Map<Int, Long>, // Day of week -> Average usage
    val appSwitchingFrequency: Float,
    val longestFocusPeriods: List<Long>,
    val mostDistractiveTimes: List<Int>,
    val consistencyScore: Float, // 0.0 to 1.0
    val impulsiveUsageEvents: Int
)

/**
 * Device requirements for insight generation
 */
data class DeviceRequirements(
    val sdkVersion: Int,
    val totalRamMb: Long,
    val hasPlayServices: Boolean,
    val processingCapability: ProcessingCapability
)

enum class ProcessingCapability {
    BASIC,
    ENHANCED,
    ADVANCED,
    PREMIUM
}

/**
 * User preferences for insight generation
 */
data class InsightPreferences(
    val insightsEnabled: Boolean,
    val goalRecommendationsEnabled: Boolean,
    val predictiveCoachingEnabled: Boolean,
    val usagePredictionsEnabled: Boolean,
    val privacyLevel: PrivacyLevel,
    val updateFrequency: UpdateFrequency
)

enum class PrivacyLevel {
    BASIC,      // Local processing only
    ENHANCED,   // Anonymous cloud processing
    FULL        // Full cloud capabilities
}

enum class UpdateFrequency {
    REAL_TIME,
    HOURLY,
    DAILY,
    WEEKLY
}

/**
 * Availability status for insight generation
 */
data class InsightAvailability(
    val available: Boolean,
    val capabilities: Set<InsightCapability>,
    val limitations: List<String>,
    val recommendedUpgrades: List<String>
)

enum class InsightCapability {
    BASIC_INSIGHTS,
    PATTERN_ANALYSIS,
    PREDICTIVE_MODELING,
    ADVANCED_RECOMMENDATIONS,
    REAL_TIME_COACHING,
    BEHAVIORAL_ANALYSIS
}