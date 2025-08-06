package com.example.screentimetracker.data.repository

import com.example.screentimetracker.data.local.AppDatabase
import com.example.screentimetracker.data.local.AppOpenData
import com.example.screentimetracker.data.local.AppSessionDao
import com.example.screentimetracker.data.local.AppSessionDataAggregate
import com.example.screentimetracker.data.local.AppSessionEvent
import com.example.screentimetracker.data.local.AppUsageDao
import com.example.screentimetracker.data.local.AppUsageEvent
import com.example.screentimetracker.data.local.DailyAppSummary
import com.example.screentimetracker.data.local.DailyAppSummaryDao
import com.example.screentimetracker.data.local.DailyScreenUnlockSummary
import com.example.screentimetracker.data.local.DailyScreenUnlockSummaryDao
import com.example.screentimetracker.data.local.LimitedApp
import com.example.screentimetracker.data.local.LimitedAppDao
import com.example.screentimetracker.data.local.ScreenUnlockDao
import com.example.screentimetracker.data.local.ScreenUnlockEvent
import com.example.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackerRepositoryImpl @Inject constructor(
    val db: AppDatabase
) : TrackerRepository {

    private val screenUnlockDao: ScreenUnlockDao = db.screenUnlockDao()
    private val appUsageDao: AppUsageDao = db.appUsageDao()
    private val appSessionDao: AppSessionDao = db.appSessionDao()
    private val dailyAppSummaryDao: DailyAppSummaryDao = db.dailyAppSummaryDao()
    private val dailyScreenUnlockSummaryDao: DailyScreenUnlockSummaryDao = db.dailyScreenUnlockSummaryDao()
    private val limitedAppDao: LimitedAppDao = db.limitedAppDao() // Initialize LimitedAppDao

    // Screen Unlock Methods
    override suspend fun insertScreenUnlockEvent(event: ScreenUnlockEvent) {
        screenUnlockDao.insertUnlockEvent(event)
    }
    override fun getUnlockCountSince(sinceTimestamp: Long): Flow<Int> {
        return screenUnlockDao.getUnlockCountSince(sinceTimestamp)
    }
    override fun getAllUnlockEvents(): Flow<List<ScreenUnlockEvent>> {
        return screenUnlockDao.getAllUnlockEvents()
    }
    override suspend fun getUnlockCountForDay(dayStartMillis: Long, dayEndMillis: Long): Int {
        return screenUnlockDao.getUnlockCountForDay(dayStartMillis, dayEndMillis).first()
    }

    override fun getUnlockCountForDayFlow(dayStartMillis: Long, dayEndMillis: Long): Flow<Int> {
        return screenUnlockDao.getUnlockCountForDay(dayStartMillis, dayEndMillis)
    }

    // App Usage Event Methods
    override suspend fun insertAppUsageEvent(event: AppUsageEvent) {
        appUsageDao.insertAppUsageEvent(event)
    }
    override fun getAppOpenCountsSince(sinceTimestamp: Long): Flow<List<AppOpenData>> {
        return appUsageDao.getAppOpenCountsSince(sinceTimestamp)
    }
    override fun getUsageEventsForApp(packageName: String): Flow<List<AppUsageEvent>> {
        return appUsageDao.getUsageEventsForApp(packageName)
    }

    // App Session Event Methods
    override suspend fun insertAppSession(session: AppSessionEvent) {
        appSessionDao.insertAppSession(session)
    }
    override fun getSessionsForAppInRange(packageName: String, startTime: Long, endTime: Long): Flow<List<AppSessionEvent>> {
        return appSessionDao.getSessionsForAppInRange(packageName, startTime, endTime)
    }
    override fun getAllSessionsInRange(startTime: Long, endTime: Long): Flow<List<AppSessionEvent>> {
        return appSessionDao.getAllSessionsInRange(startTime, endTime)
    }
    override fun getTotalDurationForAppInRange(packageName: String, startTime: Long, endTime: Long): Flow<Long?> {
        return appSessionDao.getTotalDurationForAppInRange(packageName, startTime, endTime)
    }
    override fun getTotalScreenTimeFromSessionsInRange(startTime: Long, endTime: Long): Flow<Long?> {
        return appSessionDao.getTotalScreenTimeFromSessionsInRange(startTime, endTime)
    }
    override suspend fun getAggregatedSessionDataForDay(
        dayStartMillis: Long, dayEndMillis: Long
    ): List<AppSessionDataAggregate> {
        return appSessionDao.getAggregatedSessionDataForDay(dayStartMillis, dayEndMillis).first()
    }

    override fun getAggregatedSessionDataForDayFlow(
        dayStartMillis: Long, dayEndMillis: Long
    ): Flow<List<AppSessionDataAggregate>> {
        return appSessionDao.getAggregatedSessionDataForDay(dayStartMillis, dayEndMillis)
    }

    override suspend fun getLastOpenedTimestampsForAppsInRange(
        startTime: Long, endTime: Long
    ): List<com.example.screentimetracker.data.local.AppLastOpenedData> {
        return appSessionDao.getLastOpenedTimestampsForAppsInRange(startTime, endTime).first()
    }

    override fun getLastOpenedTimestampsForAppsInRangeFlow(
        startTime: Long, endTime: Long
    ): Flow<List<com.example.screentimetracker.data.local.AppLastOpenedData>> {
        return appSessionDao.getLastOpenedTimestampsForAppsInRange(startTime, endTime)
    }

    // Daily Summary Methods
    override suspend fun insertDailyAppSummaries(summaries: List<DailyAppSummary>) {
        dailyAppSummaryDao.insertAll(summaries)
    }
    override suspend fun insertDailyScreenUnlockSummary(summary: DailyScreenUnlockSummary) {
        dailyScreenUnlockSummaryDao.insert(summary)
    }
    override fun getDailyAppSummaries(startDateMillis: Long, endDateMillis: Long): Flow<List<DailyAppSummary>> {
        return dailyAppSummaryDao.getAllSummariesInRange(startDateMillis, endDateMillis)
    }
    override fun getDailyScreenUnlockSummaries(startDateMillis: Long, endDateMillis: Long): Flow<List<DailyScreenUnlockSummary>> {
        return dailyScreenUnlockSummaryDao.getSummariesInRange(startDateMillis, endDateMillis)
    }

    // --- New Limited App Method Implementations ---
    override suspend fun insertLimitedApp(limitedApp: LimitedApp) {
        limitedAppDao.insertLimitedApp(limitedApp)
    }

    override suspend fun deleteLimitedApp(limitedApp: LimitedApp) {
        limitedAppDao.deleteLimitedApp(limitedApp)
    }

    override fun getLimitedApp(packageName: String): Flow<LimitedApp?> {
        return limitedAppDao.getLimitedApp(packageName)
    }

    override suspend fun getLimitedAppOnce(packageName: String): LimitedApp? {
        return limitedAppDao.getLimitedAppOnce(packageName)
    }

    override fun getAllLimitedApps(): Flow<List<LimitedApp>> {
        return limitedAppDao.getAllLimitedApps()
    }

    override suspend fun getAllLimitedAppsOnce(): List<LimitedApp> {
        return limitedAppDao.getAllLimitedAppsOnce()
    }
}
