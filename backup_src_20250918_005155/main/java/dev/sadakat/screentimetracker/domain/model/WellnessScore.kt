package dev.sadakat.screentimetracker.domain.model

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

enum class WellnessLevel(val displayName: String, val emoji: String, val range: IntRange) {
    DIGITAL_SPROUT("Digital Sprout", "🌱", 0..25),
    MINDFUL_EXPLORER("Mindful Explorer", "🌿", 26..50),
    BALANCED_USER("Balanced User", "🌳", 51..75),
    WELLNESS_MASTER("Wellness Master", "🏆", 76..100);

    companion object {
        fun fromScore(score: Int): WellnessLevel {
            return entries.find { score in it.range } ?: DIGITAL_SPROUT
        }
    }
}