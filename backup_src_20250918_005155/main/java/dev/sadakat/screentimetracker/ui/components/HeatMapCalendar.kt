package dev.sadakat.screentimetracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

data class HeatMapData(
    val date: LocalDate,
    val value: Float, // 0.0 to 1.0
    val displayValue: String = "",
    val details: String = ""
)

@Composable
fun HeatMapCalendar(
    data: List<HeatMapData>,
    modifier: Modifier = Modifier,
    title: String = "Activity Heat Map",
    selectedMonth: YearMonth = YearMonth.now(),
    onDateSelected: (LocalDate) -> Unit = {},
    onMonthChanged: (YearMonth) -> Unit = {},
    baseColor: Color = MaterialTheme.colorScheme.primary,
    showLegend: Boolean = true,
    animateOnLoad: Boolean = true
) {
    var currentMonth by remember { mutableStateOf(selectedMonth) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var animationStarted by remember { mutableStateOf(false) }
    
    LaunchedEffect(selectedMonth) {
        currentMonth = selectedMonth
        if (animateOnLoad && !animationStarted) {
            animationStarted = true
        }
    }
    
    Column(modifier = modifier.fillMaxWidth()) {
        // Header
        CalendarHeader(
            title = title,
            currentMonth = currentMonth,
            onMonthChanged = { 
                currentMonth = it
                onMonthChanged(it)
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Calendar grid
        HeatMapGrid(
            data = data,
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            onDateSelected = { date ->
                selectedDate = if (selectedDate == date) null else date
                onDateSelected(date)
            },
            baseColor = baseColor,
            animateOnLoad = animateOnLoad && animationStarted
        )
        
        // Legend
        if (showLegend) {
            Spacer(modifier = Modifier.height(16.dp))
            HeatMapLegend(baseColor = baseColor)
        }
        
        // Selected date details
        selectedDate?.let { date ->
            val dateData = data.find { it.date == date }
            if (dateData != null) {
                Spacer(modifier = Modifier.height(16.dp))
                SelectedDateCard(
                    date = date,
                    data = dateData,
                    onDismiss = { selectedDate = null }
                )
            }
        }
    }
}

@Composable
private fun CalendarHeader(
    title: String,
    currentMonth: YearMonth,
    onMonthChanged: (YearMonth) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChanged(currentMonth.minusMonths(1)) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous month"
                )
            }
            
            Text(
                text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            
            IconButton(onClick = { onMonthChanged(currentMonth.plusMonths(1)) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next month"
                )
            }
        }
    }
}

@Composable
private fun HeatMapGrid(
    data: List<HeatMapData>,
    currentMonth: YearMonth,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    baseColor: Color,
    animateOnLoad: Boolean
) {
    val dataMap = remember(data) { data.associateBy { it.date } }
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfMonth = currentMonth.atDay(1)
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    
    Column {
        // Day headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Calendar days grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Empty cells for days before month start
            repeat(startDayOfWeek) {
                item {
                    Spacer(modifier = Modifier.aspectRatio(1f))
                }
            }
            
            // Days of the month
            items((1..daysInMonth).toList()) { day ->
                val date = currentMonth.atDay(day)
                val dayData = dataMap[date]
                
                HeatMapDayCell(
                    day = day,
                    data = dayData,
                    isSelected = selectedDate == date,
                    onClick = { onDateSelected(date) },
                    baseColor = baseColor,
                    animateOnLoad = animateOnLoad,
                    animationDelay = day * 20 // Stagger animation
                )
            }
        }
    }
}

@Composable
private fun HeatMapDayCell(
    day: Int,
    data: HeatMapData?,
    isSelected: Boolean,
    onClick: () -> Unit,
    baseColor: Color,
    animateOnLoad: Boolean,
    animationDelay: Int
) {
    val intensity = data?.value ?: 0f
    val cellColor = when {
        intensity == 0f -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        intensity < 0.2f -> baseColor.copy(alpha = 0.2f)
        intensity < 0.4f -> baseColor.copy(alpha = 0.4f)
        intensity < 0.6f -> baseColor.copy(alpha = 0.6f)
        intensity < 0.8f -> baseColor.copy(alpha = 0.8f)
        else -> baseColor
    }
    
    val animatedScale by animateFloatAsState(
        targetValue = if (animateOnLoad) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = animationDelay,
            easing = EaseOutBack
        ),
        label = "scale_animation"
    )
    
    val selectedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "selection_animation"
    )
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (isSelected) {
                    Brush.radialGradient(
                        colors = listOf(
                            cellColor,
                            cellColor.copy(alpha = 0.7f)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(cellColor, cellColor)
                    )
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = (12 * animatedScale * selectedScale).sp
            ),
            color = if (intensity > 0.5f) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HeatMapLegend(
    baseColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Less",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        repeat(5) { index ->
            val alpha = (index + 1) * 0.2f
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(baseColor.copy(alpha = alpha))
            )
        }
        
        Text(
            text = "More",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectedDateCard(
    date: LocalDate,
    data: HeatMapData,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd")),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                
                if (data.displayValue.isNotEmpty()) {
                    Text(
                        text = data.displayValue,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                if (data.details.isNotEmpty()) {
                    Text(
                        text = data.details,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close"
                )
            }
        }
    }
}