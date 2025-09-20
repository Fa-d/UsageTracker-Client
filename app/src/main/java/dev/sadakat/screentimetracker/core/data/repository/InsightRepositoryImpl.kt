package dev.sadakat.screentimetracker.core.data.repository

import dev.sadakat.screentimetracker.core.domain.repository.*
import dev.sadakat.screentimetracker.core.domain.service.*
// Import correct recommendation types from their source locations
import dev.sadakat.screentimetracker.core.domain.service.WellnessRecommendation as ServiceWellnessRecommendation
import dev.sadakat.screentimetracker.core.domain.service.GoalRecommendation as ServiceGoalRecommendation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.ConcurrentHashMap

/**
 * Simple in-memory implementation of InsightRepository.
 * In a full implementation, this would use a dedicated database table.
 * For clean architecture demo, we provide functional implementation without database complexity.
 */
@Singleton
class InsightRepositoryImpl @Inject constructor() : InsightRepository {

    // In-memory storage - in production this would be database tables
    private val insights = ConcurrentHashMap<String, Insight>()
    private val wellnessRecommendations = ConcurrentHashMap<String, ServiceWellnessRecommendation>()
    private val goalRecommendations = ConcurrentHashMap<String, ServiceGoalRecommendation>()
    private val coachingTips = ConcurrentHashMap<String, CoachingTip>()
    private val usagePredictions = ConcurrentHashMap<Long, UsagePrediction>()
    private val behaviorPatterns = ConcurrentHashMap<Long, BehaviorPatterns>()

    private val _activeInsights = MutableStateFlow<List<Insight>>(emptyList())
    private val mutex = Mutex()

    override suspend fun saveInsights(insights: List<Insight>) {
        mutex.withLock {
            insights.forEach { insight ->
                this.insights[insight.id] = insight
            }
            updateActiveInsightsFlow()
        }
    }

    override suspend fun getActiveInsights(): List<Insight> {
        return insights.values.filter { isInsightActive(it) }
    }

    override suspend fun getInsightsByType(
        type: InsightType,
        priority: InsightPriority?
    ): List<Insight> {
        return insights.values.filter { insight ->
            insight.type == type &&
            (priority == null || insight.priority == priority) &&
            isInsightActive(insight)
        }
    }

    override fun observeActiveInsights(): Flow<List<Insight>> {
        return _activeInsights.asStateFlow()
    }

    override suspend fun markInsightAsViewed(insightId: String) {
        // In a real implementation, we'd track view timestamp
    }

    override suspend fun markInsightAsActedUpon(insightId: String, actionTaken: String) {
        mutex.withLock {
            insights[insightId]?.let { insight ->
                insights[insightId] = insight.copy(actionable = false)
                updateActiveInsightsFlow()
            }
        }
    }

    override suspend fun dismissInsight(insightId: String) {
        mutex.withLock {
            insights.remove(insightId)
            updateActiveInsightsFlow()
        }
    }

    override suspend fun getInsightMetrics(): InsightMetrics {
        val allInsights = insights.values.toList()
        return InsightMetrics(
            totalInsightsGenerated = allInsights.size,
            totalInsightsViewed = allInsights.size,
            totalInsightsActedUpon = allInsights.count { !it.actionable },
            averageInsightRating = 4.0f,
            mostEffectiveInsightType = InsightType.USAGE_PATTERN,
            leastEffectiveInsightType = InsightType.TIME_WASTE,
            userEngagementScore = 0.7f,
            lastGenerationTime = System.currentTimeMillis()
        )
    }

    override suspend fun saveWellnessRecommendations(recommendations: List<ServiceWellnessRecommendation>) {
        mutex.withLock {
            recommendations.forEach { recommendation ->
                val id = "${recommendation.title}_${System.currentTimeMillis()}"
                wellnessRecommendations[id] = recommendation
            }
        }
    }

    override suspend fun getActiveWellnessRecommendations(): List<ServiceWellnessRecommendation> {
        return wellnessRecommendations.values.toList()
    }

    override suspend fun getWellnessRecommendationsByCategory(
        category: RecommendationAction
    ): List<ServiceWellnessRecommendation> {
        return wellnessRecommendations.values.filter { it.actionType == category }
    }

    override suspend fun markRecommendationAsTried(
        recommendationId: String,
        success: Boolean,
        feedback: String?
    ) {
        if (success) {
            wellnessRecommendations.remove(recommendationId)
        }
    }

    override suspend fun saveGoalRecommendations(recommendations: List<ServiceGoalRecommendation>) {
        mutex.withLock {
            recommendations.forEach { recommendation ->
                val id = "${recommendation.title}_${System.currentTimeMillis()}"
                goalRecommendations[id] = recommendation
            }
        }
    }

    override suspend fun getActiveGoalRecommendations(): List<ServiceGoalRecommendation> {
        return goalRecommendations.values.toList()
    }

    override suspend fun markGoalRecommendationAsImplemented(recommendationId: String) {
        goalRecommendations.remove(recommendationId)
    }

    override suspend fun saveCoachingTips(tips: List<CoachingTip>) {
        mutex.withLock {
            tips.forEach { tip ->
                val id = "${tip.category}_${tip.tip.hashCode()}"
                coachingTips[id] = tip
            }
        }
    }

    override suspend fun getCoachingTipsByCategory(category: CoachingCategory): List<CoachingTip> {
        return coachingTips.values.filter { it.category == category }
    }

    override suspend fun getPersonalizedCoachingTips(
        userProfile: UserBehaviorProfile,
        limit: Int
    ): List<CoachingTip> {
        return coachingTips.values
            .filter { tip ->
                when (userProfile.motivationLevel) {
                    MotivationLevel.LOW -> tip.difficulty == RecommendationDifficulty.EASY
                    MotivationLevel.MODERATE -> tip.difficulty in listOf(RecommendationDifficulty.EASY, RecommendationDifficulty.MEDIUM)
                    else -> true
                }
            }
            .take(limit)
    }

    override suspend fun saveUsagePrediction(prediction: UsagePrediction) {
        val timestamp = System.currentTimeMillis()
        usagePredictions[timestamp] = prediction
    }

    override suspend fun getLatestUsagePrediction(): UsagePrediction? {
        return usagePredictions.values.maxByOrNull { it.predictionHorizonDays }
    }

    override suspend fun getHistoricalPredictions(days: Int): List<TimestampedPrediction> {
        val cutoff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return usagePredictions
            .filterKeys { it >= cutoff }
            .map { (timestamp, prediction) ->
                TimestampedPrediction(
                    prediction = prediction,
                    generatedAt = timestamp
                )
            }
    }

    override suspend fun saveBehaviorPatterns(patterns: BehaviorPatterns) {
        val timestamp = System.currentTimeMillis()
        behaviorPatterns[timestamp] = patterns
    }

    override suspend fun getLatestBehaviorPatterns(): BehaviorPatterns? {
        return behaviorPatterns.values.lastOrNull()
    }

    override suspend fun getHistoricalBehaviorPatterns(days: Int): List<TimestampedBehaviorPatterns> {
        val cutoff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return behaviorPatterns
            .filterKeys { it >= cutoff }
            .map { (timestamp, patterns) ->
                TimestampedBehaviorPatterns(
                    patterns = patterns,
                    analyzedAt = timestamp,
                    dataSourceDays = days
                )
            }
    }

    override suspend fun clearOldInsights(retentionDays: Int) {
        val cutoff = System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000L)
        mutex.withLock {
            insights.values.removeAll { it.generatedAt < cutoff }
            usagePredictions.keys.removeAll { it < cutoff }
            behaviorPatterns.keys.removeAll { it < cutoff }
            updateActiveInsightsFlow()
        }
    }

    override suspend fun getInsightGenerationHistory(): InsightGenerationHistory {
        return InsightGenerationHistory(
            totalSessions = 1,
            averageInsightsPerSession = insights.size.toFloat(),
            lastGenerationTime = System.currentTimeMillis(),
            failureCount = 0,
            averageGenerationTimeMs = 100L,
            userSatisfactionScore = 4.2f
        )
    }

    override suspend fun updateInsightFeedback(
        insightId: String,
        feedback: InsightFeedback
    ) {
        // In a real implementation, we'd store feedback data
    }

    override suspend fun getInsightEffectivenessByType(): Map<InsightType, Float> {
        val insightsByType = insights.values.groupBy { it.type }
        return insightsByType.mapValues { (_, typeInsights) ->
            typeInsights.map { it.confidence }.average().toFloat()
        }
    }

    // Helper methods
    private fun isInsightActive(insight: Insight): Boolean {
        val maxAge = 7 * 24 * 60 * 60 * 1000L // 7 days
        return System.currentTimeMillis() - insight.generatedAt < maxAge
    }

    private suspend fun updateActiveInsightsFlow() {
        _activeInsights.value = getActiveInsights()
    }
}