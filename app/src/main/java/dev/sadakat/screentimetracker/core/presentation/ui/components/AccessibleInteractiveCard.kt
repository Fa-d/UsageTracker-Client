package dev.sadakat.screentimetracker.core.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun AccessibleInteractiveCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    elevation: Int = 2,
    enabled: Boolean = true,
    role: Role = Role.Button,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    gradientBackground: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Accessible animation feedback
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.98f else 1f,
        animationSpec = tween(durationMillis = 100, easing = EaseInOutCubic),
        label = "press_scale_animation"
    )
    
    val animatedElevation by animateFloatAsState(
        targetValue = if (isPressed && enabled) elevation - 1f else elevation.toFloat(),
        animationSpec = tween(durationMillis = 100, easing = EaseInOutCubic),
        label = "press_elevation_animation"
    )
    
    val cardModifier = if (gradientBackground) {
        modifier
            .scale(animatedScale)
            .shadow(
                elevation = animatedElevation.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
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
                width = if (isPressed && enabled) 2.dp else 1.dp,
                color = if (isPressed && enabled) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                },
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Custom feedback through animations
                enabled = enabled,
                role = role,
                onClick = onClick
            )
    } else {
        modifier
            .scale(animatedScale)
            .shadow(
                elevation = animatedElevation.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Custom feedback through animations
                enabled = enabled,
                role = role,
                onClick = onClick
            )
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (gradientBackground) Color.Transparent else backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.38f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun AccessibleMetricCard(
    title: String,
    value: String,
    emoji: String,
    color: Color = MaterialTheme.colorScheme.primary,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (onClick != null) {
        AccessibleInteractiveCard(
            onClick = onClick,
            modifier = modifier,
            backgroundColor = MaterialTheme.colorScheme.surface,
            gradientBackground = false
        ) {
            MetricCardContent(
                title = title,
                value = value,
                emoji = emoji,
                color = color,
                subtitle = subtitle
            )
        }
    } else {
        PlayfulCard(
            modifier = modifier,
            backgroundColor = MaterialTheme.colorScheme.surface,
            withAnimation = false
        ) {
            MetricCardContent(
                title = title,
                value = value,
                emoji = emoji,
                color = color,
                subtitle = subtitle
            )
        }
    }
}

@Composable
private fun MetricCardContent(
    title: String,
    value: String,
    emoji: String,
    color: Color,
    subtitle: String
) {
    Column(
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .background(
                    color.copy(alpha = 0.12f),
                    RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.headlineLarge,
            color = color
        )
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}