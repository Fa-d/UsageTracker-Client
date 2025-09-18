package dev.sadakat.screentimetracker.core.domain.repository

import dev.sadakat.screentimetracker.core.domain.model.ScreenTimeMetrics
import dev.sadakat.screentimetracker.core.domain.model.AppSession
import dev.sadakat.screentimetracker.core.domain.model.WellnessScore
import dev.sadakat.screentimetracker.core.domain.model.TimeRange
import kotlinx.coroutines.flow.Flow

interface ScreenTimeRepository {

    /**
     * Gets comprehensive screen time metrics for a specific time range
     */
    suspend fun getScreenTimeMetrics(timeRange: TimeRange): ScreenTimeMetrics

    /**
     * Observes screen time metrics changes for a time range
     */
    fun observeScreenTimeMetrics(timeRange: TimeRange): Flow<ScreenTimeMetrics>

    /**
     * Saves a new app session
     */
    suspend fun saveAppSession(session: AppSession)

    /**
     * Gets all app sessions within a time range
     */
    suspend fun getAppSessions(timeRange: TimeRange): List<AppSession>

    /**
     * Gets app sessions for specific apps within a time range
     */
    suspend fun getAppSessionsForApps(
        packageNames: List<String>,
        timeRange: TimeRange
    ): List<AppSession>

    /**
     * Saves a wellness score
     */
    suspend fun saveWellnessScore(wellnessScore: WellnessScore)

    /**
     * Gets wellness scores for a time range
     */
    suspend fun getWellnessScores(timeRange: TimeRange): List<WellnessScore>

    /**
     * Gets the latest wellness score
     */
    suspend fun getLatestWellnessScore(): WellnessScore?

    /**
     * Observes wellness score changes
     */
    fun observeWellnessScores(): Flow<WellnessScore>

    /**
     * Records a screen unlock event
     */
    suspend fun recordScreenUnlock(timestamp: Long)

    /**
     * Gets the number of screen unlocks for a time range
     */
    suspend fun getScreenUnlockCount(timeRange: TimeRange): Int

    /**
     * Gets daily screen unlock counts for a time range
     */
    suspend fun getDailyUnlockCounts(timeRange: TimeRange): Map<String, Int>

    /**
     * Gets total screen time for a time range in milliseconds
     */
    suspend fun getTotalScreenTime(timeRange: TimeRange): Long

    /**
     * Gets screen time breakdown by app category
     */
    suspend fun getScreenTimeByCategory(timeRange: TimeRange): Map<String, Long>

    /**
     * Gets the most used apps for a time range
     */
    suspend fun getMostUsedApps(timeRange: TimeRange, limit: Int = 10): List<AppUsageSummary>

    /**
     * Deletes old data beyond a certain date to maintain performance
     */
    suspend fun cleanupOldData(beforeDate: Long)

    /**
     * Gets usage patterns and insights
     */
    suspend fun getUsagePatterns(timeRange: TimeRange): UsagePatterns
}

data class AppUsageSummary(
    val packageName: String,
    val appName: String,
    val totalTimeMillis: Long,
    val sessionCount: Int,
    val averageSessionTimeMillis: Long,
    val category: String
)

data class UsagePatterns(
    val peakUsageHours: List<Int>,
    val averageSessionDuration: Long,
    val mostActiveDay: String,
    val unlockFrequency: UnlockFrequency,
    val appSwitchingRate: Float,
    val focusTimePercentage: Float
)

data class UnlockFrequency(
    val averagePerHour: Float,
    val peakHour: Int,
    val quietHour: Int,
    val weekdayAverage: Float,
    val weekendAverage: Float
)