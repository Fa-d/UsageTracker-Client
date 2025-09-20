package dev.sadakat.screentimetracker.core.presentation.ui.smartgoals.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sadakat.screentimetracker.core.domain.usecases.SmartGoalSettingUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.SmartGoalSettingUseCase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmartGoalsViewModel @Inject constructor(
    private val smartGoalSettingUseCase: SmartGoalSettingUseCase
) : ViewModel() {

    private val _uiState = mutableStateOf(SmartGoalsUiState())
    val uiState: State<SmartGoalsUiState> = _uiState

    private val _uiEvents = MutableSharedFlow<SmartGoalsUiEvent>()
    val uiEvents: SharedFlow<SmartGoalsUiEvent> = _uiEvents.asSharedFlow()

    fun generateAIRecommendations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingRecommendations = true, error = null)
            try {
                val recommendations = smartGoalSettingUseCase.generateAIRecommendedGoals()
                _uiState.value = _uiState.value.copy(
                    recommendations = recommendations,
                    isLoadingRecommendations = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to generate recommendations: ${e.message}",
                    isLoadingRecommendations = false
                )
            }
        }
    }

    fun generateContextualRecommendations(context: GoalContext) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingRecommendations = true, error = null)
            try {
                val recommendations = smartGoalSettingUseCase.generateContextualGoals(context)
                _uiState.value = _uiState.value.copy(
                    recommendations = recommendations,
                    selectedContext = context,
                    isLoadingRecommendations = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to generate contextual recommendations: ${e.message}",
                    isLoadingRecommendations = false
                )
            }
        }
    }

    fun acceptRecommendation(recommendation: GoalRecommendation) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingGoal = true, error = null)
            try {
                val goalId = smartGoalSettingUseCase.createGoalFromRecommendation(recommendation)
                _uiState.value = _uiState.value.copy(
                    isCreatingGoal = false,
                    createdGoalId = goalId
                )
                _uiEvents.emit(SmartGoalsUiEvent.GoalCreated(goalId, recommendation.title))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to create goal: ${e.message}",
                    isCreatingGoal = false
                )
            }
        }
    }

    fun rejectRecommendation(recommendation: GoalRecommendation) {
        val currentRecommendations = _uiState.value.recommendations.toMutableList()
        currentRecommendations.remove(recommendation)
        _uiState.value = _uiState.value.copy(recommendations = currentRecommendations)
    }

    fun checkForGoalAdjustments(goalId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingAdjustments = true, error = null)
            try {
                val adjustment = smartGoalSettingUseCase.adjustGoalBasedOnPerformance(goalId)
                _uiState.value = _uiState.value.copy(
                    pendingAdjustment = adjustment,
                    isCheckingAdjustments = false
                )
                adjustment?.let {
                    _uiEvents.emit(SmartGoalsUiEvent.AdjustmentSuggested(it))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to check goal adjustments: ${e.message}",
                    isCheckingAdjustments = false
                )
            }
        }
    }

    fun applyGoalAdjustment(adjustment: GoalAdjustment) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isApplyingAdjustment = true, error = null)
            try {
                val success = smartGoalSettingUseCase.applyGoalAdjustment(adjustment)
                if (success) {
                    _uiState.value = _uiState.value.copy(
                        pendingAdjustment = null,
                        isApplyingAdjustment = false
                    )
                    _uiEvents.emit(SmartGoalsUiEvent.AdjustmentApplied(adjustment.goalId))
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to apply goal adjustment",
                        isApplyingAdjustment = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error applying adjustment: ${e.message}",
                    isApplyingAdjustment = false
                )
            }
        }
    }

    fun dismissAdjustment() {
        _uiState.value = _uiState.value.copy(pendingAdjustment = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun setSelectedContext(context: GoalContext) {
        _uiState.value = _uiState.value.copy(selectedContext = context)
        generateContextualRecommendations(context)
    }

    fun refreshRecommendations() {
        if (_uiState.value.selectedContext != null) {
            generateContextualRecommendations(_uiState.value.selectedContext!!)
        } else {
            generateAIRecommendations()
        }
    }

    data class SmartGoalsUiState(
        val recommendations: List<GoalRecommendation> = emptyList(),
        val selectedContext: GoalContext? = null,
        val pendingAdjustment: GoalAdjustment? = null,
        val isLoadingRecommendations: Boolean = false,
        val isCreatingGoal: Boolean = false,
        val isCheckingAdjustments: Boolean = false,
        val isApplyingAdjustment: Boolean = false,
        val createdGoalId: Long? = null,
        val error: String? = null
    )

    sealed class SmartGoalsUiEvent {
        data class GoalCreated(val goalId: Long, val goalTitle: String) : SmartGoalsUiEvent()
        data class AdjustmentSuggested(val adjustment: GoalAdjustment) : SmartGoalsUiEvent()
        data class AdjustmentApplied(val goalId: Long) : SmartGoalsUiEvent()
    }
}