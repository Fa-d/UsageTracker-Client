package com.example.screentimetracker.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Handler // For Toast on main thread
import android.os.IBinder
import android.os.Looper // For Toast on main thread
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.screentimetracker.R
import com.example.screentimetracker.data.local.AppSessionEvent
import com.example.screentimetracker.domain.repository.TrackerRepository
import com.example.screentimetracker.domain.usecases.RecordAppSessionUseCase
import com.example.screentimetracker.domain.usecases.RecordAppUsageEventUseCase
import com.example.screentimetracker.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import com.example.screentimetracker.services.limiter.AppUsageLimiter
import com.example.screentimetracker.utils.logger.AppLogger

@AndroidEntryPoint
class AppUsageTrackingService : Service() {

    @Inject
    lateinit var usageStatsPoller: UsageStatsPoller
    @Inject
    lateinit var recordAppSessionUseCase: RecordAppSessionUseCase
    @Inject
    lateinit var recordAppUsageEventUseCase: RecordAppUsageEventUseCase
    @Inject
    lateinit var repository: TrackerRepository
    @Inject
    lateinit var appUsageLimiter: AppUsageLimiter // Inject AppUsageLimiter
    @Inject
    lateinit var appLogger: AppLogger // Inject AppLogger

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private var currentSessionPackageName: String? = null
    private var currentSessionStartTimeMillis: Long? = null

    companion object {
        private const val TAG = "AppUsageService"
        const val NOTIFICATION_CHANNEL_ID = "AppUsageTrackingChannel"
        private const val FOREGROUND_NOTIFICATION_ID = 1
        const val ACTION_HANDLE_SCREEN_OFF = "com.example.screentimetracker.ACTION_HANDLE_SCREEN_OFF"
        const val ACTION_RELOAD_LIMIT_SETTINGS = "com.example.screentimetracker.ACTION_RELOAD_LIMIT_SETTINGS"
    }

    override fun onCreate() {
        super.onCreate()
        appLogger.d(TAG, "AppUsageTrackingService created.")
        createForegroundServiceNotificationChannel()
        serviceScope.launch { appUsageLimiter.loadLimitedAppSettings() }
    }

    private fun createForegroundServiceNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val serviceChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID, "App Usage Tracking Service Channel", NotificationManager.IMPORTANCE_DEFAULT
        )
        manager?.createNotificationChannel(serviceChannel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        appLogger.d(TAG, "onStartCommand, action: ${intent?.action}")
        startForeground(FOREGROUND_NOTIFICATION_ID, createForegroundServiceNotification())

        when (intent?.action) {
            ACTION_HANDLE_SCREEN_OFF -> serviceScope.launch { handleScreenOff() }
            ACTION_RELOAD_LIMIT_SETTINGS -> serviceScope.launch { appUsageLimiter.loadLimitedAppSettings() }
            else -> {
                usageStatsPoller.startPolling(serviceScope)
                serviceScope.launch {
                    usageStatsPoller.foregroundEvents.collect { event ->
                        val foregroundPackageName = event.packageName
                        val eventTimestamp = event.timeStamp

                        if (foregroundPackageName != null && foregroundPackageName != currentSessionPackageName) {
                            finalizeCurrentSession(eventTimestamp)
                            startNewSession(foregroundPackageName, eventTimestamp)
                        } else if (foregroundPackageName == null && currentSessionPackageName != null) {
                            finalizeCurrentSession(eventTimestamp) // Use eventTimestamp for consistency
                        }
                        appUsageLimiter.checkUsageLimits(currentSessionPackageName, eventTimestamp)
                    }
                }
            }
        }
        return START_STICKY
    }

    private fun createForegroundServiceNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, pendingIntentFlags)
        val notificationIcon = R.drawable.ic_launcher_foreground
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Screen Time Tracker")
            .setContentText("Tracking app usage in the background.")
            .setSmallIcon(notificationIcon)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun handleScreenOff() {
        appLogger.d(TAG, "Handling screen off. Current session: $currentSessionPackageName")
        finalizeCurrentSession(System.currentTimeMillis())
        appUsageLimiter.onSessionFinalized()
    }

    private fun startNewSession(packageName: String, startTime: Long) {
        currentSessionPackageName = packageName
        currentSessionStartTimeMillis = startTime
        appLogger.i(TAG, "SESSION START (Analytics): $packageName at $startTime")
        serviceScope.launch { /* ... (legacy opened event) ... */ }
        appUsageLimiter.onNewSession(packageName, startTime)
    }

    private fun finalizeCurrentSession(sessionEndTimeMillis: Long) {
        val pkgName = currentSessionPackageName
        val sessionStartTime = currentSessionStartTimeMillis

        if (pkgName != null && sessionStartTime != null) {
            serviceScope.launch {
                try {
                    recordAppSessionUseCase(pkgName, sessionStartTime, sessionEndTimeMillis)
                    appLogger.d(TAG, "Session saved: $pkgName $sessionStartTime-$sessionEndTimeMillis")
                } catch (e: Exception) {
                    appLogger.e(TAG, "Failed to save session for $pkgName", e)
                }
            }
        }
        currentSessionPackageName = null
        currentSessionStartTimeMillis = null
        appUsageLimiter.onSessionFinalized()
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() {
        appLogger.d(TAG, "AppUsageTrackingService destroying. Finalizing current session.")
        usageStatsPoller.stopPolling() // Stop the poller
        serviceScope.launch(Dispatchers.IO) {
            finalizeCurrentSession(System.currentTimeMillis()) // Ensure final session is saved
        }
        serviceJob.cancel()
        super.onDestroy()
    }
}
