package com.example.screentimetracker.ui.permissions

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.screentimetracker.domain.permissions.PermissionManager
import com.example.screentimetracker.domain.permissions.PermissionState
import com.example.screentimetracker.ui.common.error.AppError
import com.example.screentimetracker.ui.common.error.ErrorSnackbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionScreen(
    permissionState: PermissionState,
    permissionManager: PermissionManager
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var currentError by remember { mutableStateOf<AppError?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Permissions Required") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Screen Time Tracker needs some permissions to work properly",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Please grant the following permissions to continue",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Permission Cards
            PermissionCard(
                title = "Usage Access",
                description = "Required to track app usage statistics",
                icon = Icons.Default.Security,
                isGranted = permissionState.hasUsageStatsPermission,
                isRequired = true,
                onRequestPermission = {
                    coroutineScope.launch {
                        val result = permissionManager.requestUsageStatsPermission()
                        result.onError { error ->
                            currentError = error
                        }
                        // Recheck permissions after request
                        permissionManager.checkAllPermissions()
                    }
                }
            )

            PermissionCard(
                title = "Notifications",
                description = "Required for app limit alerts and wellness reminders",
                icon = Icons.Default.Notifications,
                isGranted = permissionState.hasNotificationPermission,
                isRequired = true,
                onRequestPermission = {
                    coroutineScope.launch {
                        val result = permissionManager.requestNotificationPermission()
                        result.onError { error ->
                            currentError = error
                        }
                        // Recheck permissions after request
                        permissionManager.checkAllPermissions()
                    }
                }
            )

            PermissionCard(
                title = "Accessibility Service",
                description = "Optional: Enhanced app blocking and interaction tracking",
                icon = Icons.Default.Accessibility,
                isGranted = permissionState.hasAccessibilityPermission,
                isRequired = false,
                onRequestPermission = {
                    coroutineScope.launch {
                        val result = permissionManager.requestAccessibilityPermission()
                        result.onError { error ->
                            currentError = error
                        }
                        // Recheck permissions after request
                        permissionManager.checkAllPermissions()
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Progress indicator
            if (permissionState.allRequiredPermissionsGranted) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Starting Screen Time Tracker...")
                    }
                }
            }
        }
    }

    // Error handling
    ErrorSnackbar(
        error = currentError,
        snackbarHostState = snackbarHostState,
        onDismiss = { currentError = null }
    )
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isGranted: Boolean,
    isRequired: Boolean,
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted)
                MaterialTheme.colorScheme.tertiaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(24.dp),
                        tint = if (isGranted)
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (isRequired) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "*",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isGranted) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                } else {
                    Button(
                        onClick = onRequestPermission,
                        colors = if (isRequired) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("Grant")
                    }
                }
            }
        }
    }
}
