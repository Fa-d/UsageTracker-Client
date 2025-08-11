package com.example.screentimetracker.ui.wellness

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.screentimetracker.ui.components.PlayfulCard
import com.example.screentimetracker.ui.dashboard.cards.AchievementsCard
import com.example.screentimetracker.ui.dashboard.cards.ChallengeCard
import com.example.screentimetracker.ui.dashboard.cards.FocusSessionCard
import com.example.screentimetracker.ui.dashboard.cards.HabitCard
import com.example.screentimetracker.ui.dashboard.cards.WellnessCard
import com.example.screentimetracker.ui.dashboard.utils.LocalDashboardViewModel
import com.example.screentimetracker.ui.dashboard.viewmodels.DashboardViewModel
import com.example.screentimetracker.ui.theme.LimeGreen
import com.example.screentimetracker.ui.theme.PlayfulPrimary
import com.example.screentimetracker.ui.theme.VibrantOrange

@Composable
fun WellnessScreen(
    onNavigateToHabits: (() -> Unit)? = null,
    onNavigateToMindfulness: (() -> Unit)? = null,
    viewModel: DashboardViewModel
) {
    val achievements = viewModel.achievements
    LazyColumn(
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
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp)
    ) {
        item {
            PlayfulCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                gradientBackground = true
            ) {
                Column {
                    Text(
                        text = "🌱 Digital Wellness Hub",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your journey to healthier digital habits",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Wellness Score Card
        item {
            WellnessCard(
                modifier = Modifier.fillMaxWidth(), viewModel = viewModel
            )
        }

        // Habits Card
        item {
            onNavigateToHabits?.let { navigateToHabits ->
                HabitCard(
                    onNavigateToHabits = navigateToHabits, modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Focus Session Card
        item {
            FocusSessionCard(
                focusSessionManager = LocalDashboardViewModel.current.focusSessionManagerUseCase,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Challenge Card
        item {
            ChallengeCard(
                challengeManager = LocalDashboardViewModel.current.challengeManagerUseCase,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Achievements Card
        item {
            AchievementsCard(
                achievements = achievements.value, onAchievementClick = { achievement ->
                    // Handle achievement click - could show detail dialog
                }, modifier = Modifier.fillMaxWidth()
            )
        }

        // Mindfulness Integration
        item {
            PlayfulCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "🧘‍♀️ Mindfulness Integration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Take a moment to practice mindfulness and breathing exercises",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (onNavigateToMindfulness != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        androidx.compose.material3.Button(
                            onClick = onNavigateToMindfulness, modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Start Mindfulness Session")
                        }
                    }
                }
            }
        }

        // Wellness Tips
        item {
            PlayfulCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "💡 Wellness Tips",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Take regular breaks from your screen\n" + "• Practice the 20-20-20 rule\n" + "• Set boundaries with notifications\n" + "• Create phone-free zones at home\n" + "• Use mindful breathing when feeling overwhelmed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}