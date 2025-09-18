package dev.sadakat.screentimetracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 12.dp,
    animationDuration: Int = 1000,
    centerText: String = "",
    centerSubtext: String = "",
    progressColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
    showGradient: Boolean = true,
    showGlow: Boolean = true
) {
    var animationPlayed by remember { mutableStateOf(false) }
    
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) progress else 0f,
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = EaseOutCubic
        ),
        label = "progress_animation"
    )
    
    val animatedGlow by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = EaseOutCubic
        ),
        label = "glow_animation"
    )
    
    LaunchedEffect(progress) {
        animationPlayed = true
    }
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawProgressRing(
                progress = animatedProgress,
                strokeWidth = strokeWidth.toPx(),
                progressColor = progressColor,
                backgroundColor = backgroundColor,
                showGradient = showGradient,
                showGlow = showGlow,
                glowIntensity = animatedGlow
            )
        }
        
        // Center content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (centerText.isNotEmpty()) {
                Text(
                    text = centerText,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.15f).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            if (centerSubtext.isNotEmpty()) {
                Text(
                    text = centerSubtext,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = (size.value * 0.08f).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun DrawScope.drawProgressRing(
    progress: Float,
    strokeWidth: Float,
    progressColor: Color,
    backgroundColor: Color,
    showGradient: Boolean,
    showGlow: Boolean,
    glowIntensity: Float
) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val radius = (size.minDimension - strokeWidth) / 2f
    
    // Draw background ring
    drawCircle(
        color = backgroundColor,
        radius = radius,
        center = Offset(centerX, centerY),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
    
    if (progress > 0f) {
        // Create gradient brush if enabled
        val brush = if (showGradient) {
            Brush.sweepGradient(
                colors = listOf(
                    progressColor,
                    progressColor.copy(alpha = 0.7f),
                    progressColor,
                    progressColor.copy(alpha = 0.9f)
                ),
                center = Offset(centerX, centerY)
            )
        } else {
            SolidColor(progressColor)
        }
        
        // Draw glow effect if enabled
        if (showGlow && glowIntensity > 0f) {
            val glowRadius = radius + (strokeWidth * 0.3f * glowIntensity)
            drawCircle(
                color = progressColor.copy(alpha = 0.1f * glowIntensity),
                radius = glowRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = strokeWidth * 2f * glowIntensity)
            )
        }
        
        // Draw progress arc
        drawArc(
            brush = brush,
            startAngle = -90f,
            sweepAngle = progress * 360f,
            useCenter = false,
            topLeft = Offset(centerX - radius, centerY - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        
        // Draw end cap dot for smooth finish
        val endAngle = (-90f + progress * 360f) * (Math.PI / 180f)
        val endX = centerX + radius * cos(endAngle).toFloat()
        val endY = centerY + radius * sin(endAngle).toFloat()
        
        drawCircle(
            color = progressColor,
            radius = strokeWidth / 2f,
            center = Offset(endX, endY)
        )
    }
}

@Composable
fun MultiProgressRing(
    progressData: List<ProgressData>,
    modifier: Modifier = Modifier,
    ringSize: Dp = 160.dp,
    centerContent: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val maxRadius = minOf(size.width, size.height) / 2f - 40.dp.toPx()
            
            progressData.forEachIndexed { index, data ->
                val radius = maxRadius - (index * 25.dp.toPx())
                val strokeWidth = 8.dp.toPx()
                
                // Background ring
                drawCircle(
                    color = data.color.copy(alpha = 0.2f),
                    radius = radius,
                    center = Offset(centerX, centerY),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                
                // Progress arc
                if (data.progress > 0f) {
                    drawArc(
                        color = data.color,
                        startAngle = -90f,
                        sweepAngle = data.progress * 360f,
                        useCenter = false,
                        topLeft = Offset(centerX - radius, centerY - radius),
                        size = Size(radius * 2f, radius * 2f),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }
        }
        
        centerContent()
    }
}

data class ProgressData(
    val progress: Float,
    val color: Color,
    val label: String
)