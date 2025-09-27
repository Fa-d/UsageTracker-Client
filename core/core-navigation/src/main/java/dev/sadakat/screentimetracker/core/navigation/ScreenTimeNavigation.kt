package dev.sadakat.screentimetracker.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun ScreenTimeNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = ScreenTimeDestinations.DASHBOARD,
    dashboardScreen: @Composable () -> Unit = { DashboardScreenDestination() },
    analyticsScreen: @Composable () -> Unit = { AnalyticsScreenDestination() },
    wellnessScreen: @Composable () -> Unit = { WellnessScreenDestination() },
    goalsScreen: @Composable () -> Unit = { GoalsScreenDestination() },
    settingsScreen: @Composable () -> Unit = { SettingsScreenDestination() }
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(ScreenTimeDestinations.DASHBOARD) {
            dashboardScreen()
        }

        composable(ScreenTimeDestinations.ANALYTICS) {
            analyticsScreen()
        }

        composable(ScreenTimeDestinations.WELLNESS) {
            wellnessScreen()
        }

        composable(ScreenTimeDestinations.GOALS) {
            goalsScreen()
        }

        composable(ScreenTimeDestinations.SETTINGS) {
            settingsScreen()
        }
    }
}

@Composable
fun DashboardScreenDestination() {
    // Default placeholder - to be overridden by consumer
}

@Composable
fun AnalyticsScreenDestination() {
    // Default placeholder - to be overridden by consumer
}

@Composable
fun WellnessScreenDestination() {
    // Default placeholder - to be overridden by consumer
}

@Composable
fun GoalsScreenDestination() {
    // Default placeholder - to be overridden by consumer
}

@Composable
fun SettingsScreenDestination() {
    // Default placeholder - to be overridden by consumer
}