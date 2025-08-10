package com.example.screentimetracker.domain.usecases

import android.content.Context
import com.example.screentimetracker.data.local.*
import com.example.screentimetracker.domain.repository.TrackerRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class DataExportUseCaseTest {

    private val mockContext = mockk<Context>()
    private val mockTrackerRepository = mockk<TrackerRepository>()
    private val mockAppUsageDao = mockk<AppUsageDao>()
    private val mockAppSessionDao = mockk<AppSessionDao>()
    private val mockScreenUnlockDao = mockk<ScreenUnlockDao>()
    private val mockDailyAppSummaryDao = mockk<DailyAppSummaryDao>()
    private val mockDailyScreenUnlockSummaryDao = mockk<DailyScreenUnlockSummaryDao>()
    private val mockAchievementDao = mockk<AchievementDao>()
    private val mockWellnessScoreDao = mockk<WellnessScoreDao>()
    private val mockUserGoalDao = mockk<UserGoalDao>()
    private val mockChallengeDao = mockk<ChallengeDao>()
    private val mockFocusSessionDao = mockk<FocusSessionDao>()
    private val mockHabitTrackerDao = mockk<HabitTrackerDao>()
    private val mockTimeRestrictionDao = mockk<TimeRestrictionDao>()
    private val mockProgressiveLimitDao = mockk<ProgressiveLimitDao>()
    private val mockProgressiveMilestoneDao = mockk<ProgressiveMilestoneDao>()
    private val mockLimitedAppDao = mockk<LimitedAppDao>()
    private val mockPrivacyManagerUseCase = mockk<PrivacyManagerUseCase>()

    private lateinit var dataExportUseCase: DataExportUseCase

    @Before
    fun setup() {
        dataExportUseCase = DataExportUseCase(
            context = mockContext,
            trackerRepository = mockTrackerRepository,
            appUsageDao = mockAppUsageDao,
            appSessionDao = mockAppSessionDao,
            screenUnlockDao = mockScreenUnlockDao,
            dailyAppSummaryDao = mockDailyAppSummaryDao,
            dailyScreenUnlockSummaryDao = mockDailyScreenUnlockSummaryDao,
            achievementDao = mockAchievementDao,
            wellnessScoreDao = mockWellnessScoreDao,
            userGoalDao = mockUserGoalDao,
            challengeDao = mockChallengeDao,
            focusSessionDao = mockFocusSessionDao,
            habitTrackerDao = mockHabitTrackerDao,
            timeRestrictionDao = mockTimeRestrictionDao,
            progressiveLimitDao = mockProgressiveLimitDao,
            progressiveMilestoneDao = mockProgressiveMilestoneDao,
            limitedAppDao = mockLimitedAppDao,
            privacyManagerUseCase = mockPrivacyManagerUseCase
        )
    }

    @Test
    fun `exportDataAsJson should handle missing external storage by using working directory`() = runTest {
        // Given
        setupMockDataForExport()
        every { mockContext.getExternalFilesDir(null) } returns null
        coEvery { mockPrivacyManagerUseCase.updateLastExportTime() } just Runs

        // When
        val result = dataExportUseCase.exportDataAsJson()

        // Then
        assertTrue("Should succeed even with null external storage", result.isSuccess)
        assertNotNull("Should return a file", result.getOrNull())
        coVerify(exactly = 1) { mockPrivacyManagerUseCase.updateLastExportTime() }
    }

    @Test
    fun `exportDataAsJson should handle exception and return failure`() = runTest {
        // Given
        setupMockDataForExport()
        every { mockContext.getExternalFilesDir(null) } throws RuntimeException("Storage not available")
        coEvery { mockPrivacyManagerUseCase.updateLastExportTime() } just Runs

        // When
        val result = dataExportUseCase.exportDataAsJson()

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
        assertEquals("Storage not available", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { mockPrivacyManagerUseCase.updateLastExportTime() }
    }

    @Test
    fun `exportDataAsCsv should handle missing external storage by using working directory`() = runTest {
        // Given
        setupMockDataForExport()
        every { mockContext.getExternalFilesDir(null) } returns null
        coEvery { mockPrivacyManagerUseCase.updateLastExportTime() } just Runs

        // When
        val result = dataExportUseCase.exportDataAsCsv()

        // Then
        assertTrue("Should succeed even with null external storage", result.isSuccess)
        assertNotNull("Should return files list", result.getOrNull())
        assertTrue("Should return non-empty files list", result.getOrNull()!!.isNotEmpty())
        coVerify(exactly = 1) { mockPrivacyManagerUseCase.updateLastExportTime() }
    }

    @Test
    fun `exportDataAsCsv should handle exception and return failure`() = runTest {
        // Given
        setupMockDataForExport()
        every { mockContext.getExternalFilesDir(null) } throws RuntimeException("Storage error")
        coEvery { mockPrivacyManagerUseCase.updateLastExportTime() } just Runs

        // When
        val result = dataExportUseCase.exportDataAsCsv()

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
        assertEquals("Storage error", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { mockPrivacyManagerUseCase.updateLastExportTime() }
    }

    @Test
    fun `should verify data collection from all sources`() = runTest {
        // Given
        setupMockDataForExport()
        every { mockContext.getExternalFilesDir(null) } returns null

        // When
        dataExportUseCase.exportDataAsJson()

        // Then - Verify all data sources were called
        verifyAllDataSourcesWereCalled()
    }

    // Helper functions for setting up mock data
    private fun setupMockDataForExport() {
        coEvery { mockAppUsageDao.getAllAppUsageEventsForExport() } returns listOf(
            createMockAppUsageEvent("com.app1", "START", 1000L)
        )
        coEvery { mockAppSessionDao.getAllAppSessionEventsForExport() } returns listOf(
            createMockAppSessionEvent("com.app1", 1000L, 2000L, 1000L)
        )
        coEvery { mockScreenUnlockDao.getAllScreenUnlockEventsForExport() } returns listOf(
            createMockScreenUnlockEvent(1000L)
        )
        coEvery { mockDailyAppSummaryDao.getAllDailyAppSummariesForExport() } returns listOf(
            createMockDailyAppSummary(1704067200000L, "com.app1", 3600000L, 5)
        )
        coEvery { mockDailyScreenUnlockSummaryDao.getAllDailyScreenUnlockSummariesForExport() } returns listOf(
            createMockDailyScreenUnlockSummary(1704067200000L, 50)
        )
        coEvery { mockAchievementDao.getAllAchievementsForExport() } returns listOf(
            createMockAchievement("first_week", "First Week", true, 1000L, 100)
        )
        coEvery { mockWellnessScoreDao.getAllWellnessScoresForExport() } returns listOf(
            createMockWellnessScore(1000L, 75, 80, 70, 75, 75, "BALANCED_USER")
        )
        coEvery { mockUserGoalDao.getAllUserGoalsForExport() } returns emptyList()
        coEvery { mockChallengeDao.getAllChallengesForExport() } returns emptyList()
        coEvery { mockFocusSessionDao.getAllFocusSessionsForExport() } returns emptyList()
        coEvery { mockHabitTrackerDao.getAllHabitTrackersForExport() } returns emptyList()
        coEvery { mockTimeRestrictionDao.getAllTimeRestrictionsForExport() } returns emptyList()
        coEvery { mockProgressiveLimitDao.getAllProgressiveLimitsForExport() } returns emptyList()
        coEvery { mockProgressiveMilestoneDao.getAllProgressiveMilestonesForExport() } returns emptyList()
        coEvery { mockLimitedAppDao.getAllLimitedAppsForExport() } returns emptyList()
    }

    private fun verifyAllDataSourcesWereCalled() {
        coVerify { mockAppUsageDao.getAllAppUsageEventsForExport() }
        coVerify { mockAppSessionDao.getAllAppSessionEventsForExport() }
        coVerify { mockScreenUnlockDao.getAllScreenUnlockEventsForExport() }
        coVerify { mockDailyAppSummaryDao.getAllDailyAppSummariesForExport() }
        coVerify { mockDailyScreenUnlockSummaryDao.getAllDailyScreenUnlockSummariesForExport() }
        coVerify { mockAchievementDao.getAllAchievementsForExport() }
        coVerify { mockWellnessScoreDao.getAllWellnessScoresForExport() }
        coVerify { mockUserGoalDao.getAllUserGoalsForExport() }
        coVerify { mockChallengeDao.getAllChallengesForExport() }
        coVerify { mockFocusSessionDao.getAllFocusSessionsForExport() }
        coVerify { mockHabitTrackerDao.getAllHabitTrackersForExport() }
        coVerify { mockTimeRestrictionDao.getAllTimeRestrictionsForExport() }
        coVerify { mockProgressiveLimitDao.getAllProgressiveLimitsForExport() }
        coVerify { mockProgressiveMilestoneDao.getAllProgressiveMilestonesForExport() }
        coVerify { mockLimitedAppDao.getAllLimitedAppsForExport() }
    }

    // Mock data creation helpers
    private fun createMockAppUsageEvent(packageName: String, eventName: String, timestamp: Long) = mockk<AppUsageEvent> {
        every { this@mockk.packageName } returns packageName
        every { this@mockk.eventName } returns eventName
        every { this@mockk.timestamp } returns timestamp
    }

    private fun createMockAppSessionEvent(packageName: String, startTime: Long, endTime: Long, duration: Long) = mockk<AppSessionEvent> {
        every { this@mockk.packageName } returns packageName
        every { this@mockk.startTimeMillis } returns startTime
        every { this@mockk.endTimeMillis } returns endTime
        every { this@mockk.durationMillis } returns duration
    }

    private fun createMockScreenUnlockEvent(timestamp: Long) = mockk<ScreenUnlockEvent> {
        every { this@mockk.timestamp } returns timestamp
    }

    private fun createMockDailyAppSummary(dateMillis: Long, packageName: String, totalUsage: Long, sessionCount: Int) = mockk<DailyAppSummary> {
        every { this@mockk.dateMillis } returns dateMillis
        every { this@mockk.packageName } returns packageName
        every { this@mockk.totalDurationMillis } returns totalUsage
        every { this@mockk.openCount } returns sessionCount
    }

    private fun createMockDailyScreenUnlockSummary(dateMillis: Long, unlockCount: Int) = mockk<DailyScreenUnlockSummary> {
        every { this@mockk.dateMillis } returns dateMillis
        every { this@mockk.unlockCount } returns unlockCount
    }

    private fun createMockAchievement(id: String, name: String, isUnlocked: Boolean, unlockedDate: Long, progress: Int) = mockk<Achievement> {
        every { this@mockk.achievementId } returns id
        every { this@mockk.name } returns name
        every { this@mockk.isUnlocked } returns isUnlocked
        every { this@mockk.unlockedDate } returns unlockedDate
        every { this@mockk.currentProgress } returns progress
    }

    private fun createMockWellnessScore(date: Long, total: Int, timeLimit: Int, focus: Int, breaks: Int, sleep: Int, level: String) = mockk<WellnessScore> {
        every { this@mockk.date } returns date
        every { this@mockk.totalScore } returns total
        every { this@mockk.timeLimitScore } returns timeLimit
        every { this@mockk.focusSessionScore } returns focus
        every { this@mockk.breaksScore } returns breaks
        every { this@mockk.sleepHygieneScore } returns sleep
        every { this@mockk.level } returns level
    }
}