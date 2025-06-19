package com.example.screentimetracker.data.repository

import com.example.screentimetracker.data.local.AppDatabase
import com.example.screentimetracker.data.local.AppOpenData
import com.example.screentimetracker.data.local.AppSessionDataAggregate
import com.example.screentimetracker.data.local.AppSessionDao
import com.example.screentimetracker.data.local.AppSessionEvent
import com.example.screentimetracker.data.local.AppUsageDao
import com.example.screentimetracker.data.local.AppUsageEvent
import com.example.screentimetracker.data.local.DailyAppSummary // Import DailyAppSummary
import com.example.screentimetracker.data.local.DailyAppSummaryDao // Import DailyAppSummaryDao
import com.example.screentimetracker.data.local.DailyScreenUnlockSummary // Import DailyScreenUnlockSummary
import com.example.screentimetracker.data.local.DailyScreenUnlockSummaryDao // Import DailyScreenUnlockSummaryDao
import com.example.screentimetracker.data.local.ScreenUnlockDao
import com.example.screentimetracker.data.local.ScreenUnlockEvent
import com.example.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackerRepositoryImpl @Inject constructor(
    private val db: AppDatabase
) : TrackerRepository {

    private val screenUnlockDao: ScreenUnlockDao = db.screenUnlockDao()
    private val appUsageDao: AppUsageDao = db.appUsageDao()
    private val appSessionDao: AppSessionDao = db.appSessionDao()
    private val dailyAppSummaryDao: DailyAppSummaryDao = db.dailyAppSummaryDao() // New
    private val dailyScreenUnlockSummaryDao: DailyScreenUnlockSummaryDao = db.dailyScreenUnlockSummaryDao() // New

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
    override fun getUnlockCountForDay(dayStartMillis: Long, dayEndMillis: Long): Flow<Int> {
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
    override fun getAggregatedSessionDataForDay(dayStartMillis: Long, dayEndMillis: Long): Flow<List<AppSessionDataAggregate>> {
        return appSessionDao.getAggregatedSessionDataForDay(dayStartMillis, dayEndMillis)
    }

    // --- New Daily Summary Method Implementations ---
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
}
