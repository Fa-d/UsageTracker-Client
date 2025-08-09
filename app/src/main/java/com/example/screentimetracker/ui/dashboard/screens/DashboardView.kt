package com.example.screentimetracker.ui.dashboard.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.example.screentimetracker.ui.components.PlayfulCard
import com.example.screentimetracker.ui.components.PlayfulMetricCard
import com.example.screentimetracker.ui.dashboard.cards.OverviewCard
import com.example.screentimetracker.ui.dashboard.cards.AchievementsCard
import com.example.screentimetracker.ui.dashboard.cards.ChallengeCard
import com.example.screentimetracker.ui.dashboard.cards.FocusSessionCard
import com.example.screentimetracker.ui.dashboard.cards.WeeklyInsightsCard
import com.example.screentimetracker.ui.dashboard.cards.WellnessCard
import com.example.screentimetracker.ui.dashboard.cards.HabitCard
import com.example.screentimetracker.ui.dashboard.cards.TimeRestrictionCard
import com.example.screentimetracker.ui.dashboard.state.DashboardState
import com.example.screentimetracker.ui.dashboard.utils.getCategoryDataFromAppUsages
import com.example.screentimetracker.ui.dashboard.utils.LocalDashboardViewModel
import com.example.screentimetracker.ui.dashboard.state.CategoryData
import com.example.screentimetracker.ui.theme.*
import com.example.screentimetracker.utils.millisToReadableTime
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.min

@Composable
fun DashboardView(
    expandedCategory: Int?, 
    onCategoryExpand: (Int?) -> Unit, 
    state: DashboardState,
    achievements: StateFlow<List<com.example.screentimetracker.domain.model.Achievement>>,
    wellnessScore: StateFlow<com.example.screentimetracker.domain.model.WellnessScore?>,
    onNavigateToHabits: (() -> Unit)? = null,
    onNavigateToTimeRestrictions: (() -> Unit)? = null
) {
    Column(
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
            )
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        // Fun Header
        PlayfulCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            gradientBackground = true
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "🎯 Screen Time Dashboard",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Today's digital wellness journey ✨",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "📱",
                    fontSize = 40.sp,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(12.dp)
                )
            }
        }

        // Overview Cards with emojis and colors
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PlayfulMetricCard(
                modifier = Modifier.weight(1f),
                title = "Screen Time",
                value = millisToReadableTime(state.totalScreenTimeTodayMillis),
                emoji = "⏰",
                color = MaterialTheme.colorScheme.primary,
                subtitle = "Total today"
            )
            PlayfulMetricCard(
                modifier = Modifier.weight(1f),
                title = "Unlocks",
                value = "${state.totalScreenUnlocksToday}",
                emoji = "🔓",
                color = MaterialTheme.colorScheme.secondary,
                subtitle = "Screen activations"
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PlayfulMetricCard(
                modifier = Modifier.weight(1f),
                title = "App Opens",
                value = "${state.appUsagesToday.sumOf { it.openCount }}",
                emoji = "📱",
                color = MaterialTheme.colorScheme.tertiary,
                subtitle = "App launches"
            )
            PlayfulMetricCard(
                modifier = Modifier.weight(1f),
                title = "Focus Score",
                value = calculateFocusScore(state),
                emoji = "🎯",
                color = MaterialTheme.colorScheme.secondary,
                subtitle = "Productivity level"
            )
        }

        QuickViewComponent(state)

        // Wellness Score Card
        WellnessCard(
            wellnessScore = wellnessScore.value,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Habits Card
        onNavigateToHabits?.let { navigateToHabits ->
            HabitCard(
                onNavigateToHabits = navigateToHabits,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Time Restrictions Card
        onNavigateToTimeRestrictions?.let { navigateToTimeRestrictions ->
            TimeRestrictionCard(
                activeRestrictions = emptyList(), // TODO: Get from ViewModel
                allRestrictions = emptyList(), // TODO: Get from ViewModel
                onToggleRestriction = { /* TODO: Handle toggle */ },
                onNavigateToSettings = navigateToTimeRestrictions,
                formatTime = { minutes ->
                    val hours = minutes / 60
                    val mins = minutes % 60
                    String.format("%02d:%02d", hours, mins)
                },
                formatTimeUntil = { minutes ->
                    val hours = minutes / 60
                    val mins = minutes % 60
                    when {
                        hours > 0 -> "${hours}h ${mins}m"
                        else -> "${mins}m"
                    }
                },
                getRestrictionStatusPreview = { restriction ->
                    // TODO: Implement proper status preview
                    com.example.screentimetracker.ui.timerestrictions.viewmodels.RestrictionStatusPreview(
                        restriction = restriction,
                        isCurrentlyActive = false,
                        nextChangeTimeMinutes = null,
                        timeUntilChange = null
                    )
                }
            )
        }

        // Achievements Card
        AchievementsCard(
            achievements = achievements.value,
            onAchievementClick = { achievement ->
                // Handle achievement click - could show detail dialog
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Challenge Card
        ChallengeCard(
            challengeManager = LocalDashboardViewModel.current.challengeManagerUseCase,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Focus Session Card
        FocusSessionCard(
            focusSessionManager = LocalDashboardViewModel.current.focusSessionManagerUseCase,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Weekly Insights Card
        WeeklyInsightsCard(
            weeklyInsights = LocalDashboardViewModel.current.weeklyInsightsUseCase,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Weekly Trend Chart with playful styling
        PlayfulCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
            gradientBackground = true
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "📈",
                    fontSize = 24.sp,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Weekly Trend Analysis",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(Modifier.height(16.dp))
            WeeklyTrendChart(state.historicalAppSummaries)
        }

        CategoryBreakDown(expandedCategory, onCategoryExpand, state)
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Helper function to calculate focus score
fun calculateFocusScore(state: DashboardState): String {
    val totalTime = state.totalScreenTimeTodayMillis
    val unlocks = state.totalScreenUnlocksToday
    val opens = state.appUsagesToday.sumOf { it.openCount }
    
    val score = when {
        totalTime < 3600000 && unlocks < 20 -> 95 // Less than 1 hour, few unlocks
        totalTime < 7200000 && unlocks < 50 -> 80 // Less than 2 hours, moderate unlocks
        totalTime < 14400000 && unlocks < 100 -> 65 // Less than 4 hours
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
                        .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
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
                        .background(MaterialTheme.colorScheme.secondary, shape = MaterialTheme.shapes.small)
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
            .background(MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.medium)
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
                            .background(MaterialTheme.colorScheme.secondary, shape = MaterialTheme.shapes.small)
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