package dev.sadakat.screentimetracker.core.presentation.ui.personalization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sadakat.screentimetracker.core.data.local.entities.UserPreferences
import dev.sadakat.screentimetracker.core.data.local.entities.ThemeMode
import dev.sadakat.screentimetracker.core.data.local.entities.ColorScheme as AppColorScheme
import dev.sadakat.screentimetracker.core.domain.usecases.UserPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PersonalizationUiState(
    val preferences: UserPreferences = UserPreferences(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PersonalizationViewModel @Inject constructor(
    private val userPreferencesUseCase: UserPreferencesUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PersonalizationUiState())
    val uiState: StateFlow<PersonalizationUiState> = _uiState.asStateFlow()
    
    init {
        loadPreferences()
    }
    
    private fun loadPreferences() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                userPreferencesUseCase.getUserPreferences()
                    .collect { preferences ->
                        _uiState.update { 
                            it.copy(
                                preferences = preferences,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            try {
                userPreferencesUseCase.updateThemeMode(themeMode)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun updateColorScheme(colorScheme: AppColorScheme) {
        viewModelScope.launch {
            try {
                userPreferencesUseCase.updateColorScheme(colorScheme)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    
    fun updateMotivationalMessages(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesUseCase.updateMotivationalMessages(enabled)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun updateAchievementCelebrations(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesUseCase.updateAchievementCelebrations(enabled)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun updateBreakReminders(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesUseCase.updateBreakReminders(enabled)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun updateWellnessCoaching(enabled: Boolean) {
        viewModelScope.launch {
            try {
                userPreferencesUseCase.updateWellnessCoaching(enabled)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun updateNotificationSound(soundName: String) {
        viewModelScope.launch {
            try {
                userPreferencesUseCase.updateNotificationSound(soundName)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}