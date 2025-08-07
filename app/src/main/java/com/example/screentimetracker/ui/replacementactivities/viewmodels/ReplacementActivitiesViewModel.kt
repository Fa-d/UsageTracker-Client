package com.example.screentimetracker.ui.replacementactivities.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.screentimetracker.data.local.ReplacementActivity
import com.example.screentimetracker.domain.usecases.ActivityStats
import com.example.screentimetracker.domain.usecases.ReplacementActivitiesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReplacementActivitiesUiState(
    val allActivities: List<ReplacementActivity> = emptyList(),
    val filteredActivities: List<ReplacementActivity> = emptyList(),
    val smartSuggestions: List<ReplacementActivity> = emptyList(),
    val selectedCategory: String = "all",
    val isLoading: Boolean = false,
    val todayCompletions: Int = 0,
    val weeklyStats: ActivityStats = ActivityStats(0, 0, 0f, emptyList()),
    val error: String? = null
)

@HiltViewModel
class ReplacementActivitiesViewModel @Inject constructor(
    private val replacementActivitiesUseCase: ReplacementActivitiesUseCase
) : ViewModel() {
    
    private val _selectedCategory = MutableStateFlow("all")
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    
    val uiState: StateFlow<ReplacementActivitiesUiState> = combine(
        replacementActivitiesUseCase.getAllActivities(),
        _selectedCategory,
        _isLoading,
        _error
    ) { activities, selectedCategory, isLoading, error ->
        val filtered = if (selectedCategory == "all") {
            activities
        } else {
            activities.filter { it.category == selectedCategory }
        }
        
        ReplacementActivitiesUiState(
            allActivities = activities,
            filteredActivities = filtered,
            selectedCategory = selectedCategory,
            isLoading = isLoading,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReplacementActivitiesUiState(isLoading = true)
    ).also { stateFlow ->
        // Initialize additional data
        viewModelScope.launch {
            loadInitialData()
        }
    }
    
    private suspend fun loadInitialData() {
        try {
            _isLoading.value = true
            
            // Initialize default activities if needed
            replacementActivitiesUseCase.initializeDefaultActivities()
            
            // Load today's completions and weekly stats
            loadTodayCompletions()
            loadWeeklyStats()
            loadSmartSuggestions()
            
        } catch (e: Exception) {
            _error.value = "Failed to load activities: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
    
    private suspend fun loadTodayCompletions() {
        try {
            val completions = replacementActivitiesUseCase.getTodayCompletions()
            // Update UI state with today's completions count
            // Note: This would require modifying the state flow structure
        } catch (e: Exception) {
            _error.value = "Failed to load today's completions: ${e.message}"
        }
    }
    
    private suspend fun loadWeeklyStats() {
        try {
            val stats = replacementActivitiesUseCase.getWeeklyStats()
            // Update UI state with weekly stats
            // Note: This would require modifying the state flow structure
        } catch (e: Exception) {
            _error.value = "Failed to load weekly stats: ${e.message}"
        }
    }
    
    private suspend fun loadSmartSuggestions() {
        try {
            val suggestions = replacementActivitiesUseCase.getPersonalizedRecommendations()
            // Update UI state with smart suggestions
            // Note: This would require modifying the state flow structure
        } catch (e: Exception) {
            _error.value = "Failed to load smart suggestions: ${e.message}"
        }
    }
    
    fun loadSuggestionsForBlockedApp(blockedAppName: String) {
        viewModelScope.launch {
            try {
                val packageName = extractPackageNameFromAppName(blockedAppName)
                val suggestions = replacementActivitiesUseCase.getSuggestionsForBlockedApp(packageName)
                
                // Update the current state with new suggestions
                // Note: This is a simplified approach - in a real implementation,
                // you'd want to properly update the state flow
                
            } catch (e: Exception) {
                _error.value = "Failed to load suggestions for blocked app: ${e.message}"
            }
        }
    }
    
    fun filterActivitiesByCategory(category: String) {
        _selectedCategory.value = category
    }
    
    fun completeActivity(
        activityId: Long,
        rating: Int,
        actualDurationMinutes: Int,
        notes: String,
        contextTrigger: String
    ) {
        viewModelScope.launch {
            try {
                replacementActivitiesUseCase.completeActivity(
                    activityId = activityId,
                    actualDurationMinutes = actualDurationMinutes,
                    userRating = rating,
                    notes = notes,
                    contextTrigger = contextTrigger
                )
                
                // Refresh data after completion
                loadTodayCompletions()
                loadWeeklyStats()
                
            } catch (e: Exception) {
                _error.value = "Failed to complete activity: ${e.message}"
            }
        }
    }
    
    fun createCustomActivity(
        title: String,
        description: String,
        emoji: String,
        estimatedMinutes: Int,
        category: String,
        difficulty: Int
    ) {
        viewModelScope.launch {
            try {
                replacementActivitiesUseCase.createCustomActivity(
                    title = title,
                    description = description,
                    emoji = emoji,
                    estimatedMinutes = estimatedMinutes,
                    category = category,
                    difficulty = difficulty
                )
            } catch (e: Exception) {
                _error.value = "Failed to create custom activity: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    private fun extractPackageNameFromAppName(appName: String): String {
        // Simple mapping - in a real app, you'd have a more sophisticated way
        // to map app display names to package names
        return when (appName.lowercase()) {
            "instagram" -> "com.instagram.android"
            "facebook" -> "com.facebook.katana"
            "twitter" -> "com.twitter.android"
            "tiktok" -> "com.zhiliaoapp.musically"
            "youtube" -> "com.google.android.youtube"
            "netflix" -> "com.netflix.mediaclient"
            else -> appName.lowercase().replace(" ", ".")
        }
    }
}

// Extension of the original UiState to include the missing properties
private data class ExtendedReplacementActivitiesUiState(
    val allActivities: List<ReplacementActivity> = emptyList(),
    val filteredActivities: List<ReplacementActivity> = emptyList(),
    val smartSuggestions: List<ReplacementActivity> = emptyList(),
    val selectedCategory: String = "all",
    val isLoading: Boolean = false,
    val todayCompletions: Int = 0,
    val weeklyStats: ActivityStats = ActivityStats(0, 0, 0f, emptyList()),
    val error: String? = null
)