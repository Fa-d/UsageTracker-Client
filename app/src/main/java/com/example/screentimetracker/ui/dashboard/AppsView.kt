package com.example.screentimetracker.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.screentimetracker.utils.millisToReadableTime


import androidx.navigation.NavController

@Composable
fun AppsView(
    expandedCategory: Int?, onCategoryExpand: (Int?) -> Unit, navController: NavController
) {
    val viewModel = LocalDashboardViewModel.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val categories = getCategoryDataFromAppUsages(state.appUsagesToday)
    val appUsageData = state.appUsagesToday
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("App Usage", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Button(onClick = { navController.navigate("app_search_route") }, contentPadding = PaddingValues(8.dp)) {
                Text("Filter", color = Color(0xFF2563EB), fontSize = 14.sp)
            }
        }
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
                                appUsageData.filter { it.appName == category.name }.forEach { app ->
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                // TODO: Replace with real app icon if available
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
