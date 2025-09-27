package dev.sadakat.screentimetracker.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
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
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            DashboardSummaryCard(
                totalScreenTime = uiState.totalScreenTime,
                pickupsToday = uiState.pickupsToday,
                wellnessScore = uiState.wellnessScore
            )
        }

        item {
            Text(
                text = "Top Apps Today",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.topApps) { app ->
                    AppUsageCard(
                        appName = app.name,
                        usageTime = app.usageTime,
                        iconRes = app.iconRes
                    )
                }
            }
        }

        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        item {
            QuickActionsSection(
                onSetBreak = viewModel::setBreak,
                onViewGoals = viewModel::navigateToGoals,
                onCheckWellness = viewModel::navigateToWellness
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
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Today's Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    label = "Screen Time",
                    value = totalScreenTime
                )
                SummaryItem(
                    label = "Pickups",
                    value = pickupsToday.toString()
                )
                SummaryItem(
                    label = "Wellness Score",
                    value = "${(wellnessScore * 100).toInt()}%"
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
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
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AppUsageCard(
    appName: String,
    usageTime: String,
    iconRes: Int?,
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
            // Placeholder for app icon
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("📱")
            }

            Text(
                text = appName,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )

            Text(
                text = usageTime,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun QuickActionsSection(
    onSetBreak: () -> Unit,
    onViewGoals: () -> Unit,
    onCheckWellness: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onSetBreak,
            modifier = Modifier.weight(1f)
        ) {
            Text("Take Break")
        }

        OutlinedButton(
            onClick = onViewGoals,
            modifier = Modifier.weight(1f)
        ) {
            Text("View Goals")
        }

        OutlinedButton(
            onClick = onCheckWellness,
            modifier = Modifier.weight(1f)
        ) {
            Text("Wellness")
        }
    }
}