package dev.sadakat.screentimetracker.core.common.model

data class WellnessScore(
    val date: Long,
    val totalScore: Int,
    val timeLimitScore: Int,
    val focusSessionScore: Int,
    val breaksScore: Int,
    val sleepHygieneScore: Int,
    val level: WellnessLevel,
    val calculatedAt: Long
)