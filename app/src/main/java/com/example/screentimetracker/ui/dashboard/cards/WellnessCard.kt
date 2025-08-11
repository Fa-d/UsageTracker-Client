package com.example.screentimetracker.ui.dashboard.cards

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.screentimetracker.domain.model.WellnessLevel
import com.example.screentimetracker.domain.model.WellnessScore
import com.example.screentimetracker.ui.components.PlayfulCard
import com.example.screentimetracker.ui.dashboard.viewmodels.DashboardViewModel
import com.example.screentimetracker.ui.theme.PlayfulAccent
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellnessCard(
    modifier: Modifier = Modifier, viewModel: DashboardViewModel
) {

    val wellnessScore by viewModel.wellnessScore.collectAsState()

    val score = wellnessScore?.totalScore ?: 0
    val level = wellnessScore?.level ?: WellnessLevel.DIGITAL_SPROUT
    
    // Animation for the progress ring
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(durationMillis = 1500, easing = EaseOutCubic),
        label = "wellness_score_animation"
    )

    PlayfulCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
        gradientBackground = true
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = level.emoji,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .background(
                                level.getBackgroundColor().copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Wellness Score",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = level.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Score breakdown
                wellnessScore?.let { ws ->
                    WellnessBreakdown(wellnessScore = ws)
                }
            }
            
            // Animated Progress Ring
            WellnessProgressRing(
                score = animatedScore,
                level = level,
                modifier = Modifier.size(80.dp)
            )
        }
    }
}

@Composable
private fun WellnessProgressRing(
    score: Float,
    level: WellnessLevel,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)
            
            // Background circle
            drawCircle(
                color = level.getDefaultBackgroundColor().copy(alpha = 0.2f),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            // Progress arc
            val sweepAngle = (score / 100f) * 360f
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        level.getDefaultBackgroundColor(),
                        level.getDefaultBackgroundColor().copy(alpha = 0.7f),
                        level.getDefaultBackgroundColor()
                    )
                ),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            // Animated sparkles around high scores
            if (score >= 75) {
                drawSparkles(center, radius + 16.dp.toPx(), score)
            }
        }
        
        Text(
            text = "${score.toInt()}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = level.getDefaultBackgroundColor()
        )
    }
}

@Composable
private fun WellnessBreakdown(wellnessScore: WellnessScore) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        WellnessBreakdownItem(
            label = "Time Goals",
            score = wellnessScore.timeLimitScore,
            emoji = "⏰",
            color = MaterialTheme.colorScheme.primary
        )
        WellnessBreakdownItem(
            label = "Focus Sessions",
            score = wellnessScore.focusSessionScore,
            emoji = "🎯",
            color = MaterialTheme.colorScheme.secondary
        )
        WellnessBreakdownItem(
            label = "Mindful Breaks",
            score = wellnessScore.breaksScore,
            emoji = "🧘",
            color = MaterialTheme.colorScheme.tertiary
        )
        WellnessBreakdownItem(
            label = "Sleep Hygiene",
            score = wellnessScore.sleepHygieneScore,
            emoji = "🌙",
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun WellnessBreakdownItem(
    label: String,
    score: Int,
    emoji: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = emoji, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$score/100",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

private fun DrawScope.drawSparkles(center: Offset, radius: Float, progress: Float) {
    val sparkleCount = 6
    val sparkleSize = 4.dp.toPx()
    val currentTime = System.currentTimeMillis()
    
    repeat(sparkleCount) { index ->
        val angle = (index * 60f + (currentTime / 50f) % 360f) * (Math.PI / 180f)
        val sparkleCenter = Offset(
            center.x + cos(angle).toFloat() * radius,
            center.y + sin(angle).toFloat() * radius
        )
        
        val alpha = ((sin((currentTime / 200f + index * 1.2f)) + 1f) / 2f).coerceIn(0.3f, 1f)
        
        drawCircle(
            color = PlayfulAccent.copy(alpha = alpha),
            radius = sparkleSize,
            center = sparkleCenter
        )
    }
}

private fun WellnessLevel.getDefaultBackgroundColor(): Color {
    return when (this) {
        WellnessLevel.DIGITAL_SPROUT -> Color(0xFF4CAF50) // Green
        WellnessLevel.MINDFUL_EXPLORER -> Color(0xFF2196F3) // Blue
        WellnessLevel.BALANCED_USER -> Color(0xFF9C27B0) // Purple
        WellnessLevel.WELLNESS_MASTER -> Color(0xFFFF6B35) // Orange
    }
}

@Composable
private fun WellnessLevel.getBackgroundColor(): Color {
    return when (this) {
        WellnessLevel.DIGITAL_SPROUT -> MaterialTheme.colorScheme.tertiary
        WellnessLevel.MINDFUL_EXPLORER -> MaterialTheme.colorScheme.primary
        WellnessLevel.BALANCED_USER -> MaterialTheme.colorScheme.secondary
        WellnessLevel.WELLNESS_MASTER -> MaterialTheme.colorScheme.tertiary
    }
}