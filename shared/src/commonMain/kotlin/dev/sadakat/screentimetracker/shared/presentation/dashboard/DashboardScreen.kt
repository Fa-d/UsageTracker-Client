package dev.sadakat.screentimetracker.shared.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onTakeBreak: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToWellness: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            DashboardSummaryCard(
                totalScreenTime = uiState.totalScreenTimeFormatted,
                pickupsToday = uiState.pickupsToday,
                wellnessScore = uiState.wellnessScore
            )
        }

        item {
            SectionHeader("Top Apps Today")
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.topApps) { app ->
                    AppUsageCard(
                        appName = app.appName,
                        usageTime = app.usageTimeFormatted
                    )
                }
            }
        }

        item {
            SectionHeader("Quick Actions")
        }

        item {
            ActionButtonRow(
                onTakeBreak = onTakeBreak,
                onNavigateToGoals = onNavigateToGoals,
                onNavigateToWellness = onNavigateToWellness
            )
        }
    }
}

@Composable
private fun DashboardSummaryCard(
    totalScreenTime: String,
    pickupsToday: Int,
    wellnessScore: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Today's Summary",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(
                    label = "Screen Time",
                    value = totalScreenTime
                )
                MetricItem(
                    label = "Pickups",
                    value = pickupsToday.toString()
                )
                MetricItem(
                    label = "Wellness Score",
                    value = "${(wellnessScore * 100).toInt()}%"
                )
            }
        }
    }
}

@Composable
private fun AppUsageCard(
    appName: String,
    usageTime: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(120.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("📱", style = MaterialTheme.typography.headlineSmall)
            }

            Text(
                text = appName,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )

            Text(
                text = usageTime,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActionButtonRow(
    onTakeBreak: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToWellness: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onTakeBreak,
            modifier = Modifier.weight(1f)
        ) {
            Text("Take Break")
        }
        Button(
            onClick = onNavigateToGoals,
            modifier = Modifier.weight(1f)
        ) {
            Text("View Goals")
        }
        Button(
            onClick = onNavigateToWellness,
            modifier = Modifier.weight(1f)
        ) {
            Text("Wellness")
        }
    }
}