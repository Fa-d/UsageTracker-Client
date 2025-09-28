package dev.sadakat.screentimetracker.shared.presentation.dashboard

import dev.sadakat.screentimetracker.shared.domain.model.AppUsageInfo

data class DashboardUiState(
    val totalScreenTimeMs: Long = 0L,
    val totalScreenTimeFormatted: String = "0h 0m",
    val pickupsToday: Int = 0,
    val wellnessScore: Float = 0f,
    val topApps: List<AppUsageInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)