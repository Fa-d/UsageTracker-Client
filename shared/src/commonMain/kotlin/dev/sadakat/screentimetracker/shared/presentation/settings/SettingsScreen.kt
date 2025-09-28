package dev.sadakat.screentimetracker.shared.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sadakat.screentimetracker.shared.ui.components.*
import dev.sadakat.screentimetracker.shared.ui.theme.CoreSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onSettingChanged: (String, Any) -> Unit,
    onSettingClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ScreenContainer(
        title = "Settings",
        modifier = modifier
    ) {
        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            items(uiState.settingsSections) { section ->
                SettingsSection(
                    section = section,
                    onSettingChanged = onSettingChanged,
                    onSettingClicked = onSettingClicked
                )
            }
        }

        uiState.error?.let { error ->
            item {
                ErrorCard(
                    message = error,
                    onRetry = { /* TODO: Add retry functionality */ }
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    section: SettingsSection,
    onSettingChanged: (String, Any) -> Unit,
    onSettingClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    MetricCard(
        title = section.title,
        modifier = modifier
    ) {
        section.items.forEach { item ->
            when (item.type) {
                SettingType.SWITCH -> {
                    SwitchSettingItem(
                        item = item,
                        onChanged = { onSettingChanged(item.key, it) }
                    )
                }
                SettingType.SLIDER -> {
                    SliderSettingItem(
                        item = item,
                        onChanged = { onSettingChanged(item.key, it) }
                    )
                }
                SettingType.DROPDOWN -> {
                    DropdownSettingItem(
                        item = item,
                        onChanged = { onSettingChanged(item.key, it) }
                    )
                }
                SettingType.ACTION -> {
                    ActionSettingItem(
                        item = item,
                        onClick = { onSettingClicked(item.key) }
                    )
                }
                SettingType.INFO -> {
                    InfoSettingItem(item = item)
                }
            }

            if (item != section.items.last()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = CoreSpacing.minorSpacing))
            }
        }
    }
}

@Composable
private fun SwitchSettingItem(
    item: SettingItem,
    onChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = CoreSpacing.minorSpacing),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (item.subtitle.isNotEmpty()) {
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = item.booleanValue ?: false,
            onCheckedChange = onChanged
        )
    }
}

@Composable
private fun SliderSettingItem(
    item: SettingItem,
    onChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${(item.floatValue ?: 0f).toInt()}${item.unit}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (item.subtitle.isNotEmpty()) {
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Slider(
            value = item.floatValue ?: 0f,
            onValueChange = onChanged,
            valueRange = (item.range?.first ?: 0f)..(item.range?.second ?: 100f),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSettingItem(
    item: SettingItem,
    onChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        if (item.subtitle.isNotEmpty()) {
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = item.value ?: "",
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                item.options?.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onChanged(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionSettingItem(
    item: SettingItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (item.subtitle.isNotEmpty()) {
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        TextButton(onClick = onClick) {
            Text("Open")
        }
    }
}

@Composable
private fun InfoSettingItem(
    item: SettingItem,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = CoreSpacing.minorSpacing),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (item.subtitle.isNotEmpty()) {
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = item.value ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}