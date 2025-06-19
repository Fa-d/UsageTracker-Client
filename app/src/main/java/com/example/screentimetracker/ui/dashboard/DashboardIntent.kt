package com.example.screentimetracker.ui.dashboard

sealed class DashboardIntent {
    object LoadDashboardData : DashboardIntent()
    // Example: data class AppClicked(val packageName: String) : DashboardIntent()
    // Add more intents as user interactions are defined
}
