package dev.sadakat.screentimetracker.core.presentation.ui.limiter.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.sadakat.screentimetracker.core.presentation.ui.limiter.viewmodels.InstalledAppViewItem
import dev.sadakat.screentimetracker.core.presentation.ui.limiter.viewmodels.LimitedAppViewItem
import dev.sadakat.screentimetracker.core.presentation.ui.limiter.viewmodels.LimiterConfigState
import dev.sadakat.screentimetracker.core.presentation.ui.limiter.viewmodels.LimiterConfigViewModel
import dev.sadakat.screentimetracker.core.presentation.ui.theme.ScreenTimeTrackerTheme
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimiterConfigScreen(
    viewModel: LimiterConfigViewModel = hiltViewModel()
) {
    val state by viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short,
                actionLabel = "Dismiss"
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Usage Limits") },
                // navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.onAddAppClicked() }) {
                Icon(Icons.Filled.Add, "Add new app limit")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()  // Add fillMaxSize to provide bounds
        ) {
            if (state.isLoading && state.limitedApps.isEmpty() && state.installedAppsForSelection.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            if (state.limitedApps.isEmpty() && !state.isLoading) {
                Text(
                    "No apps are currently limited. Tap '+' to add usage limits for specific apps.",
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f), // Use weight to constrain height properly
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.limitedApps) { app ->
                        LimitedAppRow(
                            app = app,
                            onRemove = { viewModel.onRemoveLimitedApp(app.packageName) },
                            onEdit = { viewModel.onEditAppClicked(app) }
                        )
                    }
                }
            }
        }

        if (state.showAppSelectionDialog) {
            AppLimitSettingDialog(
                installedApps = state.installedAppsForSelection,
                selectedApp = state.selectedAppForLimit,
                newLimitTimeMinutes = state.newLimitTimeInputMinutes,
                isLoadingApps = state.isLoading && state.selectedAppForLimit == null,
                isEditing = state.appBeingEdited != null,
                onAppSelected = { viewModel.onAppSelectedForLimiting(it) },
                onTimeChanged = { viewModel.onNewLimitTimeChanged(it) },
                onConfirm = { viewModel.onConfirmAddLimitedApp() },
                onDismiss = { viewModel.onDismissAppSelectionDialog() }
            )
        }
    }
}

@Composable
fun LimitedAppRow(
    app: LimitedAppViewItem,
    onRemove: () -> Unit,
    onEdit: (LimitedAppViewItem) -> Unit
) {
    val formattedLimit = remember(app.timeLimitMillis) {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(app.timeLimitMillis)
        "$minutes min"
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(app) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), // Adjusted padding
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(
                    LocalContext.current.packageManager.getApplicationIcon(
                        app.packageName
                    )
                ).crossfade(true).build(),
                contentDescription = "${app.appName} icon",
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(app.appName, style = MaterialTheme.typography.titleMedium)
                Text("Limit: $formattedLimit continuous usage", style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, "Remove limit")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLimitSettingDialog(
    installedApps: List<InstalledAppViewItem>,
    selectedApp: InstalledAppViewItem?,
    newLimitTimeMinutes: String,
    isLoadingApps: Boolean,
    isEditing: Boolean = false, // New parameter to indicate edit mode
    onAppSelected: (InstalledAppViewItem) -> Unit,
    onTimeChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) { // Ensure dialog takes reasonable width
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (isEditing) "Edit Limit for ${selectedApp?.appName}" else if (selectedApp == null) "Select App" else "Set Limit for ${selectedApp.appName}",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (selectedApp == null && !isEditing) { // Step 1: Select App (only for adding new)
                    if (isLoadingApps) {
                        CircularProgressIndicator(modifier = Modifier.padding(vertical = 16.dp))
                    } else if (installedApps.isEmpty()){
                        Text("No new apps available to limit.", modifier = Modifier.padding(vertical = 16.dp))
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) { // Limit height of app list
                            items(installedApps) { appInfo ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onAppSelected(appInfo) }
                                        .padding(vertical = 8.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current).data(
                                            LocalContext.current.packageManager.getApplicationIcon(
                                                appInfo.packageName
                                            )
                                        ).crossfade(true).build(),
                                        contentDescription = "${appInfo.appName} icon",
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Text(
                                        appInfo.appName, style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                } else { // Step 2: Set Time for selected App or App being edited
                    OutlinedTextField(
                        value = newLimitTimeMinutes,
                        onValueChange = onTimeChanged,
                        label = { Text("Continuous time limit (minutes)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(24.dp)) // Increased spacer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        enabled = selectedApp != null && (newLimitTimeMinutes.toLongOrNull() ?: 0) > 0
                    ) {
                        Text(if (isEditing) "Update" else "Confirm")
                    }
                }
            }
        }
    }
}

// --- Previews ---
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun LimiterConfigScreenPreview_Empty() {
    ScreenTimeTrackerTheme {
        val emptyState = LimiterConfigState(isLoading = false, limitedApps = emptyList())
        // LimiterConfigScreen() // Needs Hilt VM or a fake VM + state passed in
        Scaffold(
            topBar = { TopAppBar(title = { Text("App Usage Limits Preview") }) },
            floatingActionButton = { FloatingActionButton(onClick = {}) { Icon(Icons.Filled.Add, "Add") } }
        ) { padding -> Column(Modifier.padding(padding)) {
            Text(
                "No apps are currently limited. Tap '+' to add usage limits for specific apps.",
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )
        }}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun LimiterConfigScreenPreview_WithData() {
    ScreenTimeTrackerTheme {
         val sampleLimitedApps = listOf(
             LimitedAppViewItem("App A (Cool Game)", "com.appa", 5 * 60 * 1000L),
            LimitedAppViewItem("App B (Productivity)", "com.appb", 10 * 60 * 1000L)
        )
        // LimiterConfigScreen() // Needs Hilt VM or a fake VM + state passed in
         Scaffold(
            topBar = { TopAppBar(title = { Text("App Usage Limits Preview") }) },
            floatingActionButton = { FloatingActionButton(onClick = {}) { Icon(Icons.Filled.Add, "Add") } }
         ) { padding ->
             LazyColumn(
                 modifier = Modifier
                     .padding(padding)
                     .padding(16.dp),
                 verticalArrangement = Arrangement.spacedBy(8.dp)
             ) {
                items(sampleLimitedApps) { app ->
                    LimitedAppRow(app = app, onRemove = { }, onEdit = {})
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AppLimitSettingDialog_SelectAppPreview() {
    ScreenTimeTrackerTheme {
        AppLimitSettingDialog(
            installedApps = listOf(InstalledAppViewItem("Cool Game", "com.coolgame"), InstalledAppViewItem("Productivity Tool", "com.prodtool")),
            selectedApp = null,
            newLimitTimeMinutes = "10",
            isLoadingApps = false,
            onAppSelected = {},
            onTimeChanged = {},
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppLimitSettingDialog_SelectAppLoadingPreview() {
    ScreenTimeTrackerTheme {
        AppLimitSettingDialog(
            installedApps = emptyList(),
            selectedApp = null,
            newLimitTimeMinutes = "10",
            isLoadingApps = true,
            onAppSelected = {},
            onTimeChanged = {},
            onConfirm = {},
            onDismiss = {}
        )
    }
}


@Preview(showBackground = true)
@Composable
fun AppLimitSettingDialog_SetTimePreview() {
    ScreenTimeTrackerTheme {
        AppLimitSettingDialog(
            installedApps = emptyList(),
            selectedApp = InstalledAppViewItem("Cool Game", "com.coolgame"),
            newLimitTimeMinutes = "15",
            isLoadingApps = false,
            onAppSelected = {},
            onTimeChanged = {},
            onConfirm = {},
            onDismiss = {}
        )
    }
}
