package dev.sadakat.screentimetracker.ui.progressivelimits.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sadakat.screentimetracker.data.local.ProgressiveLimit
import dev.sadakat.screentimetracker.domain.usecases.ProgressiveLimitsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProgressiveLimitsUiState(
    val activeLimits: List<ProgressiveLimit> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProgressiveLimitsViewModel @Inject constructor(
    private val progressiveLimitsUseCase: ProgressiveLimitsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProgressiveLimitsUiState())
    val uiState: StateFlow<ProgressiveLimitsUiState> = _uiState.asStateFlow()
    
    init {
        loadActiveLimits()
    }
    
    private fun loadActiveLimits() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                progressiveLimitsUseCase.getAllActiveLimits()
                    .collect { limits ->
                        _uiState.update { 
                            it.copy(
                                activeLimits = limits,
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
    
    fun cancelLimit(packageName: String) {
        viewModelScope.launch {
            try {
                progressiveLimitsUseCase.cancelProgressiveLimit(packageName)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}