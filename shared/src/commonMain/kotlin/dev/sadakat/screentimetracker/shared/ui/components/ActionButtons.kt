package dev.sadakat.screentimetracker.shared.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.sadakat.screentimetracker.shared.ui.theme.CoreSpacing

data class ActionButtonConfig(
    val text: String,
    val onClick: () -> Unit,
    val isPrimary: Boolean = false
)

@Composable
fun ActionButtonRow(
    buttons: List<ActionButtonConfig>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CoreSpacing.buttonSpacing)
    ) {
        buttons.forEach { button ->
            if (button.isPrimary) {
                Button(
                    onClick = button.onClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(button.text)
                }
            } else {
                OutlinedButton(
                    onClick = button.onClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(button.text)
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    if (isPrimary) {
        Button(
            onClick = onClick,
            modifier = modifier
        ) {
            Text(text)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
        ) {
            Text(text)
        }
    }
}