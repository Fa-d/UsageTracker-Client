package com.example.screentimetracker.ui.dashboard

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.example.screentimetracker.ui.dashboard.components.OverviewCard
import com.example.screentimetracker.utils.millisToReadableTime
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.min

@Composable
fun DashboardView(expandedCategory: Int?, onCategoryExpand: (Int?) -> Unit, state: DashboardState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Make the entire screen scrollable
    ) {
        OverviewCard(
            totalScreenTimeTodayMillis = state.totalScreenTimeTodayMillis,
            totalScreenUnlocksToday = state.totalScreenUnlocksToday,
            totalAppOpensToday = state.appUsagesToday.sumOf { it.openCount })
        QuickViewComponent(state)

        // Weekly Trend Chart
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Weekly Trend", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                WeeklyTrendChart(state.historicalAppSummaries)
            }
        }

        CategoryBreakDown(expandedCategory, onCategoryExpand, state)
    }
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
            "Category Breakdown", fontWeight = FontWeight.SemiBold, fontSize = 16.sp
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
                                Text(category.name, fontWeight = FontWeight.Medium)
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
                                appUsageData.filter { it.appName == category.name }.forEach { app ->
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("\uD83D\uDCF1", fontSize = 22.sp)
                                            Spacer(Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    app.appName, fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    "${app.openCount} opens",
                                                    fontSize = 12.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                millisToReadableTime(app.totalDurationMillisToday),
                                                fontWeight = FontWeight.Medium
                                            )
                                            Button(
                                                onClick = { /* TODO: Tag action */ },
                                                contentPadding = PaddingValues(horizontal = 10.dp),
                                                modifier = Modifier.height(24.dp)
                                            ) {
                                                Text(
                                                    "Limit Usage", fontSize = 12.sp
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
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon placeholder
                Box(
                    Modifier
                        .size(32.dp)
                        .background(Color(0xFF2563EB), shape = MaterialTheme.shapes.small)
                ) {}
                Spacer(Modifier.height(8.dp))
                Text(
                    "${state.totalScreenUnlocksToday}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text("Phone Unlocks", fontSize = 13.sp, color = Color.Gray)
            }
        }
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon placeholder
                Box(
                    Modifier
                        .size(32.dp)
                        .background(Color(0xFF22C55E), shape = MaterialTheme.shapes.small)
                ) {}
                Spacer(Modifier.height(8.dp))
                Text(
                    "${state.appUsagesToday.sumOf { it.openCount }}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text("App Opens", fontSize = 13.sp, color = Color.Gray)
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
            .background(Color(0xFFE0E7FF), shape = MaterialTheme.shapes.medium)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Bottom
    ) {
        if (dailyData.isEmpty()) {
            Text("No historical data available", color = Color(0xFF6366F1), modifier = Modifier.align(Alignment.CenterVertically))
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
                        color = Color(0xFF6366F1)
                    )
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(barHeight)
                            .background(Color(0xFF6366F1), shape = MaterialTheme.shapes.small)
                    )
                    Text(
                        dayOfWeek,
                        fontSize = 12.sp,
                        color = Color(0xFF6366F1)
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
        modifier = modifier.fillMaxSize(), // Fill available space
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var startAngle = -90f
            val diameter =
                min(size.width, size.height) // Ensures it's a circle within the available space
            val radius = diameter / 2f
            val center = Offset(size.width / 2, size.height / 2)
            sweepAngles.forEachIndexed { i, sweep ->
                drawArc(
                    color = colors[i],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(diameter, diameter)
                )

                // Calculate text position for percentage
                val angleInRadians = Math.toRadians((startAngle + sweep / 2).toDouble()).toFloat()
                val textRadius = radius * 0.7f // Position text slightly inside the arc
                val textX = center.x + textRadius * kotlin.math.cos(angleInRadians)
                val textY = center.y + textRadius * kotlin.math.sin(angleInRadians)

                val percentage = (categories[i].value.toFloat() / total * 100).toInt()
                drawContext.canvas.nativeCanvas.drawText(
                    "$percentage%", textX, textY, android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        textSize = 30f
                        textAlign = android.graphics.Paint.Align.CENTER
                    })

                startAngle += sweep
            }
        }
    }
}