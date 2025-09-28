package dev.sadakat.screentimetracker.shared.presentation.wellness

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sadakat.screentimetracker.shared.ui.components.*
import dev.sadakat.screentimetracker.shared.ui.theme.CoreSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellnessScreen(
    uiState: WellnessUiState,
    onMetricAction: (WellnessMetricType) -> Unit,
    onStartBreak: () -> Unit,
    onScheduleBreak: () -> Unit,
    onStartExercise: (MindfulnessExercise) -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenContainer(
        title = "Wellness",
        modifier = modifier
    ) {
        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            item {
                WellnessScoreCard(
                    score = uiState.wellnessScore,
                    trend = uiState.scoreTrend,
                    insights = uiState.insights
                )
            }

            item {
                SectionHeader("Wellness Metrics")
            }

            items(uiState.wellnessMetrics) { metric ->
                WellnessMetricCard(
                    metric = metric,
                    onActionClick = { onMetricAction(metric.type) }
                )
            }

            item {
                SectionHeader("Digital Breaks")
            }

            item {
                DigitalBreaksSection(
                    todayBreaks = uiState.todayBreaks,
                    suggestedBreakTime = uiState.suggestedBreakTime,
                    onStartBreak = onStartBreak,
                    onScheduleBreak = onScheduleBreak
                )
            }

            item {
                SectionHeader("Mindfulness")
            }

            item {
                MindfulnessSection(
                    streakDays = uiState.mindfulnessStreak,
                    todayProgress = uiState.todayMindfulnessProgress,
                    exercises = uiState.mindfulnessExercises,
                    onStartExercise = onStartExercise
                )
            }
        }

        uiState.error?.let { error ->
            item {
                ErrorCard(
                    message = error,
                    onRetry = { /* TODO: Add retry functionality */ }
                )
            }
        }
    }
}

@Composable
private fun WellnessScoreCard(
    score: Float,
    trend: String,
    insights: List<String>,
    modifier: Modifier = Modifier
) {
    MetricCard(
        title = "Wellness Score",
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = trend,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (trend.contains("up")) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            CircularMetric(
                value = "${(score * 100).toInt()}",
                progress = score
            )
        }

        if (insights.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Insights",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                insights.forEach { insight ->
                    Text(
                        text = "• $insight",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun WellnessMetricCard(
    metric: WellnessMetric,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = metric.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = metric.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = metric.value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (metric.status) {
                        WellnessStatus.GOOD -> MaterialTheme.colorScheme.primary
                        WellnessStatus.WARNING -> MaterialTheme.colorScheme.secondary
                        WellnessStatus.CRITICAL -> MaterialTheme.colorScheme.error
                    }
                )
            }

            LinearProgressIndicator(
                progress = { metric.progress },
                modifier = Modifier.fillMaxWidth(),
            )

            if (metric.actionText.isNotEmpty()) {
                TextButton(
                    onClick = onActionClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(metric.actionText)
                }
            }
        }
    }
}

@Composable
private fun DigitalBreaksSection(
    todayBreaks: Int,
    suggestedBreakTime: String,
    onStartBreak: () -> Unit,
    onScheduleBreak: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Breaks Today: $todayBreaks",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Next suggested: $suggestedBreakTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            ActionButtonRow(
                buttons = listOf(
                    ActionButtonConfig("Start Break Now", onStartBreak, isPrimary = true),
                    ActionButtonConfig("Schedule Break", onScheduleBreak)
                )
            )
        }
    }
}

@Composable
private fun MindfulnessSection(
    streakDays: Int,
    todayProgress: Float,
    exercises: List<MindfulnessExercise>,
    onStartExercise: (MindfulnessExercise) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Streak: $streakDays days",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Today's progress: ${(todayProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LinearProgressIndicator(
                progress = { todayProgress },
                modifier = Modifier.fillMaxWidth(),
            )

            exercises.forEach { exercise ->
                OutlinedCard(
                    onClick = { onStartExercise(exercise) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = exercise.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = exercise.duration,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(exercise.emoji)
                    }
                }
            }
        }
    }
}