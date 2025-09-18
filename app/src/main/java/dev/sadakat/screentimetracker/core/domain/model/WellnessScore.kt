package dev.sadakat.screentimetracker.core.domain.model

data class WellnessScore(
    val overall: Int,
    val screenTime: Int,
    val unlocks: Int,
    val goals: Int,
    val productivity: Int = 0,
    val consistency: Int = 0,
    val calculatedAt: Long = System.currentTimeMillis()
) {
    init {
        require(overall in 0..100) { "Overall score must be between 0 and 100" }
        require(screenTime in 0..100) { "Screen time score must be between 0 and 100" }
        require(unlocks in 0..100) { "Unlocks score must be between 0 and 100" }
        require(goals in 0..100) { "Goals score must be between 0 and 100" }
        require(productivity in 0..100) { "Productivity score must be between 0 and 100" }
        require(consistency in 0..100) { "Consistency score must be between 0 and 100" }
    }

    val wellnessLevel: WellnessLevel
        get() = WellnessLevel.fromScore(overall)

    fun isHealthy(): Boolean = overall >= 70

    fun needsImprovement(): Boolean = overall < 50

    fun isCritical(): Boolean = overall < 30

    fun getStrongestArea(): ScoreArea {
        val scores = mapOf(
            ScoreArea.SCREEN_TIME to screenTime,
            ScoreArea.UNLOCKS to unlocks,
            ScoreArea.GOALS to goals,
            ScoreArea.PRODUCTIVITY to productivity,
            ScoreArea.CONSISTENCY to consistency
        )
        return scores.maxByOrNull { it.value }?.key ?: ScoreArea.SCREEN_TIME
    }

    fun getWeakestArea(): ScoreArea {
        val scores = mapOf(
            ScoreArea.SCREEN_TIME to screenTime,
            ScoreArea.UNLOCKS to unlocks,
            ScoreArea.GOALS to goals,
            ScoreArea.PRODUCTIVITY to productivity,
            ScoreArea.CONSISTENCY to consistency
        )
        return scores.minByOrNull { it.value }?.key ?: ScoreArea.SCREEN_TIME
    }

    companion object {
        fun default(): WellnessScore = WellnessScore(
            overall = 50,
            screenTime = 50,
            unlocks = 50,
            goals = 50,
            productivity = 50,
            consistency = 50
        )

        fun excellent(): WellnessScore = WellnessScore(
            overall = 95,
            screenTime = 95,
            unlocks = 95,
            goals = 95,
            productivity = 95,
            consistency = 95
        )
    }
}

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

enum class ScoreArea(val displayName: String) {
    SCREEN_TIME("Screen Time"),
    UNLOCKS("Phone Unlocks"),
    GOALS("Goal Achievement"),
    PRODUCTIVITY("Productivity"),
    CONSISTENCY("Consistency")
}