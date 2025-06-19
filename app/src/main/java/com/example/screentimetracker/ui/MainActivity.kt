package com.example.screentimetracker.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.screentimetracker.services.AppUsageTrackingService
import com.example.screentimetracker.ui.dashboard.DashboardScreen // Import DashboardScreen
import com.example.screentimetracker.ui.theme.ScreenTimeTrackerTheme
import com.example.screentimetracker.utils.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Service starting logic will be conditional based on permission
        // setContent moved to onResume to re-evaluate permission
    }

    override fun onResume() {
        super.onResume()
        // Check permission every time the activity resumes,
        // as user might grant/revoke it while app is in background.
        setContent {
            ScreenTimeTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PermissionWrapper()
                }
            }
        }
    }
}

@Composable
fun PermissionWrapper() {
    val context = LocalContext.current
    // State to trigger recomposition when permission status might have changed
    var hasPermission by remember { mutableStateOf(PermissionUtils.hasUsageStatsPermission(context)) }

    if (hasPermission) {
        // Start the service only if permission is granted
        LaunchedEffect(Unit) { // Ensure service is started once when permission is true
            context.startService(Intent(context, AppUsageTrackingService::class.java))
        }
        // --- Replace Greeting with DashboardScreen ---
        DashboardScreen()
        // --- End of change ---
    } else {
        RequestUsagePermissionScreen {
            PermissionUtils.requestUsageStatsPermission(context)
            // Note: We don't automatically get the result here.
            // The check in onResume will update the UI when the user returns to the app.
            // To refresh immediately after returning from settings, you might need a lifecycle observer
            // or ActivityResultLauncher, but onResume is a simpler robust approach for this step.
        }
    }
}

@Composable
fun RequestUsagePermissionScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Usage Access Permission Required",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "This app needs access to your app usage data to track screen time. Please grant the permission in the system settings.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text("Open Settings")
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) { // This can be removed if not used elsewhere
    Text(
        text = "Hello $name! Main app content.",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() { // Preview for MainActivity
    ScreenTimeTrackerTheme {
        // This preview will show the permission request screen if permissions are not "granted"
        // in the preview environment, or the DashboardScreen if they are.
        // For a stable preview of permission request:
        RequestUsagePermissionScreen {}
        // For a stable preview of what would be shown if permission granted (Dashboard):
        // DashboardScreen() // This would require a Hilt ViewModel setup for preview or a dummy ViewModel
    }
}
