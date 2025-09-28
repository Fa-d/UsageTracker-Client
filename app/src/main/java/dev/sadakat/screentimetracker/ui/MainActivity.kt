package dev.sadakat.screentimetracker.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.sadakat.screentimetracker.shared.presentation.analytics.AnalyticsScreen
import dev.sadakat.screentimetracker.shared.presentation.dashboard.DashboardScreen
import dev.sadakat.screentimetracker.shared.presentation.goals.GoalsScreen
import dev.sadakat.screentimetracker.shared.presentation.settings.SettingsScreen
import dev.sadakat.screentimetracker.shared.presentation.wellness.WellnessScreen
import dev.sadakat.screentimetracker.shared.presentation.dashboard.DashboardUiState
import dev.sadakat.screentimetracker.shared.presentation.analytics.AnalyticsUiState
import dev.sadakat.screentimetracker.shared.presentation.wellness.WellnessUiState
import dev.sadakat.screentimetracker.shared.presentation.goals.GoalsUiState
import dev.sadakat.screentimetracker.shared.presentation.settings.SettingsUiState

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScreenTimeTrackerApp()
        }
    }
}

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Dashboard : BottomNavItem("dashboard", Icons.Default.Home, "Dashboard")
    object Analytics : BottomNavItem("analytics", Icons.Default.Info, "Analytics")
    object Wellness : BottomNavItem("wellness", Icons.Default.Favorite, "Wellness")
    object Goals : BottomNavItem("goals", Icons.Default.Star, "Goals")
    object Settings : BottomNavItem("settings", Icons.Default.Settings, "Settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTimeTrackerApp() {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Analytics,
        BottomNavItem.Wellness,
        BottomNavItem.Goals,
        BottomNavItem.Settings
    )

    MaterialTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                BottomNavigationBar(navController, items)
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = BottomNavItem.Dashboard.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(BottomNavItem.Dashboard.route) {
                    DashboardScreen(
                        uiState = DashboardUiState(), // Default empty state
                        onTakeBreak = {},
                        onNavigateToGoals = { navController.navigate(BottomNavItem.Goals.route) },
                        onNavigateToWellness = { navController.navigate(BottomNavItem.Wellness.route) }
                    )
                }
                composable(BottomNavItem.Analytics.route) {
                    AnalyticsScreen(
                        uiState = AnalyticsUiState(),
                        onTimeRangeSelected = {}
                    )
                }
                composable(BottomNavItem.Wellness.route) {
                    WellnessScreen(
                        uiState = WellnessUiState(),
                        onMetricAction = {},
                        onStartBreak = {},
                        onScheduleBreak = {},
                        onStartExercise = {}
                    )
                }
                composable(BottomNavItem.Goals.route) {
                    GoalsScreen(
                        uiState = GoalsUiState(),
                        onCategorySelected = {},
                        onToggleGoalCompletion = {},
                        onEditGoal = {},
                        onDeleteGoal = {},
                        onShowCreateGoalDialog = {},
                        onHideCreateGoalDialog = {},
                        onCreateGoal = { _, _, _ -> }
                    )
                }
                composable(BottomNavItem.Settings.route) {
                    SettingsScreen(
                        uiState = SettingsUiState(),
                        onSettingChanged = { _, _ -> },
                        onSettingClicked = {}
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    items: List<BottomNavItem>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
