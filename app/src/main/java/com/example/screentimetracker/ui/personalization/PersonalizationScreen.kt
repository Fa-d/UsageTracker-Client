package com.example.screentimetracker.ui.personalization

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.screentimetracker.data.local.UserPreferences
import com.example.screentimetracker.data.local.ThemeMode
import com.example.screentimetracker.data.local.ColorScheme as AppColorScheme
import com.example.screentimetracker.data.local.PersonalityMode
import com.example.screentimetracker.data.local.DashboardLayout
import com.example.screentimetracker.ui.components.PlayfulCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalizationScreen(
    onBackPressed: () -> Unit,
    viewModel: PersonalizationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personalization") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PlayfulCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "🎨 Personalize Your Experience",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Customize the app's appearance and behavior to match your preferences and personality.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            item {
                ThemesSection(
                    preferences = uiState.preferences,
                    onThemeModeChanged = viewModel::updateThemeMode,
                    onColorSchemeChanged = viewModel::updateColorScheme
                )
            }
            
            item {
                PersonalitySection(
                    preferences = uiState.preferences,
                    onPersonalityModeChanged = viewModel::updatePersonalityMode
                )
            }
            
            item {
                NotificationSection(
                    preferences = uiState.preferences,
                    onMotivationalMessagesChanged = viewModel::updateMotivationalMessages,
                    onAchievementCelebrationsChanged = viewModel::updateAchievementCelebrations,
                    onBreakRemindersChanged = viewModel::updateBreakReminders,
                    onWellnessCoachingChanged = viewModel::updateWellnessCoaching
                )
            }
            
            item {
                DashboardSection(
                    preferences = uiState.preferences,
                    onDashboardLayoutChanged = viewModel::updateDashboardLayout
                )
            }
        }
    }
}

@Composable
fun ThemesSection(
    preferences: UserPreferences,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onColorSchemeChanged: (AppColorScheme) -> Unit
) {
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
                    Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Themes & Colors",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Theme Mode",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            
            Column(modifier = Modifier.selectableGroup()) {
                ThemeMode.values().forEach { themeMode ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = ThemeMode.valueOf(preferences.themeMode) == themeMode,
                            onClick = { onThemeModeChanged(themeMode) }
                        )
                        Text(
                            text = themeMode.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Color Scheme",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            
            Column(modifier = Modifier.selectableGroup()) {
                AppColorScheme.values().forEach { colorScheme ->
                    val description = when (colorScheme) {
                        AppColorScheme.DEFAULT -> "Default - Balanced and modern"
                        AppColorScheme.COLORFUL -> "Colorful - Bright and vibrant"
                        AppColorScheme.MINIMAL -> "Minimal - Subtle and clean"
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = AppColorScheme.valueOf(preferences.colorScheme) == colorScheme,
                            onClick = { onColorSchemeChanged(colorScheme) }
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = colorScheme.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalitySection(
    preferences: UserPreferences,
    onPersonalityModeChanged: (PersonalityMode) -> Unit
) {
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
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Personality Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Choose how the app communicates with you",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(modifier = Modifier.selectableGroup()) {
                PersonalityMode.values().forEach { personalityMode ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = PersonalityMode.valueOf(preferences.personalityMode) == personalityMode,
                            onClick = { onPersonalityModeChanged(personalityMode) }
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = personalityMode.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = personalityMode.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationSection(
    preferences: UserPreferences,
    onMotivationalMessagesChanged: (Boolean) -> Unit,
    onAchievementCelebrationsChanged: (Boolean) -> Unit,
    onBreakRemindersChanged: (Boolean) -> Unit,
    onWellnessCoachingChanged: (Boolean) -> Unit
) {
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
                    text = "Notifications & Messages",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PreferenceSwitchItem(
                title = "Motivational Messages",
                description = "Receive encouraging messages throughout the day",
                checked = preferences.motivationalMessagesEnabled,
                onCheckedChange = onMotivationalMessagesChanged
            )
            
            PreferenceSwitchItem(
                title = "Achievement Celebrations",
                description = "Get notified when you unlock achievements",
                checked = preferences.achievementCelebrationsEnabled,
                onCheckedChange = onAchievementCelebrationsChanged
            )
            
            PreferenceSwitchItem(
                title = "Break Reminders",
                description = "Gentle nudges to take breaks from screens",
                checked = preferences.breakRemindersEnabled,
                onCheckedChange = onBreakRemindersChanged
            )
            
            PreferenceSwitchItem(
                title = "Wellness Coaching",
                description = "Tips and insights for digital wellbeing",
                checked = preferences.wellnessCoachingEnabled,
                onCheckedChange = onWellnessCoachingChanged
            )
        }
    }
}

@Composable
fun DashboardSection(
    preferences: UserPreferences,
    onDashboardLayoutChanged: (DashboardLayout) -> Unit
) {
    PlayfulCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 Dashboard Layout",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Choose how much detail to show on your dashboard",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(modifier = Modifier.selectableGroup()) {
                DashboardLayout.values().forEach { layout ->
                    val description = when (layout) {
                        DashboardLayout.STANDARD -> "Balanced view with key metrics"
                        DashboardLayout.COMPACT -> "Minimal cards with essential info"
                        DashboardLayout.DETAILED -> "Expanded cards with comprehensive data"
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = DashboardLayout.valueOf(preferences.dashboardLayout) == layout,
                            onClick = { onDashboardLayoutChanged(layout) }
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = layout.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PreferenceSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
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
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}