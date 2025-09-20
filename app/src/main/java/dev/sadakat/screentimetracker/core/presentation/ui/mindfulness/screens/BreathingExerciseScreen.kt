package dev.sadakat.screentimetracker.core.presentation.ui.mindfulness.screens

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sadakat.screentimetracker.core.presentation.ui.components.PlayfulCard
import dev.sadakat.screentimetracker.core.presentation.ui.mindfulness.viewmodels.MindfulnessUiState
import dev.sadakat.screentimetracker.core.presentation.ui.mindfulness.viewmodels.MindfulnessViewModel
import dev.sadakat.screentimetracker.data.local.entities.MindfulnessSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingExerciseScreen(
    onBackPressed: () -> Unit,
    triggeredByAppBlock: Boolean = false,
    blockedAppName: String = "",
    viewModel: MindfulnessViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    
    LaunchedEffect(triggeredByAppBlock) {
        if (triggeredByAppBlock) {
            viewModel.setTriggeredByAppBlock(blockedAppName)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Breathing Exercise") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isSessionActive) {
                        IconButton(
                            onClick = { viewModel.pauseSession() }
                        ) {
                            Icon(
                                if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = if (uiState.isPaused) "Resume" else "Pause"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App block notification
            if (triggeredByAppBlock && blockedAppName.isNotEmpty()) {
                PlayfulCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Block,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$blockedAppName was blocked",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Take a mindful breath instead",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Session selection
            if (!uiState.isSessionActive) {
                BreathingSessionSelector(
                    onSessionSelected = { duration, type ->
                        viewModel.startBreathingSession(duration, type)
                    }
                )
            }
            
            // Main breathing animation
            if (uiState.isSessionActive) {
                BreathingAnimationCard(
                    uiState = uiState,
                    modifier = Modifier.weight(1f)
                )
                
                // Session controls
                BreathingSessionControls(
                    uiState = uiState,
                    onEndSession = { viewModel.endSession() },
                    onPauseResume = { viewModel.pauseSession() }
                )
            }
        }
    }
    
    // Session completion dialog
    if (uiState.showCompletionDialog) {
        SessionCompletionDialog(
            sessionData = uiState.completedSession!!,
            onDismiss = { viewModel.dismissCompletionDialog() },
            onRatingSubmitted = { rating, notes ->
                viewModel.submitSessionFeedback(rating, notes)
            }
        )
    }
}

@Composable
private fun BreathingSessionSelector(
    onSessionSelected: (Int, String) -> Unit
) {
    PlayfulCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🧘 Choose Your Breathing Exercise",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            val exercises = listOf(
                BreathingExercise("Quick Calm", "2-minute basic breathing", 2, "basic"),
                BreathingExercise("Deep Focus", "5-minute box breathing", 5, "box"),
                BreathingExercise("Stress Relief", "7-minute 4-7-8 breathing", 7, "478"),
                BreathingExercise("Sleep Prep", "10-minute calming breath", 10, "calming")
            )
            
            exercises.forEach { exercise ->
                BreathingExerciseCard(
                    exercise = exercise,
                    onClick = { onSessionSelected(exercise.durationMinutes, exercise.type) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun BreathingExerciseCard(
    exercise: BreathingExercise,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${exercise.durationMinutes}m",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = exercise.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Start",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun BreathingAnimationCard(
    uiState: MindfulnessUiState,
    modifier: Modifier = Modifier
) {
    PlayfulCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Timer and phase
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatTime(uiState.remainingTimeMillis),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Text(
                    text = uiState.currentPhase.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Breathing animation
            BreathingAnimationCircle(
                animationPhase = uiState.animationPhase,
                isPaused = uiState.isPaused,
                modifier = Modifier.size(200.dp)
            )
            
            // Instructions
            Text(
                text = getPhaseInstruction(uiState.currentPhase),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BreathingAnimationCircle(
    animationPhase: Float,
    isPaused: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (isPaused) 0.8f else 0.6f + (animationPhase * 0.4f),
        animationSpec = if (isPaused) {
            tween(300)
        } else {
            tween(4000, easing = EaseInOut)
        },
        label = "breathing_scale"
    )
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isPaused) 0.5f else 0.3f + (animationPhase * 0.4f),
        animationSpec = tween(4000, easing = EaseInOut),
        label = "breathing_alpha"
    )
    
    Canvas(modifier = modifier) {
        drawBreathingCircle(
            scale = animatedScale,
            alpha = animatedAlpha,
            isPaused = isPaused
        )
    }
}

private fun DrawScope.drawBreathingCircle(
    scale: Float,
    alpha: Float,
    isPaused: Boolean
) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val baseRadius = size.minDimension / 4f
    val radius = baseRadius * scale
    
    // Outer glow circle
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF6366F1).copy(alpha = alpha * 0.3f),
                Color(0xFF6366F1).copy(alpha = 0f)
            ),
            radius = radius * 2f
        ),
        radius = radius * 2f,
        center = size.center
    )
    
    // Main circle
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF6366F1).copy(alpha = alpha),
                Color(0xFF8B5CF6).copy(alpha = alpha * 0.7f)
            ),
            radius = radius
        ),
        radius = radius,
        center = size.center
    )
    
    // Inner highlight
    drawCircle(
        color = Color.White.copy(alpha = alpha * 0.3f),
        radius = radius * 0.6f,
        center = size.center
    )
    
    if (isPaused) {
        // Pause indicator
        val pauseWidth = radius * 0.3f
        val pauseHeight = radius * 0.8f
        val pauseSpacing = radius * 0.2f
        
        drawRect(
            color = Color.White.copy(alpha = 0.8f),
            topLeft = androidx.compose.ui.geometry.Offset(
                centerX - pauseSpacing / 2f - pauseWidth,
                centerY - pauseHeight / 2f
            ),
            size = androidx.compose.ui.geometry.Size(pauseWidth, pauseHeight)
        )
        
        drawRect(
            color = Color.White.copy(alpha = 0.8f),
            topLeft = androidx.compose.ui.geometry.Offset(
                centerX + pauseSpacing / 2f,
                centerY - pauseHeight / 2f
            ),
            size = androidx.compose.ui.geometry.Size(pauseWidth, pauseHeight)
        )
    }
}

@Composable
private fun BreathingSessionControls(
    uiState: MindfulnessUiState,
    onEndSession: () -> Unit,
    onPauseResume: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        OutlinedButton(
            onClick = onEndSession,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Default.Stop, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("End Session")
        }
        
        Button(
            onClick = onPauseResume
        ) {
            Icon(
                if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (uiState.isPaused) "Resume" else "Pause")
        }
    }
}

@Composable
private fun SessionCompletionDialog(
    sessionData: MindfulnessSession,
    onDismiss: () -> Unit,
    onRatingSubmitted: (Int, String) -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    var notes by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("🎉 Session Complete!")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Great job! You completed a ${formatDuration(sessionData.durationMillis)} breathing session.")
                
                Text(
                    text = "How was your experience?",
                    style = MaterialTheme.typography.titleSmall
                )
                
                // Star rating
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(5) { index ->
                        IconButton(
                            onClick = { rating = index + 1 }
                        ) {
                            Icon(
                                if (index < rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Rate ${index + 1} star${if (index == 0) "" else "s"}",
                                tint = if (index < rating) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    placeholder = { Text("How do you feel?") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onRatingSubmitted(rating, notes) },
                enabled = rating > 0
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Skip")
            }
        }
    )
}

private data class BreathingExercise(
    val name: String,
    val description: String,
    val durationMinutes: Int,
    val type: String
)

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

private fun formatDuration(milliseconds: Long): String {
    val minutes = milliseconds / (60 * 1000)
    return "${minutes} minute${if (minutes != 1L) "s" else ""}"
}

private fun getPhaseInstruction(phase: String): String {
    return when (phase) {
        "inhale" -> "Breathe in slowly through your nose"
        "hold" -> "Hold your breath gently"
        "exhale" -> "Breathe out slowly through your mouth"
        else -> "Follow your natural breath"
    }
}