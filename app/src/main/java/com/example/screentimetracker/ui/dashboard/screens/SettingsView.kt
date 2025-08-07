package com.example.screentimetracker.ui.dashboard.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.screentimetracker.data.local.UserPreferences
import com.example.screentimetracker.data.local.ThemeMode
import com.example.screentimetracker.data.local.ColorScheme as AppColorScheme
import com.example.screentimetracker.data.local.PersonalityMode
import com.example.screentimetracker.data.local.DashboardLayout
import com.example.screentimetracker.ui.components.PlayfulCard

@Composable
fun SettingsView(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    privacyMode: Boolean,
    onPrivacyModeChange: (Boolean) -> Unit,
    syncEnabled: Boolean,
    onSyncEnabledChange: (Boolean) -> Unit,
    preferences: UserPreferences,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onColorSchemeChanged: (AppColorScheme) -> Unit,
    onPersonalityModeChanged: (PersonalityMode) -> Unit,
    onMotivationalMessagesChanged: (Boolean) -> Unit,
    onAchievementCelebrationsChanged: (Boolean) -> Unit,
    onBreakRemindersChanged: (Boolean) -> Unit,
    onWellnessCoachingChanged: (Boolean) -> Unit,
    onDashboardLayoutChanged: (DashboardLayout) -> Unit,
    onNavigateToProgressiveLimits: () -> Unit = {},
    onNavigateToLimiterConfig: () -> Unit = {},
    onNavigateToPrivacySettings: () -> Unit = {},
    onNavigateToMindfulness: () -> Unit = {},
    onNavigateToReplacementActivities: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                "Settings", 
                fontWeight = FontWeight.Bold, 
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        item {
            // Privacy & Data
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Privacy & Data", 
                        fontWeight = FontWeight.Medium,
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
        
        item {
            // Appearance
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Appearance", fontWeight = FontWeight.Medium)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Dark Mode", fontWeight = FontWeight.Medium)
                            Text("Use dark theme", fontSize = 13.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = darkMode,
                            onCheckedChange = onDarkModeChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF8B5CF6),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFD1D5DB)
                            )
                        )
                    }
                }
            }
        }
        
        item {
            // Notifications
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Notifications", fontWeight = FontWeight.Medium)
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
                            checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(
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
                            checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(
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
        
        item {
            // Themes & Colors
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
                        ThemeMode.entries.forEach { themeMode ->
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
                        AppColorScheme.entries.forEach { colorScheme ->
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
        
        item {
            // Personality Mode
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
                        PersonalityMode.entries.forEach { personalityMode ->
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
        
        item {
            // Enhanced Notifications & Messages
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
        
        item {
            // Dashboard Layout
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
                        DashboardLayout.entries.forEach { layout ->
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

        item {
            // Advanced Features
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Advanced Features", fontWeight = FontWeight.Medium)
                    
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToProgressiveLimits() }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = Color(0xFF06B6D4),
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text("Progressive Limits", fontWeight = FontWeight.Medium)
                                Text("Gradually reduce screen time", fontSize = 13.sp, color = Color.Gray)
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                    
                    // App Limiter Configuration
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToLimiterConfig() }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = Color(0xFFE11D48),
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text("App Limits", fontWeight = FontWeight.Medium)
                                Text("Configure time limits for specific apps", fontSize = 13.sp, color = Color.Gray)
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                    
                    // Privacy Settings
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToPrivacySettings() }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF7C3AED),
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text("Privacy Settings", fontWeight = FontWeight.Medium)
                                Text("Manage your data privacy preferences", fontSize = 13.sp, color = Color.Gray)
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                    
                    // Mindfulness & Breathing
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToMindfulness() }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text("Mindfulness", fontWeight = FontWeight.Medium)
                                Text("Breathing exercises and meditation", fontSize = 13.sp, color = Color.Gray)
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                    
                    // Replacement Activities
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToReplacementActivities() }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text("Replacement Activities", fontWeight = FontWeight.Medium)
                                Text("Healthy alternatives to screen time", fontSize = 13.sp, color = Color.Gray)
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
    }
}