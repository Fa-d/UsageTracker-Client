package com.example.screentimetracker.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.screentimetracker.domain.usecases.WeeklyInsightsUseCase
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyInsightsCard(
    weeklyInsights: WeeklyInsightsUseCase,
    modifier: Modifier = Modifier
) {
    var weeklyReport by remember { mutableStateOf(WeeklyInsightsUseCase.WeeklyReport.empty()) }
    var isLoading by remember { mutableStateOf(true) }
    var showFullReport by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        weeklyReport = weeklyInsights.generateWeeklyReport()
        isLoading = false
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊 Weekly Insights",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                if (showFullReport) {
                    FullWeeklyReport(
                        report = weeklyReport,
                        onCollapse = { showFullReport = false }
                    )
                } else {
                    WeeklyInsightsSummary(
                        report = weeklyReport,
                        onExpand = { showFullReport = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyInsightsSummary(
    report: WeeklyInsightsUseCase.WeeklyReport,
    onExpand: () -> Unit
) {
    Column {
        // Week period
        val weekFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val weekPeriod = "${weekFormat.format(Date(report.weekStart))} - ${weekFormat.format(Date(report.weekEnd))}"
        
        Text(
            text = weekPeriod,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Key metrics row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricCard(
                title = "Screen Time",
                value = "${TimeUnit.MILLISECONDS.toHours(report.totalScreenTimeMillis)}h",
                subtitle = "${TimeUnit.MILLISECONDS.toMinutes(report.averageDailyScreenTimeMillis)}m/day",
                color = MaterialTheme.colorScheme.primary
            )
            
            MetricCard(
                title = "Wellness",
                value = "${report.averageWellnessScore}%",
                subtitle = "avg score",
                color = when {
                    report.averageWellnessScore >= 75 -> Color(0xFF4CAF50)
                    report.averageWellnessScore >= 50 -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                }
            )
            
            MetricCard(
                title = "Unlocks",
                value = "${report.averageUnlocksPerDay}",
                subtitle = "per day",
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Top insight
        if (report.insights.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = report.insights.first(),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Expand button
        TextButton(
            onClick = onExpand,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("View Full Report")
        }
    }
}

@Composable
private fun FullWeeklyReport(
    report: WeeklyInsightsUseCase.WeeklyReport,
    onCollapse: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.heightIn(max = 400.dp)
    ) {
        // Summary metrics
        item {
            WeeklyInsightsSummary(report = report, onExpand = {})
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Top apps section
        if (report.topApps.isNotEmpty()) {
            item {
                Text(
                    text = "📱 Most Used Apps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            items(report.topApps.take(3)) { appInsight ->
                AppUsageItem(appInsight = appInsight)
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // All insights
        item {
            Text(
                text = "💡 Insights & Recommendations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        items(report.insights) { insight ->
            InsightItem(insight = insight)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Collapse button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onCollapse,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Show Less")
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AppUsageItem(
    appInsight: WeeklyInsightsUseCase.AppUsageInsight,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App icon placeholder
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = appInsight.packageName.take(2).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = appInsight.packageName.split(".").lastOrNull()?.replaceFirstChar { it.uppercase() } 
                    ?: appInsight.packageName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "${appInsight.sessionsCount} sessions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = "${TimeUnit.MILLISECONDS.toHours(appInsight.totalTimeMillis)}h",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun InsightItem(
    insight: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .offset(y = 6.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = insight,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}