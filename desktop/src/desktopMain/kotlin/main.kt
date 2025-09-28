import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.sadakat.screentimetracker.shared.di.commonModule
import dev.sadakat.screentimetracker.shared.presentation.dashboard.DashboardScreen
import dev.sadakat.screentimetracker.shared.presentation.dashboard.DashboardUiState
import dev.sadakat.screentimetracker.shared.presentation.analytics.AnalyticsScreen
import dev.sadakat.screentimetracker.shared.presentation.analytics.AnalyticsUiState
import dev.sadakat.screentimetracker.shared.presentation.analytics.TimeRange
import dev.sadakat.screentimetracker.shared.presentation.analytics.AppUsageBreakdown
import dev.sadakat.screentimetracker.shared.presentation.wellness.WellnessScreen
import dev.sadakat.screentimetracker.shared.presentation.wellness.WellnessUiState
import dev.sadakat.screentimetracker.shared.presentation.wellness.WellnessMetric
import dev.sadakat.screentimetracker.shared.presentation.wellness.WellnessMetricType
import dev.sadakat.screentimetracker.shared.presentation.wellness.WellnessStatus
import dev.sadakat.screentimetracker.shared.presentation.wellness.MindfulnessExercise
import dev.sadakat.screentimetracker.shared.presentation.goals.GoalsScreen
import dev.sadakat.screentimetracker.shared.presentation.goals.GoalsUiState
import dev.sadakat.screentimetracker.shared.presentation.goals.Goal
import dev.sadakat.screentimetracker.shared.presentation.goals.GoalCategory
import dev.sadakat.screentimetracker.shared.presentation.settings.SettingsScreen
import dev.sadakat.screentimetracker.shared.presentation.settings.SettingsUiState
import dev.sadakat.screentimetracker.shared.presentation.settings.SettingsSection
import dev.sadakat.screentimetracker.shared.presentation.settings.SettingItem
import dev.sadakat.screentimetracker.shared.presentation.settings.SettingType
import org.koin.core.context.startKoin

fun main() = application {
    startKoin {
        modules(commonModule)
    }

    val windowState = rememberWindowState()

    Window(
        onCloseRequest = ::exitApplication,
        title = "Screen Time Tracker",
        state = windowState
    ) {
        MaterialTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                DesktopApp()
            }
        }
    }
}

sealed class NavigationItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Dashboard : NavigationItem("dashboard", Icons.Default.Home, "Dashboard")
    object Analytics : NavigationItem("analytics", Icons.Default.Info, "Analytics")
    object Wellness : NavigationItem("wellness", Icons.Default.Favorite, "Wellness")
    object Goals : NavigationItem("goals", Icons.Default.Star, "Goals")
    object Settings : NavigationItem("settings", Icons.Default.Settings, "Settings")
}

@Composable
fun DesktopApp() {
    var selectedRoute by remember { mutableStateOf(NavigationItem.Dashboard.route) }

    val navigationItems = listOf(
        NavigationItem.Dashboard,
        NavigationItem.Analytics,
        NavigationItem.Wellness,
        NavigationItem.Goals,
        NavigationItem.Settings
    )

    Row(modifier = Modifier.fillMaxSize()) {
        // Side Navigation
        NavigationSidebar(
            items = navigationItems,
            selectedRoute = selectedRoute,
            onItemSelected = { selectedRoute = it }
        )

        // Main Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp)
        ) {
            when (selectedRoute) {
                NavigationItem.Dashboard.route -> {
                    DashboardScreen(
                        uiState = createMockDashboardState(),
                        onTakeBreak = { /* TODO */ },
                        onNavigateToGoals = { selectedRoute = NavigationItem.Goals.route },
                        onNavigateToWellness = { selectedRoute = NavigationItem.Wellness.route }
                    )
                }
                NavigationItem.Analytics.route -> {
                    AnalyticsScreen(
                        uiState = createMockAnalyticsState(),
                        onTimeRangeSelected = { /* TODO */ }
                    )
                }
                NavigationItem.Wellness.route -> {
                    WellnessScreen(
                        uiState = createMockWellnessState(),
                        onMetricAction = { /* TODO */ },
                        onStartBreak = { /* TODO */ },
                        onScheduleBreak = { /* TODO */ },
                        onStartExercise = { /* TODO */ }
                    )
                }
                NavigationItem.Goals.route -> {
                    GoalsScreen(
                        uiState = createMockGoalsState(),
                        onCategorySelected = { /* TODO */ },
                        onToggleGoalCompletion = { /* TODO */ },
                        onEditGoal = { /* TODO */ },
                        onDeleteGoal = { /* TODO */ },
                        onShowCreateGoalDialog = { /* TODO */ },
                        onHideCreateGoalDialog = { /* TODO */ },
                        onCreateGoal = { _, _, _ -> /* TODO */ }
                    )
                }
                NavigationItem.Settings.route -> {
                    SettingsScreen(
                        uiState = createMockSettingsState(),
                        onSettingChanged = { _, _ -> /* TODO */ },
                        onSettingClicked = { /* TODO */ }
                    )
                }
            }
        }
    }
}

@Composable
fun NavigationSidebar(
    items: List<NavigationItem>,
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(250.dp)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        LazyColumn(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Text(
                    text = "Screen Time Tracker",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(items) { item ->
                NavigationItem(
                    item = item,
                    isSelected = selectedRoute == item.route,
                    onClick = { onItemSelected(item.route) }
                )
            }
        }
    }
}

@Composable
fun NavigationItem(
    item: NavigationItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = contentColor
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

// Mock data creation functions
fun createMockDashboardState() = DashboardUiState(
    totalScreenTimeFormatted = "4h 23m",
    pickupsToday = 18,
    wellnessScore = 0.72f,
    topApps = emptyList()
)

fun createMockAnalyticsState() = AnalyticsUiState(
    selectedTimeRange = TimeRange.WEEK,
    totalUsageTime = "28h 45m",
    averageDailyUsage = "4h 6m",
    totalPickups = 124,
    averagePickups = 18,
    appUsageBreakdown = listOf(
        AppUsageBreakdown("Social Media", "8h 30m", 0.30f, "+15%"),
        AppUsageBreakdown("Work Apps", "7h 20m", 0.26f, "-5%"),
        AppUsageBreakdown("Entertainment", "6h 45m", 0.24f, "+8%"),
        AppUsageBreakdown("Productivity", "3h 30m", 0.12f, "-2%"),
        AppUsageBreakdown("Games", "2h 40m", 0.08f, "+22%")
    )
)

fun createMockWellnessState() = WellnessUiState(
    wellnessScore = 0.68f,
    scoreTrend = "↑ 12% from last week",
    insights = listOf(
        "Your screen time decreased by 15 minutes today",
        "You took 3 more breaks than usual",
        "Your evening usage is still high"
    ),
    wellnessMetrics = listOf(
        WellnessMetric(
            type = WellnessMetricType.SCREEN_TIME,
            title = "Daily Screen Time",
            description = "Average for this week",
            value = "4h 23m",
            progress = 0.65f,
            status = WellnessStatus.WARNING,
            actionText = "View Details"
        ),
        WellnessMetric(
            type = WellnessMetricType.PICKUPS,
            title = "Phone Pickups",
            description = "Times opened today",
            value = "18",
            progress = 0.45f,
            status = WellnessStatus.GOOD,
            actionText = ""
        ),
        WellnessMetric(
            type = WellnessMetricType.BREAKS,
            title = "Breaks Taken",
            description = "Digital breaks today",
            value = "5",
            progress = 0.83f,
            status = WellnessStatus.GOOD,
            actionText = "Schedule More"
        )
    ),
    todayBreaks = 5,
    suggestedBreakTime = "in 25 minutes",
    mindfulnessStreak = 7,
    todayMindfulnessProgress = 0.6f,
    mindfulnessExercises = listOf(
        MindfulnessExercise("Deep Breathing", "5 mins", "🫁"),
        MindfulnessExercise("Body Scan", "10 mins", "🧘"),
        MindfulnessExercise("Gratitude", "3 mins", "💝"),
        MindfulnessExercise("Walking Meditation", "15 mins", "🚶")
    )
)

fun createMockGoalsState() = GoalsUiState(
    goals = listOf(
        Goal(
            id = "1",
            title = "Reduce Screen Time",
            description = "Limit daily usage to under 4 hours",
            category = GoalCategory.SCREEN_TIME,
            targetValue = 240f,
            currentValue = 263f,
            unit = "minutes",
            dueDate = "Dec 31, 2024",
            isCompleted = false
        ),
        Goal(
            id = "2",
            title = "Take Regular Breaks",
            description = "Take at least 6 breaks per day",
            category = GoalCategory.BREAKS,
            targetValue = 6f,
            currentValue = 5f,
            unit = "breaks",
            dueDate = "Daily",
            isCompleted = false
        ),
        Goal(
            id = "3",
            title = "Morning Mindfulness",
            description = "Complete 10 minutes of meditation each morning",
            category = GoalCategory.MINDFULNESS,
            targetValue = 10f,
            currentValue = 10f,
            unit = "minutes",
            dueDate = "Daily",
            isCompleted = true
        )
    ),
    filteredGoals = listOf(
        Goal(
            id = "1",
            title = "Reduce Screen Time",
            description = "Limit daily usage to under 4 hours",
            category = GoalCategory.SCREEN_TIME,
            targetValue = 240f,
            currentValue = 263f,
            unit = "minutes",
            dueDate = "Dec 31, 2024",
            isCompleted = false
        ),
        Goal(
            id = "2",
            title = "Take Regular Breaks",
            description = "Take at least 6 breaks per day",
            category = GoalCategory.BREAKS,
            targetValue = 6f,
            currentValue = 5f,
            unit = "breaks",
            dueDate = "Daily",
            isCompleted = false
        ),
        Goal(
            id = "3",
            title = "Morning Mindfulness",
            description = "Complete 10 minutes of meditation each morning",
            category = GoalCategory.MINDFULNESS,
            targetValue = 10f,
            currentValue = 10f,
            unit = "minutes",
            dueDate = "Daily",
            isCompleted = true
        )
    ),
    selectedCategory = GoalCategory.ALL,
    weeklyProgress = 0.74f
)

fun createMockSettingsState() = SettingsUiState(
    settingsSections = listOf(
        SettingsSection(
            title = "Notifications",
            items = listOf(
                SettingItem(
                    key = "break_reminders",
                    title = "Break Reminders",
                    subtitle = "Get notified when it's time for a break",
                    type = SettingType.SWITCH,
                    booleanValue = true
                ),
                SettingItem(
                    key = "usage_alerts",
                    title = "Usage Alerts",
                    subtitle = "Alert when approaching daily limits",
                    type = SettingType.SWITCH,
                    booleanValue = false
                ),
                SettingItem(
                    key = "reminder_frequency",
                    title = "Reminder Frequency",
                    subtitle = "How often to show break reminders",
                    type = SettingType.SLIDER,
                    floatValue = 30f,
                    range = 15f to 120f,
                    unit = " min"
                )
            )
        ),
        SettingsSection(
            title = "Privacy & Data",
            items = listOf(
                SettingItem(
                    key = "data_collection",
                    title = "Data Collection",
                    subtitle = "Allow anonymous usage analytics",
                    type = SettingType.SWITCH,
                    booleanValue = true
                ),
                SettingItem(
                    key = "export_data",
                    title = "Export Data",
                    subtitle = "Download your usage data",
                    type = SettingType.ACTION
                ),
                SettingItem(
                    key = "privacy_policy",
                    title = "Privacy Policy",
                    subtitle = "Review our privacy practices",
                    type = SettingType.ACTION
                )
            )
        ),
        SettingsSection(
            title = "About",
            items = listOf(
                SettingItem(
                    key = "version",
                    title = "Version",
                    subtitle = "Current app version",
                    type = SettingType.INFO,
                    value = "1.0.0"
                ),
                SettingItem(
                    key = "feedback",
                    title = "Send Feedback",
                    subtitle = "Help us improve the app",
                    type = SettingType.ACTION
                )
            )
        )
    )
)