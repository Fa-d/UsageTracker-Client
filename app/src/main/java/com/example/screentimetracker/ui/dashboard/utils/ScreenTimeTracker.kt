package com.example.screentimetracker.ui.dashboard.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.screentimetracker.ui.appsearch.AppSearchScreen
import com.example.screentimetracker.ui.components.PlayfulBottomNav
import com.example.screentimetracker.ui.dashboard.screens.DashboardView
import com.example.screentimetracker.ui.dashboard.screens.TimelineScreen
import com.example.screentimetracker.ui.dashboard.screens.GoalsView
import com.example.screentimetracker.ui.dashboard.screens.SettingsView
import com.example.screentimetracker.ui.dashboard.viewmodels.DashboardViewModel
import com.example.screentimetracker.ui.personalization.PersonalizationViewModel
import com.example.screentimetracker.ui.progressivelimits.screens.ProgressiveLimitsScreen
import com.example.screentimetracker.ui.limiter.screens.LimiterConfigScreen
import com.example.screentimetracker.ui.privacy.screens.PrivacySettingsScreen
import com.example.screentimetracker.ui.mindfulness.screens.BreathingExerciseScreen
import com.example.screentimetracker.ui.replacementactivities.screens.ReplacementActivitiesScreen
import com.example.screentimetracker.ui.theme.PlayfulPrimary
import com.example.screentimetracker.ui.theme.VibrantOrange

val LocalDashboardViewModel = staticCompositionLocalOf<DashboardViewModel> { error("No DashboardViewModel provided") }

@Composable
fun ScreenTimeTracker(viewModel: DashboardViewModel) {
    val personalizationViewModel: PersonalizationViewModel = hiltViewModel()
    CompositionLocalProvider(LocalDashboardViewModel provides viewModel) {
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val achievements by viewModel.achievements.collectAsStateWithLifecycle()
        val wellnessScore by viewModel.wellnessScore.collectAsStateWithLifecycle()
        val personalizationState by personalizationViewModel.uiState.collectAsStateWithLifecycle()
        
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
            modifier = Modifier.fillMaxSize(),
            color = if (darkMode) Color(0xFF18181B) else PlayfulPrimary.copy(alpha = 0.02f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                /*             // Playful Header
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
             */
                // Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    NavHost(navController = navController, startDestination = "dashboard_route") {
                        composable("dashboard_route") {
                            DashboardView(
                                expandedCategory = expandedCategory,
                                onCategoryExpand = { expandedCategory = it },
                                state = state,
                                achievements = viewModel.achievements,
                                wellnessScore = viewModel.wellnessScore
                            )
                        }

                        composable("timeline_route") {
                            TimelineScreen()
                        }
                        composable("goals_route") {
                            GoalsView(focusMode) { focusMode = it }
                        }
                        composable("settings_route") {
                            SettingsView(
                                darkMode,
                                { darkMode = it },
                                privacyMode,
                                { privacyMode = it },
                                syncEnabled,
                                { syncEnabled = it },
                                preferences = personalizationState.preferences,
                                onThemeModeChanged = personalizationViewModel::updateThemeMode,
                                onColorSchemeChanged = personalizationViewModel::updateColorScheme,
                                onPersonalityModeChanged = personalizationViewModel::updatePersonalityMode,
                                onMotivationalMessagesChanged = personalizationViewModel::updateMotivationalMessages,
                                onAchievementCelebrationsChanged = personalizationViewModel::updateAchievementCelebrations,
                                onBreakRemindersChanged = personalizationViewModel::updateBreakReminders,
                                onWellnessCoachingChanged = personalizationViewModel::updateWellnessCoaching,
                                onDashboardLayoutChanged = personalizationViewModel::updateDashboardLayout,
                                onNavigateToProgressiveLimits = {
                                    navController.navigate("progressive_limits_route")
                                },
                                onNavigateToLimiterConfig = {
                                    navController.navigate("limiter_config_route")
                                },
                                onNavigateToPrivacySettings = {
                                    navController.navigate("privacy_settings_route")
                                },
                                onNavigateToMindfulness = {
                                    navController.navigate("mindfulness_route")
                                },
                                onNavigateToReplacementActivities = {
                                    navController.navigate("replacement_activities_route")
                                }
                            )
                        }
                        composable("app_search_route") {
                            AppSearchScreen(navController, viewModel)
                        }
                        composable("progressive_limits_route") {
                            ProgressiveLimitsScreen(
                                onBackPressed = { navController.popBackStack() },
                                onAddNewLimit = { 
                                    // Handle add new limit
                                }
                            )
                        }
                        composable("limiter_config_route") {
                            LimiterConfigScreen()
                        }
                        composable("privacy_settings_route") {
                            PrivacySettingsScreen(
                                onBackPressed = { navController.popBackStack() }
                            )
                        }
                        composable("mindfulness_route") {
                            BreathingExerciseScreen(
                                onBackPressed = { navController.popBackStack() }
                            )
                        }
                        composable("replacement_activities_route") {
                            ReplacementActivitiesScreen(
                                onBackPressed = { navController.popBackStack() }
                            )
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