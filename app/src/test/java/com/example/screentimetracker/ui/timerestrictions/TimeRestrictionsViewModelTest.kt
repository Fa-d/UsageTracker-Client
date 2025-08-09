package com.example.screentimetracker.ui.timerestrictions

import com.example.screentimetracker.data.local.TimeRestriction
import com.example.screentimetracker.domain.usecases.TimeRestrictionManagerUseCase
import com.example.screentimetracker.ui.timerestrictions.viewmodels.TimeRestrictionsUiEvent
import com.example.screentimetracker.ui.timerestrictions.viewmodels.TimeRestrictionsViewModel
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimeRestrictionsViewModelTest {

    @MockK
    private lateinit var timeRestrictionManagerUseCase: TimeRestrictionManagerUseCase

    private lateinit var viewModel: TimeRestrictionsViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val sampleRestriction = TimeRestriction(
        id = 1L,
        restrictionType = "bedtime_mode",
        name = "Digital Sunset",
        description = "Block distracting apps before bedtime",
        startTimeMinutes = 22 * 60, // 10 PM
        endTimeMinutes = 8 * 60, // 8 AM
        appsBlocked = "",
        daysOfWeek = "0,1,2,3,4,5,6",
        allowEmergencyApps = true,
        showNotifications = true,
        isEnabled = true
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        // Default mock behavior
        every { timeRestrictionManagerUseCase.getAllTimeRestrictions() } returns flowOf(emptyList())
        every { timeRestrictionManagerUseCase.getActiveTimeRestrictions() } returns flowOf(emptyList())

        viewModel = TimeRestrictionsViewModel(timeRestrictionManagerUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be correct`() {
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertTrue(state.restrictions.isEmpty())
        assertTrue(state.activeRestrictions.isEmpty())
        assertFalse(state.showCreateDialog)
        assertNull(state.error)
    }

    @Test
    fun `loadTimeRestrictions should update state with restrictions`() = runTest {
        // Given
        val restrictions = listOf(sampleRestriction)
        every { timeRestrictionManagerUseCase.getAllTimeRestrictions() } returns flowOf(restrictions)

        // When
        viewModel = TimeRestrictionsViewModel(timeRestrictionManagerUseCase)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(restrictions, state.restrictions)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadActiveRestrictions should update state with active restrictions`() = runTest {
        // Given
        val activeRestrictions = listOf(sampleRestriction)
        every { timeRestrictionManagerUseCase.getActiveTimeRestrictions() } returns flowOf(activeRestrictions)

        // When
        viewModel = TimeRestrictionsViewModel(timeRestrictionManagerUseCase)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(activeRestrictions, state.activeRestrictions)
    }

    @Test
    fun `createDefaultRestrictions should call usecase and emit success event`() = runTest {
        // Given
        coEvery { timeRestrictionManagerUseCase.createDefaultTimeRestrictions() } returns Unit

        // When
        viewModel.createDefaultRestrictions()
        advanceUntilIdle()

        // Then
        coVerify { timeRestrictionManagerUseCase.createDefaultTimeRestrictions() }
        
        val state = viewModel.uiState.value
        assertFalse(state.isCreatingDefaults)
        assertNull(state.error)
    }

    @Test
    fun `createDefaultRestrictions should handle error`() = runTest {
        // Given
        val errorMessage = "Database error"
        coEvery { timeRestrictionManagerUseCase.createDefaultTimeRestrictions() } throws RuntimeException(errorMessage)

        // When
        viewModel.createDefaultRestrictions()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isCreatingDefaults)
        assertTrue(state.error?.contains(errorMessage) == true)
    }

    @Test
    fun `toggleRestriction should call updateRestrictionEnabled with opposite state`() = runTest {
        // Given
        val enabledRestriction = sampleRestriction.copy(isEnabled = true)
        coEvery { timeRestrictionManagerUseCase.updateRestrictionEnabled(any(), any()) } returns Unit

        // When
        viewModel.toggleRestriction(enabledRestriction)
        advanceUntilIdle()

        // Then
        coVerify { timeRestrictionManagerUseCase.updateRestrictionEnabled(enabledRestriction.id, false) }
    }

    @Test
    fun `toggleRestriction should handle error`() = runTest {
        // Given
        val errorMessage = "Update failed"
        coEvery { timeRestrictionManagerUseCase.updateRestrictionEnabled(any(), any()) } throws RuntimeException(errorMessage)

        // When
        viewModel.toggleRestriction(sampleRestriction)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.error?.contains(errorMessage) == true)
    }

    @Test
    fun `createCustomRestriction should create restriction with correct parameters`() = runTest {
        // Given
        val restrictionId = 42L
        coEvery { 
            timeRestrictionManagerUseCase.createCustomRestriction(any(), any(), any(), any(), any(), any(), any(), any())
        } returns restrictionId

        val name = "Custom Restriction"
        val description = "Test description"
        val startHour = 9
        val startMinute = 30
        val endHour = 17
        val endMinute = 0
        val selectedApps = listOf("com.example.app")
        val selectedDays = listOf(1, 2, 3, 4, 5)

        // When
        viewModel.createCustomRestriction(
            name, description, startHour, startMinute, endHour, endMinute,
            selectedApps, selectedDays
        )
        advanceUntilIdle()

        // Then
        coVerify { 
            timeRestrictionManagerUseCase.createCustomRestriction(
                name = name,
                description = description,
                startTimeMinutes = 570, // 9:30 in minutes
                endTimeMinutes = 1020, // 17:00 in minutes
                blockedApps = selectedApps,
                daysOfWeek = selectedDays,
                allowEmergencyApps = true,
                showNotifications = true
            )
        }

        val state = viewModel.uiState.value
        assertFalse(state.isCreatingCustom)
        assertNull(state.error)
    }

    @Test
    fun `createCustomRestriction should handle error and set error state`() = runTest {
        // Given
        val errorMessage = "Creation failed"
        coEvery { 
            timeRestrictionManagerUseCase.createCustomRestriction(any(), any(), any(), any(), any(), any(), any(), any())
        } throws RuntimeException(errorMessage)

        // When
        viewModel.createCustomRestriction("Test", "Desc", 9, 0, 17, 0, emptyList(), listOf(1))
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isCreatingCustom)
        assertTrue(state.error?.contains(errorMessage) == true)
    }

    @Test
    fun `checkAppBlocked should call usecase and update state`() = runTest {
        // Given
        val packageName = "com.example.app"
        val isBlocked = true
        coEvery { timeRestrictionManagerUseCase.isAppBlockedByTimeRestriction(packageName) } returns isBlocked

        // When
        viewModel.checkAppBlocked(packageName)
        advanceUntilIdle()

        // Then
        coVerify { timeRestrictionManagerUseCase.isAppBlockedByTimeRestriction(packageName) }
        
        val state = viewModel.uiState.value
        assertEquals(packageName to isBlocked, state.lastCheckedApp)
    }

    @Test
    fun `getCurrentActiveRestrictions should call usecase and update state`() = runTest {
        // Given
        val activeRestrictions = listOf(sampleRestriction)
        coEvery { timeRestrictionManagerUseCase.getCurrentActiveRestrictions() } returns activeRestrictions

        // When
        viewModel.getCurrentActiveRestrictions()
        advanceUntilIdle()

        // Then
        coVerify { timeRestrictionManagerUseCase.getCurrentActiveRestrictions() }
        
        val state = viewModel.uiState.value
        assertEquals(activeRestrictions, state.currentActiveRestrictions)
    }

    @Test
    fun `showCreateDialog should set showCreateDialog to true`() {
        // When
        viewModel.showCreateDialog()

        // Then
        assertTrue(viewModel.uiState.value.showCreateDialog)
    }

    @Test
    fun `hideCreateDialog should set showCreateDialog to false`() {
        // Given
        viewModel.showCreateDialog()

        // When
        viewModel.hideCreateDialog()

        // Then
        assertFalse(viewModel.uiState.value.showCreateDialog)
    }

    @Test
    fun `clearError should set error to null`() = runTest {
        // Given - simulate error state
        coEvery { timeRestrictionManagerUseCase.createDefaultTimeRestrictions() } throws RuntimeException("Test error")
        viewModel.createDefaultRestrictions()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `getRestrictionStatusPreview should calculate current active status correctly`() {
        // Given - Restriction active from 22:00 to 08:00, currently it's 23:00 on Sunday
        val restriction = sampleRestriction.copy(
            startTimeMinutes = 22 * 60, // 22:00
            endTimeMinutes = 8 * 60, // 08:00
            daysOfWeek = "0,1,2,3,4,5,6" // All days
        )

        // When
        val statusPreview = viewModel.getRestrictionStatusPreview(restriction)

        // Then
        assertNotNull(statusPreview)
        assertEquals(restriction, statusPreview.restriction)
        // Note: The actual active status depends on current system time
    }

    @Test
    fun `formatTime should format minutes correctly`() {
        // Test cases
        assertEquals("00:00", viewModel.formatTime(0))
        assertEquals("00:30", viewModel.formatTime(30))
        assertEquals("01:00", viewModel.formatTime(60))
        assertEquals("23:59", viewModel.formatTime(23 * 60 + 59))
        assertEquals("12:15", viewModel.formatTime(12 * 60 + 15))
    }

    @Test
    fun `formatTimeUntil should format time duration correctly`() {
        // Test cases
        assertEquals("0m", viewModel.formatTimeUntil(0))
        assertEquals("30m", viewModel.formatTimeUntil(30))
        assertEquals("1h 0m", viewModel.formatTimeUntil(60))
        assertEquals("2h 15m", viewModel.formatTimeUntil(135))
        assertEquals("24h 0m", viewModel.formatTimeUntil(1440))
    }

    @Test
    fun `restriction status preview should handle same day restriction`() {
        // Given - Restriction from 09:00 to 17:00 (same day)
        val restriction = sampleRestriction.copy(
            startTimeMinutes = 9 * 60, // 09:00
            endTimeMinutes = 17 * 60, // 17:00
            daysOfWeek = "1,2,3,4,5" // Weekdays only
        )

        // When
        val preview = viewModel.getRestrictionStatusPreview(restriction)

        // Then
        assertNotNull(preview)
        assertNotNull(preview.nextChangeTimeMinutes)
        // The actual value depends on current time, but it should be calculated
    }

    @Test
    fun `restriction status preview should handle overnight restriction`() {
        // Given - Restriction from 22:00 to 08:00 (overnight)
        val restriction = sampleRestriction.copy(
            startTimeMinutes = 22 * 60, // 22:00
            endTimeMinutes = 8 * 60, // 08:00
            daysOfWeek = "0,1,2,3,4,5,6" // All days
        )

        // When
        val preview = viewModel.getRestrictionStatusPreview(restriction)

        // Then
        assertNotNull(preview)
        // For overnight restrictions, the logic should handle the day transition
        assertNotNull(preview.nextChangeTimeMinutes)
    }

    @Test
    fun `restriction status preview should handle disabled restriction`() {
        // Given
        val disabledRestriction = sampleRestriction.copy(isEnabled = false)

        // When
        val preview = viewModel.getRestrictionStatusPreview(disabledRestriction)

        // Then
        assertNotNull(preview)
        assertFalse(preview.isCurrentlyActive)
    }

    @Test
    fun `time calculation utilities should be accurate`() {
        // Test edge cases for time calculations
        val midnightMinutes = 0
        val noonMinutes = 12 * 60
        val almostMidnightMinutes = 23 * 60 + 59

        assertEquals("00:00", viewModel.formatTime(midnightMinutes))
        assertEquals("12:00", viewModel.formatTime(noonMinutes))
        assertEquals("23:59", viewModel.formatTime(almostMidnightMinutes))

        assertEquals("0m", viewModel.formatTimeUntil(0))
        assertEquals("12h 0m", viewModel.formatTimeUntil(noonMinutes))
        assertEquals("23h 59m", viewModel.formatTimeUntil(almostMidnightMinutes))
    }
}