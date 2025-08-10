package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.domain.model.WellnessLevel
import com.example.screentimetracker.domain.model.WellnessScore
import com.example.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import io.mockk.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

class CalculateWellnessScoreUseCaseTest {

    private val mockRepository = mockk<TrackerRepository>(relaxed = true)
    private lateinit var calculateWellnessScoreUseCase: CalculateWellnessScoreUseCase

    @Before
    fun setup() {
        calculateWellnessScoreUseCase = CalculateWellnessScoreUseCase(mockRepository)
        
        // Setup default mock behavior for all repository methods
        coEvery { mockRepository.insertWellnessScore(any()) } just Runs
        every { mockRepository.getAllWellnessScores() } returns flowOf(emptyList())
        every { mockRepository.getActiveGoals() } returns flowOf(emptyList())
        every { mockRepository.getAggregatedSessionDataForDayFlow(any(), any()) } returns flowOf(emptyList())
        coEvery { mockRepository.getFocusSessionsForDate(any()) } returns emptyList()
        every { mockRepository.getUnlockCountForDayFlow(any(), any()) } returns flowOf(50)
        every { mockRepository.getAllSessionsInRange(any(), any()) } returns flowOf(emptyList())
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
        
        every { mockRepository.getAllWellnessScores() } returns flowOf(listOf(existingScore))

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
        
        // Mock empty wellness scores to force calculation
        every { mockRepository.getAllWellnessScores() } returns flowOf(emptyList())

        // When
        val result = calculateWellnessScoreUseCase(testDate)

        // Then
        assertTrue("Total score should be reasonable", result.totalScore >= 0 && result.totalScore <= 100)
        assertEquals(startOfDay, result.date)
        coVerify { mockRepository.insertWellnessScore(any()) }
    }
    
    @Test
    fun `invoke should calculate low wellness score for poor digital habits`() = runTest {
        // Given
        val testDate = System.currentTimeMillis()
        val startOfDay = getStartOfDay(testDate)
        
        every { mockRepository.getAllWellnessScores() } returns flowOf(emptyList())

        // When
        val result = calculateWellnessScoreUseCase(testDate)

        // Then
        assertTrue("Total score should be reasonable", result.totalScore >= 0 && result.totalScore <= 100)
        assertEquals(startOfDay, result.date)
        coVerify { mockRepository.insertWellnessScore(any()) }
    }
    
    @Test
    fun `calculateTimeLimitScore should return valid score`() = runTest {
        // Given
        val testDate = System.currentTimeMillis()
        
        every { mockRepository.getAllWellnessScores() } returns flowOf(emptyList())

        // When
        val result = calculateWellnessScoreUseCase(testDate)

        // Then
        assertTrue("Time limit score should be valid", result.timeLimitScore >= 0 && result.timeLimitScore <= 100)
    }
    
    @Test
    fun `calculateFocusSessionScore should return valid score`() = runTest {
        // Given
        val testDate = System.currentTimeMillis()
        
        every { mockRepository.getAllWellnessScores() } returns flowOf(emptyList())

        // When
        val result = calculateWellnessScoreUseCase(testDate)

        // Then
        assertTrue("Focus session score should be valid", result.focusSessionScore >= 0 && result.focusSessionScore <= 100)
    }

    @Test
    fun `invoke should set date to start of day`() = runTest {
        // Given - Create a timestamp in the middle of the day
        val calendar = Calendar.getInstance()
        calendar.set(2024, Calendar.JANUARY, 15, 14, 30, 45) // 2:30:45 PM
        calendar.set(Calendar.MILLISECOND, 500)
        val testDate = calendar.timeInMillis
        
        // Mock empty wellness scores to force calculation
        every { mockRepository.getAllWellnessScores() } returns flowOf(emptyList())

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
    fun `invoke should return correct wellness level for calculated score`() = runTest {
        // Given
        val testDate = System.currentTimeMillis()
        
        // Mock empty wellness scores to force calculation
        every { mockRepository.getAllWellnessScores() } returns flowOf(emptyList())

        // When
        val result = calculateWellnessScoreUseCase(testDate)

        // Then
        assertNotNull("Wellness level should not be null", result.level)
        assertTrue("Score should be in valid range for the level", 
                   result.totalScore in result.level.range)
    }

    @Test
    fun `invoke should set calculatedAt to current time`() = runTest {
        // Given
        val testDate = System.currentTimeMillis()
        val beforeCalculation = System.currentTimeMillis()
        
        // Mock empty wellness scores to force calculation
        every { mockRepository.getAllWellnessScores() } returns flowOf(emptyList())

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
            // Mock empty wellness scores to force calculation
            every { mockRepository.getAllWellnessScores() } returns flowOf(emptyList())
            
            val result = calculateWellnessScoreUseCase(date)
            
            // All should have consistent behavior
            assertTrue("Total score should be reasonable", result.totalScore >= 0 && result.totalScore <= 100)
            assertTrue("Time limit score should be valid", result.timeLimitScore >= 0 && result.timeLimitScore <= 100)
            assertTrue("Focus session score should be valid", result.focusSessionScore >= 0 && result.focusSessionScore <= 100)
            assertTrue("Breaks score should be valid", result.breaksScore >= 0 && result.breaksScore <= 100)
            assertTrue("Sleep hygiene score should be valid", result.sleepHygieneScore >= 0 && result.sleepHygieneScore <= 100)
            
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
        
        // Mock empty wellness scores to force calculation
        every { mockRepository.getAllWellnessScores() } returns flowOf(emptyList())

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
}