package dev.sadakat.screentimetracker.core.presentation.ui.dashboard.cards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.sadakat.screentimetracker.core.presentation.ui.habits.viewmodels.HabitTrackerViewModel
import dev.sadakat.screentimetracker.core.presentation.ui.theme.ColorfulPrimary
import dev.sadakat.screentimetracker.core.data.local.entities.HabitTracker


@Composable
fun HabitCard(
    onNavigateToHabits: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HabitTrackerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val (completed, total) = viewModel.getTodayProgress()
    val progress = if (total > 0) completed.toFloat() / total.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000),
        label = "habit_progress_animation"
    )

    Card(
        modifier = modifier.clickable { onNavigateToHabits() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🌱 Digital Wellness Habits",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Build healthy digital habits",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = "$completed/$total",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = ColorfulPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = ColorfulPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Motivational message
            val motivationText = when {
                total == 0 -> "Tap to start building healthy habits! 🚀"
                completed == total && total > 0 -> "Perfect! All habits completed today! 🎉"
                progress >= 0.7f -> "Excellent progress, keep it up! 💪"
                progress >= 0.5f -> "You're halfway there! ⭐"
                progress > 0f -> "Good start, you've got this! 🌟"
                else -> "Ready to begin your wellness journey? 🌱"
            }
            
            Text(
                text = motivationText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // All habits display
            if (uiState.todaysHabits.isNotEmpty()) {
                Text(
                    text = "Today's Habits:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Show habits in a vertical column for better visibility
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    uiState.todaysHabits.forEach { habit ->
                        HabitItem(
                            habit = habit,
                            onComplete = { viewModel.completeHabit(habit.id.toString()) }
                        )
                    }
                }
            }
            
            // Show best streaks if available
            val bestStreakHabits = uiState.todaysHabits.filter { it.bestStreak > 0 }
            if (bestStreakHabits.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                val bestHabit = bestStreakHabits.maxByOrNull { it.bestStreak }
                bestHabit?.let {
                    Text(
                        text = "🔥 Best streak: ${it.habitName} (${it.bestStreak} days)",
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorfulPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun HabitItemPreview() {
    val habit = HabitTracker(
        habitId = "1",
        habitName = "Read a Book",
        description = "Read for 30 minutes",
        emoji = "📚",
        date = System.currentTimeMillis(),
        isCompleted = true,
        currentStreak = 5,
        bestStreak = 10
    )
    HabitItem(
        habit = habit, onComplete = {})
}


@Composable
private fun HabitItem(
    habit: HabitTracker,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !habit.isCompleted) { onComplete() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp, pressedElevation = 1.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (habit.isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surfaceVariant, CircleShape
                    ), contentAlignment = Alignment.Center
            ) {
                if (habit.isCompleted) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Completed",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = habit.emoji, fontSize = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Habit details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Habit name
                Text(
                    text = habit.habitName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Description or status
                if (habit.description.isNotEmpty()) {
                    Text(
                        text = habit.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = if (habit.isCompleted) "Completed today" else "Tap to complete",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Streak information
                if (habit.currentStreak > 0 || habit.bestStreak > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (habit.currentStreak > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔥", fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${habit.currentStreak} day${if (habit.currentStreak != 1) "s" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (habit.bestStreak > habit.currentStreak && habit.bestStreak > 0) {
                            if (habit.currentStreak > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = "Best: ${habit.bestStreak}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Status indicator
            if (habit.isCompleted) {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Box(
                    modifier = Modifier
//                        .background(
//                            MaterialTheme.colorScheme.surfaceVariant,
//                            RoundedCornerShape(20.dp)
//                        )
                    //.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Pending",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}