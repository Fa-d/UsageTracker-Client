package dev.sadakat.screentimetracker.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.sadakat.screentimetracker.core.ui.components.*
import dev.sadakat.screentimetracker.core.common.utils.millisToReadableTime

/**
 * Dashboard screen showing key metrics and stats
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onNavigateToDetails: (String) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Quick stats cards
        items(getDashboardStats()) { stat ->
            StatisticsCard(
                title = stat.title,
                value = stat.value,
                subtitle = stat.subtitle,
                icon = {
                    Icon(
                        imageVector = stat.icon,
                        contentDescription = stat.title,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trend = stat.trend,
                onClick = { onNavigateToDetails(stat.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            ActionCard(
                title = "Setup App Limits",
                description = "Configure daily usage limits for your most used apps",
                actionText = "Set Limits",
                onAction = { onNavigateToDetails("limits") },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Set limits",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            )
        }

        item {
            FeatureCard(
                title = "Focus Mode",
                description = "Block distracting apps during work hours",
                onClick = { onNavigateToDetails("focus") },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Focus mode",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            )
        }
    }
}

private data class DashboardStat(
    val id: String,
    val title: String,
    val value: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val trend: Trend? = null
)

private fun getDashboardStats(): List<DashboardStat> {
    return listOf(
        DashboardStat(
            id = "screen_time",
            title = "Screen Time Today",
            value = millisToReadableTime(6 * 60 * 60 * 1000), // 6 hours
            subtitle = "Daily average: 5h 30m",
            icon = Icons.Default.Phone,
            trend = Trend(
                percentage = 8.5f,
                isPositive = false,
                label = "vs yesterday"
            )
        ),
        DashboardStat(
            id = "app_opens",
            title = "App Opens",
            value = "127",
            subtitle = "Most used: Instagram",
            icon = Icons.Default.TrendingUp,
            trend = Trend(
                percentage = 12.0f,
                isPositive = true,
                label = "vs yesterday"
            )
        ),
        DashboardStat(
            id = "focus_sessions",
            title = "Focus Sessions",
            value = "3",
            subtitle = "Total focus time: 2h 45m",
            icon = Icons.Default.Timer,
            trend = Trend(
                percentage = 25.0f,
                isPositive = true,
                label = "vs yesterday"
            )
        )
    )
}