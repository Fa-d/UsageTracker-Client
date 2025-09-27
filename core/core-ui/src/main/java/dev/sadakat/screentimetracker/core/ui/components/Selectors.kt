package dev.sadakat.screentimetracker.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.sadakat.screentimetracker.core.ui.theme.CoreSpacing

data class SelectorItem<T>(
    val value: T,
    val label: String
)

@Composable
fun <T> ChipSelector(
    items: List<SelectorItem<T>>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CoreSpacing.minorSpacing)
    ) {
        items.forEach { item ->
            FilterChip(
                onClick = { onItemSelected(item.value) },
                label = { Text(item.label) },
                selected = selectedItem == item.value
            )
        }
    }
}

@Composable
fun <T> TabSelector(
    items: List<SelectorItem<T>>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = items.indexOfFirst { it.value == selectedItem }

    ScrollableTabRow(
        selectedTabIndex = selectedIndex.coerceAtLeast(0),
        modifier = modifier
    ) {
        items.forEach { item ->
            Tab(
                selected = selectedItem == item.value,
                onClick = { onItemSelected(item.value) },
                text = { Text(item.label) }
            )
        }
    }
}