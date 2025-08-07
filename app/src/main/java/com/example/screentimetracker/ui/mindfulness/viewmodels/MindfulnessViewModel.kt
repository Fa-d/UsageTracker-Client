package com.example.screentimetracker.ui.mindfulness.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.screentimetracker.data.local.MindfulnessSession
import com.example.screentimetracker.domain.usecases.MindfulnessUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MindfulnessUiState(
    val isSessionActive: Boolean = false,
    val sessionType: String = "",
    val totalDurationMillis: Long = 0,
    val remainingTimeMillis: Long = 0,
    val currentPhase: String = "inhale", // inhale, hold, exhale
    val animationPhase: Float = 0f, // 0.0 to 1.0
    val isPaused: Boolean = false,
    val completionRate: Float = 0f,
    val showCompletionDialog: Boolean = false,
    val completedSession: MindfulnessSession? = null,
    val triggeredByAppBlock: Boolean = false,
    val blockedAppName: String = ""
)

@HiltViewModel
class MindfulnessViewModel @Inject constructor(
    private val mindfulnessUseCase: MindfulnessUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MindfulnessUiState())
    val uiState: StateFlow<MindfulnessUiState> = _uiState.asStateFlow()
    
    private var sessionJob: Job? = null
    private var currentSessionId: Long = 0
    private var sessionStartTime: Long = 0
    
    fun setTriggeredByAppBlock(blockedAppName: String) {
        _uiState.update { 
            it.copy(
                triggeredByAppBlock = true,
                blockedAppName = blockedAppName
            )
        }
    }
    
    fun startBreathingSession(durationMinutes: Int, type: String) {
        val durationMillis = durationMinutes * 60 * 1000L
        sessionStartTime = System.currentTimeMillis()
        
        viewModelScope.launch {
            currentSessionId = mindfulnessUseCase.startSession(
                sessionType = "breathing",
                durationMillis = durationMillis,
                triggeredByAppBlock = _uiState.value.triggeredByAppBlock,
                appThatWasBlocked = _uiState.value.blockedAppName
            )
            
            _uiState.update {
                it.copy(
                    isSessionActive = true,
                    sessionType = type,
                    totalDurationMillis = durationMillis,
                    remainingTimeMillis = durationMillis,
                    completionRate = 0f,
                    isPaused = false
                )
            }
            
            startBreathingAnimation()
        }
    }
    
    private fun startBreathingAnimation() {
        sessionJob?.cancel()
        sessionJob = viewModelScope.launch {
            while (_uiState.value.isSessionActive && _uiState.value.remainingTimeMillis > 0) {
                if (!_uiState.value.isPaused) {
                    // Update timer
                    val newRemainingTime = _uiState.value.remainingTimeMillis - 1000
                    val completionRate = 1f - (newRemainingTime.toFloat() / _uiState.value.totalDurationMillis.toFloat())
                    
                    _uiState.update {
                        it.copy(
                            remainingTimeMillis = newRemainingTime.coerceAtLeast(0),
                            completionRate = completionRate.coerceIn(0f, 1f)
                        )
                    }
                    
                    // Update breathing phase
                    updateBreathingPhase()
                    
                    // Check if session is complete
                    if (newRemainingTime <= 0) {
                        completeSession()
                        break
                    }
                }
                
                delay(1000)
            }
        }
    }
    
    private fun updateBreathingPhase() {
        val sessionType = _uiState.value.sessionType
        val elapsed = _uiState.value.totalDurationMillis - _uiState.value.remainingTimeMillis
        
        when (sessionType) {
            "basic" -> updateBasicBreathing(elapsed)
            "box" -> updateBoxBreathing(elapsed)
            "478" -> update478Breathing(elapsed)
            "calming" -> updateCalmingBreathing(elapsed)
            else -> updateBasicBreathing(elapsed)
        }
    }
    
    private fun updateBasicBreathing(elapsed: Long) {
        // Simple 4-4 breathing: 4 seconds inhale, 4 seconds exhale
        val cycleLength = 8000L // 8 seconds total
        val phaseInCycle = elapsed % cycleLength
        
        when {
            phaseInCycle < 4000 -> {
                // Inhaling
                val progress = phaseInCycle / 4000f
                _uiState.update {
                    it.copy(
                        currentPhase = "inhale",
                        animationPhase = progress
                    )
                }
            }
            else -> {
                // Exhaling
                val progress = 1f - ((phaseInCycle - 4000) / 4000f)
                _uiState.update {
                    it.copy(
                        currentPhase = "exhale",
                        animationPhase = progress
                    )
                }
            }
        }
    }
    
    private fun updateBoxBreathing(elapsed: Long) {
        // Box breathing: 4-4-4-4 (inhale-hold-exhale-hold)
        val cycleLength = 16000L // 16 seconds total
        val phaseInCycle = elapsed % cycleLength
        
        when {
            phaseInCycle < 4000 -> {
                // Inhaling
                val progress = phaseInCycle / 4000f
                _uiState.update {
                    it.copy(
                        currentPhase = "inhale",
                        animationPhase = progress
                    )
                }
            }
            phaseInCycle < 8000 -> {
                // Hold after inhale
                _uiState.update {
                    it.copy(
                        currentPhase = "hold",
                        animationPhase = 1f
                    )
                }
            }
            phaseInCycle < 12000 -> {
                // Exhaling
                val progress = 1f - ((phaseInCycle - 8000) / 4000f)
                _uiState.update {
                    it.copy(
                        currentPhase = "exhale",
                        animationPhase = progress
                    )
                }
            }
            else -> {
                // Hold after exhale
                _uiState.update {
                    it.copy(
                        currentPhase = "hold",
                        animationPhase = 0f
                    )
                }
            }
        }
    }
    
    private fun update478Breathing(elapsed: Long) {
        // 4-7-8 breathing: 4 inhale, 7 hold, 8 exhale
        val cycleLength = 19000L // 19 seconds total
        val phaseInCycle = elapsed % cycleLength
        
        when {
            phaseInCycle < 4000 -> {
                // Inhaling (4 seconds)
                val progress = phaseInCycle / 4000f
                _uiState.update {
                    it.copy(
                        currentPhase = "inhale",
                        animationPhase = progress
                    )
                }
            }
            phaseInCycle < 11000 -> {
                // Hold (7 seconds)
                _uiState.update {
                    it.copy(
                        currentPhase = "hold",
                        animationPhase = 1f
                    )
                }
            }
            else -> {
                // Exhaling (8 seconds)
                val progress = 1f - ((phaseInCycle - 11000) / 8000f)
                _uiState.update {
                    it.copy(
                        currentPhase = "exhale",
                        animationPhase = progress
                    )
                }
            }
        }
    }
    
    private fun updateCalmingBreathing(elapsed: Long) {
        // Extended calm breathing: 6 inhale, 2 hold, 8 exhale
        val cycleLength = 16000L // 16 seconds total
        val phaseInCycle = elapsed % cycleLength
        
        when {
            phaseInCycle < 6000 -> {
                // Inhaling (6 seconds)
                val progress = phaseInCycle / 6000f
                _uiState.update {
                    it.copy(
                        currentPhase = "inhale",
                        animationPhase = progress
                    )
                }
            }
            phaseInCycle < 8000 -> {
                // Hold (2 seconds)
                _uiState.update {
                    it.copy(
                        currentPhase = "hold",
                        animationPhase = 1f
                    )
                }
            }
            else -> {
                // Exhaling (8 seconds)
                val progress = 1f - ((phaseInCycle - 8000) / 8000f)
                _uiState.update {
                    it.copy(
                        currentPhase = "exhale",
                        animationPhase = progress
                    )
                }
            }
        }
    }
    
    fun pauseSession() {
        _uiState.update { it.copy(isPaused = !it.isPaused) }
    }
    
    fun endSession() {
        sessionJob?.cancel()
        
        viewModelScope.launch {
            val session = mindfulnessUseCase.endSession(
                sessionId = currentSessionId,
                completionRate = _uiState.value.completionRate,
                endTime = System.currentTimeMillis()
            )
            
            _uiState.update {
                it.copy(
                    isSessionActive = false,
                    showCompletionDialog = true,
                    completedSession = session
                )
            }
        }
    }
    
    private suspend fun completeSession() {
        val session = mindfulnessUseCase.endSession(
            sessionId = currentSessionId,
            completionRate = 1f,
            endTime = System.currentTimeMillis()
        )
        
        _uiState.update {
            it.copy(
                isSessionActive = false,
                showCompletionDialog = true,
                completedSession = session,
                remainingTimeMillis = 0
            )
        }
    }
    
    fun submitSessionFeedback(rating: Int, notes: String) {
        viewModelScope.launch {
            mindfulnessUseCase.updateSessionFeedback(
                sessionId = currentSessionId,
                rating = rating,
                notes = notes
            )
            dismissCompletionDialog()
        }
    }
    
    fun dismissCompletionDialog() {
        _uiState.update {
            it.copy(
                showCompletionDialog = false,
                completedSession = null
            )
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        sessionJob?.cancel()
    }
}