package dev.sadakat.screentimetracker.shared.presentation.dashboard

import dev.sadakat.screentimetracker.shared.domain.usecase.GetDashboardDataUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val coroutineScope: CoroutineScope
) {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        coroutineScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                getDashboardDataUseCase()
                    .onEach { dashboardData ->
                        _uiState.value = DashboardUiState(
                            totalScreenTimeMs = dashboardData.totalScreenTimeMs,
                            totalScreenTimeFormatted = dashboardData.totalScreenTimeFormatted,
                            pickupsToday = dashboardData.pickupsToday,
                            wellnessScore = dashboardData.wellnessScore,
                            topApps = dashboardData.topApps,
                            isLoading = false
                        )
                    }
                    .launchIn(coroutineScope)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun onTakeBreak() {
        // TODO: Implement break functionality
    }

    fun onNavigateToGoals() {
        // TODO: Implement navigation to goals
    }

    fun onNavigateToWellness() {
        // TODO: Implement navigation to wellness
    }

    fun refresh() {
        loadDashboardData()
    }
}