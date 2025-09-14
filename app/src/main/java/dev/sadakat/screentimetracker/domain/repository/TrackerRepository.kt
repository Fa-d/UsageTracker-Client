package dev.sadakat.screentimetracker.domain.repository

import dev.sadakat.screentimetracker.data.local.AppOpenData
import dev.sadakat.screentimetracker.data.local.AppSessionDataAggregate
import dev.sadakat.screentimetracker.data.local.AppSessionEvent
import dev.sadakat.screentimetracker.data.local.AppUsageEvent
import dev.sadakat.screentimetracker.data.local.DailyAppSummary
import dev.sadakat.screentimetracker.data.local.DailyScreenUnlockSummary
import dev.sadakat.screentimetracker.data.local.LimitedApp
import dev.sadakat.screentimetracker.data.local.ScreenUnlockEvent
import dev.sadakat.screentimetracker.domain.model.Achievement
import dev.sadakat.screentimetracker.domain.model.WellnessScore
import dev.sadakat.screentimetracker.data.local.UserGoal
import dev.sadakat.screentimetracker.data.local.Challenge
import dev.sadakat.screentimetracker.data.local.FocusSession
import dev.sadakat.screentimetracker.data.local.HabitTracker
import dev.sadakat.screentimetracker.data.local.TimeRestriction
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
    suspend fun getLastOpenedTimestampsForAppsInRange(startTime: Long, endTime: Long): List<dev.sadakat.screentimetracker.data.local.AppLastOpenedData>
    fun getLastOpenedTimestampsForAppsInRangeFlow(startTime: Long, endTime: Long): Flow<List<dev.sadakat.screentimetracker.data.local.AppLastOpenedData>>

    // Daily Summary Methods
    suspend fun insertDailyAppSummaries(summaries: List<DailyAppSummary>)
    suspend fun insertDailyScreenUnlockSummary(summary: DailyScreenUnlockSummary)
    fun getDailyAppSummaries(startDateMillis: Long, endDateMillis: Long): Flow<List<DailyAppSummary>>
    fun getDailyScreenUnlockSummaries(startDateMillis: Long, endDateMillis: Long): Flow<List<DailyScreenUnlockSummary>>

    // --- Limited App Methods ---
    suspend fun insertLimitedApp(limitedApp: LimitedApp)
    suspend fun deleteLimitedApp(limitedApp: LimitedApp)
    fun getLimitedApp(packageName: String): Flow<LimitedApp?>
    suspend fun getLimitedAppOnce(packageName: String): LimitedApp?
    fun getAllLimitedApps(): Flow<List<LimitedApp>>
    suspend fun getAllLimitedAppsOnce(): List<LimitedApp>

    // --- Achievement Methods ---
    fun getAllAchievements(): Flow<List<Achievement>>
    fun getUnlockedAchievements(): Flow<List<Achievement>>
    suspend fun getAchievementById(id: String): Achievement?
    suspend fun insertAchievements(achievements: List<Achievement>)
    suspend fun updateAchievementProgress(id: String, progress: Int)
    suspend fun unlockAchievement(id: String, unlockedDate: Long)

    // --- Wellness Score Methods ---
    fun getAllWellnessScores(): Flow<List<WellnessScore>>
    suspend fun getWellnessScoreForDate(date: Long): WellnessScore?
    suspend fun insertWellnessScore(wellnessScore: WellnessScore)

    // --- User Goal Methods ---
    fun getActiveGoals(): Flow<List<UserGoal>>
    fun getGoalsByType(goalType: String): Flow<List<UserGoal>>
    suspend fun insertGoal(goal: UserGoal): Long
    suspend fun updateGoalProgress(id: Long, progress: Long)

    // --- Challenge Methods ---
    fun getAllChallenges(): Flow<List<Challenge>>
    fun getActiveChallenges(currentTime: Long): Flow<List<Challenge>>
    suspend fun insertChallenge(challenge: Challenge): Long
    suspend fun updateChallengeProgress(id: Long, progress: Int)
    suspend fun updateChallengeStatus(id: Long, status: String)
    suspend fun getLatestChallengeByType(challengeId: String): Challenge?
    suspend fun getExpiredChallenges(currentTime: Long): List<Challenge>

    // --- Focus Session Methods ---
    fun getAllFocusSessions(): Flow<List<FocusSession>>
    suspend fun getFocusSessionsForDate(date: Long): List<FocusSession>
    suspend fun insertFocusSession(focusSession: FocusSession): Long
    suspend fun completeFocusSession(id: Long, endTime: Long, actualDuration: Long, wasSuccessful: Boolean, interruptionCount: Int)

    // --- Habit Tracker Methods ---
    fun getAllHabits(): Flow<List<HabitTracker>>
    fun getHabitsForDate(date: Long): Flow<List<HabitTracker>>
    suspend fun insertHabit(habit: HabitTracker): Long
    suspend fun updateHabit(habit: HabitTracker)

    // --- Time Restriction Methods ---
    fun getAllTimeRestrictions(): Flow<List<TimeRestriction>>
    fun getActiveTimeRestrictions(): Flow<List<TimeRestriction>>
    suspend fun getActiveRestrictionsForTime(currentTimeMinutes: Int, dayOfWeek: Int): List<TimeRestriction>
    suspend fun insertTimeRestriction(restriction: TimeRestriction): Long
    suspend fun updateRestrictionEnabled(id: Long, isEnabled: Boolean, updatedAt: Long)

    // --- Helper Methods for Wellness Calculation ---
    suspend fun getAppUsageInTimeRange(startTime: Long, endTime: Long): List<DailyAppSummary>
    
    // --- Progressive Limits Helper Methods ---
    suspend fun getAverageAppUsageLast7Days(packageName: String): Long
    
    // --- AI Insights Methods ---
    suspend fun generateAIInsights(usageData: Any, userGoals: Any): List<Any>
    suspend fun generateGoalRecommendations(userData: Any, currentGoals: Any): List<Any>
    suspend fun checkWellnessAlerts(recentData: Any): List<Any>
}