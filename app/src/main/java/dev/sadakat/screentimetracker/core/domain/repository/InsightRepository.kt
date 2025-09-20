package dev.sadakat.screentimetracker.core.domain.repository

import dev.sadakat.screentimetracker.core.domain.service.*
import dev.sadakat.screentimetracker.core.domain.service.WellnessRecommendation as WellnessCalculationRecommendation
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository for managing insights and recommendations.
 * Contains pure domain operations for storing and retrieving AI-generated insights.
 */
interface InsightRepository {

    /**
     * Saves generated insights to storage
     */
    suspend fun saveInsights(insights: List<Insight>)

    /**
     * Gets all active insights for the user
     */
    suspend fun getActiveInsights(): List<Insight>

    /**
     * Gets insights by type and priority
     */
    suspend fun getInsightsByType(
        type: InsightType,
        priority: InsightPriority? = null
    ): List<Insight>

    /**
     * Observes active insights with real-time updates
     */
    fun observeActiveInsights(): Flow<List<Insight>>

    /**
     * Marks an insight as viewed by the user
     */
    suspend fun markInsightAsViewed(insightId: String)

    /**
     * Marks an insight as acted upon by the user
     */
    suspend fun markInsightAsActedUpon(insightId: String, actionTaken: String)

    /**
     * Dismisses an insight (removes from active list)
     */
    suspend fun dismissInsight(insightId: String)

    /**
     * Gets insight effectiveness metrics
     */
    suspend fun getInsightMetrics(): InsightMetrics

    /**
     * Saves wellness recommendations
     */
    suspend fun saveWellnessRecommendations(recommendations: List<WellnessCalculationRecommendation>)

    /**
     * Gets active wellness recommendations
     */
    suspend fun getActiveWellnessRecommendations(): List<WellnessCalculationRecommendation>

    /**
     * Gets wellness recommendations by category
     */
    suspend fun getWellnessRecommendationsByCategory(
        category: RecommendationAction
    ): List<WellnessCalculationRecommendation>

    /**
     * Marks a wellness recommendation as tried
     */
    suspend fun markRecommendationAsTried(
        recommendationId: String,
        success: Boolean,
        feedback: String? = null
    )

    /**
     * Saves goal recommendations
     */
    suspend fun saveGoalRecommendations(recommendations: List<GoalRecommendation>)

    /**
     * Gets active goal recommendations
     */
    suspend fun getActiveGoalRecommendations(): List<GoalRecommendation>

    /**
     * Marks a goal recommendation as implemented
     */
    suspend fun markGoalRecommendationAsImplemented(recommendationId: String)

    /**
     * Saves coaching tips
     */
    suspend fun saveCoachingTips(tips: List<CoachingTip>)

    /**
     * Gets coaching tips by category
     */
    suspend fun getCoachingTipsByCategory(category: CoachingCategory): List<CoachingTip>

    /**
     * Gets personalized coaching tips based on user profile
     */
    suspend fun getPersonalizedCoachingTips(
        userProfile: UserBehaviorProfile,
        limit: Int = 5
    ): List<CoachingTip>

    /**
     * Saves usage predictions
     */
    suspend fun saveUsagePrediction(prediction: UsagePrediction)

    /**
     * Gets the latest usage prediction
     */
    suspend fun getLatestUsagePrediction(): UsagePrediction?

    /**
     * Gets historical usage predictions for accuracy analysis
     */
    suspend fun getHistoricalPredictions(days: Int = 30): List<TimestampedPrediction>

    /**
     * Saves analyzed behavior patterns
     */
    suspend fun saveBehaviorPatterns(patterns: BehaviorPatterns)

    /**
     * Gets the latest behavior patterns analysis
     */
    suspend fun getLatestBehaviorPatterns(): BehaviorPatterns?

    /**
     * Gets historical behavior patterns
     */
    suspend fun getHistoricalBehaviorPatterns(days: Int = 30): List<TimestampedBehaviorPatterns>

    /**
     * Clears old insights and recommendations based on retention policy
     */
    suspend fun clearOldInsights(retentionDays: Int = 30)

    /**
     * Gets insight generation history for analytics
     */
    suspend fun getInsightGenerationHistory(): InsightGenerationHistory

    /**
     * Updates insight feedback from user interactions
     */
    suspend fun updateInsightFeedback(
        insightId: String,
        feedback: InsightFeedback
    )

    /**
     * Gets insight effectiveness by type for AI model improvement
     */
    suspend fun getInsightEffectivenessByType(): Map<InsightType, Float>
}

/**
 * Metrics about insight effectiveness and user engagement
 */
data class InsightMetrics(
    val totalInsightsGenerated: Int,
    val totalInsightsViewed: Int,
    val totalInsightsActedUpon: Int,
    val averageInsightRating: Float,
    val mostEffectiveInsightType: InsightType?,
    val leastEffectiveInsightType: InsightType?,
    val userEngagementScore: Float,
    val lastGenerationTime: Long
)

/**
 * Usage prediction with timestamp for historical tracking
 */
data class TimestampedPrediction(
    val prediction: UsagePrediction,
    val generatedAt: Long,
    val actualUsage: Long? = null, // Set when we can verify accuracy
    val accuracyScore: Float? = null
)

/**
 * Behavior patterns with timestamp for historical tracking
 */
data class TimestampedBehaviorPatterns(
    val patterns: BehaviorPatterns,
    val analyzedAt: Long,
    val dataSourceDays: Int
)

/**
 * Insight generation history for analytics and improvements
 */
data class InsightGenerationHistory(
    val totalSessions: Int,
    val averageInsightsPerSession: Float,
    val lastGenerationTime: Long,
    val failureCount: Int,
    val averageGenerationTimeMs: Long,
    val userSatisfactionScore: Float
)

/**
 * User feedback on insights for continuous improvement
 */
data class InsightFeedback(
    val rating: Int, // 1-5 scale
    val helpful: Boolean,
    val accurate: Boolean,
    val actionable: Boolean,
    val comments: String? = null,
    val submittedAt: Long = System.currentTimeMillis()
)