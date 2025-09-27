package dev.sadakat.screentimetracker.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sadakat.screentimetracker.core.ui.theme.CoreSpacing
import dev.sadakat.screentimetracker.core.ui.theme.CoreTextStyles

@Composable
fun ProgressCard(
    title: String,
    progress: Float,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(CoreSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(CoreSpacing.minorSpacing)
        ) {
            Text(
                text = title,
                style = CoreTextStyles.cardTitle,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = CoreTextStyles.listItemSubtitle,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = value,
                    style = CoreTextStyles.listItemSubtitle.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun CircularMetric(
    value: String,
    progress: Float,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 80.dp
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(size),
        )
        Text(
            text = value,
            style = CoreTextStyles.metricValue,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}