package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.domain.model.WellnessLevel
import com.example.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
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
    fun `invoke should return wellness score for given date`() = runTest {
        // Given
        val testDate = System.currentTimeMillis()

        // When
        val result = calculateWellnessScoreUseCase(testDate)

        // Then
        assertEquals(75, result.totalScore)
        assertEquals(80, result.timeLimitScore)
        assertEquals(70, result.focusSessionScore)
        assertEquals(75, result.breaksScore)
        assertEquals(75, result.sleepHygieneScore)
        assertEquals(WellnessLevel.BALANCED_USER, result.level)
        assertTrue(result.calculatedAt > 0)
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
}