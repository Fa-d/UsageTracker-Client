package dev.sadakat.screentimetracker.core.domain.model

/**
 * Represents the individual factors that contribute to overall wellness score
 */
data class WellnessFactors(
    val screenTimeScore: Int,
    val unlockFrequencyScore: Int,
    val appUsageScore: Int,
    val breakScore: Int,
    val sleepScore: Int,
    val consistencyScore: Int
) {
    init {
        require(screenTimeScore in 0..100) { "Screen time score must be between 0 and 100" }
        require(unlockFrequencyScore in 0..100) { "Unlock frequency score must be between 0 and 100" }
        require(appUsageScore in 0..100) { "App usage score must be between 0 and 100" }
        require(breakScore in 0..100) { "Break score must be between 0 and 100" }
        require(sleepScore in 0..100) { "Sleep score must be between 0 and 100" }
        require(consistencyScore in 0..100) { "Consistency score must be between 0 and 100" }
    }

    /**
     * Calculate the overall wellness score based on all factors
     */
    val overallScore: Int
        get() = (screenTimeScore + unlockFrequencyScore + appUsageScore + breakScore + sleepScore + consistencyScore) / 6

    /**
     * Get the strongest wellness area
     */
    val strongestArea: WellnessArea
        get() {
            val scores = mapOf(
                WellnessArea.SCREEN_TIME to screenTimeScore,
                WellnessArea.UNLOCK_FREQUENCY to unlockFrequencyScore,
                WellnessArea.APP_USAGE to appUsageScore,
                WellnessArea.BREAKS to breakScore,
                WellnessArea.SLEEP to sleepScore,
                WellnessArea.CONSISTENCY to consistencyScore
            )
            return scores.maxByOrNull { it.value }?.key ?: WellnessArea.SCREEN_TIME
        }

    /**
     * Get the weakest wellness area (needs most improvement)
     */
    val weakestArea: WellnessArea
        get() {
            val scores = mapOf(
                WellnessArea.SCREEN_TIME to screenTimeScore,
                WellnessArea.UNLOCK_FREQUENCY to unlockFrequencyScore,
                WellnessArea.APP_USAGE to appUsageScore,
                WellnessArea.BREAKS to breakScore,
                WellnessArea.SLEEP to sleepScore,
                WellnessArea.CONSISTENCY to consistencyScore
            )
            return scores.minByOrNull { it.value }?.key ?: WellnessArea.SCREEN_TIME
        }

    /**
     * Check if overall wellness is healthy
     */
    fun isHealthy(): Boolean = overallScore >= 70

    /**
     * Check if overall wellness needs improvement
     */
    fun needsImprovement(): Boolean = overallScore < 50

    /**
     * Check if overall wellness is critical
     */
    fun isCritical(): Boolean = overallScore < 30

    companion object {
        /**
         * Create default wellness factors for new users
         */
        fun default(): WellnessFactors = WellnessFactors(
            screenTimeScore = 50,
            unlockFrequencyScore = 50,
            appUsageScore = 50,
            breakScore = 50,
            sleepScore = 50,
            consistencyScore = 50
        )

        /**
         * Create excellent wellness factors
         */
        fun excellent(): WellnessFactors = WellnessFactors(
            screenTimeScore = 95,
            unlockFrequencyScore = 95,
            appUsageScore = 95,
            breakScore = 95,
            sleepScore = 95,
            consistencyScore = 95
        )
    }
}

enum class WellnessArea(val displayName: String) {
    SCREEN_TIME("Screen Time"),
    UNLOCK_FREQUENCY("Phone Unlocks"),
    APP_USAGE("App Usage"),
    BREAKS("Taking Breaks"),
    SLEEP("Sleep Hygiene"),
    CONSISTENCY("Consistency")
}