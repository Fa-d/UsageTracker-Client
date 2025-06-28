package com.example.screentimetracker.domain.repository

import com.example.screentimetracker.data.local.AppOpenData
import com.example.screentimetracker.data.local.AppSessionDataAggregate
import com.example.screentimetracker.data.local.AppSessionEvent
import com.example.screentimetracker.data.local.AppUsageEvent
import com.example.screentimetracker.data.local.DailyAppSummary
import com.example.screentimetracker.data.local.DailyScreenUnlockSummary
import com.example.screentimetracker.data.local.LimitedApp // Import LimitedApp
import com.example.screentimetracker.data.local.ScreenUnlockEvent
import kotlinx.coroutines.flow.Flow

interface TrackerRepository {
    // Screen Unlock Methods
    suspend fun insertScreenUnlockEvent(event: ScreenUnlockEvent)
    fun getUnlockCountSince(sinceTimestamp: Long): Flow<Int>
    fun getAllUnlockEvents(): Flow<List<ScreenUnlockEvent>>
    fun getUnlockCountForDay(dayStartMillis: Long, dayEndMillis: Long): Flow<Int>

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
    fun getAggregatedSessionDataForDay(dayStartMillis: Long, dayEndMillis: Long): Flow<List<AppSessionDataAggregate>>
    fun getLastOpenedTimestampsForAppsInRange(startTime: Long, endTime: Long): Flow<List<com.example.screentimetracker.data.local.AppLastOpenedData>>

    // Daily Summary Methods
    suspend fun insertDailyAppSummaries(summaries: List<DailyAppSummary>)
    suspend fun insertDailyScreenUnlockSummary(summary: DailyScreenUnlockSummary)
    fun getDailyAppSummaries(startDateMillis: Long, endDateMillis: Long): Flow<List<DailyAppSummary>>
    fun getDailyScreenUnlockSummaries(startDateMillis: Long, endDateMillis: Long): Flow<List<DailyScreenUnlockSummary>>

    // --- New Limited App Methods ---
    suspend fun insertLimitedApp(limitedApp: LimitedApp)
    suspend fun deleteLimitedApp(limitedApp: LimitedApp)
    fun getLimitedApp(packageName: String): Flow<LimitedApp?>
    suspend fun getLimitedAppOnce(packageName: String): LimitedApp?
    fun getAllLimitedApps(): Flow<List<LimitedApp>>
    suspend fun getAllLimitedAppsOnce(): List<LimitedApp>
}
