package dev.sadakat.screentimetracker.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class CardVariant {
    FILLED, OUTLINED, ELEVATED, GRADIENT
}

enum class CardSize {
    COMPACT, STANDARD, LARGE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernCard(
    modifier: Modifier = Modifier,
    variant: CardVariant = CardVariant.FILLED,
    size: CardSize = CardSize.STANDARD,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: CardColors = CardDefaults.cardColors(),
    gradientColors: List<Color>? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable CardScope.() -> Unit
) {
    val (cornerRadius, contentPadding, elevation) = when (size) {
        CardSize.COMPACT -> Triple(12.dp, 12.dp, 1.dp)
        CardSize.STANDARD -> Triple(16.dp, 16.dp, 2.dp)
        CardSize.LARGE -> Triple(20.dp, 20.dp, 4.dp)
    }

    // Interaction animations
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    val cardColors = when (variant) {
        CardVariant.FILLED -> colors
        CardVariant.OUTLINED -> CardDefaults.outlinedCardColors()
        CardVariant.ELEVATED -> CardDefaults.elevatedCardColors()
        CardVariant.GRADIENT -> CardDefaults.cardColors(containerColor = Color.Transparent)
    }

    val cardModifier = modifier
        .scale(scale)
        .let { mod ->
            if (onClick != null) {
                mod.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick
                )
            } else mod
        }

    when (variant) {
        CardVariant.GRADIENT -> {
            if (gradientColors != null && gradientColors.size >= 2) {
                Card(
                    modifier = cardModifier,
                    shape = RoundedCornerShape(cornerRadius),
                    colors = cardColors,
                    elevation = CardDefaults.cardElevation(defaultElevation = elevation)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(gradientColors),
                                shape = RoundedCornerShape(cornerRadius)
                            )
                            .padding(contentPadding)
                    ) {
                        CardScopeImpl().content()
                    }
                }
            } else {
                StandardCard(cardModifier, cornerRadius, cardColors, elevation, contentPadding, content)
            }
        }
        CardVariant.OUTLINED -> {
            Card(
                modifier = cardModifier.border(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                    RoundedCornerShape(cornerRadius)
                ),
                shape = RoundedCornerShape(cornerRadius),
                colors = cardColors,
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(modifier = Modifier.padding(contentPadding)) {
                    CardScopeImpl().content()
                }
            }
        }
        else -> {
            StandardCard(cardModifier, cornerRadius, cardColors, elevation, contentPadding, content)
        }
    }
}

@Composable
private fun StandardCard(
    modifier: Modifier,
    cornerRadius: Dp,
    colors: CardColors,
    elevation: Dp,
    contentPadding: Dp,
    content: @Composable CardScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = colors,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            CardScopeImpl().content()
        }
    }
}

@Composable
fun StatisticsCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: (@Composable () -> Unit)? = null,
    trend: Trend? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        variant = CardVariant.ELEVATED,
        size = CardSize.STANDARD,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                subtitle?.let {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                trend?.let { 
                    Spacer(modifier = Modifier.height(4.dp))
                    TrendIndicator(trend = it) 
                }
            }
            icon?.let {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    it()
                }
            }
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    description: String,
    actionText: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        variant = CardVariant.OUTLINED,
        size = CardSize.STANDARD
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                icon?.let {
                    Spacer(modifier = Modifier.width(12.dp))
                    it()
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAction,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(actionText)
            }
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
    ),
    icon: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        variant = CardVariant.GRADIENT,
        gradientColors = gradientColors,
        size = CardSize.LARGE,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            icon?.let {
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    it()
                }
            }
        }
    }
}

data class Trend(
    val percentage: Float,
    val isPositive: Boolean,
    val label: String
)

@Composable
private fun TrendIndicator(trend: Trend) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val color = if (trend.isPositive) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.error
        }
        
        Text(
            text = "${if (trend.isPositive) "+" else ""}${trend.percentage}%",
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = trend.label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private class CardScopeImpl : CardScope {
    // Empty implementation since CardScope is just a marker interface
}

interface CardScope