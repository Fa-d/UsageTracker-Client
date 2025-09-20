package dev.sadakat.screentimetracker.core.presentation.ui.goals.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.sadakat.screentimetracker.core.domain.usecases.SmartGoalSettingUseCase
import java.util.concurrent.TimeUnit

@Composable
fun GoalCreationDialog(
    onDismiss: () -> Unit,
    onCreateGoal: (goalType: String, targetValue: Long) -> Unit
) {
    var selectedGoalType by remember { mutableStateOf(SmartGoalSettingUseCase.DAILY_SCREEN_TIME) }
    var targetHours by remember { mutableStateOf("6") }
    var targetMinutes by remember { mutableStateOf("0") }
    var targetUnlocks by remember { mutableStateOf("100") }
    var targetSessions by remember { mutableStateOf("2") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Create New Goal",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Choose a goal type and set your target:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                // Goal Type Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Goal Type",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    
                    GoalTypeOption(
                        title = "Daily Screen Time Limit",
                        description = "Set a maximum daily screen time",
                        emoji = "⏰",
                        isSelected = selectedGoalType == SmartGoalSettingUseCase.DAILY_SCREEN_TIME,
                        onClick = { selectedGoalType = SmartGoalSettingUseCase.DAILY_SCREEN_TIME }
                    )
                    
                    GoalTypeOption(
                        title = "Phone Unlock Limit",
                        description = "Limit how often you unlock your phone",
                        emoji = "📱",
                        isSelected = selectedGoalType == SmartGoalSettingUseCase.UNLOCK_FREQUENCY,
                        onClick = { selectedGoalType = SmartGoalSettingUseCase.UNLOCK_FREQUENCY }
                    )
                    
                    GoalTypeOption(
                        title = "Daily Focus Sessions",
                        description = "Complete focused work sessions",
                        emoji = "🎯",
                        isSelected = selectedGoalType == SmartGoalSettingUseCase.FOCUS_SESSIONS,
                        onClick = { selectedGoalType = SmartGoalSettingUseCase.FOCUS_SESSIONS }
                    )
                }
                
                // Target Value Input
                when (selectedGoalType) {
                    SmartGoalSettingUseCase.DAILY_SCREEN_TIME -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Daily Screen Time Limit",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = targetHours,
                                    onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 2) targetHours = it },
                                    label = { Text("Hours") },
                                    modifier = Modifier.width(80.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                Text("h")
                                OutlinedTextField(
                                    value = targetMinutes,
                                    onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 2) targetMinutes = it },
                                    label = { Text("Min") },
                                    modifier = Modifier.width(80.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                Text("m")
                            }
                        }
                    }
                    
                    SmartGoalSettingUseCase.UNLOCK_FREQUENCY -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Maximum Daily Unlocks",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                            OutlinedTextField(
                                value = targetUnlocks,
                                onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 3) targetUnlocks = it },
                                label = { Text("Max unlocks per day") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                supportingText = { Text("Recommended: 50-100 unlocks per day") }
                            )
                        }
                    }
                    
                    SmartGoalSettingUseCase.FOCUS_SESSIONS -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Daily Focus Sessions",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                            OutlinedTextField(
                                value = targetSessions,
                                onValueChange = { if (it.all { char -> char.isDigit() } && it.length <= 2) targetSessions = it },
                                label = { Text("Sessions per day") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                supportingText = { Text("Start with 1-3 sessions per day") }
                            )
                        }
                    }
                }
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val targetValue = when (selectedGoalType) {
                                SmartGoalSettingUseCase.DAILY_SCREEN_TIME -> {
                                    val hours = targetHours.toLongOrNull() ?: 0L
                                    val minutes = targetMinutes.toLongOrNull() ?: 0L
                                    TimeUnit.HOURS.toMillis(hours) + TimeUnit.MINUTES.toMillis(minutes)
                                }
                                SmartGoalSettingUseCase.UNLOCK_FREQUENCY -> {
                                    targetUnlocks.toLongOrNull() ?: 100L
                                }
                                SmartGoalSettingUseCase.FOCUS_SESSIONS -> {
                                    targetSessions.toLongOrNull() ?: 2L
                                }
                                else -> 0L
                            }
                            onCreateGoal(selectedGoalType, targetValue)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = when (selectedGoalType) {
                            SmartGoalSettingUseCase.DAILY_SCREEN_TIME -> targetHours.isNotBlank() || targetMinutes.isNotBlank()
                            SmartGoalSettingUseCase.UNLOCK_FREQUENCY -> targetUnlocks.isNotBlank()
                            SmartGoalSettingUseCase.FOCUS_SESSIONS -> targetSessions.isNotBlank()
                            else -> false
                        }
                    ) {
                        Text("Create Goal")
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalTypeOption(
    title: String,
    description: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineMedium
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}