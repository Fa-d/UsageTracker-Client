package com.example.screentimetracker.ui.smartgoals

import com.example.screentimetracker.domain.usecases.SmartGoalSettingUseCase
import com.example.screentimetracker.domain.usecases.SmartGoalSettingUseCase.*
import com.example.screentimetracker.ui.smartgoals.viewmodels.SmartGoalsViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class SmartGoalsViewModelTest {

    @Mock
    private lateinit var smartGoalSettingUseCase: SmartGoalSettingUseCase

    private lateinit var viewModel: SmartGoalsViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val backgroundScope = CoroutineScope(testDispatcher)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SmartGoalsViewModel(smartGoalSettingUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Test Data
    private fun createSampleRecommendations(): List<GoalRecommendation> {
        return listOf(
            GoalRecommendation(
                goalType = SmartGoalSettingUseCase.DAILY_SCREEN_TIME,
                title = "Reduce Daily Screen Time",
                description = "Cut screen time by 10%",
                targetValue = TimeUnit.HOURS.toMillis(4),
                currentAverage = TimeUnit.HOURS.toMillis(5),
                confidence = 0.9f,
                difficulty = DifficultyLevel.MEDIUM,
                reasoning = "Gradual reduction helps build sustainable habits"
            ),
            GoalRecommendation(
                goalType = SmartGoalSettingUseCase.APP_SPECIFIC_LIMIT,
                title = "Limit Social Media",
                description = "Set 30-minute daily limit",
                targetValue = TimeUnit.MINUTES.toMillis(30),
                currentAverage = TimeUnit.MINUTES.toMillis(60),
                confidence = 0.8f,
                difficulty = DifficultyLevel.HARD,
                reasoning = "Social media is your most-used category",
                packageName = "com.instagram.android"
            )
        )
    }

    private fun createSampleAdjustment(): GoalAdjustment {
        return GoalAdjustment(
            goalId = 1L,
            adjustmentType = AdjustmentType.MAKE_EASIER,
            newTargetValue = TimeUnit.HOURS.toMillis(5),
            reasoning = "You're struggling with the current goal. Let's make it more achievable.",
            confidence = 0.85f
        )
    }

    // ==================== AI RECOMMENDATIONS TESTS ====================

    @Test
    fun `generateAIRecommendations success updates state correctly`() = runTest {
        // Given
        val recommendations = createSampleRecommendations()
        whenever(smartGoalSettingUseCase.generateAIRecommendedGoals()).thenReturn(recommendations)

        // When
        viewModel.generateAIRecommendations()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoadingRecommendations)
        assertEquals(recommendations, state.recommendations)
        assertNull(state.error)
        assertNull(state.selectedContext)
    }

    @Test
    fun `generateAIRecommendations sets loading state during execution`() = runTest {
        // Given
        whenever(smartGoalSettingUseCase.generateAIRecommendedGoals()).thenReturn(emptyList())

        // When - start operation but don't complete
        viewModel.generateAIRecommendations()
        
        // Then - should be loading
        assertTrue(viewModel.uiState.value.isLoadingRecommendations)
        
        // Complete operation
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then - should not be loading anymore
        assertFalse(viewModel.uiState.value.isLoadingRecommendations)
    }

    @Test
    fun `generateAIRecommendations handles error correctly`() = runTest {
        // Given
        val errorMessage = "Network error"
        whenever(smartGoalSettingUseCase.generateAIRecommendedGoals()).thenThrow(RuntimeException(errorMessage))

        // When
        viewModel.generateAIRecommendations()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoadingRecommendations)
        assertTrue(state.error!!.contains(errorMessage))
        assertTrue(state.recommendations.isEmpty())
    }

    @Test
    fun `generateAIRecommendations clears previous error`() = runTest {
        // Given - previous error state
        whenever(smartGoalSettingUseCase.generateAIRecommendedGoals()).thenThrow(RuntimeException("First error"))
        viewModel.generateAIRecommendations()
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)

        // When - successful call
        whenever(smartGoalSettingUseCase.generateAIRecommendedGoals()).thenReturn(createSampleRecommendations())
        viewModel.generateAIRecommendations()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - error should be cleared
        assertNull(viewModel.uiState.value.error)
    }

    // ==================== CONTEXTUAL RECOMMENDATIONS TESTS ====================

    @Test
    fun `generateContextualRecommendations success updates state with context`() = runTest {
        // Given
        val context = GoalContext.WORKDAY
        val recommendations = listOf(createSampleRecommendations().first())
        whenever(smartGoalSettingUseCase.generateContextualGoals(context)).thenReturn(recommendations)

        // When
        viewModel.generateContextualRecommendations(context)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoadingRecommendations)
        assertEquals(recommendations, state.recommendations)
        assertEquals(context, state.selectedContext)
        assertNull(state.error)
    }

    @Test
    fun `generateContextualRecommendations handles error correctly`() = runTest {
        // Given
        val context = GoalContext.WEEKEND
        val errorMessage = "Context analysis failed"
        whenever(smartGoalSettingUseCase.generateContextualGoals(context)).thenThrow(RuntimeException(errorMessage))

        // When
        viewModel.generateContextualRecommendations(context)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoadingRecommendations)
        assertTrue(state.error!!.contains(errorMessage))
        assertEquals(context, state.selectedContext) // Context should still be set
    }

    @Test
    fun `setSelectedContext triggers contextual recommendations`() = runTest {
        // Given
        val context = GoalContext.EVENING
        val recommendations = listOf(createSampleRecommendations().first())
        whenever(smartGoalSettingUseCase.generateContextualGoals(context)).thenReturn(recommendations)

        // When
        viewModel.setSelectedContext(context)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify(smartGoalSettingUseCase).generateContextualGoals(context)
        assertEquals(context, viewModel.uiState.value.selectedContext)
        assertEquals(recommendations, viewModel.uiState.value.recommendations)
    }

    // ==================== GOAL ACCEPTANCE TESTS ====================

    @Test
    fun `acceptRecommendation success creates goal and emits event`() = runTest {
        // Given
        val recommendation = createSampleRecommendations().first()
        val goalId = 123L
        whenever(smartGoalSettingUseCase.createGoalFromRecommendation(recommendation)).thenReturn(goalId)

        // When
        viewModel.acceptRecommendation(recommendation)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isCreatingGoal)
        assertEquals(goalId, state.createdGoalId)
        assertNull(state.error)
        
        // Verify use case called
        verify(smartGoalSettingUseCase).createGoalFromRecommendation(recommendation)
        
        // Test event emission
        val events = mutableListOf<SmartGoalsViewModel.SmartGoalsUiEvent>()
        val job = backgroundScope.launch { viewModel.uiEvents.collect { events.add(it) } }
        testDispatcher.scheduler.advanceUntilIdle()
        job.cancel()
        
        assertEquals(1, events.size)
        val event = events.first() as SmartGoalsViewModel.SmartGoalsUiEvent.GoalCreated
        assertEquals(goalId, event.goalId)
        assertEquals(recommendation.title, event.goalTitle)
    }

    @Test
    fun `acceptRecommendation sets creating state during execution`() = runTest {
        // Given
        val recommendation = createSampleRecommendations().first()
        whenever(smartGoalSettingUseCase.createGoalFromRecommendation(recommendation)).thenReturn(1L)

        // When - start operation but don't complete
        viewModel.acceptRecommendation(recommendation)
        
        // Then - should be creating
        assertTrue(viewModel.uiState.value.isCreatingGoal)
        
        // Complete operation
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then - should not be creating anymore
        assertFalse(viewModel.uiState.value.isCreatingGoal)
    }

    @Test
    fun `acceptRecommendation handles error correctly`() = runTest {
        // Given
        val recommendation = createSampleRecommendations().first()
        val errorMessage = "Failed to save goal"
        whenever(smartGoalSettingUseCase.createGoalFromRecommendation(recommendation)).thenThrow(RuntimeException(errorMessage))

        // When
        viewModel.acceptRecommendation(recommendation)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isCreatingGoal)
        assertTrue(state.error!!.contains(errorMessage))
        assertNull(state.createdGoalId)
    }

    @Test
    fun `rejectRecommendation removes recommendation from list`() = runTest {
        // Given
        val recommendations = createSampleRecommendations()
        whenever(smartGoalSettingUseCase.generateAIRecommendedGoals()).thenReturn(recommendations)
        viewModel.generateAIRecommendations()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify initial state
        assertEquals(2, viewModel.uiState.value.recommendations.size)

        // When
        viewModel.rejectRecommendation(recommendations.first())

        // Then
        val state = viewModel.uiState.value
        assertEquals(1, state.recommendations.size)
        assertEquals(recommendations[1], state.recommendations.first())
    }

    // ==================== GOAL ADJUSTMENT TESTS ====================

    @Test
    fun `checkForGoalAdjustments success updates state with adjustment`() = runTest {
        // Given
        val goalId = 1L
        val adjustment = createSampleAdjustment()
        whenever(smartGoalSettingUseCase.adjustGoalBasedOnPerformance(goalId)).thenReturn(adjustment)

        // When
        viewModel.checkForGoalAdjustments(goalId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isCheckingAdjustments)
        assertEquals(adjustment, state.pendingAdjustment)
        assertNull(state.error)

        // Test event emission
        val events = mutableListOf<SmartGoalsViewModel.SmartGoalsUiEvent>()
        val job = backgroundScope.launch { viewModel.uiEvents.collect { events.add(it) } }
        testDispatcher.scheduler.advanceUntilIdle()
        job.cancel()

        assertEquals(1, events.size)
        val event = events.first() as SmartGoalsViewModel.SmartGoalsUiEvent.AdjustmentSuggested
        assertEquals(adjustment, event.adjustment)
    }

    @Test
    fun `checkForGoalAdjustments with no adjustment needed`() = runTest {
        // Given
        val goalId = 1L
        whenever(smartGoalSettingUseCase.adjustGoalBasedOnPerformance(goalId)).thenReturn(null)

        // When
        viewModel.checkForGoalAdjustments(goalId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isCheckingAdjustments)
        assertNull(state.pendingAdjustment)
        assertNull(state.error)
    }

    @Test
    fun `checkForGoalAdjustments handles error correctly`() = runTest {
        // Given
        val goalId = 1L
        val errorMessage = "Analysis failed"
        whenever(smartGoalSettingUseCase.adjustGoalBasedOnPerformance(goalId)).thenThrow(RuntimeException(errorMessage))

        // When
        viewModel.checkForGoalAdjustments(goalId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isCheckingAdjustments)
        assertTrue(state.error!!.contains(errorMessage))
        assertNull(state.pendingAdjustment)
    }

    @Test
    fun `applyGoalAdjustment success clears adjustment and emits event`() = runTest {
        // Given
        val adjustment = createSampleAdjustment()
        whenever(smartGoalSettingUseCase.applyGoalAdjustment(adjustment)).thenReturn(true)
        
        // Set up initial state with pending adjustment
        viewModel.checkForGoalAdjustments(1L) // This sets up the adjustment
        whenever(smartGoalSettingUseCase.adjustGoalBasedOnPerformance(1L)).thenReturn(adjustment)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.applyGoalAdjustment(adjustment)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isApplyingAdjustment)
        assertNull(state.pendingAdjustment)
        assertNull(state.error)

        // Test event emission
        val events = mutableListOf<SmartGoalsViewModel.SmartGoalsUiEvent>()
        val job = backgroundScope.launch { viewModel.uiEvents.collect { events.add(it) } }
        testDispatcher.scheduler.advanceUntilIdle()
        job.cancel()

        assertTrue(events.any { it is SmartGoalsViewModel.SmartGoalsUiEvent.AdjustmentApplied })
    }

    @Test
    fun `applyGoalAdjustment failure keeps adjustment and sets error`() = runTest {
        // Given
        val adjustment = createSampleAdjustment()
        whenever(smartGoalSettingUseCase.applyGoalAdjustment(adjustment)).thenReturn(false)

        // When
        viewModel.applyGoalAdjustment(adjustment)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isApplyingAdjustment)
        assertTrue(state.error!!.contains("Failed to apply"))
    }

    @Test
    fun `dismissAdjustment clears pending adjustment`() = runTest {
        // Given - set up adjustment
        val adjustment = createSampleAdjustment()
        whenever(smartGoalSettingUseCase.adjustGoalBasedOnPerformance(1L)).thenReturn(adjustment)
        viewModel.checkForGoalAdjustments(1L)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Verify adjustment is set
        assertNotNull(viewModel.uiState.value.pendingAdjustment)

        // When
        viewModel.dismissAdjustment()

        // Then
        assertNull(viewModel.uiState.value.pendingAdjustment)
    }

    // ==================== UTILITY TESTS ====================

    @Test
    fun `clearError resets error state`() = runTest {
        // Given - error state
        whenever(smartGoalSettingUseCase.generateAIRecommendedGoals()).thenThrow(RuntimeException("Test error"))
        viewModel.generateAIRecommendations()
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `refreshRecommendations calls correct method based on context`() = runTest {
        // Test 1: No context selected - should call AI recommendations
        whenever(smartGoalSettingUseCase.generateAIRecommendedGoals()).thenReturn(emptyList())
        viewModel.refreshRecommendations()
        testDispatcher.scheduler.advanceUntilIdle()
        verify(smartGoalSettingUseCase).generateAIRecommendedGoals()

        // Test 2: Context selected - should call contextual recommendations
        clearInvocations(smartGoalSettingUseCase)
        val context = GoalContext.WORKDAY
        whenever(smartGoalSettingUseCase.generateContextualGoals(context)).thenReturn(emptyList())
        viewModel.setSelectedContext(context)
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.refreshRecommendations()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Should call contextual goals twice (once for setSelectedContext, once for refresh)
        verify(smartGoalSettingUseCase, times(2)).generateContextualGoals(context)
    }

    // ==================== EDGE CASES AND INTEGRATION TESTS ====================

    @Test
    fun `multiple simultaneous operations handle state correctly`() = runTest {
        // Given
        val recommendations = createSampleRecommendations()
        whenever(smartGoalSettingUseCase.generateAIRecommendedGoals()).thenReturn(recommendations)
        whenever(smartGoalSettingUseCase.createGoalFromRecommendation(any())).thenReturn(1L)

        // When - trigger multiple operations
        viewModel.generateAIRecommendations()
        viewModel.acceptRecommendation(recommendations.first())
        
        // Don't advance - both should be in loading state
        assertTrue(viewModel.uiState.value.isLoadingRecommendations)
        assertTrue(viewModel.uiState.value.isCreatingGoal)
        
        // Advance and complete
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoadingRecommendations)
        assertFalse(state.isCreatingGoal)
        assertNull(state.error)
    }

    @Test
    fun `empty recommendations list handled gracefully`() = runTest {
        // Given
        whenever(smartGoalSettingUseCase.generateAIRecommendedGoals()).thenReturn(emptyList())

        // When
        viewModel.generateAIRecommendations()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoadingRecommendations)
        assertTrue(state.recommendations.isEmpty())
        assertNull(state.error)
    }
}