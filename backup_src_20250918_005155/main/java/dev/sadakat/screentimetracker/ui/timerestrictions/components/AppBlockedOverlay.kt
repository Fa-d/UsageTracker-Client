package dev.sadakat.screentimetracker.ui.timerestrictions.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.sadakat.screentimetracker.core.data.local.TimeRestriction
import dev.sadakat.screentimetracker.ui.theme.ColorfulPrimary
import dev.sadakat.screentimetracker.ui.theme.ColorfulSecondary
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun AppBlockedOverlay(
    appName: String,
    packageName: String,
    activeRestrictions: List<TimeRestriction>,
    timeUntilUnblocked: String?,
    onEmergencyOverride: () -> Unit,
    onClose: () -> Unit,
    onOpenAlternative: () -> Unit
) {
    var showEmergencyDialog by remember { mutableStateOf(false) }
    var animationVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        animationVisible = true
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.9f),
                            ColorfulPrimary.copy(alpha = 0.8f),
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
        ) {
            // Animated background elements
            BlockedBackground()

            AnimatedVisibility(
                visible = animationVisible,
                enter = scaleIn(
                    animationSpec = tween(500),
                    initialScale = 0.8f
                ) + fadeIn(animationSpec = tween(500)),
                exit = scaleOut(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Main Blocking Icon
                    Icon(
                        Icons.Default.Block,
                        contentDescription = "App Blocked",
                        modifier = Modifier.size(120.dp),
                        tint = Color.Red.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "$appName is Blocked",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 32.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Restriction reason
                    if (activeRestrictions.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        activeRestrictions.first().name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    activeRestrictions.first().description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )

                                timeUntilUnblocked?.let { timeLeft ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Available in $timeLeft",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = ColorfulSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Motivational message
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = ColorfulSecondary.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            getMotivationalMessage(),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Action buttons
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onOpenAlternative,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ColorfulSecondary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Find Alternative Activity",
                                fontWeight = FontWeight.Medium
                            )
                        }

                        OutlinedButton(
                            onClick = { showEmergencyDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White.copy(alpha = 0.8f)
                            )
                        ) {
                            Icon(
                                Icons.Default.Emergency,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Emergency Access")
                        }

                        TextButton(
                            onClick = onClose,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Return to Home",
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Emergency Override Dialog
            if (showEmergencyDialog) {
                EmergencyOverrideDialog(
                    appName = appName,
                    onConfirm = {
                        showEmergencyDialog = false
                        onEmergencyOverride()
                    },
                    onDismiss = { showEmergencyDialog = false }
                )
            }
        }
    }
}

@Composable
fun BlockedBackground() {
    var animationPhase by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(50)
            animationPhase += 1f
        }
    }

    val alpha by animateFloatAsState(
        targetValue = 0.3f + 0.1f * kotlin.math.sin(animationPhase * 0.02).toFloat(),
        animationSpec = tween(100), label = ""
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
    ) {
        repeat(20) { index ->
            val offset = Offset(
                x = (size.width * Random.nextFloat()),
                y = (size.height * Random.nextFloat())
            )
            
            rotate(animationPhase + index * 18f, pivot = offset) {
                drawCircle(
                    color = Color.Red.copy(alpha = 0.1f),
                    radius = 50f + (index * 10f).toFloat(),
                    center = offset
                )
                
                drawLine(
                    color = Color.Red.copy(alpha = 0.2f),
                    start = Offset(offset.x - 30f, offset.y - 30f),
                    end = Offset(offset.x + 30f, offset.y + 30f),
                    strokeWidth = 4f
                )
                
                drawLine(
                    color = Color.Red.copy(alpha = 0.2f),
                    start = Offset(offset.x + 30f, offset.y - 30f),
                    end = Offset(offset.x - 30f, offset.y + 30f),
                    strokeWidth = 4f
                )
            }
        }
    }
}

@Composable
fun EmergencyOverrideDialog(
    appName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFFFF9800)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Emergency Override",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Are you sure you need emergency access to $appName?\n\nThis will temporarily bypass the restriction for 10 minutes.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    )
                ) {
                    Text("Allow Access")
                }
            }
        }
    }
}

private fun getMotivationalMessage(): String {
    val messages = listOf(
        "🌟 Time for something more meaningful!",
        "💪 Your future self will thank you!",
        "🎯 Focus on what truly matters.",
        "🌱 Every moment is a chance to grow.",
        "⏰ This time is precious - use it wisely!",
        "🧠 Give your mind a healthy break.",
        "🎨 Perfect time for creative activities!",
        "📚 Maybe read something inspiring?",
        "🚶 How about a mindful walk?",
        "💤 Your digital wellness matters!"
    )
    return messages.random()
}