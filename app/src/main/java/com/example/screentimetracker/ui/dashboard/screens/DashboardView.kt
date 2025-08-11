package com.example.screentimetracker.ui.dashboard.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.screentimetracker.ui.dashboard.state.DashboardState


// Helper function to calculate focus score
fun calculateFocusScore(state: DashboardState): String {
    val totalTime = state.totalScreenTimeTodayMillis
    val unlocks = state.totalScreenUnlocksToday
    val opens = state.appUsagesToday.sumOf { it.openCount }

    val score = when {
        totalTime < 3600000 && unlocks < 20 && opens < 30 -> 95 // Less than 1 hour, few unlocks and opens
        totalTime < 7200000 && unlocks < 50 && opens < 60 -> 80 // Less than 2 hours, moderate unlocks and opens
        totalTime < 14400000 && unlocks < 100 && opens < 120 -> 65 // Less than 4 hours, higher unlocks/opens
        else -> 45 // Heavy usage
    }
    
    return "$score%"
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
                        .background(
                            MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small
                        )
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
                        .background(
                            MaterialTheme.colorScheme.secondary, shape = MaterialTheme.shapes.small
                        )
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
