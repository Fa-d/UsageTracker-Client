package dev.sadakat.screentimetracker.core.domain.service.impl

import dev.sadakat.screentimetracker.core.domain.model.ScreenTimeMetrics
import dev.sadakat.screentimetracker.core.domain.model.WellnessScore
import dev.sadakat.screentimetracker.core.domain.model.UserGoal
import dev.sadakat.screentimetracker.core.domain.model.DigitalPet
import dev.sadakat.screentimetracker.core.domain.model.PetMood
import dev.sadakat.screentimetracker.core.domain.model.TimeRange
import dev.sadakat.screentimetracker.core.domain.model.GoalType
import dev.sadakat.screentimetracker.core.domain.service.WellnessCalculationService
import dev.sadakat.screentimetracker.core.domain.service.WellnessRecommendation
import dev.sadakat.screentimetracker.core.domain.service.RecommendationPriority
import dev.sadakat.screentimetracker.core.domain.service.RecommendationAction
import dev.sadakat.screentimetracker.core.domain.service.WellnessTrend
import dev.sadakat.screentimetracker.core.domain.service.TrendDirection
import dev.sadakat.screentimetracker.core.domain.service.TrendStrength
import kotlin.math.max
import kotlin.math.min
import kotlin.math.abs

class WellnessCalculationServiceImpl : WellnessCalculationService {

    companion object {
        private const val HOURS_TO_MILLISECONDS = 3600000L
        private const val OPTIMAL_SCREEN_TIME_HOURS = 4f
        private const val OPTIMAL_UNLOCKS_PER_DAY = 40
        private const val OPTIMAL_SESSION_DURATION_MINUTES = 20
        private const val PRODUCTIVITY_WEIGHT = 0.3f
        private const val CONSISTENCY_WEIGHT = 0.2f
    }

    override fun calculateWellnessScore(
        metrics: ScreenTimeMetrics,
        goals: List<UserGoal>
    ): WellnessScore {
        val screenTimeScore = calculateScreenTimeScore(metrics.totalScreenTimeMillis)
        val unlockScore = calculateUnlockScore(metrics.unlockCount)
        val goalScore = calculateGoalAchievementScore(metrics, goals)
        val productivityScore = calculateProductivityScore(metrics)
        val consistencyScore = calculateConsistencyScore(metrics)

        val overall = (screenTimeScore + unlockScore + goalScore + productivityScore + consistencyScore) / 5

        return WellnessScore(
            date = System.currentTimeMillis(),
            overall = overall,
            screenTime = screenTimeScore,
            unlocks = unlockScore,
            goals = goalScore,
            productivity = productivityScore,
            consistency = consistencyScore
        )
    }

    override fun calculateWellnessScoreForPeriod(
        metrics: ScreenTimeMetrics,
        goals: List<UserGoal>,
        timeRange: TimeRange
    ): WellnessScore {
        // Apply time-based adjustments for different periods
        val periodMultiplier = when {
            timeRange.durationDays() == 1L -> 1.0f // Daily
            timeRange.durationDays() <= 7L -> 0.9f // Weekly - slightly more lenient
            timeRange.durationDays() <= 30L -> 0.8f // Monthly - more lenient
            else -> 0.7f // Longer periods - most lenient
        }

        val baseScore = calculateWellnessScore(metrics, goals)

        return baseScore.copy(
            overall = (baseScore.overall * periodMultiplier).toInt().coerceIn(0, 100),
            screenTime = (baseScore.screenTime * periodMultiplier).toInt().coerceIn(0, 100),
            unlocks = (baseScore.unlocks * periodMultiplier).toInt().coerceIn(0, 100),
            goals = baseScore.goals, // Goals score doesn't change with period
            productivity = (baseScore.productivity * periodMultiplier).toInt().coerceIn(0, 100),
            consistency = baseScore.consistency // Consistency is inherently period-based
        )
    }

    override fun calculatePetMood(
        wellnessScore: WellnessScore,
        pet: DigitalPet
    ): PetMood {
        val averageStats = (pet.health + pet.happiness + pet.energy) / 3
        val wellnessInfluence = wellnessScore.overall

        // Combine pet stats with wellness score
        val moodScore = (averageStats * 0.6f + wellnessInfluence * 0.4f).toInt()

        return when {
            moodScore >= 90 -> PetMood.THRIVING
            moodScore >= 70 -> PetMood.HAPPY
            moodScore >= 50 -> PetMood.CONTENT
            moodScore >= 30 -> PetMood.CONCERNED
            moodScore >= 10 -> PetMood.SICK
            else -> PetMood.SLEEPING
        }
    }

    override fun generateWellnessRecommendations(
        score: WellnessScore,
        metrics: ScreenTimeMetrics
    ): List<WellnessRecommendation> {
        val recommendations = mutableListOf<WellnessRecommendation>()

        // Screen time recommendations
        if (score.screenTime < 50) {
            recommendations.add(
                WellnessRecommendation(
                    title = "Reduce Screen Time",
                    description = "Your daily screen time is higher than recommended. Try setting app limits or taking regular breaks.",
                    priority = if (score.screenTime < 30) RecommendationPriority.HIGH else RecommendationPriority.MEDIUM,
                    actionType = RecommendationAction.REDUCE_SCREEN_TIME,
                    estimatedImpact = if (score.screenTime < 30) 8 else 6
                )
            )
        }

        // Unlock recommendations
        if (score.unlocks < 50) {
            recommendations.add(
                WellnessRecommendation(
                    title = "Reduce Phone Unlocks",
                    description = "You're unlocking your phone frequently. Consider turning off non-essential notifications.",
                    priority = RecommendationPriority.MEDIUM,
                    actionType = RecommendationAction.REDUCE_UNLOCKS,
                    estimatedImpact = 5
                )
            )
        }

        // Productivity recommendations
        if (score.productivity < 60) {
            recommendations.add(
                WellnessRecommendation(
                    title = "Increase Productive App Usage",
                    description = "Spend more time on productive apps and less on entertainment or social media.",
                    priority = RecommendationPriority.MEDIUM,
                    actionType = RecommendationAction.INCREASE_PRODUCTIVE_APPS,
                    estimatedImpact = 7
                )
            )
        }

        // Focus session recommendations
        if (metrics.getAverageSessionDuration() > 30 * 60 * 1000) { // 30 minutes
            recommendations.add(
                WellnessRecommendation(
                    title = "Take Regular Breaks",
                    description = "Your app sessions are quite long. Take a 5-minute break every 25 minutes.",
                    priority = RecommendationPriority.HIGH,
                    actionType = RecommendationAction.TAKE_BREAK,
                    estimatedImpact = 8
                )
            )
        }

        return recommendations.sortedByDescending { it.estimatedImpact }
    }

    override fun calculateWellnessTrend(
        currentScore: WellnessScore,
        previousScores: List<WellnessScore>
    ): WellnessTrend {
        if (previousScores.isEmpty()) {
            return WellnessTrend(TrendDirection.STABLE, TrendStrength.WEAK, 0, 0f)
        }

        val recentScores = previousScores.takeLast(7) // Last 7 scores
        val averagePrevious = recentScores.map { it.overall }.average().toFloat()
        val change = currentScore.overall - averagePrevious

        val direction = when {
            change > 5 -> TrendDirection.IMPROVING
            change < -5 -> TrendDirection.DECLINING
            else -> TrendDirection.STABLE
        }

        val strength = when {
            abs(change) > 15 -> TrendStrength.STRONG
            abs(change) > 8 -> TrendStrength.MODERATE
            else -> TrendStrength.WEAK
        }

        return WellnessTrend(
            direction = direction,
            strength = strength,
            durationDays = recentScores.size,
            averageChange = change
        )
    }

    override fun isWellnessCritical(
        score: WellnessScore,
        metrics: ScreenTimeMetrics
    ): Boolean {
        return score.overall < 30 ||
               score.screenTime < 20 ||
               (metrics.totalScreenTimeMillis > 8 * HOURS_TO_MILLISECONDS && score.screenTime < 40)
    }

    override fun calculatePetExperienceReward(
        wellnessScore: WellnessScore,
        previousScore: WellnessScore?
    ): Int {
        val baseReward = when {
            wellnessScore.overall >= 80 -> 50
            wellnessScore.overall >= 60 -> 30
            wellnessScore.overall >= 40 -> 15
            else -> 5
        }

        // Bonus for improvement
        val improvementBonus = if (previousScore != null) {
            val improvement = wellnessScore.overall - previousScore.overall
            max(0, improvement * 2)
        } else 0

        return baseReward + improvementBonus
    }

    private fun calculateScreenTimeScore(screenTimeMillis: Long): Int {
        val screenTimeHours = screenTimeMillis / HOURS_TO_MILLISECONDS.toFloat()

        return when {
            screenTimeHours <= OPTIMAL_SCREEN_TIME_HOURS * 0.5f -> 100
            screenTimeHours <= OPTIMAL_SCREEN_TIME_HOURS -> 85
            screenTimeHours <= OPTIMAL_SCREEN_TIME_HOURS * 1.5f -> 65
            screenTimeHours <= OPTIMAL_SCREEN_TIME_HOURS * 2f -> 40
            screenTimeHours <= OPTIMAL_SCREEN_TIME_HOURS * 2.5f -> 25
            else -> 10
        }.coerceIn(0, 100)
    }

    private fun calculateUnlockScore(unlockCount: Int): Int {
        return when {
            unlockCount <= OPTIMAL_UNLOCKS_PER_DAY * 0.5 -> 100
            unlockCount <= OPTIMAL_UNLOCKS_PER_DAY -> 85
            unlockCount <= OPTIMAL_UNLOCKS_PER_DAY * 1.5 -> 65
            unlockCount <= OPTIMAL_UNLOCKS_PER_DAY * 2 -> 40
            unlockCount <= OPTIMAL_UNLOCKS_PER_DAY * 2.5 -> 25
            else -> 10
        }.coerceIn(0, 100)
    }

    private fun calculateGoalAchievementScore(
        metrics: ScreenTimeMetrics,
        goals: List<UserGoal>
    ): Int {
        if (goals.isEmpty()) return 70 // Neutral score when no goals

        val goalScores = goals.map { goal ->
            when (goal.type) {
                GoalType.SCREEN_TIME_LIMIT -> {
                    val isWithinLimit = metrics.totalScreenTimeMillis <= goal.targetValue
                    if (isWithinLimit) 100 else 30
                }
                GoalType.UNLOCK_LIMIT -> {
                    val isWithinLimit = metrics.unlockCount <= goal.targetValue
                    if (isWithinLimit) 100 else 30
                }
                GoalType.PRODUCTIVE_TIME -> {
                    val productiveTime = metrics.getProductiveTimeMillis()
                    val progressPercent = (productiveTime.toFloat() / goal.targetValue * 100).coerceAtMost(100f)
                    progressPercent.toInt()
                }
                else -> 70 // Neutral for other goal types
            }
        }

        return goalScores.average().toInt().coerceIn(0, 100)
    }

    private fun calculateProductivityScore(metrics: ScreenTimeMetrics): Int {
        val totalTime = metrics.totalScreenTimeMillis
        if (totalTime == 0L) return 50

        val productiveTime = metrics.getProductiveTimeMillis()
        val productivityRatio = productiveTime.toFloat() / totalTime

        return (productivityRatio * 100).toInt().coerceIn(0, 100)
    }

    private fun calculateConsistencyScore(metrics: ScreenTimeMetrics): Int {
        // For now, return a neutral score since we need historical data for consistency
        // In a full implementation, this would analyze usage patterns over time
        return 70
    }
}