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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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

@Composable
fun TimelineScreen() {
    var selectedDate by remember { mutableStateOf("Today") }
    val timelineItems = listOf(
        TimelineUiItem(
            category = "Social Media",
            app = "Instagram",
            icon = Icons.Outlined.PhotoCamera,
            iconBg = Color(0xFFFFF1F2),
            iconColor = Color(0xFFFB7185),
            chipBg = Color(0xFFFFF1F2),
            chipTextColor = Color(0xFFBE123C),
            time = "9:00 AM - 9:30 AM",
            duration = "30 min"
        ), TimelineUiItem(
            category = "Productivity",
            app = "Notion",
            icon = Icons.Outlined.Edit,
            iconBg = Color(0xFFE0F2FE),
            iconColor = Color(0xFF0EA5E9),
            chipBg = Color(0xFFE0F2FE),
            chipTextColor = Color(0xFF0369A1),
            time = "9:30 AM - 10:00 AM",
            duration = "30 min"
        ), TimelineUiItem(
            category = "Entertainment",
            app = "YouTube",
            icon = Icons.Outlined.PlayCircleOutline,
            iconBg = Color(0xFFFEE2E2),
            iconColor = Color(0xFFEF4444),
            chipBg = Color(0xFFFEE2E2),
            chipTextColor = Color(0xFFB91C1C),
            time = "10:00 AM - 11:00 AM",
            duration = "1 hr"
        ), TimelineUiItem(
            category = "Communication",
            app = "WhatsApp",
            icon = Icons.Outlined.ChatBubbleOutline,
            iconBg = Color(0xFFD1FAE5),
            iconColor = Color(0xFF22C55E),
            chipBg = Color(0xFFD1FAE5),
            chipTextColor = Color(0xFF166534),
            time = "11:00 AM - 12:00 PM",
            duration = "1 hr"
        ), TimelineUiItem(
            category = "News",
            app = "News App",
            icon = Icons.Outlined.Article,
            iconBg = Color(0xFFF1F5F9),
            iconColor = Color(0xFF64748B),
            chipBg = Color(0xFFF1F5F9),
            chipTextColor = Color(0xFF334155),
            time = "12:00 PM - 12:30 PM",
            duration = "30 min"
        )
    )
    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8))
    ) {
        // Header
        Surface(shadowElevation = 2.dp, color = Color.White) {
            Column {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { /* TODO: Back */ }) {
                        Icon(
                            Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1E293B)
                        )
                    }
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
                        .height(40.dp), verticalAlignment = Alignment.CenterVertically
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
            }
        }
        // Timeline List
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            itemsIndexed(timelineItems) { idx, item ->
                TimelineItem(
                    item = item, isFirst = idx == 0, isLast = idx == timelineItems.lastIndex
                )
            }
        }
        // Footer Navigation
        Surface(
            shadowElevation = 4.dp, color = Color.White, modifier = Modifier.navigationBarsPadding()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FooterNavItem(Icons.Outlined.BarChart, "Summary", false)
                FooterNavItem(Icons.Outlined.Schedule, "Timeline", true)
                FooterNavItem(Icons.Outlined.Settings, "Settings", false)
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