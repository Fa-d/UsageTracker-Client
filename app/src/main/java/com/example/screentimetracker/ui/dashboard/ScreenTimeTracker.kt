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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

@Composable
fun ScreenTimeTracker() {
    var activeTab by remember { mutableStateOf("dashboard") }
    var darkMode by remember { mutableStateOf(false) }
    var focusMode by remember { mutableStateOf(false) }
    var privacyMode by remember { mutableStateOf(false) }
    var syncEnabled by remember { mutableStateOf(true) }
    var selectedDate by remember { mutableStateOf("today") }
    var expandedCategory by remember { mutableStateOf<Int?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = if (darkMode) Color(0xFF18181B) else Color(0xFFF9FAFB)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (darkMode) Color(0xFF27272A) else Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("9:41", fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(3) {
                        Box(
                            Modifier
                                .size(width = 16.dp, height = 8.dp)
                                .background(Color(0xFF22C55E), shape = MaterialTheme.shapes.small)
                        )
                    }
                    Box(
                        Modifier
                            .size(width = 16.dp, height = 8.dp)
                            .background(Color(0xFFD1D5DB), shape = MaterialTheme.shapes.small)
                    )
                }
                Text("100%", fontSize = 14.sp)
            }

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (darkMode) Color(0xFF27272A) else Color.White)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Screen Time", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                // Add focus mode indicator and eye icon here if needed
            }

            // Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                when (activeTab) {
                    "dashboard" -> DashboardView()
                    "apps" -> AppsView(expandedCategory) { expandedCategory = it }
                    // "timeline" -> TimelineView(selectedDate) { selectedDate = it }
                    "timeline" -> TimelineScreen()
                    "goals" -> GoalsView(focusMode) { focusMode = it }
                    "settings" -> SettingsView(
                        darkMode,
                        { darkMode = it },
                        privacyMode,
                        { privacyMode = it },
                        syncEnabled,
                        { syncEnabled = it })
                    // TODO: Add other views
                }
            }

            // Bottom Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (darkMode) Color(0xFF27272A) else Color.White)
                    .padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceAround
            ) {
                val tabs = listOf(
                    "dashboard" to "Dashboard",
                    "apps" to "Apps",
                    "timeline" to "Timeline",
                    "goals" to "Goals",
                    "settings" to "Settings"
                )
                tabs.forEach { (id, label) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .background(
                                if (activeTab == id) Color(0xFFE0E7FF) else Color.Transparent,
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(8.dp)
                            .clickable { activeTab = id }) {
                        // TODO: Add icons
                        Text(
                            label,
                            fontSize = 12.sp,
                            color = if (activeTab == id) Color(0xFF2563EB) else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardView() {
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
                    todayStats.screenTime,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "${todayStats.unlocks} unlocks",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                    Text(
                        "${todayStats.appOpens} app opens",
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
                            .fillMaxWidth(todayStats.goalProgress / 100f)
                            .background(Color.White, shape = MaterialTheme.shapes.extraLarge)
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text("${todayStats.goalProgress}%", color = Color.White, fontSize = 13.sp)
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
                    Text("${todayStats.unlocks}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
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
                    Text("${todayStats.appOpens}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("App Opens", fontSize = 13.sp, color = Color.Gray)
                }
            }
        }
        // Weekly Trend Chart (placeholder)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Weekly Trend", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                // TODO: Add BarChart composable here
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color(0xFFE0E7FF), shape = MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Text("[Bar Chart]", color = Color(0xFF6366F1))
                }
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
                // Pie chart for category breakdown
                PieChartCategoryBreakdown(categories = categoryData)
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    categoryData.forEach { category ->
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

@Composable
fun AppsView(
    expandedCategory: Int?, onCategoryExpand: (Int?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("App Usage", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Button(onClick = { /* TODO: Filter action */ }, contentPadding = PaddingValues(8.dp)) {
                Text("Filter", color = Color(0xFF2563EB), fontSize = 14.sp)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            categoryData.forEachIndexed { index, category ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clickable { onCategoryExpand(if (expandedCategory == index) null else index) },
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
                                imageVector = if (expandedCategory == index) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                                contentDescription = null
                            )
                        }
                        if (expandedCategory == index) {
                            Column(
                                Modifier.padding(start = 24.dp, end = 16.dp, bottom = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                appUsageData.filter { it.category == category.name }
                                    .forEach { app ->
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(app.icon, fontSize = 22.sp)
                                                Spacer(Modifier.width(8.dp))
                                                Column {
                                                    Text(app.app, fontWeight = FontWeight.Medium)
                                                    Text(
                                                        "${app.opens} opens",
                                                        fontSize = 12.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(app.time, fontWeight = FontWeight.Medium)
                                                Button(
                                                    onClick = { /* TODO: Tag action */ },
                                                    contentPadding = PaddingValues(0.dp),
                                                    modifier = Modifier.height(24.dp)
                                                ) {
                                                    Text(
                                                        "Tag",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF2563EB)
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
fun TimelineView(selectedDate: String, onDateChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Usage Timeline", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            DropdownMenuBox(selectedDate, onDateChange)
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                timelineData.forEach { entry ->
                    Row(
                        Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            entry.time,
                            fontSize = 13.sp,
                            color = Color.Gray,
                            modifier = Modifier.width(48.dp)
                        )
                        Box(
                            Modifier
                                .size(8.dp)
                                .background(Color(0xFF2563EB), shape = MaterialTheme.shapes.small)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(entry.app, fontWeight = FontWeight.Medium)
                                Text(entry.duration, fontSize = 13.sp, color = Color.Gray)
                            }
                            Text(entry.category, fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownMenuBox(selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = true }, contentPadding = PaddingValues(8.dp)) {
            Text(
                when (selected) {
                    "today" -> "Today"
                    "yesterday" -> "Yesterday"
                    "week" -> "This Week"
                    else -> selected
                }, fontSize = 14.sp
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Today") },
                onClick = { onSelected("today"); expanded = false })
            DropdownMenuItem(
                text = { Text("Yesterday") },
                onClick = { onSelected("yesterday"); expanded = false })
            DropdownMenuItem(
                text = { Text("This Week") },
                onClick = { onSelected("week"); expanded = false })
        }
    }
}

@Composable
fun GoalsView(
    focusMode: Boolean, onFocusModeChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Goals & Limits", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        // Focus Mode
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(32.dp)
                                .background(
                                    if (focusMode) Color(0xFFDCFCE7) else Color(0xFFF3F4F6),
                                    shape = MaterialTheme.shapes.small
                                )
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Focus Mode", fontWeight = FontWeight.Medium)
                            Text("Block distracting apps", fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                    Switch(
                        checked = focusMode,
                        onCheckedChange = onFocusModeChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF22C55E),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFD1D5DB)
                        )
                    )
                }
                if (focusMode) {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFDCFCE7), shape = MaterialTheme.shapes.small)
                            .padding(8.dp)
                    ) {
                        Text(
                            "Focus mode is active. 3 apps are blocked.",
                            fontSize = 13.sp,
                            color = Color(0xFF15803D)
                        )
                    }
                }
            }
        }
        // Daily Goals
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Daily Goals", fontWeight = FontWeight.Medium)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Screen Time Limit
                    Column {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Screen Time Limit", fontSize = 13.sp)
                            Text("4h 32m / 6h 00m", fontSize = 13.sp)
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(
                                    Color(0xFFE5E7EB), shape = MaterialTheme.shapes.extraLarge
                                )
                        ) {
                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(0.75f)
                                    .background(
                                        Color(0xFF2563EB), shape = MaterialTheme.shapes.extraLarge
                                    )
                            )
                        }
                    }
                    // Max Unlocks
                    Column {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Max Unlocks", fontSize = 13.sp)
                            Text("87 / 100", fontSize = 13.sp)
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .background(
                                    Color(0xFFE5E7EB), shape = MaterialTheme.shapes.extraLarge
                                )
                        ) {
                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(0.87f)
                                    .background(
                                        Color(0xFF22C55E), shape = MaterialTheme.shapes.extraLarge
                                    )
                            )
                        }
                    }
                }
            }
        }
        // App Limits
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("App Limits", fontWeight = FontWeight.Medium)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    appUsageData.take(3).forEach { app ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(app.icon, fontSize = 20.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(app.app, fontWeight = FontWeight.Medium)
                            }
                            Text("${app.time} / 1h", fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsView(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    privacyMode: Boolean,
    onPrivacyModeChange: (Boolean) -> Unit,
    syncEnabled: Boolean,
    onSyncEnabledChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Settings", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        // Privacy & Data
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Privacy & Data", fontWeight = FontWeight.Medium)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Privacy Mode", fontWeight = FontWeight.Medium)
                        Text("Hide sensitive app data", fontSize = 13.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = privacyMode,
                        onCheckedChange = onPrivacyModeChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF2563EB),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFD1D5DB)
                        )
                    )
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Auto Sync", fontWeight = FontWeight.Medium)
                        Text("Sync with Room Database", fontSize = 13.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = syncEnabled,
                        onCheckedChange = onSyncEnabledChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF22C55E),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFD1D5DB)
                        )
                    )
                }
            }
        }
        // Appearance
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Appearance", fontWeight = FontWeight.Medium)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Dark Mode", fontWeight = FontWeight.Medium)
                        Text("Use dark theme", fontSize = 13.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = darkMode,
                        onCheckedChange = onDarkModeChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF8B5CF6),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFD1D5DB)
                        )
                    )
                }
            }
        }
        // Notifications (static, no state)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Notifications", fontWeight = FontWeight.Medium)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Usage Alerts", fontWeight = FontWeight.Medium)
                        Text("Get notified about limits", fontSize = 13.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFF59E42),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFD1D5DB)
                        )
                    )
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Goal Reminders", fontWeight = FontWeight.Medium)
                        Text("Daily goal notifications", fontSize = 13.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFEF4444),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFD1D5DB)
                        )
                    )
                }
            }
        }
    }
}
