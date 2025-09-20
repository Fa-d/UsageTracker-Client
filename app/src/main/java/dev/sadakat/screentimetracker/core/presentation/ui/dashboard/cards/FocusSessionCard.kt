package dev.sadakat.screentimetracker.core.presentation.ui.dashboard.cards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sadakat.screentimetracker.domain.usecases.FocusSessionManagerUseCase
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusSessionCard(
    focusSessionManager: FocusSessionManagerUseCase,
    modifier: Modifier = Modifier
) {
    var isSessionActive by remember { mutableStateOf(false) }
    var currentDuration by remember { mutableLongStateOf(0L) }
    var selectedDuration by remember { mutableIntStateOf(25) } // Default 25 minutes
    var showDurationPicker by remember { mutableStateOf(false) }

    // Update session state
    LaunchedEffect(Unit) {
        while (true) {
            isSessionActive = focusSessionManager.isSessionActive()
            if (isSessionActive) {
                currentDuration = focusSessionManager.getCurrentSessionDuration()
            }
            delay(1000) // Update every second
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isSessionActive) 
                Color(0xFFE3F2FD) 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎯 Focus Session",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                if (isSessionActive) {
                    Text(
                        text = "ACTIVE",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isSessionActive) {
                ActiveSessionView(
                    currentDuration = currentDuration,
                    onStop = { 
                        focusSessionManager.cancelCurrentFocusSession()
                    },
                    onComplete = {
                        focusSessionManager.completeFocusSession(wasSuccessful = true)
                    }
                )
            } else {
                InactiveSessionView(
                    selectedDuration = selectedDuration,
                    onDurationChange = { selectedDuration = it },
                    onStart = { duration ->
                        focusSessionManager.startFocusSession(duration)
                    },
                    showDurationPicker = showDurationPicker,
                    onToggleDurationPicker = { showDurationPicker = !showDurationPicker }
                )
            }
        }
    }
}

@Composable
private fun ActiveSessionView(
    currentDuration: Long,
    onStop: suspend () -> Unit,
    onComplete: suspend () -> Unit
) {
    val minutes = (currentDuration / 60000).toInt()
    val seconds = ((currentDuration % 60000) / 1000).toInt()
    
    // Animated progress circle (simplified - you could add actual target time)
    val progress by animateFloatAsState(
        targetValue = (currentDuration / (25 * 60 * 1000f)).coerceAtMost(1f),
        animationSpec = tween(durationMillis = 300), label = ""
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Time display
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
        ) {
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Stay focused! Apps are blocked during this session.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { 
                    // Launch coroutine properly in real implementation
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF44336)
                )
            ) {
                Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Stop", color = Color.White)
            }

            Button(
                onClick = { 
                    // Launch coroutine properly in real implementation
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("Complete", color = Color.White)
            }
        }
    }
}

@Composable
private fun InactiveSessionView(
    selectedDuration: Int,
    onDurationChange: (Int) -> Unit,
    onStart: suspend (Int) -> Unit,
    showDurationPicker: Boolean,
    onToggleDurationPicker: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ready to focus?",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (showDurationPicker) {
            DurationPicker(
                selectedDuration = selectedDuration,
                onDurationChange = onDurationChange
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$selectedDuration min",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            TextButton(
                onClick = onToggleDurationPicker
            ) {
                Text("Change")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { 
                // Launch coroutine properly in real implementation
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Focus Session")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Distracting apps will be blocked during the session",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DurationPicker(
    selectedDuration: Int,
    onDurationChange: (Int) -> Unit
) {
    val durations = listOf(10, 15, 25, 30, 45, 60, 90)
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        durations.forEach { duration ->
            val isSelected = duration == selectedDuration
            FilterChip(
                onClick = { onDurationChange(duration) },
                label = { Text("${duration}m") },
                selected = isSelected,
                modifier = Modifier.height(32.dp)
            )
        }
    }
}