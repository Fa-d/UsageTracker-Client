package com.example.screentimetracker.ui.dashboard.cards

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.screentimetracker.domain.model.Achievement
import com.example.screentimetracker.ui.components.PlayfulCard
import com.example.screentimetracker.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AchievementsCard(
    achievements: List<Achievement>,
    onAchievementClick: (Achievement) -> Unit,
    modifier: Modifier = Modifier
) {
    val recentAchievements = achievements.take(5) // Show top 5 achievements
    val unlockedCount = achievements.count { it.isUnlocked }
    
    PlayfulCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
        gradientBackground = true
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🏆",
                        fontSize = 24.sp,
                        modifier = Modifier
                            .background(
                                PlayfulAccent.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Achievements",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PlayfulAccent
                        )
                        Text(
                            text = "$unlockedCount/${achievements.size} unlocked",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PlayfulAccent.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Overall progress ring
                AchievementProgressRing(
                    progress = if (achievements.isNotEmpty()) unlockedCount.toFloat() / achievements.size else 0f,
                    modifier = Modifier.size(50.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Achievement items
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(recentAchievements) { achievement ->
                    AchievementItem(
                        achievement = achievement,
                        onClick = { onAchievementClick(achievement) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementItem(
    achievement: Achievement,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = achievement.progressPercentage / 100f,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "achievement_progress"
    )

    Card(
        modifier = modifier
            .width(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked) 
                PlayfulAccent.copy(alpha = 0.2f) 
            else 
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(48.dp)
            ) {
                // Progress ring
                if (!achievement.isUnlocked) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawProgressRing(animatedProgress)
                    }
                }
                
                // Achievement emoji with unlock animation
                val scale by animateFloatAsState(
                    targetValue = if (achievement.isUnlocked) 1.2f else 1f,
                    animationSpec = spring(dampingRatio = 0.6f),
                    label = "achievement_scale"
                )
                
                Text(
                    text = achievement.emoji,
                    fontSize = (24 * scale).sp,
                    modifier = Modifier.then(
                        if (achievement.isUnlocked) {
                            Modifier.background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        PlayfulAccent.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                        } else Modifier
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = achievement.name,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                color = if (achievement.isUnlocked) PlayfulAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            
            if (!achievement.isUnlocked) {
                Text(
                    text = "${achievement.currentProgress}/${achievement.targetValue}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            } else {
                // Sparkle animation for unlocked achievements
                UnlockSparkles()
            }
        }
    }
}

@Composable
private fun AchievementProgressRing(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawProgressRing(progress)
        }
        
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = PlayfulAccent
        )
    }
}

@Composable
private fun UnlockSparkles() {
    val currentTime by remember {
        derivedStateOf { System.currentTimeMillis() }
    }
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
    ) {
        repeat(3) { index ->
            val sparkleX = size.width * (0.2f + index * 0.3f)
            val sparkleY = size.height / 2
            val phase = (currentTime / 300f + index * 2f)
            val alpha = ((sin(phase) + 1f) / 2f).coerceIn(0.3f, 1f)
            
            drawCircle(
                color = PlayfulAccent.copy(alpha = alpha),
                radius = 2.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(sparkleX, sparkleY)
            )
        }
    }
}

private fun DrawScope.drawProgressRing(progress: Float) {
    val strokeWidth = 4.dp.toPx()
    val radius = (size.minDimension - strokeWidth) / 2
    val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
    
    // Background circle
    drawCircle(
        color = Color.Gray.copy(alpha = 0.2f),
        radius = radius,
        center = center,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
    )
    
    // Progress arc
    if (progress > 0f) {
        drawArc(
            color = PlayfulAccent,
            startAngle = -90f,
            sweepAngle = progress * 360f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}