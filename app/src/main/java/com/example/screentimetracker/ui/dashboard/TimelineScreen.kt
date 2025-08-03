package com.example.screentimetracker.ui.dashboard

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        // Placeholder for category, icon, and colors. You'll need to implement logic
        // to determine these based on the app's package name or other criteria.
        val appName = viewModel.getAppName(event.packageName) // Assuming getAppName is public in ViewModel
        val category = "Uncategorized" // Placeholder
        val icon = Icons.AutoMirrored.Outlined.Article // Placeholder icon
        val iconBg = Color(0xFFF1F5F9) // Placeholder color
        val iconColor = Color(0xFF64748B) // Placeholder color
        val chipBg = Color(0xFFF1F5F9) // Placeholder color
        val chipTextColor = Color(0xFF334155) // Placeholder color

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val startTime = timeFormat.format(Date(event.startTimeMillis))
        val endTime = timeFormat.format(Date(event.endTimeMillis))
        val duration = millisToReadableTime(event.durationMillis)

        TimelineUiItem(
            category = category,
            app = appName,
            icon = icon,
            iconBg = iconBg,
            iconColor = iconColor,
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
    val icon: ImageVector,
    val iconBg: Color,
    val iconColor: Color,
    val chipBg: Color,
    val chipTextColor: Color,
    val time: String,
    val duration: String
)

@Composable
fun TimelineItem(item: TimelineUiItem, isFirst: Boolean, isLast: Boolean) {
    Row(Modifier.fillMaxWidth()) {
        Column(
            Modifier.width(40.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isFirst) Box(
                Modifier
                    .width(2.dp)
                    .height(12.dp)
                    .background(Color(0xFFCBD5E1))
            )
            Box(
                Modifier
                    .size(40.dp)
                    .background(item.iconBg, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    item.icon,
                    contentDescription = null,
                    tint = item.iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            if (!isLast) Box(
                Modifier
                    .width(2.dp)
                    .weight(1f)
                    .background(Color(0xFFCBD5E1))
            )
        }
        Card(
            Modifier
                .weight(1f)
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.category,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFF1E293B)
                    )
                    Box(
                        Modifier
                            .background(item.chipBg, shape = RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            item.app,
                            fontSize = 12.sp,
                            color = item.chipTextColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Text(item.time, fontSize = 13.sp, color = Color(0xFF64748B))
                Text(
                    "Duration: ${item.duration}",
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