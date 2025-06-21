package com.example.screentimetracker.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.screentimetracker.services.AppUsageTrackingService
import com.example.screentimetracker.ui.dashboard.DashboardViewModel
import com.example.screentimetracker.ui.dashboard.ScreenTimeTracker
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
                    val viewmodel = hiltViewModel<DashboardViewModel>()
                    PermissionWrapper()

                }
            }
        }
    }
}


@Composable
fun PermissionWrapper() {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(PermissionUtils.hasUsageStatsPermission(context)) }
    // State to control which screen is shown

    if (hasPermission) {
        LaunchedEffect(Unit) {
            context.startService(Intent(context, AppUsageTrackingService::class.java))
        }
        ScreenTimeTracker()

    } else {
        RequestUsagePermissionScreen {
            PermissionUtils.requestUsageStatsPermission(context)
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
