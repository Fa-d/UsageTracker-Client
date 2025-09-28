package dev.sadakat.screentimetracker.shared.presentation.wellness

import kotlinx.serialization.Serializable

@Serializable
data class WellnessUiState(
    val wellnessScore: Float = 0f,
    val scoreTrend: String = "",
    val insights: List<String> = emptyList(),
    val wellnessMetrics: List<WellnessMetric> = emptyList(),
    val todayBreaks: Int = 0,
    val suggestedBreakTime: String = "",
    val mindfulnessStreak: Int = 0,
    val todayMindfulnessProgress: Float = 0f,
    val mindfulnessExercises: List<MindfulnessExercise> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@Serializable
data class WellnessMetric(
    val type: WellnessMetricType,
    val title: String,
    val description: String,
    val value: String,
    val progress: Float,
    val status: WellnessStatus,
    val actionText: String
)

@Serializable
enum class WellnessMetricType {
    SCREEN_TIME,
    PICKUPS,
    BREAKS,
    SLEEP_QUALITY
}

@Serializable
enum class WellnessStatus {
    GOOD,
    WARNING,
    CRITICAL
}

@Serializable
data class MindfulnessExercise(
    val name: String,
    val duration: String,
    val emoji: String
)