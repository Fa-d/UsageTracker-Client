package dev.sadakat.screentimetracker.feature.wellness

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WellnessViewModel @Inject constructor(
    // TODO: Add repositories when available
) : ViewModel() {

    private val _uiState = MutableStateFlow(WellnessUiState())
    val uiState: StateFlow<WellnessUiState> = _uiState.asStateFlow()

    init {
        loadWellnessData()
    }

    private fun loadWellnessData() {
        viewModelScope.launch {
            // TODO: Load actual data from repositories
            _uiState.value = WellnessUiState(
                wellnessScore = 0.72f,
                scoreTrend = "3% up from yesterday",
                insights = listOf(
                    "Your screen time decreased by 15 minutes today",
                    "You took 2 more breaks than yesterday",
                    "Most active usage was during lunch hours"
                ),
                wellnessMetrics = listOf(
                    WellnessMetric(
                        type = WellnessMetricType.SCREEN_TIME,
                        title = "Daily Screen Time",
                        description = "Target: 4 hours",
                        value = "4h 32m",
                        progress = 0.88f,
                        status = WellnessStatus.WARNING,
                        actionText = "Set Limit"
                    ),
                    WellnessMetric(
                        type = WellnessMetricType.PICKUPS,
                        title = "Phone Pickups",
                        description = "Target: 50 per day",
                        value = "67",
                        progress = 1.34f,
                        status = WellnessStatus.CRITICAL,
                        actionText = "Reduce Notifications"
                    ),
                    WellnessMetric(
                        type = WellnessMetricType.BREAKS,
                        title = "Digital Breaks",
                        description = "Target: 6 per day",
                        value = "4",
                        progress = 0.67f,
                        status = WellnessStatus.WARNING,
                        actionText = "Schedule Break"
                    ),
                    WellnessMetric(
                        type = WellnessMetricType.SLEEP_QUALITY,
                        title = "Sleep Quality",
                        description = "Based on bedtime phone usage",
                        value = "Good",
                        progress = 0.8f,
                        status = WellnessStatus.GOOD,
                        actionText = ""
                    )
                ),
                todayBreaks = 4,
                suggestedBreakTime = "in 45 minutes",
                mindfulnessStreak = 7,
                todayMindfulnessProgress = 0.6f,
                mindfulnessExercises = listOf(
                    MindfulnessExercise("Breathing Exercise", "5 minutes", "🧘"),
                    MindfulnessExercise("Body Scan", "10 minutes", "🧠"),
                    MindfulnessExercise("Walking Meditation", "15 minutes", "🚶"),
                    MindfulnessExercise("Quick Mindfulness", "2 minutes", "⚡")
                )
            )
        }
    }

    fun handleMetricAction(metricType: WellnessMetricType) {
        when (metricType) {
            WellnessMetricType.SCREEN_TIME -> setScreenTimeLimit()
            WellnessMetricType.PICKUPS -> reduceNotifications()
            WellnessMetricType.BREAKS -> scheduleBreak()
            WellnessMetricType.SLEEP_QUALITY -> improveSeepQuality()
        }
    }

    fun startBreak() {
        // TODO: Implement break functionality
    }

    fun scheduleBreak() {
        // TODO: Implement break scheduling
    }

    fun startMindfulnessExercise(exercise: MindfulnessExercise) {
        // TODO: Implement mindfulness exercise functionality
    }

    private fun setScreenTimeLimit() {
        // TODO: Implement screen time limit setting
    }

    private fun reduceNotifications() {
        // TODO: Implement notification reduction
    }

    private fun improveSeepQuality() {
        // TODO: Implement sleep quality improvement
    }
}

data class WellnessUiState(
    val wellnessScore: Float = 0f,
    val scoreTrend: String = "",
    val insights: List<String> = emptyList(),
    val wellnessMetrics: List<WellnessMetric> = emptyList(),
    val todayBreaks: Int = 0,
    val suggestedBreakTime: String = "",
    val mindfulnessStreak: Int = 0,
    val todayMindfulnessProgress: Float = 0f,
    val mindfulnessExercises: List<MindfulnessExercise> = emptyList(),
    val isLoading: Boolean = false
)

data class WellnessMetric(
    val type: WellnessMetricType,
    val title: String,
    val description: String,
    val value: String,
    val progress: Float,
    val status: WellnessStatus,
    val actionText: String
)

enum class WellnessMetricType {
    SCREEN_TIME,
    PICKUPS,
    BREAKS,
    SLEEP_QUALITY
}

enum class WellnessStatus {
    GOOD,
    WARNING,
    CRITICAL
}

data class MindfulnessExercise(
    val name: String,
    val duration: String,
    val emoji: String
)