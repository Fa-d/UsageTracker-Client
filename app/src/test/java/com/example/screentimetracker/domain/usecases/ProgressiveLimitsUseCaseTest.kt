package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.*
import com.example.screentimetracker.domain.repository.TrackerRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class ProgressiveLimitsUseCaseTest {

    private lateinit var progressiveLimitDao: ProgressiveLimitDao
    private lateinit var progressiveMilestoneDao: ProgressiveMilestoneDao
    private lateinit var trackerRepository: TrackerRepository
    private lateinit var progressiveLimitsUseCase: ProgressiveLimitsUseCase

    private val testPackageName = "com.test.app"
    private val testTargetLimit = 30 * 60 * 1000L // 30 minutes
    private val testCurrentUsage = 60 * 60 * 1000L // 60 minutes average

    @Before
    fun setup() {
        progressiveLimitDao = mockk()
        progressiveMilestoneDao = mockk()
        trackerRepository = mockk()
        
        progressiveLimitsUseCase = ProgressiveLimitsUseCase(
            progressiveLimitDao,
            progressiveMilestoneDao,
            trackerRepository
        )
    }

    @Test
    fun `createProgressiveLimit should create limit with 10 percent buffer`() = runTest {
        // Given
        coEvery { trackerRepository.getAverageAppUsageLast7Days(testPackageName) } returns testCurrentUsage
        coEvery { progressiveLimitDao.insertLimit(any()) } returns 1L
        coEvery { progressiveMilestoneDao.insertMilestones(any()) } just Runs

        // When
        val limitId = progressiveLimitsUseCase.createProgressiveLimit(
            appPackageName = testPackageName,
            targetLimitMillis = testTargetLimit
        )

        // Then
        assertEquals(1L, limitId)
        
        val capturedLimit = slot<ProgressiveLimit>()
        coVerify { progressiveLimitDao.insertLimit(capture(capturedLimit)) }
        
        val expectedOriginalLimit = (testCurrentUsage * 1.1).toLong()
        assertEquals(expectedOriginalLimit, capturedLimit.captured.originalLimitMillis)
        assertEquals(expectedOriginalLimit, capturedLimit.captured.currentLimitMillis)
        assertEquals(testTargetLimit, capturedLimit.captured.targetLimitMillis)
        assertEquals(testPackageName, capturedLimit.captured.appPackageName)
        assertTrue(capturedLimit.captured.isActive)
        assertEquals(0f, capturedLimit.captured.progressPercentage)
    }

    @Test
    fun `createProgressiveLimit should create four milestones`() = runTest {
        // Given
        coEvery { trackerRepository.getAverageAppUsageLast7Days(testPackageName) } returns testCurrentUsage
        coEvery { progressiveLimitDao.insertLimit(any()) } returns 1L
        coEvery { progressiveMilestoneDao.insertMilestones(any()) } just Runs

        // When
        progressiveLimitsUseCase.createProgressiveLimit(testPackageName, testTargetLimit)

        // Then
        val capturedMilestones = slot<List<ProgressiveMilestone>>()
        coVerify { progressiveMilestoneDao.insertMilestones(capture(capturedMilestones)) }
        
        val milestones = capturedMilestones.captured
        assertEquals(4, milestones.size)
        
        val percentages = milestones.map { it.milestonePercentage }
        assertEquals(listOf(25, 50, 75, 100), percentages)
        
        milestones.forEach { milestone ->
            assertEquals(1L, milestone.limitId)
            assertFalse(milestone.isAchieved)
            assertNull(milestone.achievedDate)
            assertFalse(milestone.celebrationShown)
        }
    }

    @Test
    fun `processWeeklyReductions should reduce limits by correct percentage`() = runTest {
        // Given
        val currentDate = LocalDate.now().toString()
        val originalLimit = 60 * 60 * 1000L // 60 minutes
        val currentLimit = 50 * 60 * 1000L // 50 minutes
        val targetLimit = 30 * 60 * 1000L // 30 minutes
        
        val existingLimit = ProgressiveLimit(
            id = 1L,
            appPackageName = testPackageName,
            originalLimitMillis = originalLimit,
            targetLimitMillis = targetLimit,
            currentLimitMillis = currentLimit,
            reductionPercentage = 10,
            startDate = LocalDate.now().minusWeeks(1).toString(),
            nextReductionDate = currentDate,
            isActive = true,
            progressPercentage = 16.67f
        )
        
        coEvery { progressiveLimitDao.getLimitsReadyForReduction(currentDate) } returns listOf(existingLimit)
        coEvery { progressiveLimitDao.updateLimit(any()) } just Runs
        coEvery { progressiveMilestoneDao.getMilestonesForLimit(1L) } returns emptyList()

        // When
        progressiveLimitsUseCase.processWeeklyReductions()

        // Then
        val capturedLimit = slot<ProgressiveLimit>()
        coVerify { progressiveLimitDao.updateLimit(capture(capturedLimit)) }
        
        val expectedNewLimit = currentLimit - (currentLimit * 10 / 100) // 45 minutes
        assertEquals(expectedNewLimit, capturedLimit.captured.currentLimitMillis)
        
        // Progress should be updated
        val expectedProgress = (originalLimit - expectedNewLimit).toFloat() / (originalLimit - targetLimit).toFloat() * 100f
        assertEquals(expectedProgress, capturedLimit.captured.progressPercentage, 0.1f)
    }

    @Test
    fun `processWeeklyReductions should not reduce below target limit`() = runTest {
        // Given
        val currentDate = LocalDate.now().toString()
        val originalLimit = 60 * 60 * 1000L // 60 minutes
        val currentLimit = 32 * 60 * 1000L // 32 minutes (close to target)
        val targetLimit = 30 * 60 * 1000L // 30 minutes
        
        val existingLimit = ProgressiveLimit(
            id = 1L,
            appPackageName = testPackageName,
            originalLimitMillis = originalLimit,
            targetLimitMillis = targetLimit,
            currentLimitMillis = currentLimit,
            reductionPercentage = 10,
            startDate = LocalDate.now().minusWeeks(1).toString(),
            nextReductionDate = currentDate,
            isActive = true,
            progressPercentage = 93.33f
        )
        
        coEvery { progressiveLimitDao.getLimitsReadyForReduction(currentDate) } returns listOf(existingLimit)
        coEvery { progressiveLimitDao.updateLimit(any()) } just Runs
        coEvery { progressiveMilestoneDao.getMilestonesForLimit(1L) } returns emptyList()

        // When
        progressiveLimitsUseCase.processWeeklyReductions()

        // Then
        val capturedLimit = slot<ProgressiveLimit>()
        coVerify { progressiveLimitDao.updateLimit(capture(capturedLimit)) }
        
        // Should be set to target limit, not below it
        assertEquals(targetLimit, capturedLimit.captured.currentLimitMillis)
        assertFalse(capturedLimit.captured.isActive) // Should be deactivated when target reached
        assertEquals(100f, capturedLimit.captured.progressPercentage)
    }

    @Test
    fun `processWeeklyReductions should update milestones correctly`() = runTest {
        // Given
        val currentDate = LocalDate.now().toString()
        val originalLimit = 60 * 60 * 1000L // 60 minutes
        val currentLimit = 50 * 60 * 1000L // 50 minutes
        val targetLimit = 30 * 60 * 1000L // 30 minutes
        
        val existingLimit = ProgressiveLimit(
            id = 1L,
            appPackageName = testPackageName,
            originalLimitMillis = originalLimit,
            targetLimitMillis = targetLimit,
            currentLimitMillis = currentLimit,
            reductionPercentage = 10,
            startDate = LocalDate.now().minusWeeks(1).toString(),
            nextReductionDate = currentDate,
            isActive = true,
            progressPercentage = 16.67f
        )
        
        val milestone25 = ProgressiveMilestone(
            id = 1L,
            limitId = 1L,
            milestonePercentage = 25,
            isAchieved = false,
            rewardTitle = "Quarter Way There! 🎯",
            rewardDescription = "You've reduced your usage by 25%!"
        )
        
        coEvery { progressiveLimitDao.getLimitsReadyForReduction(currentDate) } returns listOf(existingLimit)
        coEvery { progressiveLimitDao.updateLimit(any()) } just Runs
        coEvery { progressiveMilestoneDao.getMilestonesForLimit(1L) } returns listOf(milestone25)
        coEvery { progressiveMilestoneDao.markMilestoneAchieved(any(), any(), any()) } just Runs

        // When
        progressiveLimitsUseCase.processWeeklyReductions()

        // Then - milestone should be marked as achieved if progress >= 25%
        val capturedLimit = slot<ProgressiveLimit>()
        coVerify { progressiveLimitDao.updateLimit(capture(capturedLimit)) }
        
        val newProgress = capturedLimit.captured.progressPercentage
        if (newProgress >= 25f) {
            coVerify { progressiveMilestoneDao.markMilestoneAchieved(1L, 25, currentDate) }
        }
    }

    @Test
    fun `getAllActiveLimits should return flow from dao`() = runTest {
        // Given
        val expectedLimits = listOf(
            ProgressiveLimit(
                id = 1L,
                appPackageName = testPackageName,
                originalLimitMillis = 60 * 60 * 1000L,
                targetLimitMillis = 30 * 60 * 1000L,
                currentLimitMillis = 45 * 60 * 1000L,
                startDate = LocalDate.now().toString(),
                nextReductionDate = LocalDate.now().plusWeeks(1).toString(),
                isActive = true,
                progressPercentage = 50f
            )
        )
        every { progressiveLimitDao.getAllActiveLimits() } returns flowOf(expectedLimits)

        // When
        val result = progressiveLimitsUseCase.getAllActiveLimits()

        // Then
        result.collect { limits ->
            assertEquals(expectedLimits, limits)
        }
        verify { progressiveLimitDao.getAllActiveLimits() }
    }

    @Test
    fun `cancelProgressiveLimit should deactivate limit`() = runTest {
        // Given
        coEvery { progressiveLimitDao.deactivateLimitForApp(testPackageName) } just Runs

        // When
        progressiveLimitsUseCase.cancelProgressiveLimit(testPackageName)

        // Then
        coVerify { progressiveLimitDao.deactivateLimitForApp(testPackageName) }
    }

    @Test
    fun `getUncelebratedMilestones should return milestones from dao`() = runTest {
        // Given
        val expectedMilestones = listOf(
            ProgressiveMilestone(
                id = 1L,
                limitId = 1L,
                milestonePercentage = 25,
                isAchieved = true,
                celebrationShown = false,
                rewardTitle = "Quarter Way There! 🎯",
                rewardDescription = "Great progress!"
            )
        )
        coEvery { progressiveMilestoneDao.getUncelebratedMilestones() } returns expectedMilestones

        // When
        val result = progressiveLimitsUseCase.getUncelebratedMilestones()

        // Then
        assertEquals(expectedMilestones, result)
        coVerify { progressiveMilestoneDao.getUncelebratedMilestones() }
    }

    @Test
    fun `markCelebrationShown should update milestone`() = runTest {
        // Given
        val milestoneId = 1L
        coEvery { progressiveMilestoneDao.markCelebrationShown(milestoneId) } just Runs

        // When
        progressiveLimitsUseCase.markCelebrationShown(milestoneId)

        // Then
        coVerify { progressiveMilestoneDao.markCelebrationShown(milestoneId) }
    }
}