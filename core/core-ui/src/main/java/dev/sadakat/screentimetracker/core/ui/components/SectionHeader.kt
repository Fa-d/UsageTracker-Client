package dev.sadakat.screentimetracker.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.sadakat.screentimetracker.core.ui.theme.CoreTextStyles

@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = CoreTextStyles.sectionHeader,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}