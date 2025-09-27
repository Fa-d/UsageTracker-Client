package dev.sadakat.screentimetracker.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.sadakat.screentimetracker.core.ui.components.*
import dev.sadakat.screentimetracker.core.ui.theme.CoreSpacing
import dev.sadakat.screentimetracker.core.ui.theme.CoreTextStyles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ScreenContainer(
        title = "Dashboard",
        modifier = modifier
    ) {
        item {
            DashboardSummaryCard(
                totalScreenTime = uiState.totalScreenTime,
                pickupsToday = uiState.pickupsToday,
                wellnessScore = uiState.wellnessScore
            )
        }

        item {
            SectionHeader("Top Apps Today")
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(CoreSpacing.cardContentSpacing)
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
            SectionHeader("Quick Actions")
        }

        item {
            ActionButtonRow(
                buttons = listOf(
                    ActionButtonConfig("Take Break", viewModel::setBreak),
                    ActionButtonConfig("View Goals", viewModel::navigateToGoals),
                    ActionButtonConfig("Wellness", viewModel::navigateToWellness)
                )
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
    MetricCard(
        title = "Today's Summary",
        modifier = modifier
    ) {
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

@Composable
private fun AppUsageCard(
    appName: String,
    usageTime: String,
    @Suppress("UNUSED_PARAMETER") iconRes: Int?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(120.dp)
    ) {
        Column(
            modifier = Modifier.padding(CoreSpacing.cardContentSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CoreSpacing.minorSpacing)
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
                style = CoreTextStyles.listItemSubtitle,
                maxLines = 1
            )

            Text(
                text = usageTime,
                style = CoreTextStyles.listItemTitle,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}