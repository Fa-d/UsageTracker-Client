package com.example.screentimetracker.ui.dashboard.cards

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
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
    var productivityHours by remember { mutableStateOf<List<WeeklyInsightsUseCase.ProductivityHour>>(emptyList()) }
    var categoryInsights by remember { mutableStateOf<List<WeeklyInsightsUseCase.CategoryInsight>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showFullReport by remember { mutableStateOf(false) }
    var selectedView by remember { mutableStateOf("summary") } // "summary", "productivity", "categories"

    LaunchedEffect(Unit) {
        isLoading = true
        weeklyReport = weeklyInsights.generateWeeklyReport()
        productivityHours = weeklyInsights.getProductivityHours()
        categoryInsights = weeklyInsights.getAppCategoryInsights()
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
                // View selection tabs
                ViewSelectionTabs(
                    selectedView = selectedView,
                    onViewChanged = { selectedView = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedView) {
                    "summary" -> {
                        if (showFullReport) {
                            FullWeeklyReport(
                                report = weeklyReport,
                                productivityHours = productivityHours,
                                categoryInsights = categoryInsights,
                                onCollapse = { showFullReport = false }
                            )
                        } else {
                            WeeklyInsightsSummary(
                                report = weeklyReport,
                                onExpand = { showFullReport = true }
                            )
                        }
                    }
                    "productivity" -> {
                        ProductivityHoursHeatmap(
                            productivityHours = productivityHours
                        )
                    }
                    "categories" -> {
                        CategoryInsightsChart(
                            categoryInsights = categoryInsights
                        )
                    }
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
    productivityHours: List<WeeklyInsightsUseCase.ProductivityHour>,
    categoryInsights: List<WeeklyInsightsUseCase.CategoryInsight>,
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

@Composable
private fun ViewSelectionTabs(
    selectedView: String,
    onViewChanged: (String) -> Unit
) {
    val tabs = listOf(
        "summary" to "Summary",
        "productivity" to "Productivity", 
        "categories" to "Categories"
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(tabs) { (key, label) ->
            FilterChip(
                onClick = { onViewChanged(key) },
                label = { Text(label) },
                selected = selectedView == key,
                leadingIcon = {
                    Icon(
                        imageVector = when (key) {
                            "productivity" -> Icons.Default.AccessTime
                            "categories" -> Icons.Default.Category
                            else -> Icons.Default.TrendingUp
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun ProductivityHoursHeatmap(
    productivityHours: List<WeeklyInsightsUseCase.ProductivityHour>
) {
    Column {
        Text(
            text = "⏰ Productivity Hours Heatmap",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Shows your phone usage intensity throughout the day (24-hour view)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // 24-hour heatmap grid
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Hour labels
            Row {
                Spacer(modifier = Modifier.width(40.dp)) // Space for day labels
                repeat(24) { hour ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (hour % 4 == 0) { // Show labels every 4 hours
                            Text(
                                text = "${hour}h",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Single week row (simplified for demo)
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.width(40.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Week",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                repeat(24) { hour ->
                    val hourData = productivityHours.find { it.hour == hour }
                    val intensity = hourData?.let { 
                        (it.usageTimeMillis.toFloat() / TimeUnit.HOURS.toMillis(1).toFloat()).coerceAtMost(1f)
                    } ?: 0f
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(1.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = intensity * 0.8f + 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Less",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(5) { level ->
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = (level * 0.2f + 0.1f)),
                                RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
            
            Text(
                text = "More",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable 
private fun CategoryInsightsChart(
    categoryInsights: List<WeeklyInsightsUseCase.CategoryInsight>
) {
    Column {
        Text(
            text = "📊 App Category Breakdown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Time spent across different app categories",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        if (categoryInsights.isNotEmpty()) {
            // Pie chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier.size(160.dp)
                ) {
                    drawCategoryPieChart(categoryInsights, size)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Category list with percentages
            categoryInsights.forEach { category ->
                CategoryItem(
                    name = category.categoryName,
                    timeMillis = category.totalTimeMillis,
                    percentage = category.percentageOfTotal,
                    color = getCategoryColor(category.categoryName)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            Text(
                text = "No category data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun CategoryItem(
    name: String,
    timeMillis: Long,
    percentage: Float,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, CircleShape)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${TimeUnit.MILLISECONDS.toHours(timeMillis)}h ${TimeUnit.MILLISECONDS.toMinutes(timeMillis) % 60}m",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = "${percentage.toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun DrawScope.drawCategoryPieChart(
    categories: List<WeeklyInsightsUseCase.CategoryInsight>,
    canvasSize: Size
) {
    val total = categories.sumOf { it.totalTimeMillis }.toFloat()
    if (total <= 0) return

    val radius = (canvasSize.width.coerceAtMost(canvasSize.height)) / 2f * 0.8f
    val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
    
    var startAngle = -90f // Start from top
    
    categories.forEach { category ->
        val sweepAngle = (category.totalTimeMillis.toFloat() / total) * 360f
        val color = getCategoryColor(category.categoryName)
        
        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset(
                center.x - radius,
                center.y - radius
            ),
            size = Size(radius * 2, radius * 2)
        )
        
        startAngle += sweepAngle
    }
}

private fun getCategoryColor(categoryName: String): Color {
    return when (categoryName) {
        "Social" -> Color(0xFF2196F3)
        "Entertainment" -> Color(0xFFFF9800)
        "Productivity" -> Color(0xFF4CAF50)
        "Communication" -> Color(0xFF9C27B0)
        "Games" -> Color(0xFFF44336)
        else -> Color(0xFF607D8B)
    }
}