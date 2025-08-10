package com.example.screentimetracker.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import com.example.screentimetracker.domain.usecases.*
import com.example.screentimetracker.ui.dashboard.viewmodels.QuickActionsViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class QuickActionsViewModelTest {

    private val mockFocusSessionManagerUseCase = mockk<FocusSessionManagerUseCase>()
    private val mockCalculateWellnessScoreUseCase = mockk<CalculateWellnessScoreUseCase>()
    private val mockGetDashboardDataUseCase = mockk<GetDashboardDataUseCase>()

    private lateinit var viewModel: QuickActionsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)
        
        // Setup default mock behaviors
        setupDefaultMocks()
        
        viewModel = QuickActionsViewModel(
            focusSessionManagerUseCase = mockFocusSessionManagerUseCase,
            calculateWellnessScoreUseCase = mockCalculateWellnessScoreUseCase,
            getDashboardDataUseCase = mockGetDashboardDataUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun setupDefaultMocks() {
        // Mock usage summary
        val mockUsageData = mockk<DashboardUsageData> {
            every { totalUsageMillis } returns 3600000L // 1 hour
        }
        every { mockGetDashboardDataUseCase.getTodayUsageSummary() } returns flowOf(mockUsageData)
        
        // Mock wellness score
        val mockWellnessScore = mockk<TodayWellnessScore> {
            every { totalScore } returns 75
        }
        every { mockCalculateWellnessScoreUseCase.getTodayWellnessScore() } returns flowOf(mockWellnessScore)
        
        // Mock focus session operations
        coEvery { mockFocusSessionManagerUseCase.endCurrentFocusSession() } just Runs
        coEvery { mockFocusSessionManagerUseCase.startFocusSession(any(), any()) } just Runs
        coEvery { mockCalculateWellnessScoreUseCase.calculateAndStoreTodayScore() } just Runs
    }

    // Test initial state
    @Test
    fun `initial uiState should have default values`() = runTest {
        // When
        val initialState = viewModel.uiState.first()

        // Then
        assertEquals(false, initialState.isFocusModeActive)
        assertEquals(false, initialState.isEmergencyBlockActive)
        assertEquals(0, initialState.activeTimerMinutes)
        assertEquals("1h", initialState.todayUsageHours)
        assertEquals(75, initialState.wellnessScore)
        assertEquals(false, initialState.isBreakReminderActive)
    }

    @Test
    fun `uiState should combine flows correctly`() = runTest {
        // Given
        val mockUsageData = mockk<DashboardUsageData> {
            every { totalUsageMillis } returns 7200000L // 2 hours
        }
        val mockWellnessScore = mockk<TodayWellnessScore> {
            every { totalScore } returns 85
        }
        
        every { mockGetDashboardDataUseCase.getTodayUsageSummary() } returns flowOf(mockUsageData)
        every { mockCalculateWellnessScoreUseCase.getTodayWellnessScore() } returns flowOf(mockWellnessScore)

        // When
        val state = viewModel.uiState.first()

        // Then
        assertEquals("2h", state.todayUsageHours)
        assertEquals(85, state.wellnessScore)
    }

    @Test
    fun `uiState should handle null wellness score gracefully`() = runTest {
        // Given
        every { mockCalculateWellnessScoreUseCase.getTodayWellnessScore() } returns flowOf(null)

        // When
        val state = viewModel.uiState.first()

        // Then
        assertEquals(0, state.wellnessScore)
    }

    // Test focus mode functionality
    @Test
    fun `toggleFocusMode should start focus session when not active`() = runTest {
        // Given - focus mode is initially inactive

        // When
        viewModel.toggleFocusMode()

        // Then
        coVerify { 
            mockFocusSessionManagerUseCase.startFocusSession(
                durationMinutes = 25, // Default pomodoro
                blockedApps = emptyList()
            )
        }
        coVerify(exactly = 0) { mockFocusSessionManagerUseCase.endCurrentFocusSession() }
    }

    @Test
    fun `toggleFocusMode should end focus session when already active`() = runTest {
        // Given - start focus mode first
        viewModel.toggleFocusMode()
        advanceUntilIdle()

        // When - toggle again
        viewModel.toggleFocusMode()
        advanceUntilIdle()

        // Then
        coVerify { mockFocusSessionManagerUseCase.endCurrentFocusSession() }
        coVerify(exactly = 1) { mockFocusSessionManagerUseCase.startFocusSession(any(), any()) }
    }

    @Test
    fun `toggleFocusMode should update uiState correctly when starting`() = runTest {
        // When
        viewModel.toggleFocusMode()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.isFocusModeActive)
    }

    @Test
    fun `toggleFocusMode should update uiState correctly when ending`() = runTest {
        // Given
        viewModel.toggleFocusMode() // Start
        advanceUntilIdle()
        
        // When
        viewModel.toggleFocusMode() // End
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isFocusModeActive)
    }

    // Test emergency block functionality
    @Test
    fun `triggerEmergencyBlock should activate emergency block`() = runTest {
        // When
        viewModel.triggerEmergencyBlock()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.isEmergencyBlockActive)
    }

    @Test
    fun `triggerEmergencyBlock should auto-disable after 30 minutes`() = runTest {
        // When
        viewModel.triggerEmergencyBlock()
        
        // Verify it's initially active
        assertTrue(viewModel.uiState.first().isEmergencyBlockActive)
        
        // Advance time by 30 minutes (30 * 60 * 1000ms)
        advanceTimeBy(30 * 60 * 1000L)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isEmergencyBlockActive)
    }

    @Test
    fun `multiple triggerEmergencyBlock calls should not interfere`() = runTest {
        // When
        viewModel.triggerEmergencyBlock()
        viewModel.triggerEmergencyBlock() // Second call
        
        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.isEmergencyBlockActive)
    }

    // Test activity timer functionality
    @Test
    fun `startActivityTimer should set timer minutes correctly`() = runTest {
        // Given
        val minutes = 15

        // When
        viewModel.startActivityTimer(minutes)

        // Then
        val state = viewModel.uiState.first()
        assertEquals(minutes, state.activeTimerMinutes)
    }

    @Test
    fun `startActivityTimer should countdown correctly`() = runTest {
        // Given
        val minutes = 3

        // When
        viewModel.startActivityTimer(minutes)
        
        // Advance by 1 minute
        advanceTimeBy(60 * 1000L)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertEquals(2, state.activeTimerMinutes) // Should be 2 minutes left
    }

    @Test
    fun `startActivityTimer should complete and reset to zero`() = runTest {
        // Given
        val minutes = 2

        // When
        viewModel.startActivityTimer(minutes)
        
        // Advance by 2 minutes
        advanceTimeBy(2 * 60 * 1000L)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertEquals(0, state.activeTimerMinutes) // Should be completed
    }

    @Test
    fun `startActivityTimer with zero minutes should complete immediately`() = runTest {
        // Given
        val minutes = 0

        // When
        viewModel.startActivityTimer(minutes)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertEquals(0, state.activeTimerMinutes)
    }

    // Test break reminder functionality
    @Test
    fun `startBreakReminder should activate break reminder`() = runTest {
        // When
        viewModel.startBreakReminder()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.isBreakReminderActive)
    }

    @Test
    fun `startBreakReminder should auto-disable after 10 minutes`() = runTest {
        // When
        viewModel.startBreakReminder()
        
        // Verify it's initially active
        assertTrue(viewModel.uiState.first().isBreakReminderActive)
        
        // Advance time by 10 minutes
        advanceTimeBy(10 * 60 * 1000L)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isBreakReminderActive)
    }

    // Test wellness score refresh functionality
    @Test
    fun `refreshWellnessScore should call calculateAndStoreTodayScore`() = runTest {
        // When
        viewModel.refreshWellnessScore()

        // Then
        coVerify { mockCalculateWellnessScoreUseCase.calculateAndStoreTodayScore() }
    }

    @Test
    fun `refreshWellnessScore should handle exceptions gracefully`() = runTest {
        // Given
        coEvery { mockCalculateWellnessScoreUseCase.calculateAndStoreTodayScore() } throws RuntimeException("Network error")

        // When
        viewModel.refreshWellnessScore()

        // Then
        // Should not crash, just verify the call was made
        coVerify { mockCalculateWellnessScoreUseCase.calculateAndStoreTodayScore() }
    }

    // Test usage time formatting functionality
    @Test
    fun `formatUsageTime should format hours correctly`() = runTest {
        // Given - Mock usage data with different time values
        val testCases = mapOf(
            3600000L to "1h", // 1 hour
            7200000L to "2h", // 2 hours
            5400000L to "1h", // 1.5 hours should round down to 1h
            3900000L to "1h"  // 1 hour 5 minutes should show as 1h
        )

        testCases.forEach { (millis, expected) ->
            // Given
            val mockUsageData = mockk<DashboardUsageData> {
                every { totalUsageMillis } returns millis
            }
            every { mockGetDashboardDataUseCase.getTodayUsageSummary() } returns flowOf(mockUsageData)

            // When
            val state = viewModel.uiState.first()

            // Then
            assertEquals("For $millis ms", expected, state.todayUsageHours)
        }
    }

    @Test
    fun `formatUsageTime should format minutes correctly when no hours`() = runTest {
        // Given - Mock usage data with minute values
        val testCases = mapOf(
            300000L to "5m", // 5 minutes
            60000L to "1m",  // 1 minute
            120000L to "2m", // 2 minutes
            0L to "0m"       // 0 minutes
        )

        testCases.forEach { (millis, expected) ->
            // Given
            val mockUsageData = mockk<DashboardUsageData> {
                every { totalUsageMillis } returns millis
            }
            every { mockGetDashboardDataUseCase.getTodayUsageSummary() } returns flowOf(mockUsageData)

            // When
            val state = viewModel.uiState.first()

            // Then
            assertEquals("For $millis ms", expected, state.todayUsageHours)
        }
    }

    @Test
    fun `formatUsageTime should handle very small values`() = runTest {
        // Given
        val mockUsageData = mockk<DashboardUsageData> {
            every { totalUsageMillis } returns 500L // Less than a minute
        }
        every { mockGetDashboardDataUseCase.getTodayUsageSummary() } returns flowOf(mockUsageData)

        // When
        val state = viewModel.uiState.first()

        // Then
        assertEquals("0m", state.todayUsageHours)
    }

    // Test concurrent operations
    @Test
    fun `multiple simultaneous operations should not interfere`() = runTest {
        // When - trigger multiple operations simultaneously
        viewModel.toggleFocusMode()
        viewModel.triggerEmergencyBlock()
        viewModel.startActivityTimer(10)
        viewModel.startBreakReminder()
        viewModel.refreshWellnessScore()

        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.isFocusModeActive)
        assertTrue(state.isEmergencyBlockActive)
        assertEquals(10, state.activeTimerMinutes)
        assertTrue(state.isBreakReminderActive)

        // Verify all use case calls were made
        coVerify { mockFocusSessionManagerUseCase.startFocusSession(any(), any()) }
        coVerify { mockCalculateWellnessScoreUseCase.calculateAndStoreTodayScore() }
    }

    // Test edge cases
    @Test
    fun `startActivityTimer with negative minutes should handle gracefully`() = runTest {
        // Given
        val minutes = -5

        // When
        viewModel.startActivityTimer(minutes)

        // Then
        val state = viewModel.uiState.first()
        assertEquals(minutes, state.activeTimerMinutes) // Should accept the value as-is
    }

    @Test
    fun `startActivityTimer with very large value should handle gracefully`() = runTest {
        // Given
        val minutes = Int.MAX_VALUE

        // When
        viewModel.startActivityTimer(minutes)

        // Then
        val state = viewModel.uiState.first()
        assertEquals(minutes, state.activeTimerMinutes)
    }

    @Test
    fun `focus mode toggle during emergency block should work independently`() = runTest {
        // Given
        viewModel.triggerEmergencyBlock()
        advanceUntilIdle()

        // When
        viewModel.toggleFocusMode()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.isEmergencyBlockActive)
        assertTrue(state.isFocusModeActive)
    }

    @Test
    fun `timer operations should work during other active states`() = runTest {
        // Given
        viewModel.toggleFocusMode()
        viewModel.triggerEmergencyBlock()
        advanceUntilIdle()

        // When
        viewModel.startActivityTimer(5)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.isFocusModeActive)
        assertTrue(state.isEmergencyBlockActive)
        assertEquals(5, state.activeTimerMinutes)
    }

    // Test error handling
    @Test
    fun `focus session start failure should not crash`() = runTest {
        // Given
        coEvery { mockFocusSessionManagerUseCase.startFocusSession(any(), any()) } throws RuntimeException("Focus start failed")

        // When
        viewModel.toggleFocusMode()
        advanceUntilIdle()

        // Then - Should not crash and state should still update
        val state = viewModel.uiState.first()
        assertTrue(state.isFocusModeActive) // State should still be updated
    }

    @Test
    fun `focus session end failure should not crash`() = runTest {
        // Given
        viewModel.toggleFocusMode() // Start first
        advanceUntilIdle()
        coEvery { mockFocusSessionManagerUseCase.endCurrentFocusSession() } throws RuntimeException("Focus end failed")

        // When
        viewModel.toggleFocusMode() // Try to end
        advanceUntilIdle()

        // Then - Should not crash
        val state = viewModel.uiState.first()
        assertFalse(state.isFocusModeActive) // State should still be updated
    }

    // Mock data classes for testing
    private interface DashboardUsageData {
        val totalUsageMillis: Long
    }

    private interface TodayWellnessScore {
        val totalScore: Int
    }
}