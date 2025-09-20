package dev.sadakat.screentimetracker.core.presentation.ui.permissions

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import dev.sadakat.screentimetracker.core.presentation.ui.common.error.AppError
import dev.sadakat.screentimetracker.core.presentation.ui.common.error.Result
import dev.sadakat.screentimetracker.core.presentation.ui.common.error.ErrorSnackbar
import dev.sadakat.screentimetracker.core.domain.permissions.PermissionManager
import dev.sadakat.screentimetracker.core.domain.service.PermissionState
import dev.sadakat.screentimetracker.core.domain.error.DomainResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionScreen(
    permissionState: PermissionState,
    permissionManager: PermissionManager
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var currentError by remember { mutableStateOf<AppError?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Refresh permissions when user returns from settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch {
                    permissionManager.checkAllPermissions()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
                        when (val result = permissionManager.requestUsageStatsPermission()) {
                            is DomainResult.Failure -> {
                                currentError = AppError.PermissionError("Failed to request usage stats permission", result.error)
                            }
                            is DomainResult.Success -> {
                                // Permission request succeeded
                            }
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
                        when (val result = permissionManager.requestNotificationPermission()) {
                            is DomainResult.Failure -> {
                                currentError = AppError.PermissionError("Failed to request notification permission", result.error)
                            }
                            is DomainResult.Success -> {
                                // Permission request succeeded
                            }
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
                        when (val result = permissionManager.requestAccessibilityPermission()) {
                            is DomainResult.Failure -> {
                                currentError = AppError.PermissionError("Failed to request accessibility permission", result.error)
                            }
                            is DomainResult.Success -> {
                                // Permission request succeeded
                            }
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
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                
                Column(
                    modifier = Modifier.weight(1f) // This constrains the text and prevents it from pushing the button
                ) {
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2 // Limit description to 2 lines
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                if (isGranted) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                } else {
                    Button(
                        onClick = onRequestPermission,
                        colors = if (isRequired) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                        modifier = Modifier.wrapContentWidth() // Ensure button doesn't get compressed
                    ) {
                        Text("Grant")
                    }
                }
            }
        }
    }
}
