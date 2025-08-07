package com.example.screentimetracker.ui.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.screentimetracker.ui.components.PlayfulCard

data class QuickAction(
    val id: String,
    val title: String,
    val emoji: String,
    val description: String,
    val color: Color,
    val icon: ImageVector,
    val isActive: Boolean = false,
    val badge: String? = null,
    val onClick: () -> Unit
)

@Composable
fun QuickActionsCard(
    modifier: Modifier = Modifier,
    onFocusModeToggle: () -> Unit = {},
    onEmergencyBlock: () -> Unit = {},
    onStartTimer: (Int) -> Unit = {},
    onViewStats: () -> Unit = {},
    viewModel: QuickActionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showTimerDialog by remember { mutableStateOf(false) }
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val surfaceColor = MaterialTheme.colorScheme.surface
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    
    val quickActions = remember(uiState, primaryColor, errorColor, surfaceColor, secondaryColor, tertiaryColor) {
        listOf(
            QuickAction(
                id = "focus_mode",
                title = "Focus Mode",
                emoji = "🎯",
                description = if (uiState.isFocusModeActive) "Exit Focus" else "Start Focus",
                color = primaryColor,
                icon = if (uiState.isFocusModeActive) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                isActive = uiState.isFocusModeActive,
                onClick = { 
                    onFocusModeToggle()
                    viewModel.toggleFocusMode()
                }
            ),
            QuickAction(
                id = "quick_timer",
                title = "Quick Timer",
                emoji = "⏰",
                description = "Set activity timer",
                color = secondaryColor,
                icon = Icons.Default.Timer,
                onClick = { showTimerDialog = true }
            ),
            QuickAction(
                id = "emergency_block",
                title = "Emergency Block",
                emoji = "🚨",
                description = "Block distracting apps",
                color = errorColor,
                icon = Icons.Default.Block,
                onClick = {
                    onEmergencyBlock()
                    viewModel.triggerEmergencyBlock()
                }
            ),
            QuickAction(
                id = "quick_stats",
                title = "Quick Stats",
                emoji = "📊",
                description = "View today's usage",
                color = tertiaryColor,
                icon = Icons.Default.Analytics,
                badge = uiState.todayUsageHours,
                onClick = onViewStats
            ),
            QuickAction(
                id = "break_reminder",
                title = "Break Time",
                emoji = "☕",
                description = "Take a mindful break",
                color = Color(0xFF4CAF50),
                icon = Icons.Default.SelfImprovement,
                onClick = { viewModel.startBreakReminder() }
            ),
            QuickAction(
                id = "wellness_check",
                title = "Wellness Check",
                emoji = "❤️",
                description = "Check your score",
                color = Color(0xFFE91E63),
                icon = Icons.Default.FavoriteBorder,
                badge = "${uiState.wellnessScore}/100",
                onClick = { viewModel.refreshWellnessScore() }
            )
        )
    }
    
    PlayfulCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🚀 Quick Actions",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(240.dp)
            ) {
                items(quickActions) { action ->
                    QuickActionButton(
                        action = action,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
    
    // Timer Dialog
    if (showTimerDialog) {
        TimerSelectionDialog(
            onDismiss = { showTimerDialog = false },
            onTimerSelected = { minutes ->
                onStartTimer(minutes)
                viewModel.startActivityTimer(minutes)
                showTimerDialog = false
            }
        )
    }
}

@Composable
private fun QuickActionButton(
    action: QuickAction,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "press_animation"
    )
    
    val backgroundColor = if (action.isActive) {
        action.color.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    
    Card(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                action.onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Badge
            action.badge?.let { badge ->
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = action.color
                    ),
                    shape = CircleShape
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White
                            ),
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp
                        )
                    }
                }
            }
            
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                // Icon and emoji
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = action.emoji,
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.title,
                        tint = action.color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Title and description
                Column {
                    Text(
                        text = action.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = action.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
            
            // Active indicator
            if (action.isActive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    action.color,
                                    action.color.copy(alpha = 0.7f)
                                )
                            )
                        )
                )
            }
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun TimerSelectionDialog(
    onDismiss: () -> Unit,
    onTimerSelected: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⏰")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Quick Timer")
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Choose a timer duration for your activity:")
                
                val timerOptions = listOf(
                    5 to "5 minutes - Quick break",
                    15 to "15 minutes - Short focus",
                    25 to "25 minutes - Pomodoro",
                    45 to "45 minutes - Deep work",
                    60 to "1 hour - Extended session"
                )
                
                timerOptions.forEach { (minutes, description) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTimerSelected(minutes) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "${minutes} min",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
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
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}