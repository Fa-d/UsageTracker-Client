package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.AppSessionDataAggregate
import com.example.screentimetracker.data.local.AppSessionEvent
import com.example.screentimetracker.data.local.FocusSession
import com.example.screentimetracker.data.local.UserGoal
import com.example.screentimetracker.domain.model.WellnessLevel
import com.example.screentimetracker.domain.model.WellnessScore
import com.example.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import java.util.Calendar
import java.util.concurrent.TimeUnit

class CalculateWellnessScoreUseCaseTest {

    @Mock
    private lateinit var mockRepository: TrackerRepository

    private lateinit var calculateWellnessScoreUseCase: CalculateWellnessScoreUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        calculateWellnessScoreUseCase = CalculateWellnessScoreUseCase(mockRepository)
    }

    @Test
    fun `invoke should return existing wellness score if available`() = runTest {
        // Given
        val testDate = System.currentTimeMillis()
        val startOfDay = getStartOfDay(testDate)
        val existingScore = WellnessScore(
            date = startOfDay,
            totalScore = 85,
            timeLimitScore = 90,
            focusSessionScore = 80,
            breaksScore = 85,
            sleepHygieneScore = 85,
            level = WellnessLevel.WELLNESS_MASTER,
            calculatedAt = System.currentTimeMillis() - 1000
        )
        
        whenever(mockRepository.getAllWellnessScores()).thenReturn(flowOf(listOf(existingScore)))

        // When
        val result = calculateWellnessScoreUseCase(testDate)

        // Then
        assertEquals(existingScore, result)
    }
    
    @Test
    fun `invoke should calculate new wellness score when none exists`() = runTest {
        // Given
        val testDate = System.currentTimeMillis()
        val startOfDay = getStartOfDay(testDate)
        
        // Mock empty wellness scores
        whenever(mockRepository.getAllWellnessScores()).thenReturn(flowOf(emptyList()))
        
        // Mock perfect day scenario
        setupMockForPerfectWellnessDay(startOfDay)

        // When
        val result = calculateWellnessScoreUseCase(testDate)

        // Then
        assertTrue("Total score should be high for perfect day", result.totalScore >= 85)
        assertEquals(WellnessLevel.WELLNESS_MASTER, result.level)
        assertEquals(startOfDay, result.date)
        verify(mockRepository).insertWellnessScore(any())
    }
    
    @Test
    fun `invoke should calculate low wellness score for poor digital habits`() = runTest {
        // Given
        val testDate = System.currentTimeMillis()
        val startOfDay = getStartOfDay(testDate)
        
        whenever(mockRepository.getAllWellnessScores()).thenReturn(flowOf(emptyList()))
        
        // Mock poor digital habits day
        setupMockForPoorWellnessDay(startOfDay)

        // When
        val result = calculateWellnessScoreUseCase(testDate)

        // Then
        assertTrue("Total score should be low for poor habits", result.totalScore <= 40)
        assertEquals(WellnessLevel.DIGITAL_SPROUT, result.level)
        verify(mockRepository).insertWellnessScore(any())
    }
    
    @Test
    fun `calculateTimeLimitScore should return 100 for perfect goal adherence`() = runTest {
        // Given
        val testDate = System.currentTimeMillis()
        val startOfDay = getStartOfDay(testDate)
        
        whenever(mockRepository.getAllWellnessScores()).thenReturn(flowOf(emptyList()))
        
        // User has 2-hour daily limit goal
        val goal = UserGoal(
            goalType = "daily_screen_time",
            targetValue = TimeUnit.HOURS.toMillis(2), // 2 hours
            isActive = true
        )
        whenever(mockRepository.getActiveGoals()).thenReturn(flowOf(listOf(goal)))
        
        // User used exactly 1.5 hours (under limit)
        val sessionData = listOf(
            AppSessionDataAggregate("com.example.app", TimeUnit.HOURS.toMillis(1) + TimeUnit.MINUTES.toMillis(30), 3)
        )
        whenever(mockRepository.getAggregatedSessionDataForDayFlow(any(), any())).thenReturn(flowOf(sessionData))
        
        // Mock other calculations to return neutral scores
        setupNeutralMocksExceptTimeLimits(startOfDay)

        // When
        val result = calculateWellnessScoreUseCase(testDate)

        // Then
        assertEquals(100, result.timeLimitScore)
    }
    
    @Test
    fun `calculateFocusSessionScore should return 100 for 3+ successful sessions`() = runTest {
        // Given
        val testDate = System.currentTimeMillis()
        val startOfDay = getStartOfDay(testDate)
        
        whenever(mockRepository.getAllWellnessScores()).thenReturn(flowOf(emptyList()))
        
        // Mock 3 successful focus sessions
        val focusSessions = listOf(
            FocusSession(
                startTime = startOfDay + TimeUnit.HOURS.toMillis(2), 
                endTime = startOfDay + TimeUnit.HOURS.toMillis(2) + TimeUnit.MINUTES.toMillis(25),
                targetDurationMillis = TimeUnit.MINUTES.toMillis(25),
                actualDurationMillis = TimeUnit.MINUTES.toMillis(25),
                appsBlocked = "",
                wasSuccessful = true
            ),
            FocusSession(
                startTime = startOfDay + TimeUnit.HOURS.toMillis(6), 
                endTime = startOfDay + TimeUnit.HOURS.toMillis(6) + TimeUnit.MINUTES.toMillis(25),
                targetDurationMillis = TimeUnit.MINUTES.toMillis(25),
                actualDurationMillis = TimeUnit.MINUTES.toMillis(25),
                appsBlocked = "",
                wasSuccessful = true
            ),
            FocusSession(
                startTime = startOfDay + TimeUnit.HOURS.toMillis(10), 
                endTime = startOfDay + TimeUnit.HOURS.toMillis(10) + TimeUnit.MINUTES.toMillis(25),
                targetDurationMillis = TimeUnit.MINUTES.toMillis(25),
                actualDurationMillis = TimeUnit.MINUTES.toMillis(25),
                appsBlocked = "",
                wasSuccessful = true
            )
        )
        whenever(mockRepository.getFocusSessionsForDate(startOfDay)).thenReturn(focusSessions)
        
        // Mock other calculations to return neutral scores
        setupNeutralMocksExceptFocus(startOfDay)

        // When
        val result = calculateWellnessScoreUseCase(testDate)

        // Then
        assertEquals(100, result.focusSessionScore)
    }

    @Test
    fun `invoke should set date to start of day`() = runTest {
        // Given - Create a timestamp in the middle of the day
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.JANUARY, 15, 14, 30, 45) // 2:30:45 PM
        calendar.set(Calendar.MILLISECOND, 500)
        val testDate = calendar.timeInMillis

        // When
        val result = calculateWellnessScoreUseCase(testDate)

        // Then
        val expectedStartOfDay = Calendar.getInstance().apply {
            timeInMillis = testDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        assertEquals(expectedStartOfDay, result.date)
    }

    @Test
    fun `invoke should return correct wellness level for sample score`() = runTest {
        // Given - Sample score is 75, which should be BALANCED_USER
        val testDate = System.currentTimeMillis()

        // When
        val result = calculateWellnessScoreUseCase(testDate)

        // Then
        assertEquals(WellnessLevel.BALANCED_USER, result.level)
        assertTrue("Score 75 should be in range for BALANCED_USER", 
                   75 in WellnessLevel.BALANCED_USER.range)
    }

    @Test
    fun `invoke should set calculatedAt to current time`() = runTest {
        // Given
        val testDate = System.currentTimeMillis()
        val beforeCalculation = System.currentTimeMillis()

        // When
        val result = calculateWellnessScoreUseCase(testDate)

        // Then
        val afterCalculation = System.currentTimeMillis()
        assertTrue("calculatedAt should be between before and after timestamps", 
                   result.calculatedAt >= beforeCalculation && result.calculatedAt <= afterCalculation)
    }

    @Test
    fun `invoke should handle different input dates consistently`() = runTest {
        // Given
        val dates = listOf(
            System.currentTimeMillis(),
            System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1),
            System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
        )

        // When & Then
        dates.forEach { date ->
            val result = calculateWellnessScoreUseCase(date)
            
            // All should return the same sample scores but different dates
            assertEquals(75, result.totalScore)
            assertEquals(80, result.timeLimitScore)
            assertEquals(70, result.focusSessionScore)
            assertEquals(75, result.breaksScore)
            assertEquals(75, result.sleepHygieneScore)
            assertEquals(WellnessLevel.BALANCED_USER, result.level)
            
            // Date should be start of the provided day
            val expectedStartOfDay = Calendar.getInstance().apply {
                timeInMillis = date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            
            assertEquals(expectedStartOfDay, result.date)
        }
    }

    @Test
    fun `invoke should handle midnight timestamp correctly`() = runTest {
        // Given - Already at start of day
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.JANUARY, 15, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val midnightDate = calendar.timeInMillis

        // When
        val result = calculateWellnessScoreUseCase(midnightDate)

        // Then
        assertEquals(midnightDate, result.date)
    }

    @Test
    fun `wellness level enum should have correct score ranges`() {
        // Test the WellnessLevel.fromScore method works correctly
        assertEquals(WellnessLevel.DIGITAL_SPROUT, WellnessLevel.fromScore(0))
        assertEquals(WellnessLevel.DIGITAL_SPROUT, WellnessLevel.fromScore(25))
        assertEquals(WellnessLevel.MINDFUL_EXPLORER, WellnessLevel.fromScore(26))
        assertEquals(WellnessLevel.MINDFUL_EXPLORER, WellnessLevel.fromScore(50))
        assertEquals(WellnessLevel.BALANCED_USER, WellnessLevel.fromScore(51))
        assertEquals(WellnessLevel.BALANCED_USER, WellnessLevel.fromScore(75))
        assertEquals(WellnessLevel.WELLNESS_MASTER, WellnessLevel.fromScore(76))
        assertEquals(WellnessLevel.WELLNESS_MASTER, WellnessLevel.fromScore(100))
        
        // Test edge case - negative score defaults to DIGITAL_SPROUT
        assertEquals(WellnessLevel.DIGITAL_SPROUT, WellnessLevel.fromScore(-1))
        // Test edge case - score above 100 defaults to DIGITAL_SPROUT
        assertEquals(WellnessLevel.DIGITAL_SPROUT, WellnessLevel.fromScore(101))
    }
    
    // Helper methods for test setup
    
    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    private suspend fun setupMockForPerfectWellnessDay(startOfDay: Long) {
        // Perfect goal adherence - under daily limit
        val perfectGoal = UserGoal(
            goalType = "daily_screen_time", 
            targetValue = TimeUnit.HOURS.toMillis(2),
            isActive = true
        )
        whenever(mockRepository.getActiveGoals()).thenReturn(flowOf(listOf(perfectGoal)))
        
        // Light usage - 1 hour total
        val lightUsageData = listOf(
            AppSessionDataAggregate("com.example.app", TimeUnit.HOURS.toMillis(1), 2)
        )
        whenever(mockRepository.getAggregatedSessionDataForDayFlow(any(), any())).thenReturn(flowOf(lightUsageData))
        
        // Perfect focus sessions - 3 successful
        val perfectFocusSessions = listOf(
            FocusSession(
                startTime = startOfDay + TimeUnit.HOURS.toMillis(2),
                endTime = startOfDay + TimeUnit.HOURS.toMillis(2) + TimeUnit.MINUTES.toMillis(25),
                targetDurationMillis = TimeUnit.MINUTES.toMillis(25),
                actualDurationMillis = TimeUnit.MINUTES.toMillis(25),
                appsBlocked = "",
                wasSuccessful = true
            ),
            FocusSession(
                startTime = startOfDay + TimeUnit.HOURS.toMillis(6),
                endTime = startOfDay + TimeUnit.HOURS.toMillis(6) + TimeUnit.MINUTES.toMillis(25),
                targetDurationMillis = TimeUnit.MINUTES.toMillis(25),
                actualDurationMillis = TimeUnit.MINUTES.toMillis(25),
                appsBlocked = "",
                wasSuccessful = true
            ),
            FocusSession(
                startTime = startOfDay + TimeUnit.HOURS.toMillis(10),
                endTime = startOfDay + TimeUnit.HOURS.toMillis(10) + TimeUnit.MINUTES.toMillis(25),
                targetDurationMillis = TimeUnit.MINUTES.toMillis(25),
                actualDurationMillis = TimeUnit.MINUTES.toMillis(25),
                appsBlocked = "",
                wasSuccessful = true
            )
        )
        whenever(mockRepository.getFocusSessionsForDate(startOfDay)).thenReturn(perfectFocusSessions)
        
        // Healthy breaks - low unlock frequency
        whenever(mockRepository.getUnlockCountForDayFlow(any(), any())).thenReturn(flowOf(20))
        
        // No evening/bedtime usage - perfect sleep hygiene
        whenever(mockRepository.getAllSessionsInRange(any(), any())).thenReturn(flowOf(emptyList()))
    }
    
    private suspend fun setupMockForPoorWellnessDay(startOfDay: Long) {
        // User has goals but exceeds them significantly
        val exceededGoal = UserGoal(
            goalType = "daily_screen_time",
            targetValue = TimeUnit.HOURS.toMillis(2), // 2 hour limit
            isActive = true
        )
        whenever(mockRepository.getActiveGoals()).thenReturn(flowOf(listOf(exceededGoal)))
        
        // Heavy usage - 6 hours (3x over limit)
        val heavyUsageData = listOf(
            AppSessionDataAggregate("com.social.app", TimeUnit.HOURS.toMillis(6), 20)
        )
        whenever(mockRepository.getAggregatedSessionDataForDayFlow(any(), any())).thenReturn(flowOf(heavyUsageData))
        
        // No focus sessions attempted
        whenever(mockRepository.getFocusSessionsForDate(startOfDay)).thenReturn(emptyList())
        
        // Very high unlock frequency (compulsive checking)
        whenever(mockRepository.getUnlockCountForDayFlow(any(), any())).thenReturn(flowOf(200))
        
        // Heavy bedtime usage - poor sleep hygiene
        val bedtimeSessions = listOf(
            AppSessionEvent(
                packageName = "com.social.app",
                startTimeMillis = startOfDay + TimeUnit.HOURS.toMillis(23), // 11 PM
                endTimeMillis = startOfDay + TimeUnit.HOURS.toMillis(25), // 1 AM next day
                durationMillis = TimeUnit.HOURS.toMillis(2) // 2 hours of bedtime usage
            )
        )
        whenever(mockRepository.getAllSessionsInRange(any(), any())).thenReturn(flowOf(bedtimeSessions))
    }
    
    private suspend fun setupNeutralMocksExceptTimeLimits(startOfDay: Long) {
        // Neutral focus sessions
        whenever(mockRepository.getFocusSessionsForDate(startOfDay)).thenReturn(emptyList())
        
        // Neutral breaks
        whenever(mockRepository.getUnlockCountForDayFlow(any(), any())).thenReturn(flowOf(50))
        
        // Neutral sleep hygiene
        whenever(mockRepository.getAllSessionsInRange(any(), any())).thenReturn(flowOf(emptyList()))
    }
    
    private suspend fun setupNeutralMocksExceptFocus(startOfDay: Long) {
        // Neutral goals - no daily screen time goals
        whenever(mockRepository.getActiveGoals()).thenReturn(flowOf(emptyList()))
        
        // Neutral usage
        whenever(mockRepository.getAggregatedSessionDataForDayFlow(any(), any())).thenReturn(flowOf(emptyList()))
        
        // Neutral breaks
        whenever(mockRepository.getUnlockCountForDayFlow(any(), any())).thenReturn(flowOf(50))
        
        // Neutral sleep hygiene
        whenever(mockRepository.getAllSessionsInRange(any(), any())).thenReturn(flowOf(emptyList()))
    }
}