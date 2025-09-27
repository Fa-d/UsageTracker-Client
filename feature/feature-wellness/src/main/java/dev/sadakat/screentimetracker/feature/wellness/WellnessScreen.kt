package dev.sadakat.screentimetracker.feature.wellness

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellnessScreen(
    modifier: Modifier = Modifier,
    viewModel: WellnessViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Wellness",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            WellnessScoreCard(
                score = uiState.wellnessScore,
                trend = uiState.scoreTrend,
                insights = uiState.insights
            )
        }

        item {
            Text(
                text = "Wellness Metrics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        items(uiState.wellnessMetrics) { metric ->
            WellnessMetricCard(
                metric = metric,
                onActionClick = { viewModel.handleMetricAction(metric.type) }
            )
        }

        item {
            Text(
                text = "Digital Breaks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            DigitalBreaksSection(
                todayBreaks = uiState.todayBreaks,
                suggestedBreakTime = uiState.suggestedBreakTime,
                onStartBreak = viewModel::startBreak,
                onScheduleBreak = viewModel::scheduleBreak
            )
        }

        item {
            Text(
                text = "Mindfulness",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            MindfulnessSection(
                streakDays = uiState.mindfulnessStreak,
                todayProgress = uiState.todayMindfulnessProgress,
                exercises = uiState.mindfulnessExercises,
                onStartExercise = viewModel::startMindfulnessExercise
            )
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
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Wellness Score",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
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

                // Circular progress indicator for wellness score
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { score },
                        modifier = Modifier.size(80.dp),
                    )
                    Text(
                        text = "${(score * 100).toInt()}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onStartBreak,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Start Break Now")
                }

                OutlinedButton(
                    onClick = onScheduleBreak,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Schedule Break")
                }
            }
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