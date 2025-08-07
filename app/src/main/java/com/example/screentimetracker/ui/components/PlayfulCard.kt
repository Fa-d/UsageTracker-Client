package com.example.screentimetracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.screentimetracker.ui.theme.*

@Composable
fun PlayfulCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    elevation: Int = 4,
    withAnimation: Boolean = true,
    gradientBackground: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    // Playful animations
    val infiniteTransition = rememberInfiniteTransition(label = "playful_animation")
    val animatedScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (withAnimation) 1.02f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ), label = "scale_animation"
    )

    val animatedRotation by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = if (withAnimation) 0.5f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "rotation_animation"
    )

    val cardModifier = if (gradientBackground) {
        modifier
            .scale(animatedScale)
            .rotate(animatedRotation)
            .shadow(
                elevation = elevation.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = backgroundColor.copy(alpha = 0.3f),
                spotColor = backgroundColor.copy(alpha = 0.3f)
            )
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = 0.7f),
                        backgroundColor.copy(alpha = 0.9f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        contentColor.copy(alpha = 0.1f),
                        contentColor.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
    } else {
        modifier
            .scale(animatedScale)
            .shadow(
                elevation = elevation.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = backgroundColor.copy(alpha = 0.2f),
                spotColor = backgroundColor.copy(alpha = 0.2f)
            )
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (gradientBackground) Color.Transparent else backgroundColor
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    gradientColors: List<Color>,
    contentColor: Color = Color.White,
    elevation: Int = 6,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = elevation.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = gradientColors.first().copy(alpha = 0.3f),
                spotColor = gradientColors.first().copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(gradientColors),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: @Composable () -> Unit,
    backgroundColor: Color = PlayfulPrimary.copy(alpha = 0.1f),
    contentColor: Color = PlayfulPrimary,
    subtitle: String? = null
) {
    // Pulsing animation for the icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_animation")
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ), label = "icon_pulse"
    )

    PlayfulCard(
        modifier = modifier,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        gradientBackground = true
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "📊 $title",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = contentColor
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "✨ $subtitle",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .scale(iconScale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                contentColor.copy(alpha = 0.2f),
                                contentColor.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
        }
    }
}

