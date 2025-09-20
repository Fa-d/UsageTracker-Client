package dev.sadakat.screentimetracker.core.presentation.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sadakat.screentimetracker.core.presentation.ui.components.PlayfulCard
import dev.sadakat.screentimetracker.core.presentation.ui.dashboard.cards.WeeklyInsightsCard
import dev.sadakat.screentimetracker.core.presentation.ui.dashboard.state.CategoryData
import dev.sadakat.screentimetracker.core.presentation.ui.dashboard.state.DashboardState
import dev.sadakat.screentimetracker.core.presentation.ui.dashboard.utils.LocalDashboardViewModel
import dev.sadakat.screentimetracker.core.presentation.ui.dashboard.utils.getCategoryDataFromAppUsages
import dev.sadakat.screentimetracker.core.presentation.ui.theme.LimeGreen
import dev.sadakat.screentimetracker.core.presentation.ui.theme.PlayfulPrimary
import dev.sadakat.screentimetracker.core.presentation.ui.theme.VibrantOrange
import dev.sadakat.screentimetracker.core.data.local.entities.DailyAppSummary
import dev.sadakat.screentimetracker.utils.millisToReadableTime
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.collections.forEachIndexed
import kotlin.math.min

@Composable
fun AnalyticsScreen(
    state: DashboardState,
    expandedCategory: Int?,
    onCategoryExpand: (Int?) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PlayfulPrimary.copy(alpha = 0.05f),
                        VibrantOrange.copy(alpha = 0.02f),
                        LimeGreen.copy(alpha = 0.03f)
                    )
                )
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp)
    ) {
        item {
            PlayfulCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                gradientBackground = true
            ) {
                Column {
                    Text(
                        text = "📊 Detailed Analytics",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Deep insights into your digital habits",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Weekly Insights Card
        item {
            WeeklyInsightsCard(
                weeklyInsights = LocalDashboardViewModel.current.weeklyInsightsUseCase,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Weekly Trend Chart
        item {
            PlayfulCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                gradientBackground = true
            ) {
                Column {
                    Text(
                        "📈 Weekly Trend Analysis",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.height(16.dp))
                    WeeklyTrendChart(state.historicalAppSummaries)
                }
            }
        }
        
        // Category Breakdown
        item {
            CategoryBreakdownCard(
                expandedCategory = expandedCategory,
                onCategoryExpand = onCategoryExpand,
                state = state
            )
        }
        
        // Time-based Analysis
        item {
            PlayfulCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "⏰ Time-based Analysis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Coming soon: Hourly usage patterns, peak activity times, and productivity insights",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // App Comparison
        item {
            PlayfulCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "📱 App Comparison",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Coming soon: Compare usage across different apps and time periods",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyTrendChart(historicalAppSummaries: List<DailyAppSummary>) {
    val dailyData = historicalAppSummaries
        .groupBy { it.dateMillis }
        .mapValues { entry -> entry.value.sumOf { it.totalDurationMillis } }
        .toSortedMap()

    val maxScreenTime = dailyData.values.maxOrNull() ?: 1L
    val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.medium
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Bottom
    ) {
        if (dailyData.isEmpty()) {
            Text("No historical data available", color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.align(Alignment.CenterVertically))
        } else {
            dailyData.forEach { (dateMillis, totalDuration) ->
                val dayOfWeek = dateFormat.format(dateMillis)
                val barHeight = ((totalDuration.toFloat() / maxScreenTime) * 100).dp

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        millisToReadableTime(totalDuration),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(barHeight)
                            .background(
                                MaterialTheme.colorScheme.secondary,
                                shape = MaterialTheme.shapes.small
                            )
                    )
                    Text(
                        dayOfWeek,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryBreakdownCard(
    expandedCategory: Int?, 
    onCategoryExpand: (Int?) -> Unit, 
    state: DashboardState
) {
    val appUsageData = state.appUsagesToday
    val categories = getCategoryDataFromAppUsages(state.appUsagesToday)
    
    PlayfulCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "📊 Category Breakdown", 
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(Modifier.height(8.dp))
            
            PieChartCategoryBreakdown(
                categories = categories, 
                modifier = Modifier.size(350.dp)
            )
            
            Spacer(Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                categories.forEachIndexed { index, category ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .clickable(
                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                        indication = null
                                    ) { onCategoryExpand(if (expandedCategory == index) null else index) },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .size(14.dp)
                                            .background(
                                                category.color, shape = MaterialTheme.shapes.small
                                            )
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(category.name, fontWeight = FontWeight.Medium)
                                        Text(
                                            "${category.openCount} opens",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(category.time, fontSize = 13.sp, color = Color.Gray)
                                }
                                Icon(
                                    imageVector = if (expandedCategory == index) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowRight,
                                    contentDescription = null
                                )
                            }
                            if (expandedCategory == index) {
                                Column(
                                    Modifier.padding(start = 24.dp, end = 16.dp, bottom = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val categoryApps = appUsageData.filter { it.packageName == category.packageName }
                                    if (categoryApps.isNotEmpty()) {
                                        val totalDuration = categoryApps.sumOf { it.totalDurationMillisToday }
                                        val totalOpens = categoryApps.sumOf { it.openCount }
                                        
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = category.color.copy(alpha = 0.1f)
                                            )
                                        ) {
                                            Row(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("📱", fontSize = 20.sp)
                                                    Spacer(Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            category.name,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp
                                                        )
                                                        Text(
                                                            "${totalOpens} opens total",
                                                            fontSize = 12.sp,
                                                            color = Color.Gray
                                                        )
                                                    }
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(
                                                        millisToReadableTime(totalDuration),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = category.color
                                                    )
                                                    Text(
                                                        "Total time",
                                                        fontSize = 11.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Text(
                                            "No data available",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PieChartCategoryBreakdown(categories: List<CategoryData>, modifier: Modifier = Modifier) {
    val total = categories.sumOf { it.value }
    val sweepAngles = categories.map { 360f * (it.value / total.toFloat()) }
    val colors = categories.map { it.color }
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var startAngle = -90f
            val diameter = min(size.width, size.height) * 0.85f
            val radius = diameter / 2f
            val center = Offset(size.width / 2, size.height / 2)
            val strokeWidth = 3.dp.toPx()
            val segmentGap = 4f
            
            sweepAngles.forEachIndexed { i, sweep ->
                if (sweep > 1f) {
                    val baseColor = colors[i]
                    val adjustedSweep = sweep - segmentGap
                    
                    val gradient = Brush.sweepGradient(
                        colorStops = arrayOf(
                            0f to baseColor.copy(alpha = 0.7f),
                            0.5f to baseColor,
                            1f to baseColor.copy(alpha = 0.9f)
                        ),
                        center = center
                    )
                    
                    drawArc(
                        brush = gradient,
                        startAngle = startAngle + segmentGap/2,
                        sweepAngle = adjustedSweep,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(diameter, diameter)
                    )
                    
                    drawArc(
                        color = Color.White.copy(alpha = 0.9f),
                        startAngle = startAngle + segmentGap/2,
                        sweepAngle = adjustedSweep,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(diameter, diameter),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                    )
                    
                    if (sweep > 15f) {
                        val angleInRadians = Math.toRadians((startAngle + sweep / 2).toDouble()).toFloat()
                        val textRadius = radius * 0.75f
                        val textX = center.x + textRadius * kotlin.math.cos(angleInRadians)
                        val textY = center.y + textRadius * kotlin.math.sin(angleInRadians)
                        
                        val percentage = (categories[i].value.toFloat() / total * 100).toInt()
                        if (percentage > 5) {
                            drawContext.canvas.nativeCanvas.drawText(
                                "$percentage%", 
                                textX, 
                                textY, 
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.WHITE
                                    textSize = 28f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                                    isAntiAlias = true
                                    setShadowLayer(3f, 1f, 1f, android.graphics.Color.parseColor("#40000000"))
                                }
                            )
                        }
                    }
                }
                startAngle += sweep
            }
            
            val centerCircleRadius = radius * 0.35f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFF8F9FA)
                    ),
                    center = center,
                    radius = centerCircleRadius
                ),
                radius = centerCircleRadius,
                center = center
            )
            
            drawCircle(
                color = Color(0xFFE9ECEF),
                radius = centerCircleRadius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = millisToReadableTime(total),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}