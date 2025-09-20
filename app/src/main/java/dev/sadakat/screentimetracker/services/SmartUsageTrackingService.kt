package dev.sadakat.screentimetracker.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.sadakat.screentimetracker.R
import dev.sadakat.screentimetracker.core.presentation.ui.MainActivity
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import dev.sadakat.screentimetracker.services.limiter.AppUsageLimiter
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmartUsageTrackingService : Service() {

    @Inject
    lateinit var repository: TrackerRepository
    @Inject
    lateinit var appUsageLimiter: AppUsageLimiter
    @Inject
    lateinit var usageStatsPoller: UsageStatsPoller
    @Inject
    lateinit var appLogger: AppLogger

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private var trackingJob: Job? = null
    private var isTrackingActive = false
    private var isUsagePollingActive = false

    companion object {
        private const val TAG = "SmartTrackingService"
        const val NOTIFICATION_CHANNEL_ID = "SmartTrackingChannel"
        private const val FOREGROUND_NOTIFICATION_ID = 2
        const val ACTION_START_SMART_TRACKING = "dev.sadakat.screentimetracker.ACTION_START_SMART_TRACKING"
        const val ACTION_STOP_SMART_TRACKING = "dev.sadakat.screentimetracker.ACTION_STOP_SMART_TRACKING"
        const val ACTION_CHECK_LIMITS = "dev.sadakat.screentimetracker.ACTION_CHECK_LIMITS"

        // Smart polling intervals
        private const val ACTIVE_POLLING_INTERVAL_MS = 30_000L // 30 seconds for active limited apps
        private const val BACKGROUND_POLLING_INTERVAL_MS = 300_000L // 5 minutes for background checks
    }

    override fun onCreate() {
        super.onCreate()
        appLogger.d(TAG, "SmartUsageTrackingService created")
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val serviceChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Smart Usage Tracking",
            NotificationManager.IMPORTANCE_MIN
        )
        serviceChannel.description = "Continuous app usage tracking and monitoring"
        manager?.createNotificationChannel(serviceChannel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        appLogger.d(TAG, "onStartCommand, action: ${intent?.action}")

        when (intent?.action) {
            ACTION_START_SMART_TRACKING -> {
                if (!isTrackingActive) {
                    startSmartTracking()
                }
            }
            ACTION_STOP_SMART_TRACKING -> {
                stopSmartTracking()
            }
            ACTION_CHECK_LIMITS -> {
                serviceScope.launch {
                    checkCurrentLimits()
                }
            }
            else -> {
                // Default behavior - start if needed
                serviceScope.launch {
                    if (shouldStartTracking()) {
                        startSmartTracking()
                    } else {
                        stopSelf()
                    }
                }
            }
        }

        return START_STICKY // Restart if killed to maintain continuous tracking
    }

    private fun startSmartTracking() {
        if (isTrackingActive) return

        appLogger.i(TAG, "Starting smart usage tracking")
        isTrackingActive = true

        // Only show notification when actively tracking limited apps
        startForeground(FOREGROUND_NOTIFICATION_ID, createTrackingNotification())

        trackingJob = serviceScope.launch {
            while (isActive && isTrackingActive) {
                try {
                    // Track usage statistics for dashboard
                    trackUsageStatistics()

                    // Check limits for limited apps
                    val hasActiveLimitedApps = checkCurrentLimits()

                    // Adjust polling interval based on activity
                    val pollingInterval = if (hasActiveLimitedApps) {
                        ACTIVE_POLLING_INTERVAL_MS
                    } else {
                        BACKGROUND_POLLING_INTERVAL_MS
                    }

                    delay(pollingInterval)

                } catch (e: Exception) {
                    appLogger.e(TAG, "Error in smart tracking loop", e)
                    delay(BACKGROUND_POLLING_INTERVAL_MS)
                }
            }
        }
    }

    private fun trackUsageStatistics() {
        try {
            // Start polling for usage statistics if not already running
            if (!isUsagePollingActive) {
                usageStatsPoller.startPolling(serviceScope)
                isUsagePollingActive = true
                appLogger.d(TAG, "Started usage statistics polling")
            }
        } catch (e: Exception) {
            appLogger.e(TAG, "Error starting usage statistics tracking", e)
        }
    }

    private suspend fun checkCurrentLimits(): Boolean {
        return try {
            val hasActiveLimitedApps = appUsageLimiter.hasActiveLimitedApps()

            if (hasActiveLimitedApps) {
                // Perform limit checks
                appUsageLimiter.performPeriodicLimitCheck()
            }

            hasActiveLimitedApps
        } catch (e: Exception) {
            appLogger.e(TAG, "Error checking current limits", e)
            false
        }
    }

    private suspend fun shouldStartTracking(): Boolean {
        return try {
            // Always return true to ensure continuous tracking for usage statistics
            // Even without limited apps, we need to track for dashboard data
            true
        } catch (e: Exception) {
            appLogger.e(TAG, "Error checking if tracking should start", e)
            true // Default to tracking
        }
    }

    private fun stopSmartTracking() {
        appLogger.i(TAG, "Stopping smart usage tracking")
        isTrackingActive = false
        trackingJob?.cancel()
        trackingJob = null

        // Stop usage stats polling
        if (isUsagePollingActive) {
            usageStatsPoller.stopPolling()
            isUsagePollingActive = false
        }

        // Stop foreground service
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createTrackingNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, pendingIntentFlags)

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Usage Tracking Active")
            .setContentText("Monitoring app usage and limits")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Keep persistent for background tracking
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        appLogger.d(TAG, "SmartUsageTrackingService destroying")
        isTrackingActive = false
        trackingJob?.cancel()

        // Stop usage stats polling
        if (isUsagePollingActive) {
            usageStatsPoller.stopPolling()
            isUsagePollingActive = false
        }

        serviceJob.cancel()
        super.onDestroy()
    }

    // Static methods for easy service control
    object ServiceController {
        fun startSmartTracking(context: android.content.Context) {
            val intent = Intent(context, SmartUsageTrackingService::class.java).apply {
                action = ACTION_START_SMART_TRACKING
            }
            context.startService(intent)
        }

        fun stopSmartTracking(context: android.content.Context) {
            val intent = Intent(context, SmartUsageTrackingService::class.java).apply {
                action = ACTION_STOP_SMART_TRACKING
            }
            context.startService(intent)
        }

        fun checkLimits(context: android.content.Context) {
            val intent = Intent(context, SmartUsageTrackingService::class.java).apply {
                action = ACTION_CHECK_LIMITS
            }
            context.startService(intent)
        }
    }
}