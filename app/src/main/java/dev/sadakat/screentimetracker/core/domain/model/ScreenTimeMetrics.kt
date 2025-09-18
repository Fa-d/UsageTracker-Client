package dev.sadakat.screentimetracker.core.domain.model

data class ScreenTimeMetrics(
    val totalScreenTimeMillis: Long,
    val unlockCount: Int,
    val appSessions: List<AppSession>,
    val wellnessScore: WellnessScore
) {
    fun calculateWellnessTrend(): WellnessTrend {
        return when {
            wellnessScore.overall >= 80 -> WellnessTrend.IMPROVING
            wellnessScore.overall >= 60 -> WellnessTrend.STABLE
            wellnessScore.overall >= 40 -> WellnessTrend.DECLINING
            else -> WellnessTrend.CONCERNING
        }
    }

    fun isWithinHealthyLimits(limits: ScreenTimeLimits): Boolean {
        return totalScreenTimeMillis <= limits.dailyLimitMillis &&
                unlockCount <= limits.maxUnlocksPerDay
    }

    fun getProductiveTimeMillis(): Long {
        return appSessions
            .filter { it.category.isProductive }
            .sumOf { it.durationMillis }
    }

    fun getUnproductiveTimeMillis(): Long {
        return appSessions
            .filter { !it.category.isProductive }
            .sumOf { it.durationMillis }
    }

    fun getAverageSessionDuration(): Long {
        return if (appSessions.isNotEmpty()) {
            appSessions.sumOf { it.durationMillis } / appSessions.size
        } else 0L
    }
}

enum class WellnessTrend {
    IMPROVING,
    STABLE,
    DECLINING,
    CONCERNING
}

data class ScreenTimeLimits(
    val dailyLimitMillis: Long,
    val maxUnlocksPerDay: Int,
    val sessionWarningThresholdMillis: Long
)