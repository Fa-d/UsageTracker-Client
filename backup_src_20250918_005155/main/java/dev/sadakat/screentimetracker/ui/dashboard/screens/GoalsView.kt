package dev.sadakat.screentimetracker.ui.dashboard.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import dev.sadakat.screentimetracker.ui.components.PlayfulCard
import dev.sadakat.screentimetracker.ui.components.PlayfulMetricCard
import dev.sadakat.screentimetracker.ui.focus.components.FocusDurationDialog
import dev.sadakat.screentimetracker.ui.focus.viewmodels.FocusModeViewModel
import dev.sadakat.screentimetracker.ui.goals.viewmodels.UserGoalsViewModel
import dev.sadakat.screentimetracker.ui.limiter.screens.AppLimitSettingDialog
import dev.sadakat.screentimetracker.ui.limiter.viewmodels.LimiterConfigViewModel
import dev.sadakat.screentimetracker.ui.goals.components.GoalCreationDialog
import dev.sadakat.screentimetracker.ui.smartgoals.components.GoalRecommendationCard
import dev.sadakat.screentimetracker.ui.smartgoals.viewmodels.SmartGoalsViewModel
import dev.sadakat.screentimetracker.ui.theme.LavenderPurple
import dev.sadakat.screentimetracker.ui.theme.LimeGreen
import dev.sadakat.screentimetracker.ui.theme.PlayfulSecondary
import dev.sadakat.screentimetracker.ui.theme.SkyBlue
import dev.sadakat.screentimetracker.ui.theme.VibrantOrange

@Composable
fun GoalsView(
    focusMode: Boolean, onFocusModeChange: (Boolean) -> Unit,
    onNavigateToSmartGoals: (() -> Unit)? = null
) {
    val limiterViewModel: LimiterConfigViewModel = hiltViewModel()
    val limiterState by limiterViewModel.uiState
    val smartGoalsViewModel: SmartGoalsViewModel = hiltViewModel()
    val smartGoalsState by smartGoalsViewModel.uiState
    val focusModeViewModel: FocusModeViewModel = hiltViewModel()
    val focusModeState by focusModeViewModel.uiState.collectAsState()
    val userGoalsViewModel: UserGoalsViewModel = hiltViewModel()
    val userGoalsState by userGoalsViewModel.uiState
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
            .padding(top = 16.dp),
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
                            LavenderPurple.copy(alpha = 0.1f), RoundedCornerShape(16.dp)
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
            val screenTimeGoal = userGoalsState.dailyScreenTimeGoal
            val screenTimeProgress = userGoalsViewModel.getScreenTimeProgress()
            val formattedCurrentTime = userGoalsViewModel.formatDuration(userGoalsState.currentScreenTime)
            val formattedTargetTime = screenTimeGoal?.let { userGoalsViewModel.formatDuration(it.targetValue) } ?: "Not set"
            
            PlayfulMetricCard(
                modifier = Modifier.weight(1f),
                title = "Daily Limit",
                value = if (screenTimeGoal != null) "$formattedCurrentTime / $formattedTargetTime" else "No goal set",
                emoji = "⏰",
                color = if (screenTimeProgress > 0.8f) VibrantOrange else SkyBlue,
                subtitle = "Screen time goal"
            )
            
            val unlockGoal = userGoalsState.unlockFrequencyGoal
            val unlockProgress = userGoalsViewModel.getUnlockProgress()
            val currentUnlocks = userGoalsState.currentUnlockCount
            val targetUnlocks = unlockGoal?.targetValue?.toInt() ?: 0
            
            PlayfulMetricCard(
                modifier = Modifier.weight(1f),
                title = "Max Unlocks",
                value = if (unlockGoal != null) "$currentUnlocks / $targetUnlocks" else "No goal set",
                emoji = "📱",
                color = if (unlockProgress > 0.8f) VibrantOrange else LimeGreen,
                subtitle = "Daily unlock limit"
            )
        }

        // Focus Mode Card
        PlayfulCard(
            backgroundColor = if (focusModeState.isActive) LimeGreen.copy(alpha = 0.15f) else PlayfulSecondary.copy(alpha = 0.1f),
            gradientBackground = true
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("🧘 Focus Mode", fontWeight = FontWeight.Medium)
                        Text(
                            if (focusModeState.isActive) "Session in progress" else "Block distracting apps", 
                            fontSize = 13.sp, 
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = focusModeState.isActive,
                        onCheckedChange = { focusModeViewModel.toggleFocusMode() },
                        enabled = !focusModeState.isLoading,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF22C55E),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFD1D5DB)
                        )
                    )
                }
                
                if (focusModeState.isActive) {
                    Spacer(Modifier.height(12.dp))
                    
                    // Session progress
                    val progressPercentage = if (focusModeState.targetDurationMillis > 0) {
                        (focusModeState.currentSessionDuration.toFloat() / focusModeState.targetDurationMillis.toFloat()).coerceAtMost(1f)
                    } else 0f
                    
                    // Progress bar
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(Color(0xFFE5E7EB), shape = MaterialTheme.shapes.extraLarge)
                    ) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progressPercentage)
                                .background(LimeGreen, shape = MaterialTheme.shapes.extraLarge)
                        )
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    // Session info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Time: ${formatDuration(focusModeState.currentSessionDuration)} / ${focusModeState.selectedDurationMinutes}m",
                            fontSize = 12.sp,
                            color = Color(0xFF15803D)
                        )
                        Text(
                            "${focusModeState.blockedApps.size} apps blocked",
                            fontSize = 12.sp,
                            color = Color(0xFF15803D)
                        )
                    }
                    
                    if (focusModeState.interruptionCount > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "⚠️ ${focusModeState.interruptionCount} interruption${if (focusModeState.interruptionCount > 1) "s" else ""}",
                            fontSize = 11.sp,
                            color = VibrantOrange
                        )
                    }
                } else if (!focusModeState.isLoading && focusModeState.blockedApps.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Ready to block ${focusModeState.blockedApps.size} distracting apps",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                
                // Error handling
                focusModeState.error?.let { error ->
                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(VibrantOrange.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small)
                            .padding(8.dp)
                    ) {
                        Text(
                            error,
                            fontSize = 12.sp,
                            color = VibrantOrange
                        )
                    }
                }
            }
        }
        // Detailed Daily Goals
        Card(
            modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Daily Goals", fontWeight = FontWeight.Medium)
                    if (userGoalsState.activeGoals.isEmpty() && !userGoalsState.isLoading) {
                        androidx.compose.material3.TextButton(
                            onClick = { userGoalsViewModel.showGoalCreationDialog() }
                        ) {
                            androidx.compose.material3.Icon(
                                androidx.compose.material.icons.Icons.Filled.Add,
                                contentDescription = "Add goals",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Add Goals", color = MaterialTheme.colorScheme.primary)
                        }
                    } else if (!userGoalsState.isLoading) {
                        androidx.compose.material3.TextButton(
                            onClick = { userGoalsViewModel.showGoalCreationDialog() }
                        ) {
                            androidx.compose.material3.Icon(
                                androidx.compose.material.icons.Icons.Filled.Add,
                                contentDescription = "Add goal",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Add", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                
                if (userGoalsState.isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Loading your goals...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                } else if (userGoalsState.activeGoals.isEmpty()) {
                    Text(
                        "No daily goals set yet. Tap 'Add Goals' to create your first goals and start tracking your progress.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Screen Time Limit
                        userGoalsState.dailyScreenTimeGoal?.let { goal ->
                            val progress = userGoalsViewModel.getScreenTimeProgress()
                            val currentTime = userGoalsViewModel.formatDuration(userGoalsState.currentScreenTime)
                            val targetTime = userGoalsViewModel.formatDuration(goal.targetValue)
                            
                            Column {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Screen Time Limit", fontSize = 13.sp)
                                    Text("$currentTime / $targetTime", fontSize = 13.sp)
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
                                            .fillMaxWidth(progress)
                                            .background(
                                                if (progress > 0.8f) VibrantOrange else Color(0xFF2563EB),
                                                shape = MaterialTheme.shapes.extraLarge
                                            )
                                    )
                                }
                            }
                        }
                        
                        // Max Unlocks
                        userGoalsState.unlockFrequencyGoal?.let { goal ->
                            val progress = userGoalsViewModel.getUnlockProgress()
                            val currentUnlocks = userGoalsState.currentUnlockCount
                            val targetUnlocks = goal.targetValue.toInt()
                            
                            Column {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Max Unlocks", fontSize = 13.sp)
                                    Text("$currentUnlocks / $targetUnlocks", fontSize = 13.sp)
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
                                            .fillMaxWidth(progress)
                                            .background(
                                                if (progress > 0.8f) VibrantOrange else Color(0xFF22C55E),
                                                shape = MaterialTheme.shapes.extraLarge
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Show error if any
                userGoalsState.error?.let { error ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(VibrantOrange.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small)
                            .padding(8.dp)
                    ) {
                        Text(
                            error,
                            fontSize = 12.sp,
                            color = VibrantOrange
                        )
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "AI-powered goal suggestions",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    androidx.compose.material3.TextButton(
                        onClick = { 
                            onNavigateToSmartGoals?.invoke() ?: smartGoalsViewModel.generateAIRecommendations() 
                        }
                    ) {
                        Text("View All", color = MaterialTheme.colorScheme.primary)
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
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
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
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Analyzing your usage patterns...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    Text(
                        "Tap 'View All' to get personalized goal recommendations based on your usage patterns.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Control your app usage",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    androidx.compose.material3.TextButton(
                        onClick = { limiterViewModel.onAddAppClicked() }
                    ) {
                        androidx.compose.material3.Icon(
                            androidx.compose.material.icons.Icons.Filled.Add,
                            contentDescription = "Add limit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Add", color = MaterialTheme.colorScheme.primary)
                    }
                }
                
                if (limiterState.limitedApps.isEmpty() && !limiterState.isLoading) {
                    Text(
                        "No apps are currently limited. Tap 'Add' to set usage limits for specific apps.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
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
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
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
        
        // Focus Duration Dialog
        if (focusModeState.showDurationDialog) {
            FocusDurationDialog(
                selectedDuration = focusModeState.selectedDurationMinutes,
                onDurationChanged = { focusModeViewModel.updateSelectedDuration(it) },
                onConfirm = { focusModeViewModel.startFocusSession(it) },
                onDismiss = { focusModeViewModel.dismissDurationDialog() }
            )
        }
        
        // Goal Creation Dialog
        if (userGoalsState.showGoalCreationDialog) {
            GoalCreationDialog(
                onDismiss = { userGoalsViewModel.dismissGoalCreationDialog() },
                onCreateGoal = { goalType, targetValue ->
                    userGoalsViewModel.createGoal(goalType, targetValue)
                }
            )
        }
    }
}

// Helper function to format duration
private fun formatDuration(millis: Long): String {
    val minutes = (millis / 60000).toInt()
    val seconds = ((millis % 60000) / 1000).toInt()
    return String.format("%02d:%02d", minutes, seconds)
}
