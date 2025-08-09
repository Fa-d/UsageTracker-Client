package com.example.screentimetracker.ui.timerestrictions.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.screentimetracker.ui.theme.ColorfulPrimary
import com.example.screentimetracker.ui.theme.ColorfulSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeRestrictionCreateDialog(
    onDismiss: () -> Unit,
    onCreateRestriction: (
        name: String,
        description: String,
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        selectedApps: List<String>,
        selectedDays: List<Int>,
        allowEmergencyApps: Boolean,
        showNotifications: Boolean
    ) -> Unit,
    isCreating: Boolean
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var allowEmergencyApps by remember { mutableStateOf(true) }
    var showNotifications by remember { mutableStateOf(true) }
    var selectedDays by remember { mutableStateOf(setOf(0, 1, 2, 3, 4, 5, 6)) } // All days selected by default
    var selectedApps by remember { mutableStateOf(emptyList<String>()) }
    
    val startTimePickerState = rememberTimePickerState(
        initialHour = 22,
        initialMinute = 0
    )
    val endTimePickerState = rememberTimePickerState(
        initialHour = 8,
        initialMinute = 0
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Create Time Restriction",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name and Description
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Restriction Name") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., Evening Focus Time") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., Block social apps during evening study") },
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Time Selection
                Text(
                    "Time Period",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Start Time",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TimePickerCompact(
                            timePickerState = startTimePickerState
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "End Time",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TimePickerCompact(
                            timePickerState = endTimePickerState
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Days of Week Selection
                Text(
                    "Active Days",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                DayOfWeekSelector(
                    selectedDays = selectedDays,
                    onDaysChanged = { selectedDays = it }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // App Selection Section
                Text(
                    "Blocked Apps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                AppSelectionSection(
                    selectedApps = selectedApps,
                    onAppsChanged = { selectedApps = it }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Settings
                Text(
                    "Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Allow Emergency Apps",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Keep calls, messages accessible",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = allowEmergencyApps,
                        onCheckedChange = { allowEmergencyApps = it }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Show Notifications",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Notify when restriction starts/ends",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = showNotifications,
                        onCheckedChange = { showNotifications = it }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onCreateRestriction(
                                    name,
                                    description,
                                    startTimePickerState.hour,
                                    startTimePickerState.minute,
                                    endTimePickerState.hour,
                                    endTimePickerState.minute,
                                    selectedApps,
                                    selectedDays.toList(),
                                    allowEmergencyApps,
                                    showNotifications
                                )
                            }
                        },
                        enabled = name.isNotBlank() && !isCreating,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isCreating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isCreating) "Creating..." else "Create")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerCompact(
    timePickerState: TimePickerState
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
fun DayOfWeekSelector(
    selectedDays: Set<Int>,
    onDaysChanged: (Set<Int>) -> Unit
) {
    val daysOfWeek = listOf(
        0 to "Sun", 1 to "Mon", 2 to "Tue", 3 to "Wed", 
        4 to "Thu", 5 to "Fri", 6 to "Sat"
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(daysOfWeek) { (dayIndex, dayName) ->
            val isSelected = selectedDays.contains(dayIndex)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) ColorfulPrimary 
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable {
                        val newSelection = if (isSelected) {
                            selectedDays - dayIndex
                        } else {
                            selectedDays + dayIndex
                        }
                        onDaysChanged(newSelection)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    dayName,
                    color = if (isSelected) Color.White 
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun AppSelectionSection(
    selectedApps: List<String>,
    onAppsChanged: (List<String>) -> Unit
) {
    val commonApps = listOf(
        "com.instagram.android" to "Instagram",
        "com.twitter.android" to "Twitter",
        "com.facebook.katana" to "Facebook", 
        "com.snapchat.android" to "Snapchat",
        "com.zhiliaoapp.musically" to "TikTok",
        "com.netflix.mediaclient" to "Netflix",
        "com.spotify.music" to "Spotify",
        "com.google.android.youtube" to "YouTube"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selectedApps.isEmpty(),
                    onCheckedChange = { blockAll ->
                        if (blockAll) {
                            onAppsChanged(emptyList()) // Empty list means block all
                        } else {
                            onAppsChanged(commonApps.map { it.first }) // Select all common apps
                        }
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Block All Apps (except emergency)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            if (selectedApps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Or select specific apps to block:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                commonApps.chunked(2).forEach { appPair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        appPair.forEach { (packageName, displayName) ->
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val newSelection = if (selectedApps.contains(packageName)) {
                                            selectedApps - packageName
                                        } else {
                                            selectedApps + packageName
                                        }
                                        onAppsChanged(newSelection)
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedApps.contains(packageName),
                                    onCheckedChange = null
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}