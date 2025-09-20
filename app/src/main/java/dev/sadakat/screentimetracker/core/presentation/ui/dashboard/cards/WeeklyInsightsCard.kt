package dev.sadakat.screentimetracker.core.presentation.ui.dashboard.cards

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
import androidx.compose.material.icons.filled.Close
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
import dev.sadakat.screentimetracker.domain.usecases.WeeklyInsightsUseCase
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
                .padding(8.dp)
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

        // Productivity insights section
        item {
            ProductivityInsightsSection(
                productivityHours = productivityHours,
                totalScreenTime = report.totalScreenTimeMillis
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Category insights summary section  
        item {
            CategoryInsightsSummary(
                categoryInsights = categoryInsights,
                totalScreenTime = report.totalScreenTimeMillis
            )
            Spacer(modifier = Modifier.height(16.dp))
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
private fun ProductivityInsightsSection(
    productivityHours: List<WeeklyInsightsUseCase.ProductivityHour>,
    totalScreenTime: Long
) {
    Column {
        Text(
            text = "⏰ Productivity Patterns",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        val peakHours = productivityHours.sortedByDescending { it.usageTimeMillis }.take(3)
        val lowUsageHours = productivityHours.filter { it.usageTimeMillis > 0 }.sortedBy { it.usageTimeMillis }.take(3)
        val avgProductivity = productivityHours.map { it.productivity }.average()
        
        // Quick stats
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Peak Hour",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${peakHours.firstOrNull()?.hour ?: 0}:00",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Avg Score",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(avgProductivity * 100).toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                avgProductivity >= 0.7f -> Color(0xFF4CAF50)
                                avgProductivity >= 0.4f -> Color(0xFFFF9800)
                                else -> Color(0xFFF44336)
                            }
                        )
                    }
                }
            }
            
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Active Hours",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${productivityHours.count { it.usageTimeMillis > 0 }}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryInsightsSummary(
    categoryInsights: List<WeeklyInsightsUseCase.CategoryInsight>,
    totalScreenTime: Long
) {
    if (categoryInsights.isEmpty()) return
    
    Column {
        Text(
            text = "📊 Category Highlights",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        val topCategory = categoryInsights.first()
        val balanceScore = calculateCategoryBalance(categoryInsights)
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = getCategoryColor(topCategory.categoryName).copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
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
                            text = "Most Used Category",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = topCategory.categoryName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = getCategoryColor(topCategory.categoryName)
                        )
                        Text(
                            text = "${topCategory.percentageOfTotal.toInt()}% of total time",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Balance Score",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${balanceScore.toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                balanceScore >= 75 -> Color(0xFF4CAF50)
                                balanceScore >= 50 -> Color(0xFFFF9800)
                                else -> Color(0xFFF44336)
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = getBalanceRecommendation(balanceScore, topCategory.categoryName),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun calculateCategoryBalance(categoryInsights: List<WeeklyInsightsUseCase.CategoryInsight>): Float {
    if (categoryInsights.size <= 1) return 100f
    
    val maxPercentage = categoryInsights.maxOfOrNull { it.percentageOfTotal } ?: 0f
    return 100f - maxPercentage.coerceAtMost(100f)
}

private fun getBalanceRecommendation(balanceScore: Float, topCategory: String): String {
    return when {
        balanceScore >= 75 -> "Great app category balance! You're using a good variety of apps."
        balanceScore >= 50 -> "Moderate balance. Consider diversifying your app usage a bit more."
        else -> "Your time is heavily focused on $topCategory. Try exploring other productive categories."
    }
}

@Composable
private fun ProductivityHoursHeatmap(
    productivityHours: List<WeeklyInsightsUseCase.ProductivityHour>
) {
    var selectedHour by remember { mutableStateOf<Int?>(null) }
    val maxUsageTime = productivityHours.maxOfOrNull { it.usageTimeMillis } ?: 1L
    
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

        // Selected hour details
        if (selectedHour != null) {
            val hourData = productivityHours.find { it.hour == selectedHour }
            if (hourData != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedHour}:00 - ${(selectedHour ?: 0) + 1}:00",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { selectedHour = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close details",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Usage Time",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${TimeUnit.MILLISECONDS.toMinutes(hourData.usageTimeMillis)}m",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            Column {
                                Text(
                                    text = "Productivity Score",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${(hourData.productivity * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = when {
                                        hourData.productivity >= 0.7f -> Color(0xFF4CAF50)
                                        hourData.productivity >= 0.4f -> Color(0xFFFF9800)
                                        else -> Color(0xFFF44336)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

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

            // Single week row with clickable hours
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
                    val intensity = if (maxUsageTime > 0) {
                        (hourData?.usageTimeMillis?.toFloat() ?: 0f) / maxUsageTime.toFloat()
                    } else 0f
                    val isSelected = selectedHour == hour
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(1.dp)
                            .clickable { selectedHour = if (isSelected) null else hour }
                            .background(
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = intensity * 0.8f + 0.1f)
                                },
                                shape = RoundedCornerShape(4.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Text(
                                text = hour.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Peak hours summary
        val peakHours = productivityHours.sortedByDescending { it.usageTimeMillis }.take(3)
        if (peakHours.isNotEmpty()) {
            Text(
                text = "📈 Peak Usage Hours",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(peakHours) { hour ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.clickable { selectedHour = hour.hour }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${hour.hour}:00",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${TimeUnit.MILLISECONDS.toMinutes(hour.usageTimeMillis)}m",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }

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
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap on any hour for detailed insights",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable 
private fun CategoryInsightsChart(
    categoryInsights: List<WeeklyInsightsUseCase.CategoryInsight>
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val totalTime = categoryInsights.sumOf { it.totalTimeMillis }
    
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
            // Summary stats card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Total Categories",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${categoryInsights.size}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Total Time",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${TimeUnit.MILLISECONDS.toHours(totalTime)}h ${TimeUnit.MILLISECONDS.toMinutes(totalTime) % 60}m",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Top Category",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = categoryInsights.firstOrNull()?.categoryName ?: "N/A",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
            
            // Enhanced Pie chart with click interaction
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(180.dp)
                        .clickable { selectedCategory = null }
                ) {
                    drawEnhancedCategoryPieChart(categoryInsights, selectedCategory, size)
                }
                
                // Center text showing selected category or total
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (selectedCategory != null) {
                        val category = categoryInsights.find { it.categoryName == selectedCategory }
                        category?.let {
                            Text(
                                text = it.categoryName,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${it.percentageOfTotal.toInt()}%",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = getCategoryColor(it.categoryName)
                            )
                        }
                    } else {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${TimeUnit.MILLISECONDS.toHours(totalTime)}h",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Enhanced category list with click interaction and better visual hierarchy
            categoryInsights.forEachIndexed { index, category ->
                val isSelected = selectedCategory == category.categoryName
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            selectedCategory = if (isSelected) null else category.categoryName
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            getCategoryColor(category.categoryName).copy(alpha = 0.1f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isSelected) 4.dp else 1.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rank indicator
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    getCategoryColor(category.categoryName).copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = getCategoryColor(category.categoryName)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = category.categoryName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${TimeUnit.MILLISECONDS.toHours(category.totalTimeMillis)}h ${TimeUnit.MILLISECONDS.toMinutes(category.totalTimeMillis) % 60}m",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                // Progress indicator
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .background(
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                            RoundedCornerShape(2.dp)
                                        )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(category.percentageOfTotal / 100f)
                                            .background(
                                                getCategoryColor(category.categoryName),
                                                RoundedCornerShape(2.dp)
                                            )
                                    )
                                }
                            }
                        }
                        
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "${category.percentageOfTotal.toInt()}%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = getCategoryColor(category.categoryName)
                            )
                            
                            if (totalTime > 0) {
                                val avgDailyTime = category.totalTimeMillis / 7
                                Text(
                                    text = "${TimeUnit.MILLISECONDS.toMinutes(avgDailyTime)}m/day",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Tap hint
            Text(
                text = "Tap on categories to highlight them in the chart",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📱",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No category data available",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Start using apps to see category insights",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


private fun DrawScope.drawEnhancedCategoryPieChart(
    categories: List<WeeklyInsightsUseCase.CategoryInsight>,
    selectedCategory: String?,
    canvasSize: Size
) {
    val total = categories.sumOf { it.totalTimeMillis }.toFloat()
    if (total <= 0) return

    val baseRadius = (canvasSize.width.coerceAtMost(canvasSize.height)) / 2f * 0.35f
    val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
    
    var startAngle = -90f // Start from top
    
    categories.forEach { category ->
        val sweepAngle = (category.totalTimeMillis.toFloat() / total) * 360f
        val color = getCategoryColor(category.categoryName)
        val isSelected = selectedCategory == category.categoryName
        
        // Use different radius for selected category (make it pop out)
        val radius = if (isSelected) baseRadius * 1.1f else baseRadius
        val alpha = if (selectedCategory != null && !isSelected) 0.3f else 1.0f
        
        drawArc(
            color = color.copy(alpha = alpha),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset(
                center.x - radius,
                center.y - radius
            ),
            size = Size(radius * 2, radius * 2)
        )
        
        // Draw border for better separation
        drawArc(
            color = Color.White.copy(alpha = 0.8f),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset(
                center.x - radius,
                center.y - radius
            ),
            size = Size(radius * 2, radius * 2),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
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