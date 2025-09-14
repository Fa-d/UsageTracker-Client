package dev.sadakat.screentimetracker.domain.tracking.repository

import dev.sadakat.screentimetracker.core.database.entities.AppOpenData
import dev.sadakat.screentimetracker.core.database.entities.AppSessionDataAggregate
import dev.sadakat.screentimetracker.core.database.entities.AppSessionEvent
import dev.sadakat.screentimetracker.core.database.entities.AppUsageEvent
import dev.sadakat.screentimetracker.core.database.entities.DailyAppSummary
import dev.sadakat.screentimetracker.core.database.entities.DailyScreenUnlockSummary
import dev.sadakat.screentimetracker.core.database.entities.ScreenUnlockEvent
import kotlinx.coroutines.flow.Flow

interface TrackerRepository {
    // Screen Unlock Methods
    suspend fun insertScreenUnlockEvent(event: ScreenUnlockEvent)
    fun getUnlockCountSince(sinceTimestamp: Long): Flow<Int>
    fun getAllUnlockEvents(): Flow<List<ScreenUnlockEvent>>
    suspend fun getUnlockCountForDay(dayStartMillis: Long, dayEndMillis: Long): Int
    fun getUnlockCountForDayFlow(dayStartMillis: Long, dayEndMillis: Long): Flow<Int>

    // App Usage Event Methods
    suspend fun insertAppUsageEvent(event: AppUsageEvent)
    fun getAppOpenCountsSince(sinceTimestamp: Long): Flow<List<AppOpenData>>
    fun getUsageEventsForApp(packageName: String): Flow<List<AppUsageEvent>>

    // App Session Event Methods
    suspend fun insertAppSession(session: AppSessionEvent)
    fun getSessionsForAppInRange(packageName: String, startTime: Long, endTime: Long): Flow<List<AppSessionEvent>>
    fun getAllSessionsInRange(startTime: Long, endTime: Long): Flow<List<AppSessionEvent>>
    fun getTotalDurationForAppInRange(packageName: String, startTime: Long, endTime: Long): Flow<Long?>
    fun getTotalScreenTimeFromSessionsInRange(startTime: Long, endTime: Long): Flow<Long?>
    suspend fun getAggregatedSessionDataForDay(dayStartMillis: Long, dayEndMillis: Long): List<AppSessionDataAggregate>
    fun getAggregatedSessionDataForDayFlow(dayStartMillis: Long, dayEndMillis: Long): Flow<List<AppSessionDataAggregate>>
    suspend fun getLastOpenedTimestampsForAppsInRange(startTime: Long, endTime: Long): List<dev.sadakat.screentimetracker.core.database.entities.AppLastOpenedData>
    fun getLastOpenedTimestampsForAppsInRangeFlow(startTime: Long, endTime: Long): Flow<List<dev.sadakat.screentimetracker.core.database.entities.AppLastOpenedData>>

    // Daily Summary Methods
    suspend fun insertDailyAppSummaries(summaries: List<DailyAppSummary>)
    suspend fun insertDailyScreenUnlockSummary(summary: DailyScreenUnlockSummary)
    fun getDailyAppSummaries(startDateMillis: Long, endDateMillis: Long): Flow<List<DailyAppSummary>>
    fun getDailyScreenUnlockSummaries(startDateMillis: Long, endDateMillis: Long): Flow<List<DailyScreenUnlockSummary>>

    // --- Helper Methods for Usage Calculation ---
    suspend fun getAppUsageInTimeRange(startTime: Long, endTime: Long): List<DailyAppSummary>

    // --- Progressive Limits Helper Methods ---
    suspend fun getAverageAppUsageLast7Days(packageName: String): Long
}