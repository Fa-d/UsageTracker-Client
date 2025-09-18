package dev.sadakat.screentimetracker.core.domain.service

import dev.sadakat.screentimetracker.core.domain.model.ScreenTimeMetrics
import dev.sadakat.screentimetracker.core.domain.model.WellnessScore
import dev.sadakat.screentimetracker.core.domain.model.UserGoal
import dev.sadakat.screentimetracker.core.domain.model.DigitalPet
import dev.sadakat.screentimetracker.core.domain.model.PetMood
import dev.sadakat.screentimetracker.core.domain.model.TimeRange

interface WellnessCalculationService {

    /**
     * Calculates a comprehensive wellness score based on screen time metrics and user goals
     */
    fun calculateWellnessScore(
        metrics: ScreenTimeMetrics,
        goals: List<UserGoal>
    ): WellnessScore

    /**
     * Calculates wellness score for a specific time range
     */
    fun calculateWellnessScoreForPeriod(
        metrics: ScreenTimeMetrics,
        goals: List<UserGoal>,
        timeRange: TimeRange
    ): WellnessScore

    /**
     * Calculates the pet's mood based on wellness score and pet stats
     */
    fun calculatePetMood(
        wellnessScore: WellnessScore,
        pet: DigitalPet
    ): PetMood

    /**
     * Generates personalized wellness recommendations based on score
     */
    fun generateWellnessRecommendations(
        score: WellnessScore,
        metrics: ScreenTimeMetrics
    ): List<WellnessRecommendation>

    /**
     * Calculates wellness trend over time
     */
    fun calculateWellnessTrend(
        currentScore: WellnessScore,
        previousScores: List<WellnessScore>
    ): WellnessTrend

    /**
     * Determines if the current wellness state is critical and needs immediate attention
     */
    fun isWellnessCritical(
        score: WellnessScore,
        metrics: ScreenTimeMetrics
    ): Boolean

    /**
     * Calculates experience points to award to the digital pet based on wellness
     */
    fun calculatePetExperienceReward(
        wellnessScore: WellnessScore,
        previousScore: WellnessScore?
    ): Int
}

data class WellnessRecommendation(
    val title: String,
    val description: String,
    val priority: RecommendationPriority,
    val actionType: RecommendationAction,
    val estimatedImpact: Int // 1-10 scale
)

enum class RecommendationPriority {
    LOW, MEDIUM, HIGH, URGENT
}

enum class RecommendationAction {
    REDUCE_SCREEN_TIME,
    TAKE_BREAK,
    SET_APP_LIMIT,
    IMPROVE_SLEEP_HYGIENE,
    INCREASE_PRODUCTIVE_APPS,
    REDUCE_UNLOCKS,
    START_FOCUS_SESSION
}

data class WellnessTrend(
    val direction: TrendDirection,
    val strength: TrendStrength,
    val durationDays: Int,
    val averageChange: Float
)

enum class TrendDirection {
    IMPROVING, STABLE, DECLINING
}

enum class TrendStrength {
    WEAK, MODERATE, STRONG
}