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
    elevation: Int = 2,
    withAnimation: Boolean = false, // Disabled by default for better accessibility
    gradientBackground: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    // Subtle, accessibility-friendly animations
    val animatedElevation by animateFloatAsState(
        targetValue = if (withAnimation) elevation + 2f else elevation.toFloat(),
        animationSpec = tween(durationMillis = 300, easing = EaseInOutCubic),
        label = "elevation_animation"
    )
    
    val animatedScale by animateFloatAsState(
        targetValue = if (withAnimation) 1.02f else 1f,
        animationSpec = tween(durationMillis = 200, easing = EaseInOutCubic),
        label = "scale_animation"
    )

    val cardModifier = if (gradientBackground) {
        modifier
            .scale(animatedScale)

            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = 0.8f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            )
    } else {
        modifier
            .scale(animatedScale)
            .shadow(
                elevation = animatedElevation.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.shadow.copy(alpha = 0.15f),
                spotColor = MaterialTheme.colorScheme.shadow.copy(alpha = 0.25f)
            )
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (gradientBackground) Color.Transparent else backgroundColor
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
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
                shape = RoundedCornerShape(16.dp),
                ambientColor = gradientColors.first().copy(alpha = 0.3f),
                spotColor = gradientColors.first().copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(16.dp),
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
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
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
                    text = title,
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
                        text = subtitle,
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

