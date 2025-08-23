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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.screentimetracker.ui.components.PlayfulCard
import com.example.screentimetracker.ui.components.PlayfulMetricCard
import com.example.screentimetracker.ui.components.ModernCard
import com.example.screentimetracker.ui.components.StatisticsCard
import com.example.screentimetracker.ui.components.FeatureCard
import com.example.screentimetracker.ui.components.ActionCard
import com.example.screentimetracker.ui.components.CardVariant
import com.example.screentimetracker.ui.components.Trend
import com.example.screentimetracker.ui.dashboard.cards.OverviewCard
import com.example.screentimetracker.ui.dashboard.cards.DigitalPetCard
import com.example.screentimetracker.ui.dashboard.state.DashboardState
import com.example.screentimetracker.ui.theme.*
import com.example.screentimetracker.utils.millisToReadableTime
import com.example.screentimetracker.ui.ai.components.AIInsightsCard
import com.example.screentimetracker.ui.ai.components.AIInsight

@Composable
fun SimpleDashboardView(
    state: DashboardState,
    digitalPet: com.example.screentimetracker.data.local.DigitalPet?,
    petStats: com.example.screentimetracker.data.local.PetStats?,
    aiInsights: List<AIInsight> = emptyList(),
    isLoadingInsights: Boolean = false,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToWellness: () -> Unit,
    onNavigateToHabits: (() -> Unit)? = null,
    onNavigateToTimeRestrictions: (() -> Unit)? = null,
    onPetClick: () -> Unit = {},
    onPetInteraction: () -> Unit = {},
    onPetFeed: () -> Unit = {},
    onRefreshAIInsights: () -> Unit = {}
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

        // Primary Statistics Card
        StatisticsCard(
            title = "Screen Time Today",
            value = millisToReadableTime(state.totalScreenTimeTodayMillis),
            subtitle = "Digital wellness tracking",
            icon = {
                Text(
                    text = "⏰",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            trend = Trend(
                percentage = -12.5f,
                isPositive = true,
                label = "vs yesterday"
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            onClick = onNavigateToAnalytics
        )

        // Core Metrics Grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatisticsCard(
                title = "Focus Score",
                value = calculateFocusScore(state),
                modifier = Modifier.weight(1f),
                icon = {
                    Text(
                        text = "🎯",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            )
            StatisticsCard(
                title = "Unlocks",
                value = "${state.totalScreenUnlocksToday}",
                modifier = Modifier.weight(1f),
                icon = {
                    Text(
                        text = "🔓",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatisticsCard(
                title = "App Opens",
                value = "${state.appUsagesToday.sumOf { it.openCount }}",
                modifier = Modifier.weight(1f),
                icon = {
                    Text(
                        text = "📱",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            )
            StatisticsCard(
                title = "Wellness",
                value = "Good",
                subtitle = "On track",
                modifier = Modifier.weight(1f),
                icon = {
                    Text(
                        text = "🌱",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                onClick = onNavigateToWellness
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

        // AI Insights Card
        AIInsightsCard(
            insights = aiInsights,
            isLoading = isLoadingInsights,
            onRefresh = onRefreshAIInsights,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        // Feature Cards
        onNavigateToHabits?.let { navigateToHabits ->
            FeatureCard(
                title = "Build Better Habits",
                description = "Track and improve your daily digital wellness habits",
                gradientColors = listOf(
                    MaterialTheme.colorScheme.tertiary,
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
                ),
                icon = {
                    Text(
                        text = "💪",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                },
                onClick = navigateToHabits,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }

        onNavigateToTimeRestrictions?.let { navigateToTimeRestrictions ->
            ActionCard(
                title = "Time Restrictions",
                description = "Set and manage app time limits to stay focused",
                actionText = "Manage Limits",
                onAction = navigateToTimeRestrictions,
                icon = {
                    Text(
                        text = "⏰",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
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