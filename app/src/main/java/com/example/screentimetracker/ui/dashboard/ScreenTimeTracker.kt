package com.example.screentimetracker.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.screentimetracker.ui.theme.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.screentimetracker.ui.appsearch.AppSearchScreen
import com.example.screentimetracker.ui.components.PlayfulBottomNav
import com.example.screentimetracker.ui.limiter.LimiterConfigScreen

val LocalDashboardViewModel = staticCompositionLocalOf<DashboardViewModel> { error("No DashboardViewModel provided") }

@Composable
fun ScreenTimeTracker(viewModel: DashboardViewModel, padding: WindowInsets) {
    CompositionLocalProvider(LocalDashboardViewModel provides viewModel) {
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        LaunchedEffect(Unit) { viewModel.loadData() }
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        var darkMode by remember { mutableStateOf(false) }
        var focusMode by remember { mutableStateOf(false) }
        var privacyMode by remember { mutableStateOf(false) }
        var syncEnabled by remember { mutableStateOf(true) }
        var selectedDate by remember { mutableStateOf("today") }
        var expandedCategory by remember { mutableStateOf<Int?>(null) }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding.asPaddingValues()),
            color = if (darkMode) Color(0xFF18181B) else PlayfulPrimary.copy(alpha = 0.02f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Playful Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (darkMode) {
                                Modifier.background(Color(0xFF27272A))
                            } else {
                                Modifier.background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            PlayfulPrimary.copy(alpha = 0.1f),
                                            VibrantOrange.copy(alpha = 0.1f),
                                            LimeGreen.copy(alpha = 0.1f)
                                        )
                                    )
                                )
                            }
                        )
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "📱 Screen Time Tracker",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (darkMode) Color.White else PlayfulPrimary
                            )
                            Text(
                                text = "Your digital wellness companion ✨",
                                fontSize = 14.sp,
                                color = if (darkMode) Color.White.copy(alpha = 0.7f) 
                                      else PlayfulPrimary.copy(alpha = 0.7f)
                            )
                        }
                        if (focusMode) {
                            Text(
                                text = "🧘",
                                fontSize = 24.sp,
                                modifier = Modifier
                                    .background(
                                        LavenderPurple.copy(alpha = 0.2f),
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                                    .padding(8.dp)
                            )
                        }
                    }
                }

                // Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    NavHost(navController = navController, startDestination = "dashboard_route") {
                        composable("dashboard_route") {
                            DashboardView(expandedCategory, { expandedCategory = it }, state)
                        }

                        composable("timeline_route") {
                            TimelineScreen()
                        }
                        composable("goals_route") {
                            GoalsView(focusMode) { focusMode = it }
                        }
                        composable("limiter_route") {
                            LimiterConfigScreen()
                        }
                        composable("settings_route") {
                            SettingsView(
                                darkMode,
                                { darkMode = it },
                                privacyMode,
                                { privacyMode = it },
                                syncEnabled,
                                { syncEnabled = it })
                        }
                        composable("app_search_route") {
                            AppSearchScreen(navController, viewModel)
                        }
                    }
                }

                // Playful Bottom Navigation
                PlayfulBottomNav(
                    navController = navController,
                    currentRoute = currentRoute,
                    modifier = if (darkMode) {
                        Modifier.background(Color(0xFF27272A))
                    } else {
                        Modifier.background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.95f),
                                    PlayfulPrimary.copy(alpha = 0.05f),
                                    VibrantOrange.copy(alpha = 0.05f)
                                )
                            )
                        )
                    }
                )
            }
        }
    }

}