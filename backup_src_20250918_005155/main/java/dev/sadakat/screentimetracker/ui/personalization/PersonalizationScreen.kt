package dev.sadakat.screentimetracker.ui.personalization

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
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sadakat.screentimetracker.core.data.local.ThemeMode
import dev.sadakat.screentimetracker.core.data.local.UserPreferences
import dev.sadakat.screentimetracker.ui.components.PlayfulCard
import dev.sadakat.screentimetracker.core.data.local.entities.ColorScheme as AppColorScheme

@Composable
fun PersonalizationScreen(
    preferences: UserPreferences,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onColorSchemeChanged: (AppColorScheme) -> Unit,
    onMotivationalMessagesChanged: (Boolean) -> Unit,
    onAchievementCelebrationsChanged: (Boolean) -> Unit,
    onBreakRemindersChanged: (Boolean) -> Unit,
    onWellnessCoachingChanged: (Boolean) -> Unit
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
                        text = "🎨 Personalization",
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

        // Themes & Colors
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
        
    }
}