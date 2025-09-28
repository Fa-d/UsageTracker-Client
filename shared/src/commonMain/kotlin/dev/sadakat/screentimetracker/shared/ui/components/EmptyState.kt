package dev.sadakat.screentimetracker.shared.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.sadakat.screentimetracker.shared.ui.theme.CoreSpacing
import dev.sadakat.screentimetracker.shared.ui.theme.CoreTextStyles

@Composable
fun EmptyStateCard(
    emoji: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(CoreSpacing.emptyStateSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CoreSpacing.sectionSpacing)
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.displayMedium
            )

            Text(
                text = title,
                style = CoreTextStyles.emptyStateTitle,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = description,
                style = CoreTextStyles.emptyStateDescription,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (actionText != null && onActionClick != null) {
                Button(
                    onClick = onActionClick
                ) {
                    Text(actionText)
                }
            }
        }
    }
}