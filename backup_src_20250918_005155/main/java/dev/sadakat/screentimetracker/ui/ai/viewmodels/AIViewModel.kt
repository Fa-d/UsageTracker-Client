package dev.sadakat.screentimetracker.ui.ai.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sadakat.screentimetracker.ui.ai.AIDownloadState
import dev.sadakat.screentimetracker.core.data.local.UserPreferences
import dev.sadakat.screentimetracker.core.data.repository.UserPreferencesRepository
import dev.sadakat.screentimetracker.utils.AIDownloadManager
import dev.sadakat.screentimetracker.utils.AIDownloadProgress
import dev.sadakat.screentimetracker.utils.AIUtils
import dev.sadakat.screentimetracker.utils.AIAvailabilityStatus
import dev.sadakat.screentimetracker.domain.usecases.AIIntegrationUseCase
import dev.sadakat.screentimetracker.ui.ai.components.AIInsight
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AIFeatureState(
    val isAvailable: Boolean = false,
    val isEnabled: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: AIDownloadProgress? = null,
    val availabilityStatus: AIAvailabilityStatus? = null,
    val userPreferences: UserPreferences? = null,
    val error: String? = null,
    val showOptInDialog: Boolean = false,
    val showDownloadDialog: Boolean = false,
    val insights: List<AIInsight> = emptyList(),
    val recommendations: List<AIIntegrationUseCase.AIRecommendation> = emptyList(),
    val wellnessAlerts: List<AIIntegrationUseCase.WellnessAlert> = emptyList(),
    val isLoadingInsights: Boolean = false
)

@HiltViewModel
class AIViewModel @Inject constructor(
    private val aiDownloadManager: AIDownloadManager,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val aiIntegrationUseCase: AIIntegrationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIFeatureState())
    val uiState: StateFlow<AIFeatureState> = _uiState.asStateFlow()

    private val _showOptInDialog = MutableStateFlow(false)
    val showOptInDialog: StateFlow<Boolean> = _showOptInDialog.asStateFlow()

    private val _showDownloadDialog = MutableStateFlow(false)
    val showDownloadDialog: StateFlow<Boolean> = _showDownloadDialog.asStateFlow()

    init {
        observeAIState()
        // Generate initial AI insights when app starts
        refreshAIInsights()
    }

    private fun observeAIState() {
        viewModelScope.launch {
            combine(
                userPreferencesRepository.getUserPreferences(),
                aiDownloadManager.downloadProgress,
                aiDownloadManager.isDownloading
            ) { userPrefs, downloadProgress, isDownloading ->
                
                val availabilityStatus = userPrefs?.let { prefs ->
                    AIUtils.getAIAvailabilityStatus(
                        context = aiDownloadManager.getContext(),
                        userPreferences = prefs
                    )
                }

                AIFeatureState(
                    isAvailable = availabilityStatus?.canUseAI ?: false,
                    isEnabled = userPrefs?.aiFeaturesEnabled ?: false,
                    isDownloading = isDownloading,
                    downloadProgress = downloadProgress,
                    availabilityStatus = availabilityStatus,
                    userPreferences = userPrefs,
                    showOptInDialog = _showOptInDialog.value,
                    showDownloadDialog = _showDownloadDialog.value
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    fun enableAIFeatures() {
        viewModelScope.launch {
            try {
                // First check if AI module is downloaded
                if (!aiDownloadManager.isAIAvailable()) {
                    // Show download dialog
                    _showDownloadDialog.value = true
                    return@launch
                }

                // Enable AI features in preferences
                userPreferencesRepository.updateAIFeaturesEnabled(true)
                userPreferencesRepository.updateAIModuleDownloaded(true)
                
                Log.d("AIViewModel", "AI features enabled successfully")
            } catch (e: Exception) {
                Log.e("AIViewModel", "Failed to enable AI features", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to enable AI features: ${e.localizedMessage}"
                )
            }
        }
    }

    fun disableAIFeatures() {
        viewModelScope.launch {
            try {
                userPreferencesRepository.updateAIFeaturesEnabled(false)
                Log.d("AIViewModel", "AI features disabled successfully")
            } catch (e: Exception) {
                Log.e("AIViewModel", "Failed to disable AI features", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to disable AI features: ${e.localizedMessage}"
                )
            }
        }
    }

    fun startAIDownload() {
        viewModelScope.launch {
            try {
                val success = aiDownloadManager.startDownload()
                if (success) {
                    _showDownloadDialog.value = true
                    Log.d("AIViewModel", "AI download started successfully")
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to start AI download"
                    )
                }
            } catch (e: Exception) {
                Log.e("AIViewModel", "Error starting AI download", e)
                _uiState.value = _uiState.value.copy(
                    error = "Error starting download: ${e.localizedMessage}"
                )
            }
        }
    }

    fun cancelAIDownload() {
        viewModelScope.launch {
            try {
                aiDownloadManager.cancelDownload()
                _showDownloadDialog.value = false
                Log.d("AIViewModel", "AI download cancelled")
            } catch (e: Exception) {
                Log.e("AIViewModel", "Error cancelling AI download", e)
            }
        }
    }

    fun retryAIDownload() {
        viewModelScope.launch {
            try {
                val success = aiDownloadManager.retryDownload()
                if (!success) {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to retry AI download"
                    )
                }
            } catch (e: Exception) {
                Log.e("AIViewModel", "Error retrying AI download", e)
                _uiState.value = _uiState.value.copy(
                    error = "Error retrying download: ${e.localizedMessage}"
                )
            }
        }
    }

    fun showOptInDialog() {
        _showOptInDialog.value = true
    }

    fun hideOptInDialog() {
        _showOptInDialog.value = false
    }

    fun showDownloadDialog() {
        _showDownloadDialog.value = true
    }

    fun hideDownloadDialog() {
        _showDownloadDialog.value = false
    }

    fun onDownloadCompleted() {
        viewModelScope.launch {
            try {
                // Update preferences to reflect successful download
                userPreferencesRepository.updateAIModuleDownloaded(true)
                userPreferencesRepository.updateAIFeaturesEnabled(true)
                
                _showDownloadDialog.value = false
                Log.d("AIViewModel", "AI download completed and features enabled")
            } catch (e: Exception) {
                Log.e("AIViewModel", "Error handling download completion", e)
            }
        }
    }

    fun updateAIInsightsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.updateAIInsightsEnabled(enabled)
            } catch (e: Exception) {
                Log.e("AIViewModel", "Failed to update AI insights setting", e)
            }
        }
    }

    fun updateAIGoalRecommendationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.updateAIGoalRecommendationsEnabled(enabled)
            } catch (e: Exception) {
                Log.e("AIViewModel", "Failed to update AI goal recommendations setting", e)
            }
        }
    }

    fun updateAIPredictiveCoachingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.updateAIPredictiveCoachingEnabled(enabled)
            } catch (e: Exception) {
                Log.e("AIViewModel", "Failed to update AI predictive coaching setting", e)
            }
        }
    }

    fun updateAIUsagePredictionsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.updateAIUsagePredictionsEnabled(enabled)
            } catch (e: Exception) {
                Log.e("AIViewModel", "Failed to update AI usage predictions setting", e)
            }
        }
    }

    fun uninstallAI() {
        viewModelScope.launch {
            try {
                aiDownloadManager.uninstallAI()
                
                // Reset all AI preferences
                userPreferencesRepository.updateAIFeaturesEnabled(false)
                userPreferencesRepository.updateAIModuleDownloaded(false)
                userPreferencesRepository.updateAIInsightsEnabled(false)
                userPreferencesRepository.updateAIGoalRecommendationsEnabled(false)
                userPreferencesRepository.updateAIPredictiveCoachingEnabled(false)
                userPreferencesRepository.updateAIUsagePredictionsEnabled(false)
                
                Log.d("AIViewModel", "AI features uninstalled successfully")
            } catch (e: Exception) {
                Log.e("AIViewModel", "Failed to uninstall AI features", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to uninstall AI features: ${e.localizedMessage}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun getAIModuleSize(): String {
        return aiDownloadManager.getAIModuleSize()
    }

    fun getDownloadRequirements(): List<String> {
        return aiDownloadManager.getDownloadRequirements()
    }

    fun generateAIInsights() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoadingInsights = true)
                
                // For now, use empty data since we don't have repository integration
                val insights = aiIntegrationUseCase.generateAIInsights(
                    sessionEvents = emptyList(),
                    dailySummaries = emptyList()
                )
                
                val recommendations = aiIntegrationUseCase.generateGoalRecommendations(
                    sessionEvents = emptyList(),
                    dailySummaries = emptyList()
                )
                
                val wellnessAlerts = aiIntegrationUseCase.checkWellnessAlerts(
                    sessionEvents = emptyList(),
                    currentSessionDuration = 0L
                )
                
                _uiState.value = _uiState.value.copy(
                    insights = insights,
                    recommendations = recommendations,
                    wellnessAlerts = wellnessAlerts,
                    isLoadingInsights = false
                )
                
                Log.d("AIViewModel", "AI insights generated: ${insights.size} insights, ${recommendations.size} recommendations")
            } catch (e: Exception) {
                Log.e("AIViewModel", "Failed to generate AI insights", e)
                _uiState.value = _uiState.value.copy(
                    error = "Failed to generate insights: ${e.localizedMessage}",
                    isLoadingInsights = false
                )
            }
        }
    }

    fun refreshAIInsights() {
        if (_uiState.value.isEnabled) {
            generateAIInsights()
        }
    }

    override fun onCleared() {
        super.onCleared()
        aiDownloadManager.cleanup()
    }
}