package dev.sadakat.screentimetracker.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    // TODO: Add repositories when available
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            // TODO: Load actual data from repositories
            _uiState.value = DashboardUiState(
                totalScreenTime = "4h 32m",
                pickupsToday = 67,
                wellnessScore = 0.72f,
                topApps = listOf(
                    AppUsageInfo("Instagram", "1h 45m", null),
                    AppUsageInfo("WhatsApp", "52m", null),
                    AppUsageInfo("YouTube", "1h 12m", null),
                    AppUsageInfo("Chrome", "35m", null)
                )
            )
        }
    }

    fun setBreak() {
        // TODO: Implement break functionality
    }

    fun navigateToGoals() {
        // TODO: Implement navigation to goals
    }

    fun navigateToWellness() {
        // TODO: Implement navigation to wellness
    }
}

data class DashboardUiState(
    val totalScreenTime: String = "0h 0m",
    val pickupsToday: Int = 0,
    val wellnessScore: Float = 0f,
    val topApps: List<AppUsageInfo> = emptyList(),
    val isLoading: Boolean = false
)

data class AppUsageInfo(
    val name: String,
    val usageTime: String,
    val iconRes: Int?
)