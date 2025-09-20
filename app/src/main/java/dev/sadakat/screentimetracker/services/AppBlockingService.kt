package dev.sadakat.screentimetracker.services

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.sadakat.screentimetracker.R
import dev.sadakat.screentimetracker.core.presentation.ui.timerestrictions.components.AppBlockedActivity
import dev.sadakat.screentimetracker.domain.usecases.FocusSessionManagerUseCase
import dev.sadakat.screentimetracker.domain.usecases.TimeRestrictionManagerUseCase
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AppBlockingService : Service() {

    @Inject
    lateinit var timeRestrictionManager: TimeRestrictionManagerUseCase

    @Inject
    lateinit var focusSessionManager: FocusSessionManagerUseCase

    @Inject
    lateinit var appLogger: AppLogger

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private val handler = Handler(Looper.getMainLooper())
    private var monitoringRunnable: Runnable? = null

    private var lastCheckedApp: String? = null
    private var emergencyOverrides = mutableSetOf<String>()

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val MONITORING_INTERVAL = 1000L // Check every second
        private const val EMERGENCY_OVERRIDE_DURATION = 10 * 60 * 1000L // 10 minutes

        fun start(context: Context) {
            val intent = Intent(context, AppBlockingService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, AppBlockingService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        appLogger.i("AppBlockingService", "Service created")
        startForegroundService()
        startAppMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        appLogger.i("AppBlockingService", "Service started")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopAppMonitoring()
        appLogger.i("AppBlockingService", "Service destroyed")
    }

    private fun startForegroundService() {
        serviceScope.launch {
            val isFocusModeActive = focusSessionManager.isSessionActive()
            val notificationTitle = if (isFocusModeActive) {
                "🧘 Focus Mode Active"
            } else {
                "Time Restrictions Active"
            }
            val notificationText = if (isFocusModeActive) {
                "Blocking distracting apps during focus session"
            } else {
                "Monitoring app usage for restrictions"
            }

            val notification = NotificationCompat.Builder(this@AppBlockingService, "app_blocking_channel")
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()

            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startAppMonitoring() {
        monitoringRunnable = object : Runnable {
            override fun run() {
                checkCurrentApp()
                handler.postDelayed(this, MONITORING_INTERVAL)
            }
        }
        handler.post(monitoringRunnable!!)
    }

    private fun stopAppMonitoring() {
        monitoringRunnable?.let { runnable ->
            handler.removeCallbacks(runnable)
        }
    }

    private fun checkCurrentApp() {
        serviceScope.launch {
            try {
                val currentApp = getCurrentForegroundApp()

                if (currentApp != null && currentApp != lastCheckedApp && currentApp != packageName) {
                    lastCheckedApp = currentApp

                    // Skip if emergency override is active
                    if (emergencyOverrides.contains(currentApp)) {
                        return@launch
                    }

                    // Check both time restrictions and focus mode
                    val isBlockedByTimeRestriction = timeRestrictionManager.isAppBlockedByTimeRestriction(currentApp)
                    val isBlockedByFocusMode = focusSessionManager.isAppBlocked(currentApp)

                    if (isBlockedByTimeRestriction || isBlockedByFocusMode) {
                        val blockReason = when {
                            isBlockedByFocusMode -> "Focus session active"
                            isBlockedByTimeRestriction -> "Time restriction active"
                            else -> "App blocked"
                        }

                        appLogger.d("AppBlockingService", "Blocking app: $currentApp - $blockReason")
                        showBlockedAppScreen(currentApp, blockReason)

                        // Record interruption if it's during focus mode
                        if (isBlockedByFocusMode) {
                            serviceScope.launch {
                                // This could be improved to integrate with the FocusModeViewModel
                                // For now, we'll just log it
                                appLogger.w("AppBlockingService", "Focus session interrupted by attempt to open $currentApp")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                appLogger.e("AppBlockingService", "Error checking current app", e)
            }
        }
    }

    private fun getCurrentForegroundApp(): String? {
        return try {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningApps = activityManager.runningAppProcesses

            runningApps?.firstOrNull {
                it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            }?.processName
        } catch (e: Exception) {
            appLogger.e("AppBlockingService", "Error getting foreground app", e)
            null
        }
    }

    private fun showBlockedAppScreen(packageName: String, reason: String = "App blocked") {
        try {
            val intent = Intent(this, AppBlockedActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("blocked_app_package", packageName)
                putExtra("block_reason", reason)
            }
            startActivity(intent)
        } catch (e: Exception) {
            appLogger.e("AppBlockingService", "Error showing blocked app screen", e)
        }
    }

    fun addEmergencyOverride(packageName: String) {
        emergencyOverrides.add(packageName)

        // Remove override after specified duration
        handler.postDelayed({
            emergencyOverrides.remove(packageName)
            appLogger.i("AppBlockingService", "Emergency override expired for $packageName")
        }, EMERGENCY_OVERRIDE_DURATION)

        appLogger.i("AppBlockingService", "Emergency override granted for $packageName")
    }

    fun removeEmergencyOverride(packageName: String) {
        emergencyOverrides.remove(packageName)
        appLogger.i("AppBlockingService", "Emergency override removed for $packageName")
    }

    fun isEmergencyOverrideActive(packageName: String): Boolean {
        return emergencyOverrides.contains(packageName)
    }
}