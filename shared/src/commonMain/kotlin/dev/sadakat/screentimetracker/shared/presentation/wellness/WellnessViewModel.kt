package dev.sadakat.screentimetracker.shared.presentation.wellness

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WellnessViewModel(
    private val coroutineScope: CoroutineScope
) {

    private val _uiState = MutableStateFlow(WellnessUiState())
    val uiState: StateFlow<WellnessUiState> = _uiState.asStateFlow()

    init {
        loadWellnessData()
    }

    private fun loadWellnessData() {
        coroutineScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // TODO: Load actual data from repositories
                val wellnessMetrics = listOf(
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
                )

                val mindfulnessExercises = listOf(
                    MindfulnessExercise("Breathing Exercise", "5 minutes", "🧘"),
                    MindfulnessExercise("Body Scan", "10 minutes", "🧠"),
                    MindfulnessExercise("Walking Meditation", "15 minutes", "🚶"),
                    MindfulnessExercise("Quick Mindfulness", "2 minutes", "⚡")
                )

                val insights = listOf(
                    "Your screen time decreased by 15 minutes today",
                    "You took 2 more breaks than yesterday",
                    "Most active usage was during lunch hours"
                )

                _uiState.value = _uiState.value.copy(
                    wellnessScore = 0.72f,
                    scoreTrend = "3% up from yesterday",
                    insights = insights,
                    wellnessMetrics = wellnessMetrics,
                    todayBreaks = 4,
                    suggestedBreakTime = "in 45 minutes",
                    mindfulnessStreak = 7,
                    todayMindfulnessProgress = 0.6f,
                    mindfulnessExercises = mindfulnessExercises,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun handleMetricAction(metricType: WellnessMetricType) {
        when (metricType) {
            WellnessMetricType.SCREEN_TIME -> setScreenTimeLimit()
            WellnessMetricType.PICKUPS -> reduceNotifications()
            WellnessMetricType.BREAKS -> scheduleBreak()
            WellnessMetricType.SLEEP_QUALITY -> improveSleepQuality()
        }
    }

    fun startBreak() {
        // TODO: Implement break functionality
        coroutineScope.launch {
            // Start break timer and update state
        }
    }

    fun scheduleBreak() {
        // TODO: Implement break scheduling
        coroutineScope.launch {
            // Schedule next break reminder
        }
    }

    fun startMindfulnessExercise(exercise: MindfulnessExercise) {
        // TODO: Implement mindfulness exercise functionality
        coroutineScope.launch {
            // Start mindfulness exercise session
        }
    }

    fun refresh() {
        loadWellnessData()
    }

    private fun setScreenTimeLimit() {
        // TODO: Implement screen time limit setting
        coroutineScope.launch {
            // Navigate to screen time limit settings
        }
    }

    private fun reduceNotifications() {
        // TODO: Implement notification reduction
        coroutineScope.launch {
            // Navigate to notification settings
        }
    }

    private fun improveSleepQuality() {
        // TODO: Implement sleep quality improvement
        coroutineScope.launch {
            // Navigate to sleep quality settings
        }
    }
}