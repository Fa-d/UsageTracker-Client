package com.example.screentimetracker.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.screentimetracker.domain.permissions.PermissionManager
import com.example.screentimetracker.domain.service.ServiceManager
import com.example.screentimetracker.ui.common.error.ErrorHandler
import com.example.screentimetracker.ui.permissions.PermissionScreen
import com.example.screentimetracker.ui.theme.ScreenTimeTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var serviceManager: ServiceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ScreenTimeTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent(
                        permissionManager = permissionManager,
                        serviceManager = serviceManager
                    )
                }
            }
        }
    }
}

@Composable
private fun AppContent(
    permissionManager: PermissionManager,
    serviceManager: ServiceManager
) {
    val permissionState by permissionManager.permissionState.collectAsState(
        initial = com.example.screentimetracker.domain.permissions.PermissionState()
    )
    val serviceState by serviceManager.serviceState.collectAsState(
        initial = com.example.screentimetracker.domain.service.ServiceState.Stopped
    )
    val dashboardViewModel: com.example.screentimetracker.ui.dashboard.viewmodels.DashboardViewModel = hiltViewModel()

    // Check permissions on startup
    LaunchedEffect(Unit) {
        permissionManager.checkAllPermissions()
    }

    // Start service when permissions are granted
    LaunchedEffect(permissionState.allRequiredPermissionsGranted) {
        if (permissionState.allRequiredPermissionsGranted && !serviceManager.isServiceRunning()) {
            serviceManager.startTrackingService()
        }
    }

    if (permissionState.allRequiredPermissionsGranted) {
        // Use existing ScreenTimeTracker component
        com.example.screentimetracker.ui.dashboard.utils.ScreenTimeTracker(dashboardViewModel)
    } else {
        // Permission request screen
        PermissionScreen(
            permissionState = permissionState,
            permissionManager = permissionManager
        )
    }
}
