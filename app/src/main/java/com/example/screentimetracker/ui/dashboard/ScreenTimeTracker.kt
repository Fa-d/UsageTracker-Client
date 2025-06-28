package com.example.screentimetracker.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.screentimetracker.ui.appsearch.AppSearchScreen
import com.example.screentimetracker.ui.limiter.LimiterConfigScreen

val LocalDashboardViewModel = staticCompositionLocalOf<DashboardViewModel> { error("No DashboardViewModel provided") }

@Composable
fun ScreenTimeTracker(viewModel: DashboardViewModel, padding: WindowInsets) {
    CompositionLocalProvider(LocalDashboardViewModel provides viewModel) {
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        LaunchedEffect(Unit) { viewModel.loadData() }
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        var darkMode by remember { mutableStateOf(false) }
        var focusMode by remember { mutableStateOf(false) }
        var privacyMode by remember { mutableStateOf(false) }
        var syncEnabled by remember { mutableStateOf(true) }
        var selectedDate by remember { mutableStateOf("today") }
        var expandedCategory by remember { mutableStateOf<Int?>(null) }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding.asPaddingValues()),
            color = if (darkMode) Color(0xFF18181B) else Color(0xFFF9FAFB)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
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
                    NavHost(navController = navController, startDestination = "dashboard_route") {
                        composable("dashboard_route") {
                            DashboardView(state)
                        }
                        composable("apps_route") {
                            AppsView(expandedCategory, { expandedCategory = it }, navController)
                        }
                        composable("timeline_route") {
                            TimelineScreen()
                        }
                        composable("goals_route") {
                            GoalsView(focusMode) { focusMode = it }
                        }
                        composable("limiter_route") {
                            LimiterConfigScreen()
                        }
                        composable("settings_route") {
                            SettingsView(
                                darkMode,
                                { darkMode = it },
                                privacyMode,
                                { privacyMode = it },
                                syncEnabled,
                                { syncEnabled = it })
                        }
                        composable("app_search_route") {
                            AppSearchScreen(navController, viewModel)
                        }
                    }
                }

                // Bottom Navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (darkMode) Color(0xFF27272A) else Color.White)
                        .padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceAround
                ) { // Make this LazyRow scrollable
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val tabs = listOf(
                            "dashboard_route" to "Dashboard",
                            "apps_route" to "Apps",
                            "timeline_route" to "Timeline",
                            "goals_route" to "Goals",
                            "limiter_route" to "Limiter",
                            "settings_route" to "Settings"
                        )
                        items(tabs) { (route, label) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .background(
                                        if (currentRoute == route) Color(0xFFE0E7FF) else Color.Transparent,
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(8.dp)
                                    .clickable(
                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        navController.navigate(route) {
                                            popUpTo(navController.graph.startDestinationId)
                                            launchSingleTop = true
                                        }
                                    }) {
                                // TODO: Add icons
                                Text(
                                    label,
                                    fontSize = 12.sp,
                                    color = if (currentRoute == route) Color(0xFF2563EB) else Color.Gray,
                                )
                            }
                        }
                    } // End of LazyRow
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
                //    DropdownMenuBox(selectedDate, onDateChange)
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
                                    .background(
                                        Color(0xFF2563EB), shape = MaterialTheme.shapes.small
                                    )
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


}