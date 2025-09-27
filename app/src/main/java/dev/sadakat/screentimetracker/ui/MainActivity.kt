package dev.sadakat.screentimetracker.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.sadakat.screentimetracker.core.navigation.ScreenTimeBottomNavigationBar
import dev.sadakat.screentimetracker.core.navigation.ScreenTimeNavigation
import dev.sadakat.screentimetracker.feature.analytics.AnalyticsScreen
import dev.sadakat.screentimetracker.feature.dashboard.DashboardScreen
import dev.sadakat.screentimetracker.feature.goals.GoalsScreen
import dev.sadakat.screentimetracker.feature.settings.SettingsScreen
import dev.sadakat.screentimetracker.feature.wellness.WellnessScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScreenTimeTrackerApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTimeTrackerApp() {
    val navController = rememberNavController()

    MaterialTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                ScreenTimeBottomNavigationBar(navController)
            }
        ) { innerPadding ->
            ScreenTimeNavigation(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                dashboardScreen = { DashboardScreen() },
                analyticsScreen = { AnalyticsScreen() },
                wellnessScreen = { WellnessScreen() },
                goalsScreen = { GoalsScreen() },
                settingsScreen = { SettingsScreen() }
            )
        }
    }
}
