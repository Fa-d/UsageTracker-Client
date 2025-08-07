package com.example.screentimetracker.domain.repository

import com.example.screentimetracker.data.local.AppOpenData
import com.example.screentimetracker.data.local.AppSessionDataAggregate
import com.example.screentimetracker.data.local.AppSessionEvent
import com.example.screentimetracker.data.local.AppUsageEvent
import com.example.screentimetracker.data.local.DailyAppSummary
import com.example.screentimetracker.data.local.DailyScreenUnlockSummary
import com.example.screentimetracker.data.local.LimitedApp
import com.example.screentimetracker.data.local.ScreenUnlockEvent
import com.example.screentimetracker.domain.model.Achievement
import com.example.screentimetracker.domain.model.WellnessScore
import com.example.screentimetracker.data.local.UserGoal
import com.example.screentimetracker.data.local.Challenge
import com.example.screentimetracker.data.local.FocusSession
import com.example.screentimetracker.data.local.HabitTracker
import com.example.screentimetracker.data.local.TimeRestriction
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
    suspend fun getLastOpenedTimestampsForAppsInRange(startTime: Long, endTime: Long): List<com.example.screentimetracker.data.local.AppLastOpenedData>
    fun getLastOpenedTimestampsForAppsInRangeFlow(startTime: Long, endTime: Long): Flow<List<com.example.screentimetracker.data.local.AppLastOpenedData>>

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
}
