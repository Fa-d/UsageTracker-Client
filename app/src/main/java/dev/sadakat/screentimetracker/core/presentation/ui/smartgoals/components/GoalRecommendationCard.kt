package dev.sadakat.screentimetracker.core.presentation.ui.smartgoals.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sadakat.screentimetracker.core.presentation.ui.theme.LimeGreen
import dev.sadakat.screentimetracker.core.presentation.ui.theme.PlayfulAccent
import dev.sadakat.screentimetracker.core.presentation.ui.theme.SkyBlue
import dev.sadakat.screentimetracker.core.presentation.ui.theme.VibrantOrange
import dev.sadakat.screentimetracker.core.domain.usecases.SmartGoalSettingUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.SmartGoalSettingUseCase.DifficultyLevel
import dev.sadakat.screentimetracker.core.domain.usecases.SmartGoalSettingUseCase.GoalRecommendation
import java.util.concurrent.TimeUnit

@Composable
fun GoalRecommendationCard(
    recommendation: GoalRecommendation,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val confidenceColor by animateColorAsState(
        targetValue = when {
            recommendation.confidence >= 0.8f -> LimeGreen
            recommendation.confidence >= 0.6f -> VibrantOrange
            else -> Color.Red.copy(alpha = 0.7f)
        },
        animationSpec = tween(300),
        label = "confidence_color"
    )

    val difficultyColor = when (recommendation.difficulty) {
        DifficultyLevel.EASY -> LimeGreen
        DifficultyLevel.MEDIUM -> VibrantOrange  
        DifficultyLevel.HARD -> PlayfulAccent
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, confidenceColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with confidence indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(confidenceColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (recommendation.confidence >= 0.8f) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "High confidence",
                                tint = Color.White,
                                modifier = Modifier.size(8.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${(recommendation.confidence * 100).toInt()}% confident",
                        style = MaterialTheme.typography.bodySmall,
                        color = confidenceColor,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Difficulty indicator
                Box(
                    modifier = Modifier
                        .background(
                            difficultyColor.copy(alpha = 0.15f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        recommendation.difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = difficultyColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Goal title and description
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    getGoalEmoji(recommendation.goalType) + " " + recommendation.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    recommendation.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
            }

            // Progress visualization for current vs target
            if (recommendation.currentAverage > 0) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Progress Visualization",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Current: ${formatValue(recommendation.currentAverage, recommendation.goalType)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            "Target: ${formatValue(recommendation.targetValue, recommendation.goalType)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = LimeGreen
                        )
                    }
                    
                    LinearProgressIndicator(
                        progress = { 
                            minOf(1f, recommendation.targetValue.toFloat() / recommendation.currentAverage.toFloat()) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = LimeGreen,
                        trackColor = Color.Gray.copy(alpha = 0.2f)
                    )
                }
            }

            // Reasoning section
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Info",
                    tint = SkyBlue,
                    modifier = Modifier.size(16.dp)
                )
                Column {
                    Text(
                        "Why this goal?",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = SkyBlue
                    )
                    Text(
                        recommendation.reasoning,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        lineHeight = 16.sp
                    )
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Reject",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Pass")
                }
                
                androidx.compose.material3.Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = LimeGreen
                    )
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Accept",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Accept Goal")
                }
            }
        }
    }
}

private fun getGoalEmoji(goalType: String): String {
    return when (goalType) {
        SmartGoalSettingUseCase.DAILY_SCREEN_TIME -> "⏰"
        SmartGoalSettingUseCase.APP_SPECIFIC_LIMIT -> "📱"
        SmartGoalSettingUseCase.SESSION_LIMIT -> "⏱️"
        SmartGoalSettingUseCase.UNLOCK_FREQUENCY -> "🔓"
        SmartGoalSettingUseCase.FOCUS_SESSIONS -> "🎯"
        SmartGoalSettingUseCase.BREAK_GOALS -> "☕"
        else -> "🎯"
    }
}

private fun formatValue(value: Long, goalType: String): String {
    return when (goalType) {
        SmartGoalSettingUseCase.DAILY_SCREEN_TIME, 
        SmartGoalSettingUseCase.APP_SPECIFIC_LIMIT,
        SmartGoalSettingUseCase.SESSION_LIMIT -> {
            val hours = TimeUnit.MILLISECONDS.toHours(value)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(value) % 60
            when {
                hours > 0 -> "${hours}h ${minutes}m"
                minutes > 0 -> "${minutes}m"
                else -> "${TimeUnit.MILLISECONDS.toSeconds(value)}s"
            }
        }
        SmartGoalSettingUseCase.UNLOCK_FREQUENCY -> "${value} unlocks"
        SmartGoalSettingUseCase.FOCUS_SESSIONS -> "${value} sessions"
        SmartGoalSettingUseCase.BREAK_GOALS -> "${value} breaks"
        else -> value.toString()
    }
}