package dev.sadakat.screentimetracker.core.presentation.ui.smartgoals.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sadakat.screentimetracker.core.presentation.ui.theme.LimeGreen
import dev.sadakat.screentimetracker.core.presentation.ui.theme.PlayfulAccent
import dev.sadakat.screentimetracker.core.presentation.ui.theme.VibrantOrange
import dev.sadakat.screentimetracker.core.domain.usecases.SmartGoalSettingUseCase.AdjustmentType
import dev.sadakat.screentimetracker.core.domain.usecases.SmartGoalSettingUseCase.GoalAdjustment
import java.util.concurrent.TimeUnit

@Composable
fun GoalAdjustmentDialog(
    adjustment: GoalAdjustment,
    isApplying: Boolean,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    val progressAnimated by animateFloatAsState(
        targetValue = adjustment.confidence,
        animationSpec = tween(1000),
        label = "confidence_progress"
    )

    AlertDialog(
        onDismissRequest = { if (!isApplying) onDismiss() },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    when (adjustment.adjustmentType) {
                        AdjustmentType.MAKE_EASIER -> Icons.Default.TrendingDown
                        AdjustmentType.MAKE_HARDER -> Icons.Default.TrendingUp
                    },
                    contentDescription = null,
                    tint = when (adjustment.adjustmentType) {
                        AdjustmentType.MAKE_EASIER -> VibrantOrange
                        AdjustmentType.MAKE_HARDER -> LimeGreen
                    }
                )
                Text(
                    "Smart Goal Adjustment",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Adjustment type indicator
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (adjustment.adjustmentType) {
                            AdjustmentType.MAKE_EASIER -> VibrantOrange.copy(alpha = 0.1f)
                            AdjustmentType.MAKE_HARDER -> LimeGreen.copy(alpha = 0.1f)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            when (adjustment.adjustmentType) {
                                AdjustmentType.MAKE_EASIER -> Icons.Default.ArrowDownward
                                AdjustmentType.MAKE_HARDER -> Icons.Default.ArrowUpward
                            },
                            contentDescription = null,
                            tint = when (adjustment.adjustmentType) {
                                AdjustmentType.MAKE_EASIER -> VibrantOrange
                                AdjustmentType.MAKE_HARDER -> LimeGreen
                            },
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                when (adjustment.adjustmentType) {
                                    AdjustmentType.MAKE_EASIER -> "Make Goal Easier"
                                    AdjustmentType.MAKE_HARDER -> "Make Goal Harder"
                                },
                                fontWeight = FontWeight.Bold,
                                color = when (adjustment.adjustmentType) {
                                    AdjustmentType.MAKE_EASIER -> VibrantOrange
                                    AdjustmentType.MAKE_HARDER -> LimeGreen
                                }
                            )
                            Text(
                                "New target: ${formatAdjustmentValue(adjustment.newTargetValue)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Reasoning
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Why this adjustment?",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        adjustment.reasoning,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = 20.sp
                    )
                }

                // Confidence indicator
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Confidence Level",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${(progressAnimated * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                progressAnimated >= 0.8f -> LimeGreen
                                progressAnimated >= 0.6f -> VibrantOrange
                                else -> PlayfulAccent
                            }
                        )
                    }
                    
                    LinearProgressIndicator(
                        progress = { progressAnimated },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = when {
                            progressAnimated >= 0.8f -> LimeGreen
                            progressAnimated >= 0.6f -> VibrantOrange
                            else -> PlayfulAccent
                        },
                        trackColor = Color.Gray.copy(alpha = 0.2f)
                    )
                    
                    Text(
                        when {
                            progressAnimated >= 0.8f -> "High confidence - this adjustment is well-founded"
                            progressAnimated >= 0.6f -> "Medium confidence - based on your recent progress"
                            else -> "Lower confidence - consider your personal preferences"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Benefits of adjustment
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Expected Benefits:",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    val benefits = when (adjustment.adjustmentType) {
                        AdjustmentType.MAKE_EASIER -> listOf(
                            "• Improved success rate",
                            "• Reduced frustration",
                            "• Better long-term habit building"
                        )
                        AdjustmentType.MAKE_HARDER -> listOf(
                            "• Greater challenge to maintain engagement",
                            "• Faster progress toward wellness goals",
                            "• Enhanced sense of achievement"
                        )
                    }
                    
                    benefits.forEach { benefit ->
                        Text(
                            benefit,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = onApply,
                enabled = !isApplying,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = when (adjustment.adjustmentType) {
                        AdjustmentType.MAKE_EASIER -> VibrantOrange
                        AdjustmentType.MAKE_HARDER -> LimeGreen
                    }
                )
            ) {
                if (isApplying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Applying...")
                } else {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Apply",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Apply Adjustment")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isApplying
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cancel",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Keep Current Goal")
            }
        }
    )
}

private fun formatAdjustmentValue(value: Long): String {
    return when {
        value > TimeUnit.HOURS.toMillis(1) -> {
            val hours = TimeUnit.MILLISECONDS.toHours(value)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(value) % 60
            "${hours}h ${minutes}m"
        }
        value > TimeUnit.MINUTES.toMillis(1) -> {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(value)
            "${minutes}m"
        }
        value > 100 -> "${value} times"
        else -> value.toString()
    }
}