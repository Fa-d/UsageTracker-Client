package dev.sadakat.screentimetracker.shared.presentation.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sadakat.screentimetracker.shared.ui.components.*
import dev.sadakat.screentimetracker.shared.ui.theme.CoreSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    uiState: AnalyticsUiState,
    onTimeRangeSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenContainer(
        title = "Analytics",
        modifier = modifier
    ) {
        item {
            ChipSelector(
                items = TimeRange.values().map {
                    SelectorItem(it, it.label)
                },
                selectedItem = uiState.selectedTimeRange,
                onItemSelected = onTimeRangeSelected
            )
        }

        item {
            UsageOverviewCard(
                totalTime = uiState.totalUsageTime,
                averageDaily = uiState.averageDailyUsage,
                totalPickups = uiState.totalPickups,
                averagePickups = uiState.averagePickups
            )
        }

        item {
            SectionHeader("Usage Trends")
        }

        item {
            UsageTrendsChart(
                data = uiState.usageTrendsData
            )
        }

        item {
            SectionHeader("App Usage Breakdown")
        }

        items(uiState.appUsageBreakdown) { appUsage ->
            AppUsageListItem(
                appName = appUsage.appName,
                usageTime = appUsage.usageTime,
                percentage = appUsage.percentage,
                change = appUsage.change
            )
        }
    }
}

@Composable
private fun UsageOverviewCard(
    totalTime: String,
    averageDaily: String,
    totalPickups: Int,
    averagePickups: Int,
    modifier: Modifier = Modifier
) {
    MetricCard(
        title = "Usage Overview",
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricItem(
                label = "Total Time",
                value = totalTime,
                subtitle = "Avg: $averageDaily/day"
            )
            MetricItem(
                label = "Total Pickups",
                value = totalPickups.toString(),
                subtitle = "Avg: $averagePickups/day"
            )
        }
    }
}

@Composable
private fun UsageTrendsChart(
    @Suppress("UNUSED_PARAMETER") data: List<UsageTrendData>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(CoreSpacing.cardPadding)
        ) {
            Text(
                text = "Daily Usage Trend",
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(CoreSpacing.sectionSpacing))

            // Placeholder for chart - would use a charting library in real implementation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📊 Usage Trends Chart\n(Chart implementation pending)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}