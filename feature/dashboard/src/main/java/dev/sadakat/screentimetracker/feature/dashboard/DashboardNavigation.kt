package dev.sadakat.screentimetracker.feature.dashboard

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val DASHBOARD_ROUTE = "dashboard"

/**
 * Navigation extension for Dashboard feature
 */
fun NavController.navigateToDashboard() {
    navigate(DASHBOARD_ROUTE)
}

/**
 * Navigation graph builder for Dashboard feature
 */
fun NavGraphBuilder.dashboardScreen(
    onNavigateToDetails: (String) -> Unit
) {
    composable(DASHBOARD_ROUTE) {
        DashboardScreen(onNavigateToDetails = onNavigateToDetails)
    }
}