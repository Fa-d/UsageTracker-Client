package com.example.screentimetracker.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.screentimetracker.ui.components.PlayfulCard
import com.example.screentimetracker.ui.components.PlayfulMetricCard
import com.example.screentimetracker.ui.theme.*

@Composable
fun GoalsView(
    focusMode: Boolean, onFocusModeChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        LavenderPurple.copy(alpha = 0.05f),
                        VibrantOrange.copy(alpha = 0.03f),
                        LimeGreen.copy(alpha = 0.04f)
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Playful Header
        PlayfulCard(
            backgroundColor = LavenderPurple.copy(alpha = 0.1f),
            gradientBackground = true
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        "🎯 Goals & Digital Wellness",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = LavenderPurple
                    )
                    Text(
                        "Transform your digital habits ✨",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LavenderPurple.copy(alpha = 0.7f)
                    )
                }
                Text(
                    "🧘",
                    fontSize = 40.sp,
                    modifier = Modifier
                        .background(
                            LavenderPurple.copy(alpha = 0.1f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(12.dp)
                )
            }
        }

        // Daily Goals
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PlayfulMetricCard(
                modifier = Modifier.weight(1f),
                title = "Daily Limit",
                value = "4h",
                emoji = "⏰",
                color = SkyBlue,
                subtitle = "Screen time goal"
            )
            PlayfulMetricCard(
                modifier = Modifier.weight(1f),
                title = "Mindful Breaks",
                value = "8",
                emoji = "☕",
                color = VibrantOrange,
                subtitle = "Taken today"
            )
        }

        // Focus Mode Card
        PlayfulCard(
            backgroundColor = if (focusMode) LimeGreen.copy(alpha = 0.15f) else PlayfulSecondary.copy(alpha = 0.1f),
            gradientBackground = true
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Focus Mode", fontWeight = FontWeight.Medium)
                        Text("Block distracting apps", fontSize = 13.sp, color = Color.Gray)
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
                    // appUsageData.take(3).forEach { app ->
                    //     Row(
                    //         Modifier.fillMaxWidth(),
                    //         horizontalArrangement = Arrangement.SpaceBetween,
                    //         verticalAlignment = Alignment.CenterVertically
                    //     ) {
                    //         Row(verticalAlignment = Alignment.CenterVertically) {
                    //             Text(app.icon, fontSize = 20.sp)
                    //             Spacer(Modifier.width(8.dp))
                    //             Text(app.app, fontWeight = FontWeight.Medium)
                    //         }
                    //         Text("${app.time} / 1h", fontSize = 13.sp, color = Color.Gray)
                    //     }
                    // }
                }
            }
        }
    }
}
