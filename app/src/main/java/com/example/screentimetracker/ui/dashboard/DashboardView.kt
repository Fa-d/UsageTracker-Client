package com.example.screentimetracker.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.example.screentimetracker.utils.millisToReadableTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.min

@Composable
fun DashboardView(state: DashboardState) {
    val viewModel = LocalDashboardViewModel.current
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // Today's Overview
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF6366F1))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Today's Screen Time",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    millisToReadableTime(state.totalScreenTimeTodayMillis),
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "${state.totalScreenUnlocksToday} unlocks",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                    Text(
                        "${state.appUsagesToday.sumOf { it.openCount }} app opens",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text("Daily Goal Progress", color = Color.White, fontSize = 13.sp)
                Spacer(Modifier.height(2.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            Color.White.copy(alpha = 0.2f), shape = MaterialTheme.shapes.extraLarge
                        )
                ) {
                    Box(
                        Modifier
                            .fillMaxHeight()
                            .fillMaxWidth((state.totalScreenTimeTodayMillis / (6 * 60 * 60 * 1000f)).coerceAtMost(1f)) // Example: 6h goal
                            .background(Color.White, shape = MaterialTheme.shapes.extraLarge)
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    val progress = ((state.totalScreenTimeTodayMillis / (6 * 60 * 60 * 1000f)) * 100).toInt().coerceAtMost(100)
                    Text("${progress}%", color = Color.White, fontSize = 13.sp)
                }
            }
        }
        // Quick Stats Grid
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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
                    Text("${state.totalScreenUnlocksToday}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
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
                    Text("${state.appUsagesToday.sumOf { it.openCount }}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("App Opens", fontSize = 13.sp, color = Color.Gray)
                }
            }
        }
        // Weekly Trend Chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Weekly Trend", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                WeeklyTrendChart(state.historicalAppSummaries)
            }
        }
        // Category Breakdown (pie chart placeholder)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Category Breakdown", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                // Use real data for categories
                val categories = getCategoryDataFromAppUsages(state.appUsagesToday)
                PieChartCategoryBreakdown(categories = categories)
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { category ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(10.dp)
                                        .background(
                                            category.color, shape = MaterialTheme.shapes.small
                                        )
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(category.name, fontSize = 13.sp)
                            }
                            Text(category.time, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
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
            .height(120.dp)
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
        modifier = modifier
            .aspectRatio(1f)
            .fillMaxWidth(), contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var startAngle = -90f
            val diameter = min(size.width, size.height)
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
                startAngle += sweep
            }
        }
    }
}
