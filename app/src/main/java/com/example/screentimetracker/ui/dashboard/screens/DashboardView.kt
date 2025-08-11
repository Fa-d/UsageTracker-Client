package com.example.screentimetracker.ui.dashboard.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.example.screentimetracker.ui.dashboard.state.CategoryData
import com.example.screentimetracker.ui.dashboard.state.DashboardState
import com.example.screentimetracker.ui.dashboard.utils.getCategoryDataFromAppUsages
import com.example.screentimetracker.utils.millisToReadableTime
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.min


// Helper function to calculate focus score
fun calculateFocusScore(state: DashboardState): String {
    val totalTime = state.totalScreenTimeTodayMillis
    val unlocks = state.totalScreenUnlocksToday
    val opens = state.appUsagesToday.sumOf { it.openCount }

    val score = when {
        totalTime < 3600000 && unlocks < 20 && opens < 30 -> 95 // Less than 1 hour, few unlocks and opens
        totalTime < 7200000 && unlocks < 50 && opens < 60 -> 80 // Less than 2 hours, moderate unlocks and opens
        totalTime < 14400000 && unlocks < 100 && opens < 120 -> 65 // Less than 4 hours, higher unlocks/opens
        else -> 45 // Heavy usage
    }
    
    return "$score%"
}

@Composable
private fun CategoryBreakDown(
    expandedCategory: Int?, onCategoryExpand: (Int?) -> Unit, state: DashboardState
) {
    val appUsageData = state.appUsagesToday

    val categories = getCategoryDataFromAppUsages(state.appUsagesToday)
    Column(
        modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Category Breakdown", 
            fontWeight = FontWeight.SemiBold, 
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        PieChartCategoryBreakdown(
            categories = categories, modifier = Modifier.size(350.dp)
        )
        Spacer(Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            categories.forEachIndexed { index, category ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
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
                                // Filter by packageName and show individual app entries
                                val categoryApps = appUsageData.filter { it.packageName == category.packageName }
                                if (categoryApps.isNotEmpty()) {
                                    // Show aggregated info for the category
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
                                    Spacer(Modifier.height(4.dp))
                                    
                                    // Show individual sessions if there are multiple
                                    if (categoryApps.size > 1) {
                                        Text(
                                            "Sessions:",
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                        categoryApps.forEach { app ->
                                            Row(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 16.dp, top = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        Modifier
                                                            .size(8.dp)
                                                            .background(
                                                                category.color.copy(alpha = 0.6f),
                                                                shape = MaterialTheme.shapes.extraSmall
                                                            )
                                                    )
                                                    Spacer(Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            "Session ${categoryApps.indexOf(app) + 1}",
                                                            fontSize = 12.sp,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            "${app.openCount} opens",
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                                Text(
                                                    millisToReadableTime(app.totalDurationMillisToday),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
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

@Composable
private fun QuickViewComponent(state: DashboardState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon placeholder
                Box(
                    Modifier
                        .size(32.dp)
                        .background(
                            MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small
                        )
                ) {}
                Spacer(Modifier.height(8.dp))
                Text(
                    "${state.totalScreenUnlocksToday}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text("Phone Unlocks", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon placeholder
                Box(
                    Modifier
                        .size(32.dp)
                        .background(
                            MaterialTheme.colorScheme.secondary, shape = MaterialTheme.shapes.small
                        )
                ) {}
                Spacer(Modifier.height(8.dp))
                Text(
                    "${state.appUsagesToday.sumOf { it.openCount }}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text("App Opens", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun WeeklyTrendChart(historicalAppSummaries: List<com.example.screentimetracker.data.local.DailyAppSummary>) {
    val dailyData = historicalAppSummaries
        .groupBy { it.dateMillis }
        .mapValues { entry -> entry.value.sumOf { it.totalDurationMillis } }
        .toSortedMap() // Sort by date

    val maxScreenTime = dailyData.values.maxOrNull() ?: 1L

    val dateFormat = SimpleDateFormat("EEE", Locale.getDefault()) // Mon, Tue, etc.

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
                val barHeight = (totalDuration.toFloat() / maxScreenTime) * 100.dp

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        millisToReadableTime(totalDuration), // Display actual time
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
            val diameter = min(size.width, size.height) * 0.9f // Leave some padding
            val radius = diameter / 2f
            val center = Offset(size.width / 2, size.height / 2)
            val strokeWidth = 8.dp.toPx()
            
            // Draw shadow first
            drawIntoCanvas { canvas ->
                sweepAngles.forEachIndexed { i, sweep ->
                    val shadowOffset = 4.dp.toPx()
                    val shadowPaint = Paint().apply {
                        color = Color.Black.copy(alpha = 0.1f)
                        isAntiAlias = true
                    }
                    
                    canvas.drawArc(
                        left = center.x - radius + shadowOffset,
                        top = center.y - radius + shadowOffset,
                        right = center.x + radius + shadowOffset,
                        bottom = center.y + radius + shadowOffset,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true,
                        paint = shadowPaint
                    )
                }
            }
            
            // Draw main pie chart with gradients and borders
            sweepAngles.forEachIndexed { i, sweep ->
                val baseColor = colors[i]
                val gradientBrush = Brush.radialGradient(
                    colors = listOf(
                        baseColor.copy(alpha = 0.8f),
                        baseColor,
                        baseColor.copy(alpha = 1.2f)
                    ),
                    center = center,
                    radius = radius
                )
                
                // Draw main arc with gradient
                drawArc(
                    brush = gradientBrush,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(diameter, diameter)
                )
                
                // Draw border/stroke
                drawArc(
                    color = Color.White,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(diameter, diameter),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                )
                
                // Enhanced text rendering with better styling
                if (sweep > 20f) { // Only show percentage if segment is large enough
                    val angleInRadians = Math.toRadians((startAngle + sweep / 2).toDouble()).toFloat()
                    val textRadius = radius * 0.7f
                    val textX = center.x + textRadius * kotlin.math.cos(angleInRadians)
                    val textY = center.y + textRadius * kotlin.math.sin(angleInRadians)
                    
                    val percentage = (categories[i].value.toFloat() / total * 100).toInt()
                    
                    // Draw text shadow
                    drawContext.canvas.nativeCanvas.drawText(
                        "$percentage%", 
                        textX + 2f, 
                        textY + 2f, 
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            alpha = 50
                            textSize = 32f
                            textAlign = android.graphics.Paint.Align.CENTER
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                            isAntiAlias = true
                        }
                    )
                    
                    // Draw main text
                    drawContext.canvas.nativeCanvas.drawText(
                        "$percentage%", 
                        textX, 
                        textY, 
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 32f
                            textAlign = android.graphics.Paint.Align.CENTER
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                            isAntiAlias = true
                            setShadowLayer(4f, 2f, 2f, android.graphics.Color.BLACK)
                        }
                    )
                }
                
                startAngle += sweep
            }
            
            // Draw center circle for a donut effect
            val centerCircleRadius = radius * 0.3f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.9f),
                        Color.Gray.copy(alpha = 0.1f)
                    ),
                    center = center,
                    radius = centerCircleRadius
                ),
                radius = centerCircleRadius,
                center = center
            )
            
            // Draw center circle border
            drawCircle(
                color = Color.Gray.copy(alpha = 0.3f),
                radius = centerCircleRadius,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
            )
        }
    }
}