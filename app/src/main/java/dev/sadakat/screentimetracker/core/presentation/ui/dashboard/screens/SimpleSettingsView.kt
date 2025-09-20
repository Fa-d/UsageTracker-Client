package dev.sadakat.screentimetracker.core.presentation.ui.dashboard.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sadakat.screentimetracker.core.presentation.ui.components.ActionCard
import dev.sadakat.screentimetracker.core.presentation.ui.components.PlayfulCard
import dev.sadakat.screentimetracker.core.presentation.ui.personalization.PersonalizationScreen
import dev.sadakat.screentimetracker.data.local.entities.ThemeMode
import dev.sadakat.screentimetracker.data.local.entities.UserPreferences
import dev.sadakat.screentimetracker.data.local.entities.ColorScheme as AppColorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleSettingsView(
    currentThemeMode: ThemeMode = ThemeMode.SYSTEM, 
    onThemeModeChange: (ThemeMode) -> Unit,
    preferences: UserPreferences = UserPreferences(),
    onColorSchemeChanged: (AppColorScheme) -> Unit = {},
    onMotivationalMessagesChanged: (Boolean) -> Unit = {},
    onAchievementCelebrationsChanged: (Boolean) -> Unit = {},
    onBreakRemindersChanged: (Boolean) -> Unit = {},
    onWellnessCoachingChanged: (Boolean) -> Unit = {},
    privacyMode: Boolean,
    onPrivacyModeChange: (Boolean) -> Unit,
    syncEnabled: Boolean,
    onSyncEnabledChange: (Boolean) -> Unit,
    usageAlertsEnabled: Boolean = true,
    onUsageAlertsChange: (Boolean) -> Unit = {},
    goalRemindersEnabled: Boolean = true,
    onGoalRemindersChange: (Boolean) -> Unit = {},
    onNavigateToProgressiveLimits: () -> Unit = {},
    onNavigateToLimiterConfig: () -> Unit = {},
    onNavigateToPrivacySettings: () -> Unit = {},
    onNavigateToMindfulness: () -> Unit = {},
    onNavigateToAISettings: () -> Unit = {},
    onNavigateToReplacementActivities: () -> Unit = {},
    onExportData: () -> Unit = {},
    onClearCache: () -> Unit = {}
) {
    var showPersonalizationBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()

    // Personalization Bottom Sheet
    if (showPersonalizationBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPersonalizationBottomSheet = false },
            sheetState = bottomSheetState
        ) {
            PersonalizationScreen(
                preferences = preferences,
                onThemeModeChanged = onThemeModeChange,
                onColorSchemeChanged = onColorSchemeChanged,
                onMotivationalMessagesChanged = onMotivationalMessagesChanged,
                onAchievementCelebrationsChanged = onAchievementCelebrationsChanged,
                onBreakRemindersChanged = onBreakRemindersChanged,
                onWellnessCoachingChanged = onWellnessCoachingChanged
            )
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp)
    ) {
        item {
            PlayfulCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                gradientBackground = true
            ) {
                Column {
                    Text(
                        text = "⚙️ Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Customize your app experience",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Quick Settings - Theme Mode
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Appearance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Theme Mode",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val themeDescription = when (currentThemeMode) {
                                ThemeMode.LIGHT -> "Light theme"
                                ThemeMode.DARK -> "Dark theme"
                                ThemeMode.SYSTEM -> "Follow system theme (${if (isSystemInDarkTheme()) "Dark" else "Light"})"
                            }
                            Text(
                                themeDescription, fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Theme mode toggle buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { onThemeModeChange(ThemeMode.LIGHT) }) {
                                Icon(
                                    Icons.Default.LightMode,
                                    contentDescription = "Light Mode",
                                    tint = if (currentThemeMode == ThemeMode.LIGHT) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { onThemeModeChange(ThemeMode.DARK) }) {
                                Icon(
                                    Icons.Default.DarkMode,
                                    contentDescription = "Dark Mode",
                                    tint = if (currentThemeMode == ThemeMode.DARK) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { onThemeModeChange(ThemeMode.SYSTEM) }) {
                                Icon(
                                    Icons.Default.SettingsBrightness,
                                    contentDescription = "System Theme",
                                    tint = if (currentThemeMode == ThemeMode.SYSTEM) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Settings Categories
        item {
            ActionCard(
                title = "Personalization",
                description = "Themes, colors, and app personality",
                actionText = "Customize",
                onAction = { showPersonalizationBottomSheet = true },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Privacy & Data
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Privacy & Data",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Privacy Mode",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Hide sensitive app data",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = privacyMode,
                            onCheckedChange = onPrivacyModeChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Auto Sync", fontWeight = FontWeight.Medium)
                            Text("Sync with Room Database", fontSize = 13.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = syncEnabled,
                            onCheckedChange = onSyncEnabledChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF22C55E),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFD1D5DB)
                            )
                        )
                    }
                }
            }
        }
        // Notifications
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "System Notifications",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Usage Alerts", fontWeight = FontWeight.Medium)
                            Text("Get notified about limits", fontSize = 13.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = usageAlertsEnabled,
                            onCheckedChange = onUsageAlertsChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFF59E42),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFD1D5DB)
                            )
                        )
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Goal Reminders", fontWeight = FontWeight.Medium)
                            Text("Daily goal notifications", fontSize = 13.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = goalRemindersEnabled,
                            onCheckedChange = onGoalRemindersChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFEF4444),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFD1D5DB)
                            )
                        )
                    }
                }
            }
        }
        // Advanced Features
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Advanced Features",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Progressive Limits
                    Row(Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToProgressiveLimits() }
                        .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = Color(0xFF06B6D4),
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text("Progressive Limits", fontWeight = FontWeight.Medium)
                                Text(
                                    "Gradually reduce screen time",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }

                    // App Limiter Configuration
                    Row(Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToLimiterConfig() }
                        .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = Color(0xFFE11D48),
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text("App Limits", fontWeight = FontWeight.Medium)
                                Text(
                                    "Configure time limits for specific apps",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }

                    // Privacy Settings
                    Row(Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToPrivacySettings() }
                        .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF7C3AED),
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text("Privacy Settings", fontWeight = FontWeight.Medium)
                                Text(
                                    "Manage your data privacy preferences",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }

                    // AI Features
                    Row(Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAISettings() }
                        .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text("AI Features", fontWeight = FontWeight.Medium)
                                Text(
                                    "Smart insights and recommendations",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }

                    // Mindfulness & Breathing
                    Row(Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToMindfulness() }
                        .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text("Mindfulness", fontWeight = FontWeight.Medium)
                                Text(
                                    "Breathing exercises and meditation",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }

                    // Replacement Activities
                    Row(Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToReplacementActivities() }
                        .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text("Replacement Activities", fontWeight = FontWeight.Medium)
                                Text(
                                    "Healthy alternatives to screen time",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                }
            }
        }
        // Data Management
        item {
            PlayfulCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📊 Data Management",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Export data, clear cache, and manage storage",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Export Data Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onExportData() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(
                                "Export Data",
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Export your usage data as CSV",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Clear Cache Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClearCache() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(
                                "Clear Cache",
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Free up storage space",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        // Enhanced Notifications & Messages
        item {
            PlayfulCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Enhanced Notifications",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Motivational Messages
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Motivational Messages",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Receive encouraging messages throughout the day",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = preferences.motivationalMessagesEnabled,
                            onCheckedChange = onMotivationalMessagesChanged
                        )
                    }

                    // Achievement Celebrations
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Achievement Celebrations",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Get notified when you unlock achievements",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = preferences.achievementCelebrationsEnabled,
                            onCheckedChange = onAchievementCelebrationsChanged
                        )
                    }

                    // Break Reminders
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Break Reminders",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Gentle nudges to take breaks from screens",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = preferences.breakRemindersEnabled,
                            onCheckedChange = onBreakRemindersChanged
                        )
                    }

                    // Wellness Coaching
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Wellness Coaching",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Tips and insights for digital wellbeing",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = preferences.wellnessCoachingEnabled,
                            onCheckedChange = onWellnessCoachingChanged
                        )
                    }
                }
            }
        }

        // App Info
        item {
            PlayfulCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "📱 App Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Screen Time Tracker v1.0\nBuilt for your digital wellbeing",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SimpleSettingsViewPreview() {
    SimpleSettingsView(
        currentThemeMode = ThemeMode.SYSTEM,
        onThemeModeChange = {},
        preferences = UserPreferences(),
        onColorSchemeChanged = {},
        onMotivationalMessagesChanged = {},
        onAchievementCelebrationsChanged = {},
        onBreakRemindersChanged = {},
        onWellnessCoachingChanged = {},
        privacyMode = false,
        onPrivacyModeChange = {},
        syncEnabled = true,
        onSyncEnabledChange = {},
        usageAlertsEnabled = true,
        onUsageAlertsChange = {},
        goalRemindersEnabled = true,
        onGoalRemindersChange = {},
        onNavigateToProgressiveLimits = {},
        onNavigateToLimiterConfig = {},
        onNavigateToPrivacySettings = {},
        onNavigateToAISettings = {},
        onNavigateToMindfulness = {},
        onNavigateToReplacementActivities = {},
        onExportData = {},
        onClearCache = {}
    )
}
