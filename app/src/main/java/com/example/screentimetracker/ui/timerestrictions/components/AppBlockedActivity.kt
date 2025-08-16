package com.example.screentimetracker.ui.timerestrictions.components

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.example.screentimetracker.services.AppBlockingService
import com.example.screentimetracker.ui.MainActivity
import com.example.screentimetracker.ui.replacementactivities.screens.ReplacementActivitiesScreen
import com.example.screentimetracker.ui.theme.ScreenTimeTrackerTheme
import com.example.screentimetracker.ui.timerestrictions.viewmodels.TimeRestrictionsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AppBlockedActivity : ComponentActivity() {

    private val timeRestrictionsViewModel: TimeRestrictionsViewModel by viewModels()
    private var blockedAppPackage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        blockedAppPackage = intent.getStringExtra("blocked_app_package")
        
        setContent {
            ScreenTimeTrackerTheme {
                val uiState by timeRestrictionsViewModel.uiState
                
                blockedAppPackage?.let { packageName ->
                    val appName = getAppName(packageName)
                    val activeRestrictions = uiState.currentActiveRestrictions
                    val timeUntilUnblocked = calculateTimeUntilUnblocked(activeRestrictions)
                    
                    AppBlockedOverlay(
                        appName = appName,
                        packageName = packageName,
                        activeRestrictions = activeRestrictions,
                        timeUntilUnblocked = timeUntilUnblocked,
                        onEmergencyOverride = {
                            handleEmergencyOverride(packageName)
                        },
                        onClose = {
                            goToHome()
                        },
                        onOpenAlternative = {
                            openAlternativeActivities()
                        }
                    )
                }
            }
        }
        
        // Load current active restrictions
        lifecycleScope.launch {
            timeRestrictionsViewModel.getCurrentActiveRestrictions()
        }
    }

    private fun getAppName(packageName: String): String {
        return try {
            val pm = packageManager
            val appInfo: ApplicationInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    private fun calculateTimeUntilUnblocked(activeRestrictions: List<com.example.screentimetracker.data.local.TimeRestriction>): String? {
        if (activeRestrictions.isEmpty()) return null
        
        // Get the earliest end time from active restrictions
        val earliestEndTime = activeRestrictions.minOfOrNull { restriction ->
            val statusPreview = timeRestrictionsViewModel.getRestrictionStatusPreview(restriction)
            statusPreview.timeUntilChange ?: Int.MAX_VALUE
        }
        
        return earliestEndTime?.let { minutes ->
            if (minutes < Int.MAX_VALUE) {
                timeRestrictionsViewModel.formatTimeUntil(minutes)
            } else null
        }
    }

    private fun handleEmergencyOverride(packageName: String) {
        // Grant temporary emergency access
        val appBlockingService = Intent(this, AppBlockingService::class.java)
        // In a real implementation, you would communicate with the service
        // For now, we'll just close the overlay and let the user access the app
        
        // Log the emergency override usage
        lifecycleScope.launch {
            // Could store emergency override usage for analytics
        }
        
        finish() // Close the blocking overlay
    }

    private fun goToHome() {
        val homeIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(homeIntent)
        finish()
    }

    private fun openAlternativeActivities() {
        val alternativeIntent = Intent(this, AlternativeActivitiesActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(alternativeIntent)
        finish()
    }

    @Suppress("DEPRECATION", "MissingSuperCall")
    override fun onBackPressed() {
        // Prevent back button from bypassing the restriction
        // Don't call super.onBackPressed() to prevent bypassing the restriction
        goToHome()
    }
}

@AndroidEntryPoint
class AlternativeActivitiesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            ScreenTimeTrackerTheme {
                ReplacementActivitiesScreen(
                    onBackPressed = {
                        finish()
                    }
                )
            }
        }
    }
}

// Extension function to make intents
fun Context.createAppBlockedIntent(packageName: String): Intent {
    return Intent(this, AppBlockedActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        putExtra("blocked_app_package", packageName)
    }
}