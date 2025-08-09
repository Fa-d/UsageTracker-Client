package com.example.screentimetracker.ui.dashboard.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.screentimetracker.ui.components.PlayfulCard
import com.example.screentimetracker.ui.components.PlayfulMetricCard
import com.example.screentimetracker.ui.limiter.viewmodels.LimiterConfigViewModel
import com.example.screentimetracker.ui.limiter.screens.AppLimitSettingDialog
import com.example.screentimetracker.ui.smartgoals.viewmodels.SmartGoalsViewModel
import com.example.screentimetracker.ui.smartgoals.components.GoalRecommendationCard
import com.example.screentimetracker.ui.theme.*

@Composable
fun GoalsView(
    focusMode: Boolean, onFocusModeChange: (Boolean) -> Unit,
    onNavigateToSmartGoals: (() -> Unit)? = null
) {
    val limiterViewModel: LimiterConfigViewModel = hiltViewModel()
    val limiterState by limiterViewModel.uiState
    val smartGoalsViewModel: SmartGoalsViewModel = hiltViewModel()
    val smartGoalsState by smartGoalsViewModel.uiState
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Playful Header
        PlayfulCard(
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            gradientBackground = true
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        "🎯 Goals & Digital Wellness",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Transform your digital habits ✨",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }
                Text(
                    "🧘",
                    fontSize = 40.sp,
                    modifier = Modifier
                        .background(
                            LavenderPurple.copy(alpha = 0.1f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(12.dp)
                )
            }
        }

        // Daily Goals
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PlayfulMetricCard(
                modifier = Modifier.weight(1f),
                title = "Daily Limit",
                value = "4h",
                emoji = "⏰",
                color = SkyBlue,
                subtitle = "Screen time goal"
            )
            PlayfulMetricCard(
                modifier = Modifier.weight(1f),
                title = "Mindful Breaks",
                value = "8",
                emoji = "☕",
                color = VibrantOrange,
                subtitle = "Taken today"
            )
        }

        // Focus Mode Card
        PlayfulCard(
            backgroundColor = if (focusMode) LimeGreen.copy(alpha = 0.15f) else PlayfulSecondary.copy(alpha = 0.1f),
            gradientBackground = true
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Focus Mode", fontWeight = FontWeight.Medium)
                        Text("Block distracting apps", fontSize = 13.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = focusMode,
                        onCheckedChange = onFocusModeChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF22C55E),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFD1D5DB)
                        )
                    )
                }
                if (focusMode) {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFDCFCE7), shape = MaterialTheme.shapes.small)
                            .padding(8.dp)
                    ) {
                        Text(
                            "Focus mode is active. 3 apps are blocked.",
                            fontSize = 13.sp,
                            color = Color(0xFF15803D)
                        )
                    }
                }
            }
        }
        // Daily Goals
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Daily Goals", fontWeight = FontWeight.Medium)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Screen Time Limit
                    Column {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Screen Time Limit", fontSize = 13.sp)
                            Text("4h 32m / 6h 00m", fontSize = 13.sp)
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(
                                    Color(0xFFE5E7EB), shape = MaterialTheme.shapes.extraLarge
                                )
                        ) {
                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(0.75f)
                                    .background(
                                        Color(0xFF2563EB), shape = MaterialTheme.shapes.extraLarge
                                    )
                            )
                        }
                    }
                    // Max Unlocks
                    Column {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Max Unlocks", fontSize = 13.sp)
                            Text("87 / 100", fontSize = 13.sp)
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(
                                    Color(0xFFE5E7EB), shape = MaterialTheme.shapes.extraLarge
                                )
                        ) {
                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(0.87f)
                                    .background(
                                        Color(0xFF22C55E), shape = MaterialTheme.shapes.extraLarge
                                    )
                            )
                        }
                    }
                }
            }
        }
        
        // Smart Goal Recommendations Section
        PlayfulCard(
            backgroundColor = SkyBlue.copy(alpha = 0.1f),
            gradientBackground = true
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "🤖 Smart Recommendations",
                            fontWeight = FontWeight.Bold,
                            color = SkyBlue
                        )
                        Text(
                            "AI-powered goal suggestions",
                            fontSize = 13.sp,
                            color = SkyBlue.copy(alpha = 0.7f)
                        )
                    }
                    androidx.compose.material3.TextButton(
                        onClick = { 
                            onNavigateToSmartGoals?.invoke() ?: smartGoalsViewModel.generateAIRecommendations() 
                        }
                    ) {
                        Text("View All", color = SkyBlue)
                    }
                }
                
                if (smartGoalsState.recommendations.isNotEmpty()) {
                    // Show first recommendation as preview
                    val firstRecommendation = smartGoalsState.recommendations.first()
                    GoalRecommendationCard(
                        recommendation = firstRecommendation,
                        onAccept = { smartGoalsViewModel.acceptRecommendation(firstRecommendation) },
                        onReject = { smartGoalsViewModel.rejectRecommendation(firstRecommendation) }
                    )
                    
                    if (smartGoalsState.recommendations.size > 1) {
                        Text(
                            "...and ${smartGoalsState.recommendations.size - 1} more recommendations available",
                            fontSize = 12.sp,
                            color = SkyBlue.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                } else if (smartGoalsState.isLoadingRecommendations) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = SkyBlue
                        )
                        Text(
                            "Analyzing your usage patterns...",
                            style = MaterialTheme.typography.bodySmall,
                            color = SkyBlue.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    Text(
                        "Tap 'View All' to get personalized goal recommendations based on your usage patterns.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SkyBlue.copy(alpha = 0.8f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
        
        // App Limits - Enhanced Section
        PlayfulCard(
            backgroundColor = VibrantOrange.copy(alpha = 0.1f),
            gradientBackground = true
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "📱 App Usage Limits",
                            fontWeight = FontWeight.Bold,
                            color = VibrantOrange
                        )
                        Text(
                            "Control your app usage",
                            fontSize = 13.sp,
                            color = VibrantOrange.copy(alpha = 0.7f)
                        )
                    }
                    androidx.compose.material3.TextButton(
                        onClick = { limiterViewModel.onAddAppClicked() }
                    ) {
                        androidx.compose.material3.Icon(
                            androidx.compose.material.icons.Icons.Filled.Add,
                            contentDescription = "Add limit",
                            tint = VibrantOrange
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Add", color = VibrantOrange)
                    }
                }
                
                if (limiterState.limitedApps.isEmpty() && !limiterState.isLoading) {
                    Text(
                        "No apps are currently limited. Tap 'Add' to set usage limits for specific apps.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VibrantOrange.copy(alpha = 0.8f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    limiterState.limitedApps.take(3).forEach { app ->
                        val formattedLimit = remember(app.timeLimitMillis) {
                            val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(app.timeLimitMillis)
                            "$minutes min"
                        }
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { limiterViewModel.onEditAppClicked(app) },
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    coil3.compose.AsyncImage(
                                        model = coil3.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                            .data(androidx.compose.ui.platform.LocalContext.current.packageManager.getApplicationIcon(app.packageName))
                                            .build(),
                                        contentDescription = "${app.appName} icon",
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            app.appName,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            "Limit: $formattedLimit",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                androidx.compose.material3.IconButton(
                                    onClick = { limiterViewModel.onRemoveLimitedApp(app.packageName) }
                                ) {
                                    androidx.compose.material3.Icon(
                                        androidx.compose.material.icons.Icons.Filled.Delete,
                                        contentDescription = "Remove limit",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    if (limiterState.limitedApps.size > 3) {
                        Text(
                            "...and ${limiterState.limitedApps.size - 3} more apps",
                            fontSize = 12.sp,
                            color = VibrantOrange.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
        
        // App Limit Dialog
        if (limiterState.showAppSelectionDialog) {
            AppLimitSettingDialog(
                installedApps = limiterState.installedAppsForSelection,
                selectedApp = limiterState.selectedAppForLimit,
                newLimitTimeMinutes = limiterState.newLimitTimeInputMinutes,
                isLoadingApps = limiterState.isLoading && limiterState.selectedAppForLimit == null,
                isEditing = limiterState.appBeingEdited != null,
                onAppSelected = { limiterViewModel.onAppSelectedForLimiting(it) },
                onTimeChanged = { limiterViewModel.onNewLimitTimeChanged(it) },
                onConfirm = { limiterViewModel.onConfirmAddLimitedApp() },
                onDismiss = { limiterViewModel.onDismissAppSelectionDialog() }
            )
        }
    }
}
