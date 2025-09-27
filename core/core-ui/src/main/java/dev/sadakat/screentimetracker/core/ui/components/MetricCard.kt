package dev.sadakat.screentimetracker.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.sadakat.screentimetracker.core.ui.theme.CoreSpacing
import dev.sadakat.screentimetracker.core.ui.theme.CoreTextStyles

@Composable
fun MetricCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(CoreSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(CoreSpacing.cardContentSpacing)
        ) {
            Text(
                text = title,
                style = CoreTextStyles.cardTitle,
                color = MaterialTheme.colorScheme.onSurface
            )

            content()
        }
    }
}

@Composable
fun MetricItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = CoreTextStyles.metricValue,
            color = valueColor
        )
        Text(
            text = label,
            style = CoreTextStyles.metricLabel,
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
}