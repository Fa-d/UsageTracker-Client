package dev.sadakat.screentimetracker.core.presentation.ui.ai.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sadakat.screentimetracker.core.presentation.ui.ai.viewmodels.AIFeatureState

@Composable
fun AIStatusIndicator(
    aiState: AIFeatureState,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val statusColor by animateColorAsState(
        targetValue = getStatusColor(aiState),
        animationSpec = tween(durationMillis = 300),
        label = "AI Status Color"
    )
    
    if (compact) {
        CompactAIStatusIndicator(
            aiState = aiState,
            statusColor = statusColor,
            onOpenSettings = onOpenSettings,
            modifier = modifier
        )
    } else {
        FullAIStatusIndicator(
            aiState = aiState,
            statusColor = statusColor,
            onOpenSettings = onOpenSettings,
            modifier = modifier
        )
    }
}

@Composable
private fun CompactAIStatusIndicator(
    aiState: AIFeatureState,
    statusColor: Color,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable { onOpenSettings() }
            .padding(8.dp)
    ) {
        Canvas(
            modifier = Modifier.size(8.dp)
        ) {
            drawCircle(color = statusColor)
        }
        
        Spacer(modifier = Modifier.width(6.dp))
        
        Text(
            text = "AI",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = statusColor
        )
        
        if (aiState.isDownloading) {
            Spacer(modifier = Modifier.width(4.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                strokeWidth = 1.5.dp,
                color = statusColor
            )
        }
    }
}

@Composable
private fun FullAIStatusIndicator(
    aiState: AIFeatureState,
    statusColor: Color,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenSettings() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = getStatusIcon(aiState),
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column {
                    Text(
                        text = "AI Features",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = getStatusText(aiState),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            when {
                aiState.isDownloading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        aiState.downloadProgress?.let { progress ->
                            Text(
                                text = "${progress.progress}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = statusColor
                        )
                    }
                }
                aiState.isAvailable -> {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Open AI Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
                else -> {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Configure AI",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun getStatusColor(aiState: AIFeatureState): Color {
    return when {
        aiState.isDownloading -> MaterialTheme.colorScheme.tertiary
        aiState.isAvailable && aiState.isEnabled -> MaterialTheme.colorScheme.primary
        aiState.isAvailable && !aiState.isEnabled -> MaterialTheme.colorScheme.outline
        aiState.availabilityStatus?.isDeviceCompatible == false -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }
}

private fun getStatusIcon(aiState: AIFeatureState): ImageVector {
    return when {
        aiState.isDownloading -> Icons.Default.Download
        aiState.isAvailable && aiState.isEnabled -> Icons.Default.Psychology
        aiState.isAvailable && !aiState.isEnabled -> Icons.Default.PsychologyAlt
        aiState.availabilityStatus?.isDeviceCompatible == false -> Icons.Default.ErrorOutline
        else -> Icons.Default.CloudDownload
    }
}

private fun getStatusText(aiState: AIFeatureState): String {
    return when {
        aiState.isDownloading -> "Downloading..."
        aiState.isAvailable && aiState.isEnabled -> "Active"
        aiState.isAvailable && !aiState.isEnabled -> "Available (Disabled)"
        aiState.availabilityStatus?.isDeviceCompatible == false -> "Not Compatible"
        else -> "Not Downloaded"
    }
}

@Composable
fun AIBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun AIFeatureChip(
    label: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isEnabled,
        onClick = { onToggle(!isEnabled) },
        label = { Text(label) },
        modifier = modifier,
        leadingIcon = if (isEnabled) {
            {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null
    )
}