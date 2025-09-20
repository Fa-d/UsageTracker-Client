package dev.sadakat.screentimetracker.core.presentation.ui.timerestrictions.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.sadakat.screentimetracker.core.presentation.ui.theme.ColorfulPrimary
import dev.sadakat.screentimetracker.core.presentation.ui.theme.ColorfulSecondary
import dev.sadakat.screentimetracker.core.presentation.ui.timerestrictions.components.TimeRestrictionCreateDialog
import dev.sadakat.screentimetracker.core.presentation.ui.timerestrictions.viewmodels.RestrictionStatusPreview
import dev.sadakat.screentimetracker.core.presentation.ui.timerestrictions.viewmodels.TimeRestrictionsUiEvent
import dev.sadakat.screentimetracker.core.presentation.ui.timerestrictions.viewmodels.TimeRestrictionsViewModel
import dev.sadakat.screentimetracker.data.local.entities.TimeRestriction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeRestrictionsScreen(
    viewModel: TimeRestrictionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is TimeRestrictionsUiEvent.ShowSuccess -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is TimeRestrictionsUiEvent.CloseCreateDialog -> {
                    viewModel.hideCreateDialog()
                }
                is TimeRestrictionsUiEvent.NavigateToAppSelection -> {
                    // Handle navigation to app selection
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getCurrentActiveRestrictions()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Time Restrictions",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateDialog() },
                containerColor = ColorfulPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Restriction")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ColorfulPrimary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Current Active Restrictions Section
                    if (uiState.currentActiveRestrictions.isNotEmpty()) {
                        item {
                            ActiveRestrictionsCard(
                                activeRestrictions = uiState.currentActiveRestrictions,
                                viewModel = viewModel
                            )
                        }
                    }

                    // Quick Setup Section
                    item {
                        QuickSetupCard(
                            onCreateDefaults = { viewModel.createDefaultRestrictions() },
                            isCreatingDefaults = uiState.isCreatingDefaults
                        )
                    }

                    // All Restrictions Section
                    if (uiState.restrictions.isNotEmpty()) {
                        item {
                            Text(
                                "All Restrictions",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(uiState.restrictions) { restriction ->
                            TimeRestrictionCard(
                                restriction = restriction,
                                statusPreview = viewModel.getRestrictionStatusPreview(restriction),
                                onToggle = { viewModel.toggleRestriction(restriction) },
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }

            // Create Dialog
            if (uiState.showCreateDialog) {
                TimeRestrictionCreateDialog(
                    onDismiss = { viewModel.hideCreateDialog() },
                    onCreateRestriction = { name, description, startHour, startMinute, endHour, endMinute, apps, days, emergencyApps, notifications ->
                        viewModel.createCustomRestriction(
                            name, description, startHour, startMinute, endHour, endMinute, 
                            apps, days, emergencyApps, notifications
                        )
                    },
                    isCreating = uiState.isCreatingCustom
                )
            }

            // Error handling
            uiState.error?.let { error ->
                LaunchedEffect(error) {
                    snackbarHostState.showSnackbar("Error: $error")
                    viewModel.clearError()
                }
            }
        }
    }
}

@Composable
fun ActiveRestrictionsCard(
    activeRestrictions: List<TimeRestriction>,
    viewModel: TimeRestrictionsViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ColorfulPrimary.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Block,
                    contentDescription = null,
                    tint = ColorfulPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Currently Active",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ColorfulPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            activeRestrictions.forEach { restriction ->
                val statusPreview = viewModel.getRestrictionStatusPreview(restriction)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            restriction.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        statusPreview.timeUntilChange?.let { timeLeft ->
                            Text(
                                "Ends in ${viewModel.formatTimeUntil(timeLeft)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Active",
                        tint = Color.Green,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickSetupCard(
    onCreateDefaults: () -> Unit,
    isCreatingDefaults: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = ColorfulSecondary.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    tint = ColorfulSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Quick Setup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ColorfulSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Create recommended time restrictions for digital wellness:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                "• Digital Sunset (Bedtime protection)\n• Work Focus Mode\n• Morning Routine\n• Mindful Meals",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onCreateDefaults,
                enabled = !isCreatingDefaults,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isCreatingDefaults) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isCreatingDefaults) "Creating..." else "Create Default Restrictions")
            }
        }
    }
}

@Composable
fun TimeRestrictionCard(
    restriction: TimeRestriction, statusPreview: RestrictionStatusPreview,
    onToggle: () -> Unit,
    viewModel: TimeRestrictionsViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (statusPreview.isCurrentlyActive) {
                ColorfulPrimary.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            restriction.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (statusPreview.isCurrentlyActive) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        Color.Green.copy(alpha = 0.2f), CircleShape
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "ACTIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Green,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Text(
                        restriction.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = restriction.isEnabled,
                    onCheckedChange = { onToggle() }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RestrictionInfoChip(
                    icon = Icons.Default.Schedule,
                    text = "${viewModel.formatTime(restriction.startTimeMinutes)} - ${viewModel.formatTime(restriction.endTimeMinutes)}"
                )
                
                val daysText = restriction.daysOfWeek.split(",").size.let { count ->
                    when (count) {
                        7 -> "Every day"
                        5 -> if (restriction.daysOfWeek.contains("1,2,3,4,5")) "Weekdays" else "$count days"
                        2 -> if (restriction.daysOfWeek.contains("0,6")) "Weekends" else "$count days"
                        else -> "$count days"
                    }
                }
                RestrictionInfoChip(
                    icon = Icons.Default.Info,
                    text = daysText
                )
            }
            
            if (statusPreview.isCurrentlyActive) {
                Spacer(modifier = Modifier.height(8.dp))
                statusPreview.timeUntilChange?.let { timeLeft ->
                    Text(
                        "⏱️ Ends in ${viewModel.formatTimeUntil(timeLeft)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorfulPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else if (restriction.isEnabled) {
                statusPreview.timeUntilChange?.let { timeLeft ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "⏰ Starts in ${viewModel.formatTimeUntil(timeLeft)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun RestrictionInfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}