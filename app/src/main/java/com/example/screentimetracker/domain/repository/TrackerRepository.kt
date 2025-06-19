package com.example.screentimetracker.domain.repository

import com.example.screentimetracker.data.local.AppOpenData // Keep for existing features if any
import com.example.screentimetracker.data.local.AppSessionDataAggregate
import com.example.screentimetracker.data.local.AppSessionEvent
import com.example.screentimetracker.data.local.AppUsageEvent // Keep for existing features if any
import com.example.screentimetracker.data.local.DailyAppSummary
import com.example.screentimetracker.data.local.DailyScreenUnlockSummary
import com.example.screentimetracker.data.local.ScreenUnlockEvent
import kotlinx.coroutines.flow.Flow

interface TrackerRepository {
    // Screen Unlock Methods (existing)
    suspend fun insertScreenUnlockEvent(event: ScreenUnlockEvent)
    fun getUnlockCountSince(sinceTimestamp: Long): Flow<Int>
    fun getAllUnlockEvents(): Flow<List<ScreenUnlockEvent>>
    fun getUnlockCountForDay(dayStartMillis: Long, dayEndMillis: Long): Flow<Int> // New

    // App Usage Event Methods (existing - may be deprecated or changed later)
    suspend fun insertAppUsageEvent(event: AppUsageEvent) // For discrete "open" events if still used
    fun getAppOpenCountsSince(sinceTimestamp: Long): Flow<List<AppOpenData>>
    fun getUsageEventsForApp(packageName: String): Flow<List<AppUsageEvent>>

    // App Session Event Methods (existing)
    suspend fun insertAppSession(session: AppSessionEvent)
    fun getSessionsForAppInRange(packageName: String, startTime: Long, endTime: Long): Flow<List<AppSessionEvent>>
    fun getAllSessionsInRange(startTime: Long, endTime: Long): Flow<List<AppSessionEvent>>
    fun getTotalDurationForAppInRange(packageName: String, startTime: Long, endTime: Long): Flow<Long?>
    fun getTotalScreenTimeFromSessionsInRange(startTime: Long, endTime: Long): Flow<Long?>
    fun getAggregatedSessionDataForDay(dayStartMillis: Long, dayEndMillis: Long): Flow<List<AppSessionDataAggregate>>

    // --- New Daily Summary Methods ---
    suspend fun insertDailyAppSummaries(summaries: List<DailyAppSummary>)
    suspend fun insertDailyScreenUnlockSummary(summary: DailyScreenUnlockSummary)
    fun getDailyAppSummaries(startDateMillis: Long, endDateMillis: Long): Flow<List<DailyAppSummary>>
    fun getDailyScreenUnlockSummaries(startDateMillis: Long, endDateMillis: Long): Flow<List<DailyScreenUnlockSummary>>
}
