package com.example.screentimetracker.ui.dashboard.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.screentimetracker.data.local.ThemeMode
import com.example.screentimetracker.ui.components.PlayfulCard

@Composable
fun SimpleSettingsView(
    currentThemeMode: ThemeMode = ThemeMode.SYSTEM, onThemeModeChange: (ThemeMode) -> Unit,
    onNavigateToPersonalization: () -> Unit,
    onNavigateToAdvancedSettings: () -> Unit
) {
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
                                themeDescription, 
                                fontSize = 13.sp, 
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Settings Categories", 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Personalization
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToPersonalization() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = Color(0xFF8B5CF6),
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text("Personalization", fontWeight = FontWeight.Medium)
                                Text("Themes, colors, and app personality", fontSize = 13.sp, color = Color.Gray)
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                    
                    // Advanced Settings
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToAdvancedSettings() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = Color(0xFF06B6D4),
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text("Advanced Settings", fontWeight = FontWeight.Medium)
                                Text("Privacy, data, and advanced features", fontSize = 13.sp, color = Color.Gray)
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

@Preview
@Composable
fun SimpleSettingsViewPreview() {
    SimpleSettingsView(
        currentThemeMode = ThemeMode.SYSTEM, onThemeModeChange = {},
        onNavigateToPersonalization = {},
        onNavigateToAdvancedSettings = {}
    )
}