package dev.sadakat.screentimetracker.ui

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sadakat.screentimetracker.data.local.entities.ThemeMode
import dev.sadakat.screentimetracker.data.local.entities.ColorScheme
import dev.sadakat.screentimetracker.domain.permissions.PermissionManager
import dev.sadakat.screentimetracker.domain.service.ServiceManager
import dev.sadakat.screentimetracker.ui.permissions.PermissionScreen
import dev.sadakat.screentimetracker.ui.personalization.PersonalizationViewModel
import dev.sadakat.screentimetracker.ui.theme.ScreenTimeTrackerTheme
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
            val personalizationViewModel: PersonalizationViewModel = hiltViewModel()
            val personalizationState by personalizationViewModel.uiState.collectAsStateWithLifecycle()
            
            ScreenTimeTrackerTheme(
                themeMode = ThemeMode.valueOf(personalizationState.preferences.themeMode),
                colorScheme = ColorScheme.valueOf(personalizationState.preferences.colorScheme)
            ) {
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
        initial = dev.sadakat.screentimetracker.domain.permissions.PermissionState()
    )
    val dashboardViewModel: dev.sadakat.screentimetracker.ui.dashboard.viewmodels.DashboardViewModel = hiltViewModel()

    // Check permissions on startup
    LaunchedEffect(Unit) {
        permissionManager.checkAllPermissions()
    }

    val context = LocalContext.current
    
    // Start service when permissions are granted
    LaunchedEffect(permissionState.allRequiredPermissionsGranted) {
        if (permissionState.allRequiredPermissionsGranted && !serviceManager.isServiceRunning()) {
            serviceManager.startTrackingService()
            // Also start the smart tracking service for usage limits
            dev.sadakat.screentimetracker.services.SmartUsageTrackingService.ServiceController.startSmartTracking(context)
        }
    }

    if (permissionState.allRequiredPermissionsGranted) {
        // Use existing ScreenTimeTracker component
        dev.sadakat.screentimetracker.ui.dashboard.utils.ScreenTimeTracker(dashboardViewModel)
    } else {
        // Permission request screen
        PermissionScreen(
            permissionState = permissionState,
            permissionManager = permissionManager
        )
    }
}
