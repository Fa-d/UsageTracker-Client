package com.example.screentimetracker.ui.dashboard.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.screentimetracker.ui.dashboard.utils.LocalDashboardViewModel
import com.example.screentimetracker.utils.millisToReadableTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TimelineScreen() {
    val viewModel = LocalDashboardViewModel.current
    var selectedDate by remember { mutableStateOf("Today") }
    val timelineEvents by viewModel.timelineEvents.collectAsStateWithLifecycle()

    LaunchedEffect(selectedDate) {
        val calendar = Calendar.getInstance()
        val currentDayEndMillis = calendar.timeInMillis

        val startTimeMillis: Long
        val endTimeMillis: Long

        when (selectedDate) {
            "Today" -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startTimeMillis = calendar.timeInMillis
                endTimeMillis = currentDayEndMillis
            }
            "Yesterday" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startTimeMillis = calendar.timeInMillis

                val yesterdayCalendar = Calendar.getInstance()
                yesterdayCalendar.add(Calendar.DAY_OF_YEAR, -1)
                yesterdayCalendar.set(Calendar.HOUR_OF_DAY, 23)
                yesterdayCalendar.set(Calendar.MINUTE, 59)
                yesterdayCalendar.set(Calendar.SECOND, 59)
                yesterdayCalendar.set(Calendar.MILLISECOND, 999)
                endTimeMillis = yesterdayCalendar.timeInMillis
            }
            "Week" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -6) // Last 7 days including today
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startTimeMillis = calendar.timeInMillis
                endTimeMillis = currentDayEndMillis
            }
            else -> {
                startTimeMillis = 0L // All time, or handle specific date selection
                endTimeMillis = currentDayEndMillis
            }
        }
        viewModel.loadTimelineEvents(startTimeMillis, endTimeMillis)
    }

    val timelineUiItems = timelineEvents.map { event ->
        val appName = viewModel.getAppName(event.packageName)
        val category = "App Usage"
        val chipBg = Color(0xFFEBF8FF)
        val chipTextColor = Color(0xFF0F172A)

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val startTime = timeFormat.format(Date(event.startTimeMillis))
        val endTime = timeFormat.format(Date(event.endTimeMillis))
        val duration = millisToReadableTime(event.durationMillis)

        TimelineUiItem(
            category = category,
            app = appName,
            packageName = event.packageName,
            chipBg = chipBg,
            chipTextColor = chipTextColor,
            time = "$startTime - $endTime",
            duration = duration
        )
    }


    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Timeline",
                Modifier.weight(1f),
                color = Color(0xFF1E293B),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                maxLines = 1
            )
            Spacer(Modifier.width(40.dp))
        }
        Row(
            Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(Color(0xFFF0F4F8), shape = MaterialTheme.shapes.medium)
                .height(40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("Today", "Yesterday", "Week").forEach { label ->
                val selected = selectedDate == label
                TextButton(
                    onClick = { selectedDate = label },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (selected) Color.White else Color(0xFFF0F4F8),
                        contentColor = if (selected) Color(0xFF3B82F6) else Color(0xFF64748B)
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        label,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }
        }
        // Timeline List
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            itemsIndexed(timelineUiItems) { idx, item ->
                TimelineItem(
                    item = item, isFirst = idx == 0, isLast = idx == timelineUiItems.lastIndex
                )
            }
        }
    }


}

data class TimelineUiItem(
    val category: String,
    val app: String,
    val packageName: String,
    val chipBg: Color,
    val chipTextColor: Color,
    val time: String,
    val duration: String
)

@Composable
fun TimelineItem(item: TimelineUiItem, isFirst: Boolean, isLast: Boolean) {
    val context = LocalContext.current
    
    // Move the risky operation outside the composable calls
    val appIcon = remember(item.packageName) {
        try {
            context.packageManager.getApplicationIcon(item.packageName)
        } catch (e: Exception) {
            null
        }
    }

    Row(Modifier.fillMaxWidth()) {
        Column(
            Modifier.width(48.dp), 
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isFirst) Box(
                Modifier
                    .width(3.dp)
                    .height(16.dp)
                    .background(Color(0xFF3B82F6))
            )
            Box(
                Modifier
                    .size(48.dp)
                    .background(Color.White, shape = CircleShape)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                // Use conditional rendering instead of try-catch around composables
                if (appIcon != null) {
                    val bitmap = appIcon.toBitmap(96, 96)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "${item.app} icon",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        Modifier
                            .size(36.dp)
                            .background(Color(0xFFF1F5F9), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Outlined.Article,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            if (!isLast) Box(
                Modifier
                    .width(3.dp)
                    .height(80.dp)
                    .background(Color(0xFF3B82F6))
            )
        }
        
        Spacer(Modifier.width(12.dp))
        
        Card(
            Modifier
                .weight(1f)
                .padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            item.app,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            item.category,
                            fontSize = 14.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Box(
                        Modifier
                            .background(item.chipBg, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal =10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            item.duration,
                            fontSize = 12.sp,
                            color = item.chipTextColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    item.time, 
                    fontSize = 13.sp, 
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun FooterNavItem(icon: ImageVector, label: String, selected: Boolean) {
    val color = if (selected) Color(0xFF3B82F6) else Color(0xFF64748B)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        Text(
            label,
            fontSize = 12.sp,
            color = color,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}