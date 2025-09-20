package dev.sadakat.screentimetracker.core.presentation.ui.focus.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sadakat.screentimetracker.core.presentation.ui.theme.LimeGreen
import dev.sadakat.screentimetracker.core.presentation.ui.theme.SkyBlue
import dev.sadakat.screentimetracker.core.presentation.ui.theme.VibrantOrange

@Composable
fun FocusDurationDialog(
    selectedDuration: Int,
    onDurationChanged: (Int) -> Unit,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var customDuration by remember { mutableStateOf("") }
    var showCustomInput by remember { mutableStateOf(false) }

    val presetDurations = listOf(
        15 to "Quick Focus",
        25 to "Pomodoro",
        45 to "Deep Work",
        60 to "Power Hour",
        90 to "Flow State",
        120 to "Marathon"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    "🧘 Start Focus Session",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Choose your focus duration",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Preset duration grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(presetDurations) { (minutes, label) ->
                        DurationCard(
                            minutes = minutes,
                            label = label,
                            isSelected = selectedDuration == minutes && !showCustomInput,
                            onClick = {
                                onDurationChanged(minutes)
                                showCustomInput = false
                            }
                        )
                    }
                }

                // Custom duration option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCustomInput = !showCustomInput },
                    colors = CardDefaults.cardColors(
                        containerColor = if (showCustomInput) 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else 
                            MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (showCustomInput) 4.dp else 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "⚙️ Custom",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (showCustomInput) {
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = customDuration,
                                onValueChange = { 
                                    customDuration = it
                                    it.toIntOrNull()?.let { duration ->
                                        onDurationChanged(duration)
                                    }
                                },
                                label = { Text("Minutes") },
                                modifier = Modifier.width(120.dp),
                                singleLine = true
                            )
                        }
                    }
                }

                // Session info
                if (selectedDuration > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = LimeGreen.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "📱 Apps will be blocked for",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            Text(
                                "$selectedDuration minutes",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedDuration) },
                enabled = selectedDuration > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LimeGreen,
                    contentColor = Color.White
                )
            ) {
                Text("Start Focus 🚀")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DurationCard(
    minutes: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        minutes <= 25 -> SkyBlue.copy(alpha = 0.1f)
        minutes <= 60 -> VibrantOrange.copy(alpha = 0.1f)
        else -> LimeGreen.copy(alpha = 0.1f)
    }
    
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(
                            borderColor.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        )
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "${minutes}m",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary 
                           else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    label,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}