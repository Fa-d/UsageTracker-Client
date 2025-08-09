package com.example.screentimetracker.ui.dashboard.cards

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.screentimetracker.data.local.HabitTracker
import com.example.screentimetracker.ui.habits.viewmodels.HabitTrackerViewModel
import com.example.screentimetracker.ui.theme.ColorfulPrimary
import com.example.screentimetracker.ui.theme.ColorfulSecondary
import com.example.screentimetracker.ui.theme.ColorfulTertiary

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
                progress = animatedProgress,
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
            
            // Quick habit preview (first 3 habits)
            if (uiState.todaysHabits.isNotEmpty()) {
                Text(
                    text = "Today's Focus:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.todaysHabits.take(3)) { habit ->
                        QuickHabitPreview(
                            habit = habit,
                            onComplete = { viewModel.completeHabit(habit.habitId) }
                        )
                    }
                    
                    if (uiState.todaysHabits.size > 3) {
                        item {
                            MoreHabitsIndicator(
                                remaining = uiState.todaysHabits.size - 3
                            )
                        }
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

@Composable
private fun QuickHabitPreview(
    habit: HabitTracker,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                if (habit.isCompleted) 
                    ColorfulTertiary.copy(alpha = 0.1f) 
                else 
                    MaterialTheme.colorScheme.surface,
                RoundedCornerShape(12.dp)
            )
            .clickable(enabled = !habit.isCompleted) { onComplete() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Completion indicator
        Icon(
            imageVector = if (habit.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
            contentDescription = if (habit.isCompleted) "Completed" else "Not completed",
            modifier = Modifier.size(16.dp),
            tint = if (habit.isCompleted) ColorfulTertiary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.width(6.dp))
        
        // Habit emoji
        Text(
            text = habit.emoji,
            fontSize = 16.sp
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        // Habit name (shortened)
        Text(
            text = habit.habitName.take(10) + if (habit.habitName.length > 10) "..." else "",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        // Show streak if available
        if (habit.currentStreak > 0) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${habit.currentStreak}🔥",
                style = MaterialTheme.typography.bodySmall,
                color = ColorfulPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MoreHabitsIndicator(
    remaining: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(12.dp)
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+$remaining more",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}