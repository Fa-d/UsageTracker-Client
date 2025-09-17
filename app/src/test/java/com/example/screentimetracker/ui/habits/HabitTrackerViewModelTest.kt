package dev.sadakat.screentimetracker.ui.habits

import dev.sadakat.screentimetracker.data.local.entities.HabitTracker
import dev.sadakat.screentimetracker.domain.usecases.HabitTrackerUseCase
import dev.sadakat.screentimetracker.ui.habits.viewmodels.HabitTrackerViewModel
import dev.sadakat.screentimetracker.ui.habits.viewmodels.HabitUiState
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
class HabitTrackerViewModelTest {

    private val mockHabitTrackerUseCase = mockk<HabitTrackerUseCase>()
    private lateinit var viewModel: HabitTrackerViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleHabits = listOf(
        HabitTracker(
            id = 1,
            habitId = "morning_no_phone",
            habitName = "Morning Phone-Free",
            description = "No phone for first hour after waking up",
            emoji = "🌅",
            date = System.currentTimeMillis(),
            isCompleted = false,
            currentStreak = 3,
            bestStreak = 5
        ),
        HabitTracker(
            id = 2,
            habitId = "bedtime_routine",
            habitName = "Digital Sunset",
            description = "No screens 1 hour before bed",
            emoji = "🌙",
            date = System.currentTimeMillis(),
            isCompleted = true,
            currentStreak = 7,
            bestStreak = 10
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)
        
        setupDefaultMocks()
        
        viewModel = HabitTrackerViewModel(mockHabitTrackerUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun setupDefaultMocks() {
        every { mockHabitTrackerUseCase.getTodaysHabits() } returns flowOf(sampleHabits)
        coEvery { mockHabitTrackerUseCase.initializeDigitalWellnessHabits() } just Runs
        coEvery { mockHabitTrackerUseCase.checkAndCompleteHabitsAutomatically() } just Runs
        coEvery { mockHabitTrackerUseCase.completeHabit(any()) } returns true
        coEvery { mockHabitTrackerUseCase.createCustomHabit(any(), any(), any()) } returns 1L
        coEvery { mockHabitTrackerUseCase.resetHabitStreak(any()) } returns true
        
        val mockStats = HabitTrackerUseCase.HabitStats(
            habitId = "morning_no_phone",
            habitName = "Morning Phone-Free",
            completedDays = 15,
            totalTrackedDays = 20,
            completionRate = 75f,
            currentStreak = 3,
            bestStreak = 5
        )
        coEvery { mockHabitTrackerUseCase.getHabitStats(any(), any()) } returns mockStats
    }

    @Test
    fun `initial uiState should have correct default values`() = runTest {
        // When
        val initialState = viewModel.uiState.first()

        // Then
        assertFalse(initialState.isLoading)
        assertEquals(sampleHabits, initialState.todaysHabits)
        assertNull(initialState.completionCelebration)
        assertNull(initialState.error)
        assertFalse(initialState.showCreateDialog)
        assertTrue(initialState.habitStats.isEmpty())
    }

    @Test
    fun `initialization should call initializeDigitalWellnessHabits`() = runTest {
        // Then
        coVerify { mockHabitTrackerUseCase.initializeDigitalWellnessHabits() }
    }

    @Test
    fun `completeHabit should call use case and show celebration`() = runTest {
        // Given
        val habitId = "morning_no_phone"
        val incompletedHabit = sampleHabits.first { !it.isCompleted }

        // When
        viewModel.completeHabit(habitId)
        advanceUntilIdle()

        // Then
        coVerify { mockHabitTrackerUseCase.completeHabit(habitId) }
        
        val state = viewModel.uiState.first()
        assertNotNull(state.completionCelebration)
        assertEquals(incompletedHabit.habitName, state.completionCelebration?.habitName)
        assertEquals(incompletedHabit.currentStreak + 1, state.completionCelebration?.streak)
    }

    @Test
    fun `completeHabit should not complete already completed habit`() = runTest {
        // Given
        val completedHabitId = "bedtime_routine"

        // When
        viewModel.completeHabit(completedHabitId)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { mockHabitTrackerUseCase.completeHabit(completedHabitId) }
        
        val state = viewModel.uiState.first()
        assertNull(state.completionCelebration)
    }

    @Test
    fun `completeHabit should show new record celebration when appropriate`() = runTest {
        // Given
        val habitWithLowBestStreak = sampleHabits.first { !it.isCompleted }.copy(
            currentStreak = 4,
            bestStreak = 4
        )
        val habitsWithLowBest = listOf(habitWithLowBestStreak)
        every { mockHabitTrackerUseCase.getTodaysHabits() } returns flowOf(habitsWithLowBest)
        
        // Recreate viewModel to get updated habits
        viewModel = HabitTrackerViewModel(mockHabitTrackerUseCase)

        // When
        viewModel.completeHabit(habitWithLowBestStreak.habitId)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertNotNull(state.completionCelebration)
        assertTrue(state.completionCelebration?.isNewRecord == true)
    }

    @Test
    fun `dismissCelebration should clear celebration state`() = runTest {
        // Given - complete a habit first to show celebration
        viewModel.completeHabit("morning_no_phone")
        advanceUntilIdle()
        
        // Verify celebration is shown
        assertNotNull(viewModel.uiState.first().completionCelebration)

        // When
        viewModel.dismissCelebration()

        // Then
        val state = viewModel.uiState.first()
        assertNull(state.completionCelebration)
    }

    @Test
    fun `loadHabitStats should fetch and store stats`() = runTest {
        // Given
        val habitId = "morning_no_phone"
        val days = 30

        // When
        viewModel.loadHabitStats(habitId, days)
        advanceUntilIdle()

        // Then
        coVerify { mockHabitTrackerUseCase.getHabitStats(habitId, days) }
        
        val state = viewModel.uiState.first()
        assertTrue(state.habitStats.containsKey(habitId))
        assertEquals("Morning Phone-Free", state.habitStats[habitId]?.habitName)
    }

    @Test
    fun `showCreateDialog should set dialog state to true`() = runTest {
        // When
        viewModel.showCreateDialog()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.showCreateDialog)
    }

    @Test
    fun `hideCreateDialog should set dialog state to false and clear data`() = runTest {
        // Given
        viewModel.showCreateDialog()
        viewModel.updateCustomHabitName("Test Habit")
        
        // When
        viewModel.hideCreateDialog()

        // Then
        val uiState = viewModel.uiState.first()
        val customHabitData = viewModel.customHabitData.first()
        
        assertFalse(uiState.showCreateDialog)
        assertEquals("", customHabitData.name)
        assertEquals("", customHabitData.description)
        assertEquals("✨", customHabitData.emoji)
    }

    @Test
    fun `updateCustomHabitName should update habit name`() = runTest {
        // Given
        val habitName = "Test Habit"

        // When
        viewModel.updateCustomHabitName(habitName)

        // Then
        val customHabitData = viewModel.customHabitData.first()
        assertEquals(habitName, customHabitData.name)
    }

    @Test
    fun `updateCustomHabitDescription should update habit description`() = runTest {
        // Given
        val description = "This is a test habit"

        // When
        viewModel.updateCustomHabitDescription(description)

        // Then
        val customHabitData = viewModel.customHabitData.first()
        assertEquals(description, customHabitData.description)
    }

    @Test
    fun `updateCustomHabitEmoji should update habit emoji`() = runTest {
        // Given
        val emoji = "🎯"

        // When
        viewModel.updateCustomHabitEmoji(emoji)

        // Then
        val customHabitData = viewModel.customHabitData.first()
        assertEquals(emoji, customHabitData.emoji)
    }

    @Test
    fun `createCustomHabit should call use case when name is not blank`() = runTest {
        // Given
        viewModel.updateCustomHabitName("Reading")
        viewModel.updateCustomHabitDescription("Read 30 minutes daily")
        viewModel.updateCustomHabitEmoji("📚")
        viewModel.showCreateDialog()

        // When
        viewModel.createCustomHabit()
        advanceUntilIdle()

        // Then
        coVerify { 
            mockHabitTrackerUseCase.createCustomHabit(
                habitName = "Reading",
                description = "Read 30 minutes daily",
                emoji = "📚"
            )
        }
        
        val state = viewModel.uiState.first()
        assertFalse(state.showCreateDialog)
    }

    @Test
    fun `createCustomHabit should show error when name is blank`() = runTest {
        // Given
        viewModel.updateCustomHabitName("")

        // When
        viewModel.createCustomHabit()

        // Then
        coVerify(exactly = 0) { mockHabitTrackerUseCase.createCustomHabit(any(), any(), any()) }
        
        val state = viewModel.uiState.first()
        assertEquals("Habit name cannot be empty", state.error)
    }

    @Test
    fun `getTodayProgress should return correct completed and total counts`() = runTest {
        // When
        val (completed, total) = viewModel.getTodayProgress()

        // Then
        assertEquals(1, completed) // One habit is completed
        assertEquals(2, total)     // Two habits total
    }

    @Test
    fun `getTodayProgress should handle empty habits list`() = runTest {
        // Given
        every { mockHabitTrackerUseCase.getTodaysHabits() } returns flowOf(emptyList())
        viewModel = HabitTrackerViewModel(mockHabitTrackerUseCase)

        // When
        val (completed, total) = viewModel.getTodayProgress()

        // Then
        assertEquals(0, completed)
        assertEquals(0, total)
    }

    @Test
    fun `clearError should reset error state`() = runTest {
        // Given - trigger an error first
        viewModel.createCustomHabit() // This will cause error due to empty name

        // Verify error exists
        assertNotNull(viewModel.uiState.first().error)

        // When
        viewModel.clearError()

        // Then
        val state = viewModel.uiState.first()
        assertNull(state.error)
    }

    @Test
    fun `resetHabitStreak should call use case`() = runTest {
        // Given
        val habitId = "morning_no_phone"

        // When
        viewModel.resetHabitStreak(habitId)
        advanceUntilIdle()

        // Then
        coVerify { mockHabitTrackerUseCase.resetHabitStreak(habitId) }
    }

    @Test
    fun `error handling should work for initialization failure`() = runTest {
        // Given
        coEvery { mockHabitTrackerUseCase.initializeDigitalWellnessHabits() } throws RuntimeException("Init failed")

        // When
        viewModel = HabitTrackerViewModel(mockHabitTrackerUseCase)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.error?.contains("Failed to initialize habits") == true)
    }

    @Test
    fun `error handling should work for completeHabit failure`() = runTest {
        // Given
        coEvery { mockHabitTrackerUseCase.completeHabit(any()) } throws RuntimeException("Complete failed")

        // When
        viewModel.completeHabit("morning_no_phone")
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.error?.contains("Failed to complete habit") == true)
    }

    @Test
    fun `error handling should work for loadHabitStats failure`() = runTest {
        // Given
        coEvery { mockHabitTrackerUseCase.getHabitStats(any(), any()) } throws RuntimeException("Stats failed")

        // When
        viewModel.loadHabitStats("morning_no_phone")
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.error?.contains("Failed to load habit stats") == true)
    }

    @Test
    fun `error handling should work for createCustomHabit failure`() = runTest {
        // Given
        viewModel.updateCustomHabitName("Test")
        coEvery { mockHabitTrackerUseCase.createCustomHabit(any(), any(), any()) } throws RuntimeException("Create failed")

        // When
        viewModel.createCustomHabit()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.error?.contains("Failed to create habit") == true)
    }

    @Test
    fun `error handling should work for resetHabitStreak failure`() = runTest {
        // Given
        coEvery { mockHabitTrackerUseCase.resetHabitStreak(any()) } throws RuntimeException("Reset failed")

        // When
        viewModel.resetHabitStreak("morning_no_phone")
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.error?.contains("Failed to reset streak") == true)
    }

    @Test
    fun `habits flow error should be handled gracefully`() = runTest {
        // Given
        every { mockHabitTrackerUseCase.getTodaysHabits() } throws RuntimeException("Flow error")

        // When
        viewModel = HabitTrackerViewModel(mockHabitTrackerUseCase)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertTrue(state.error?.contains("Failed to load habits") == true)
    }

    // ===== NEW TESTS FOR AUTOMATIC DETECTION =====
    
    @Test
    fun `initialization should call checkAndCompleteHabitsAutomatically`() = runTest {
        // When ViewModel is initialized (already done in setup)
        
        // Then
        coVerify { mockHabitTrackerUseCase.checkAndCompleteHabitsAutomatically() }
    }

    @Test
    fun `checkHabitsAutomatically should call use case method`() = runTest {
        // When
        viewModel.checkHabitsAutomatically()
        advanceUntilIdle()

        // Then
        coVerify { mockHabitTrackerUseCase.checkAndCompleteHabitsAutomatically() }
    }

    @Test
    fun `checkHabitsAutomatically should handle errors gracefully`() = runTest {
        // Given
        coEvery { mockHabitTrackerUseCase.checkAndCompleteHabitsAutomatically() } throws RuntimeException("Auto check failed")

        // When
        viewModel.checkHabitsAutomatically()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertTrue(state.error?.contains("Failed to check habits automatically") == true)
    }

    @Test
    fun `manual completeHabit should still work for override cases`() = runTest {
        // Given
        val habitId = "phone_free_social" // This one allows manual completion
        val manualHabit = sampleHabits.first { !it.isCompleted }.copy(habitId = habitId)
        val habitsWithManual = listOf(manualHabit)
        every { mockHabitTrackerUseCase.getTodaysHabits() } returns flowOf(habitsWithManual)
        
        // Recreate viewModel to get updated habits
        viewModel = HabitTrackerViewModel(mockHabitTrackerUseCase)

        // When
        viewModel.completeHabit(habitId)
        advanceUntilIdle()

        // Then
        coVerify { mockHabitTrackerUseCase.completeHabit(habitId) }
        
        val state = viewModel.uiState.first()
        assertNotNull(state.completionCelebration)
        assertEquals(manualHabit.habitName, state.completionCelebration?.habitName)
    }

    @Test
    fun `automatic detection should be called on ViewModel creation`() = runTest {
        // Given - fresh ViewModel creation
        val freshViewModel = HabitTrackerViewModel(mockHabitTrackerUseCase)
        advanceUntilIdle()

        // Then - automatic detection should be called during initialization
        coVerify(exactly = 2) { mockHabitTrackerUseCase.checkAndCompleteHabitsAutomatically() }
        // Called twice: once for original viewModel in setup, once for freshViewModel
    }
}