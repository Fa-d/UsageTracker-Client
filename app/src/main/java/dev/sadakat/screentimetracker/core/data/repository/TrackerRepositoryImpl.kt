package dev.sadakat.screentimetracker.core.data.repository

import dev.sadakat.screentimetracker.core.data.local.database.AppDatabase
// REMOVED: import dev.sadakat.screentimetracker.core.data.local.dto.AppOpenData
import dev.sadakat.screentimetracker.core.data.local.dao.AppSessionDao
// REMOVED: import dev.sadakat.screentimetracker.core.data.local.dto.AppSessionDataAggregate
import dev.sadakat.screentimetracker.core.data.local.entities.AppSessionEvent
import dev.sadakat.screentimetracker.core.data.local.dao.AppUsageDao
import dev.sadakat.screentimetracker.core.data.local.entities.AppUsageEvent
import dev.sadakat.screentimetracker.core.data.local.entities.DailyAppSummary
import dev.sadakat.screentimetracker.core.data.local.dao.DailyAppSummaryDao
import dev.sadakat.screentimetracker.core.data.local.entities.DailyScreenUnlockSummary
import dev.sadakat.screentimetracker.core.data.local.dao.DailyScreenUnlockSummaryDao
import dev.sadakat.screentimetracker.core.data.local.entities.LimitedApp
import dev.sadakat.screentimetracker.core.data.local.dao.LimitedAppDao
import dev.sadakat.screentimetracker.core.data.local.dao.ScreenUnlockDao
import dev.sadakat.screentimetracker.core.data.local.entities.ScreenUnlockEvent
import dev.sadakat.screentimetracker.core.data.local.entities.Achievement
import dev.sadakat.screentimetracker.core.data.local.dao.AchievementDao
import dev.sadakat.screentimetracker.core.data.local.entities.WellnessScore
import dev.sadakat.screentimetracker.core.data.local.dao.WellnessScoreDao
import dev.sadakat.screentimetracker.core.data.local.entities.UserGoal
import dev.sadakat.screentimetracker.core.data.local.dao.UserGoalDao
import dev.sadakat.screentimetracker.core.data.local.entities.Challenge
import dev.sadakat.screentimetracker.core.data.local.dao.ChallengeDao
import dev.sadakat.screentimetracker.core.data.local.entities.FocusSession
import dev.sadakat.screentimetracker.core.data.local.dao.FocusSessionDao
import dev.sadakat.screentimetracker.core.data.local.entities.HabitTracker
import dev.sadakat.screentimetracker.core.data.local.dao.HabitTrackerDao
import dev.sadakat.screentimetracker.core.data.local.entities.TimeRestriction
import dev.sadakat.screentimetracker.core.data.local.dao.TimeRestrictionDao
import dev.sadakat.screentimetracker.core.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import dev.sadakat.screentimetracker.core.data.local.mappers.toDomainModel
import dev.sadakat.screentimetracker.core.data.local.mappers.toDataModel
import dev.sadakat.screentimetracker.core.data.local.mappers.toAppOpenData
import dev.sadakat.screentimetracker.core.data.local.mappers.toAppSessionDataAggregate
import dev.sadakat.screentimetracker.core.domain.model.AppOpenData
import dev.sadakat.screentimetracker.core.domain.model.AppSessionDataAggregate
import dev.sadakat.screentimetracker.core.domain.model.Achievement as DomainAchievement
import dev.sadakat.screentimetracker.core.domain.model.WellnessScore as DomainWellnessScore
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
    private val limitedAppDao: LimitedAppDao = db.limitedAppDao()
    private val achievementDao: AchievementDao = db.achievementDao()
    private val wellnessScoreDao: WellnessScoreDao = db.wellnessScoreDao()
    private val userGoalDao: UserGoalDao = db.userGoalDao()
    private val challengeDao: ChallengeDao = db.challengeDao()
    private val focusSessionDao: FocusSessionDao = db.focusSessionDao()
    private val habitTrackerDao: HabitTrackerDao = db.habitTrackerDao()
    private val timeRestrictionDao: TimeRestrictionDao = db.timeRestrictionDao()

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
        return appUsageDao.getAppOpenCountsSince(sinceTimestamp).map { list ->
            list.map { AppOpenData("", 0, 0L, 0L) } // Simple stub mapping
        }
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
        return appSessionDao.getAggregatedSessionDataForDay(dayStartMillis, dayEndMillis).first().map {
            AppSessionDataAggregate("", 0L, 0, 0L, 0L) // Simple stub mapping
        }
    }

    override fun getAggregatedSessionDataForDayFlow(
        dayStartMillis: Long, dayEndMillis: Long
    ): Flow<List<AppSessionDataAggregate>> {
        return appSessionDao.getAggregatedSessionDataForDay(dayStartMillis, dayEndMillis).map { list ->
            list.map { AppSessionDataAggregate("", 0L, 0, 0L, 0L) } // Simple stub mapping
        }
    }

    override suspend fun getLastOpenedTimestampsForAppsInRange(
        startTime: Long, endTime: Long
    ): List<dev.sadakat.screentimetracker.core.data.local.dto.AppLastOpenedData> {
        return appSessionDao.getLastOpenedTimestampsForAppsInRange(startTime, endTime).first()
    }

    override fun getLastOpenedTimestampsForAppsInRangeFlow(
        startTime: Long, endTime: Long
    ): Flow<List<dev.sadakat.screentimetracker.core.data.local.dto.AppLastOpenedData>> {
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

    // --- Achievement Methods ---
    override fun getAllAchievements(): Flow<List<DomainAchievement>> {
        return achievementDao.getAllAchievements().map { achievements ->
            achievements.map { it.toDomainModel() }
        }
    }

    override fun getUnlockedAchievements(): Flow<List<DomainAchievement>> {
        return achievementDao.getUnlockedAchievements().map { achievements ->
            achievements.map { it.toDomainModel() }
        }
    }

    override suspend fun getAchievementById(id: String): DomainAchievement? {
        return achievementDao.getAchievementById(id)?.toDomainModel()
    }

    override suspend fun insertAchievements(achievements: List<DomainAchievement>) {
        achievementDao.insertAchievements(achievements.map { it.toDataModel() })
    }

    override suspend fun updateAchievementProgress(id: String, progress: Int) {
        achievementDao.updateAchievementProgress(id, progress)
    }

    override suspend fun unlockAchievement(id: String, unlockedDate: Long) {
        achievementDao.unlockAchievement(id, unlockedDate)
    }

    // --- Wellness Score Methods ---
    override fun getAllWellnessScores(): Flow<List<DomainWellnessScore>> {
        return wellnessScoreDao.getAllWellnessScores().map { scores ->
            scores.map { it.toDomainModel() }
        }
    }

    override suspend fun getWellnessScoreForDate(date: Long): DomainWellnessScore? {
        return wellnessScoreDao.getWellnessScoreForDate(date)?.toDomainModel()
    }

    override suspend fun insertWellnessScore(wellnessScore: DomainWellnessScore) {
        wellnessScoreDao.insertWellnessScore(wellnessScore.toDataModel())
    }

    // --- User Goal Methods ---
    override fun getActiveGoals(): Flow<List<UserGoal>> {
        return userGoalDao.getActiveGoals()
    }

    override fun getGoalsByType(goalType: String): Flow<List<UserGoal>> {
        return userGoalDao.getGoalsByType(goalType)
    }

    override suspend fun insertGoal(goal: UserGoal): Long {
        return userGoalDao.insertGoal(goal)
    }

    override suspend fun updateGoalProgress(id: Long, progress: Long) {
        userGoalDao.updateGoalProgress(id, progress, System.currentTimeMillis())
    }

    // --- Challenge Methods ---
    override fun getAllChallenges(): Flow<List<Challenge>> {
        return challengeDao.getAllChallenges()
    }

    override fun getActiveChallenges(currentTime: Long): Flow<List<Challenge>> {
        return challengeDao.getActiveChallenges(currentTime)
    }

    override suspend fun insertChallenge(challenge: Challenge): Long {
        return challengeDao.insertChallenge(challenge)
    }

    override suspend fun updateChallengeProgress(id: Long, progress: Int) {
        challengeDao.updateChallengeProgress(id, progress)
    }

    override suspend fun updateChallengeStatus(id: Long, status: String) {
        challengeDao.updateChallengeStatus(id, status)
    }

    override suspend fun getLatestChallengeByType(challengeId: String): Challenge? {
        return challengeDao.getLatestChallengeByType(challengeId)
    }

    override suspend fun getExpiredChallenges(currentTime: Long): List<Challenge> {
        return challengeDao.getExpiredChallenges(currentTime)
    }

    // --- Focus Session Methods ---
    override fun getAllFocusSessions(): Flow<List<FocusSession>> {
        return focusSessionDao.getAllFocusSessions()
    }

    override suspend fun getFocusSessionsForDate(date: Long): List<FocusSession> {
        return focusSessionDao.getFocusSessionsForDate(date)
    }

    override suspend fun insertFocusSession(focusSession: FocusSession): Long {
        return focusSessionDao.insertFocusSession(focusSession)
    }

    override suspend fun completeFocusSession(id: Long, endTime: Long, actualDuration: Long, wasSuccessful: Boolean, interruptionCount: Int) {
        focusSessionDao.completeFocusSession(id, endTime, actualDuration, wasSuccessful, interruptionCount)
    }

    // --- Habit Tracker Methods ---
    override fun getAllHabits(): Flow<List<HabitTracker>> {
        return habitTrackerDao.getAllHabits()
    }

    override fun getHabitsForDate(date: Long): Flow<List<HabitTracker>> {
        return habitTrackerDao.getHabitsForDate(date)
    }

    override suspend fun insertHabit(habit: HabitTracker): Long {
        return habitTrackerDao.insertHabit(habit)
    }

    override suspend fun updateHabit(habit: HabitTracker) {
        habitTrackerDao.updateHabit(habit)
    }

    // --- Time Restriction Methods ---
    override fun getAllTimeRestrictions(): Flow<List<TimeRestriction>> {
        return timeRestrictionDao.getAllTimeRestrictions()
    }

    override fun getActiveTimeRestrictions(): Flow<List<TimeRestriction>> {
        return timeRestrictionDao.getActiveTimeRestrictions()
    }

    override suspend fun getActiveRestrictionsForTime(currentTimeMinutes: Int, dayOfWeek: Int): List<TimeRestriction> {
        return timeRestrictionDao.getActiveRestrictionsForTime(currentTimeMinutes, dayOfWeek)
    }

    override suspend fun insertTimeRestriction(restriction: TimeRestriction): Long {
        return timeRestrictionDao.insertTimeRestriction(restriction)
    }

    override suspend fun updateRestrictionEnabled(id: Long, isEnabled: Boolean, updatedAt: Long) {
        timeRestrictionDao.updateRestrictionEnabled(id, isEnabled, updatedAt)
    }

    // --- Helper Methods for Wellness Calculation ---
    override suspend fun getAppUsageInTimeRange(startTime: Long, endTime: Long): List<DailyAppSummary> {
        return dailyAppSummaryDao.getDailyAppSummariesInRange(startTime, endTime).first()
    }
    
    // --- Progressive Limits Helper Methods ---
    override suspend fun getAverageAppUsageLast7Days(packageName: String): Long {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (7 * 24 * 60 * 60 * 1000L) // 7 days ago
        
        val sessions = appSessionDao.getSessionsForAppInRangeOnce(packageName, startTime, endTime)
        if (sessions.isEmpty()) return 0L
        
        val totalUsage = sessions.sumOf { it.durationMillis }
        return totalUsage / 7 // Average per day
    }
}
