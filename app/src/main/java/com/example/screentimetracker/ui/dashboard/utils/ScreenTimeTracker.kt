package com.example.screentimetracker.ui.dashboard.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.screentimetracker.ui.ai.viewmodels.AIViewModel
import com.example.screentimetracker.ui.analytics.AnalyticsScreen
import com.example.screentimetracker.ui.appsearch.AppSearchScreen
import com.example.screentimetracker.ui.components.PlayfulBottomNav
import com.example.screentimetracker.ui.dashboard.screens.GoalsView
import com.example.screentimetracker.ui.dashboard.screens.SimpleDashboardView
import com.example.screentimetracker.ui.dashboard.screens.SimpleSettingsView
import com.example.screentimetracker.ui.dashboard.screens.TimelineScreen
import com.example.screentimetracker.ui.dashboard.viewmodels.DashboardViewModel
import com.example.screentimetracker.ui.habits.screens.HabitsScreen
import com.example.screentimetracker.ui.limiter.screens.LimiterConfigScreen
import com.example.screentimetracker.ui.mindfulness.screens.BreathingExerciseScreen
import com.example.screentimetracker.ui.personalization.PersonalizationViewModel
import com.example.screentimetracker.ui.privacy.screens.PrivacySettingsScreen
import com.example.screentimetracker.ui.progressivelimits.screens.ProgressiveLimitsScreen
import com.example.screentimetracker.ui.replacementactivities.screens.ReplacementActivitiesScreen
import com.example.screentimetracker.ui.smartgoals.screens.SmartGoalsScreen
import com.example.screentimetracker.ui.timerestrictions.screens.TimeRestrictionsScreen
import com.example.screentimetracker.ui.wellness.WellnessScreen
import com.example.screentimetracker.ui.ai.AISettingsScreen

val LocalDashboardViewModel = staticCompositionLocalOf<DashboardViewModel> { error("No DashboardViewModel provided") }

@Composable
fun ScreenTimeTracker(viewModel: DashboardViewModel) {
    val personalizationViewModel: PersonalizationViewModel = hiltViewModel()
    val aiViewModel: AIViewModel = hiltViewModel()
    CompositionLocalProvider(LocalDashboardViewModel provides viewModel) {
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val aiState by aiViewModel.uiState.collectAsStateWithLifecycle()
        val achievements by viewModel.achievements.collectAsStateWithLifecycle()
        val wellnessScore by viewModel.wellnessScore.collectAsStateWithLifecycle()
        val digitalPet by viewModel.digitalPet.collectAsStateWithLifecycle()
        val petStats by viewModel.petStats.collectAsStateWithLifecycle()
        val personalizationState by personalizationViewModel.uiState.collectAsStateWithLifecycle()
        
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        var privacyMode by remember { mutableStateOf(false) }
        var syncEnabled by remember { mutableStateOf(true) }
        var selectedDate by remember { mutableStateOf("today") }
        var expandedCategory by remember { mutableStateOf<Int?>(null) }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    NavHost(navController = navController, startDestination = "dashboard_route") {
                        composable("dashboard_route") {
                            SimpleDashboardView(
                                state = state,
                                digitalPet = digitalPet,
                                petStats = petStats,
                                aiInsights = aiState.insights,
                                isLoadingInsights = aiState.isLoadingInsights,
                                onNavigateToAnalytics = { navController.navigate("analytics_route") },
                                onNavigateToWellness = { navController.navigate("wellness_route") },
                                onNavigateToHabits = { navController.navigate("habits_route") },
                                onNavigateToTimeRestrictions = { navController.navigate("time_restrictions_route") },
                                onPetClick = { 
                                    // Refresh pet wellness when clicked
                                    viewModel.refreshPetWellness()
                                },
                                onPetInteraction = { 
                                    viewModel.interactWithPet()
                                },
                                onPetFeed = { 
                                    viewModel.feedPet()
                                },
                                onRefreshAIInsights = {
                                    aiViewModel.generateAIInsights()
                                }
                            )
                        }

                        composable("timeline_route") {
                            TimelineScreen()
                        }
                        composable("goals_route") {
                            GoalsView(
                                focusMode = false, // Legacy parameter, no longer used
                                onFocusModeChange = { }, // Legacy parameter, no longer used
                                onNavigateToSmartGoals = { navController.navigate("smart_goals_route") }
                            )
                        }
                        composable("settings_route") {
                            SimpleSettingsView(
                                currentThemeMode = com.example.screentimetracker.data.local.ThemeMode.valueOf(personalizationState.preferences.themeMode),
                                onThemeModeChange = personalizationViewModel::updateThemeMode,
                                onNavigateToAdvancedSettings = { navController.navigate("advanced_settings_route") },
                                preferences = personalizationState.preferences,
                                onColorSchemeChanged = personalizationViewModel::updateColorScheme,
                                onMotivationalMessagesChanged = personalizationViewModel::updateMotivationalMessages,
                                onAchievementCelebrationsChanged = personalizationViewModel::updateAchievementCelebrations,
                                onBreakRemindersChanged = personalizationViewModel::updateBreakReminders,
                                onWellnessCoachingChanged = personalizationViewModel::updateWellnessCoaching,
                                privacyMode = privacyMode,
                                onPrivacyModeChange = { privacyMode = it },
                                syncEnabled = syncEnabled,
                                onSyncEnabledChange = { syncEnabled = it },
                                usageAlertsEnabled = personalizationState.preferences.achievementCelebrationsEnabled,
                                onUsageAlertsChange = personalizationViewModel::updateAchievementCelebrations,
                                goalRemindersEnabled = personalizationState.preferences.breakRemindersEnabled,
                                onGoalRemindersChange = personalizationViewModel::updateBreakReminders,
                                onNavigateToProgressiveLimits = {
                                    navController.navigate("progressive_limits_route")
                                },
                                onNavigateToLimiterConfig = {
                                    navController.navigate("limiter_config_route")
                                },
                                onNavigateToPrivacySettings = {
                                    navController.navigate("privacy_settings_route")
                                },
                                onNavigateToAISettings = {
                                    navController.navigate("ai_settings_route")
                                },
                                onNavigateToMindfulness = {
                                    navController.navigate("mindfulness_route")
                                },
                                onNavigateToReplacementActivities = {
                                    navController.navigate("replacement_activities_route")
                                },
                                onExportData = {
                                    // Implement data export functionality
                                    viewModel.exportUsageData()
                                },
                                onClearCache = {
                                    // Implement cache clearing functionality
                                    viewModel.clearCache()
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
                        composable("ai_settings_route") {
                            AISettingsScreen(
                                viewModel = aiViewModel,
                                onNavigateBack = { navController.popBackStack() }
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
                        composable("habits_route") {
                            HabitsScreen()
                        }
                        composable("smart_goals_route") {
                            SmartGoalsScreen()
                        }
                        composable("time_restrictions_route") {
                            TimeRestrictionsScreen()
                        }
                        composable("analytics_route") {
                            AnalyticsScreen(
                                state = state,
                                expandedCategory = expandedCategory,
                                onCategoryExpand = { expandedCategory = it })
                        }
                        composable("wellness_route") {
                            WellnessScreen(
                                onNavigateToHabits = { navController.navigate("habits_route") },
                                onNavigateToMindfulness = { navController.navigate("mindfulness_route") },
                                viewModel = viewModel
                            )
                        }

                    }
                }

                // Playful Bottom Navigation
                PlayfulBottomNav(
                    navController = navController,
                    currentRoute = currentRoute,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                )
            }
        }
    }

}