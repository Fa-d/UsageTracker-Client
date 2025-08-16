package com.example.screentimetracker.ui.dashboard.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.screentimetracker.ui.components.PlayfulCard
import com.example.screentimetracker.ui.components.PlayfulMetricCard
import com.example.screentimetracker.ui.dashboard.cards.OverviewCard
import com.example.screentimetracker.ui.dashboard.cards.DigitalPetCard
import com.example.screentimetracker.ui.dashboard.state.DashboardState
import com.example.screentimetracker.ui.theme.*
import com.example.screentimetracker.utils.millisToReadableTime

@Composable
fun SimpleDashboardView(
    state: DashboardState,
    digitalPet: com.example.screentimetracker.data.local.DigitalPet?,
    petStats: com.example.screentimetracker.data.local.PetStats?,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToWellness: () -> Unit,
    onNavigateToHabits: (() -> Unit)? = null,
    onNavigateToTimeRestrictions: (() -> Unit)? = null,
    onPetClick: () -> Unit = {},
    onPetInteraction: () -> Unit = {},
    onPetFeed: () -> Unit = {}
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
        
        // Header
        PlayfulCard(
            modifier = Modifier
                .fillMaxWidth(),
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            gradientBackground = true
        ) {
            Column {
                Text(
                    text = "🎯 Today's Overview",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Your key digital wellness metrics",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Core Metrics
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
                title = "Focus Score",
                value = calculateFocusScore(state),
                emoji = "🎯",
                color = MaterialTheme.colorScheme.secondary,
                subtitle = "Productivity level"
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
                title = "Unlocks",
                value = "${state.totalScreenUnlocksToday}",
                emoji = "🔓",
                color = MaterialTheme.colorScheme.tertiary,
                subtitle = "Screen activations"
            )
            PlayfulMetricCard(
                modifier = Modifier.weight(1f),
                title = "App Opens",
                value = "${state.appUsagesToday.sumOf { it.openCount }}",
                emoji = "📱",
                color = MaterialTheme.colorScheme.secondary,
                subtitle = "App launches"
            )
        }

        // Digital Pet Card
        if (digitalPet != null && petStats != null) {
            DigitalPetCard(
                pet = digitalPet,
                petStats = petStats,
                onPetClick = onPetClick,
                onInteract = onPetInteraction,
                onFeed = onPetFeed,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }

       /* // Overview Card
        OverviewCard(
            totalScreenTimeTodayMillis = state.totalScreenTimeTodayMillis,
            totalScreenUnlocksToday = state.totalScreenUnlocksToday,
            totalAppOpensToday = state.appUsagesToday.sumOf { it.openCount }
        )

        // Quick Navigation Actions
        PlayfulCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column {
                Text(
                    text = "🚀 Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    androidx.compose.material3.Button(
                        onClick = onNavigateToAnalytics,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("📊 Analytics", fontSize = 14.sp)
                    }

                    androidx.compose.material3.Button(
                        onClick = onNavigateToWellness,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🌱 Wellness", fontSize = 14.sp)
                    }
                }

                if (onNavigateToHabits != null || onNavigateToTimeRestrictions != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Secondary Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        onNavigateToHabits?.let { navigateToHabits ->
                            androidx.compose.material3.OutlinedButton(
                                onClick = navigateToHabits,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("💪 Habits", fontSize = 14.sp)
                            }
                        }

                        onNavigateToTimeRestrictions?.let { navigateToTimeRestrictions ->
                            androidx.compose.material3.OutlinedButton(
                                onClick = navigateToTimeRestrictions,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("⏰ Limits", fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))*/
    }
}