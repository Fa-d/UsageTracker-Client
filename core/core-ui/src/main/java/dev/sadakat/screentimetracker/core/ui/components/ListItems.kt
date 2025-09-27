package dev.sadakat.screentimetracker.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.sadakat.screentimetracker.core.ui.theme.CoreSpacing
import dev.sadakat.screentimetracker.core.ui.theme.CoreTextStyles

@Composable
fun TwoColumnListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    action: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val cardModifier = if (onClick != null) {
        modifier.fillMaxWidth()
    } else {
        modifier.fillMaxWidth()
    }

    Card(
        modifier = cardModifier,
        onClick = onClick ?: {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CoreSpacing.cardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = CoreTextStyles.listItemTitle,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = CoreTextStyles.listItemSubtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (action != null) {
                action()
            } else if (value != null) {
                Text(
                    text = value,
                    style = CoreTextStyles.listItemTitle,
                    color = valueColor
                )
            }
        }
    }
}

@Composable
fun AppUsageListItem(
    appName: String,
    usageTime: String,
    percentage: Float,
    change: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CoreSpacing.cardPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (icon != null) {
                    icon()
                    Spacer(modifier = Modifier.width(CoreSpacing.cardContentSpacing))
                }

                Column {
                    Text(
                        text = appName,
                        style = CoreTextStyles.listItemTitle,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = usageTime,
                        style = CoreTextStyles.listItemSubtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${(percentage * 100).toInt()}%",
                    style = CoreTextStyles.listItemTitle,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = change,
                    style = CoreTextStyles.listItemSubtitle,
                    color = if (change.startsWith("+")) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }
        }
    }
}