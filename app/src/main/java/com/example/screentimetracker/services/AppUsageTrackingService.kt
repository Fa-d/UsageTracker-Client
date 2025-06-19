package com.example.screentimetracker.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.graphics.Color // For notification channel, if used
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat // For showing warning
import com.example.screentimetracker.R
import com.example.screentimetracker.data.local.AppSessionEvent
import com.example.screentimetracker.data.local.LimitedApp
import com.example.screentimetracker.domain.repository.TrackerRepository
import com.example.screentimetracker.domain.usecases.RecordAppSessionUseCase
import com.example.screentimetracker.domain.usecases.RecordAppUsageEventUseCase
import com.example.screentimetracker.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class AppUsageTrackingService : Service() {

    @Inject
    lateinit var usageStatsManager: UsageStatsManager
    @Inject
    lateinit var recordAppSessionUseCase: RecordAppSessionUseCase
    @Inject
    lateinit var recordAppUsageEventUseCase: RecordAppUsageEventUseCase
    @Inject
    lateinit var repository: TrackerRepository

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private var currentSessionPackageName: String? = null
    private var currentSessionStartTimeMillis: Long? = null

    private var limitedAppSettings: List<LimitedApp> = emptyList()
    private var currentLimitedAppDetails: LimitedApp? = null
    private var continuousUsageStartTimeForLimiterMillis: Long? = null
    private var warningShownForCurrentSessionApp: String? = null // Track if warning shown for current app session

    private var lastTrackedEventTimeForPolling: Long = 0L

    companion object {
        private const val TAG = "AppUsageService"
        private const val POLLING_INTERVAL_MS = 3000L
        const val NOTIFICATION_CHANNEL_ID = "AppUsageTrackingChannel"
        const val WARNING_NOTIFICATION_CHANNEL_ID = "AppUsageWarningChannel" // New
        private const val FOREGROUND_NOTIFICATION_ID = 1 // Renamed for clarity
        private const val WARNING_NOTIFICATION_ID_BASE = 1000
        const val ACTION_HANDLE_SCREEN_OFF = "com.example.screentimetracker.ACTION_HANDLE_SCREEN_OFF"
        const val ACTION_RELOAD_LIMIT_SETTINGS = "com.example.screentimetracker.ACTION_RELOAD_LIMIT_SETTINGS"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AppUsageTrackingService created.")
        createNotificationChannels() // Updated to create both channels
        lastTrackedEventTimeForPolling = System.currentTimeMillis() - POLLING_INTERVAL_MS
        serviceScope.launch { loadLimitedAppSettings() }
    }

    private suspend fun loadLimitedAppSettings() { /* ... same as before ... */
        try {
            limitedAppSettings = repository.getAllLimitedAppsOnce()
            Log.d(TAG, "Loaded limited app settings: ${limitedAppSettings.size} apps.")
            currentSessionPackageName?.let { currentPkg ->
                val activeAppDetails = limitedAppSettings.find { it.packageName == currentPkg }
                if (activeAppDetails != null) {
                    if (currentLimitedAppDetails?.packageName != currentPkg || currentLimitedAppDetails?.timeLimitMillis != activeAppDetails.timeLimitMillis) {
                        Log.d(TAG, "Limiter settings changed for active app $currentPkg. Restarting continuous tracking for it.")
                        currentLimitedAppDetails = activeAppDetails
                        continuousUsageStartTimeForLimiterMillis = currentSessionStartTimeMillis
                        warningShownForCurrentSessionApp = null // Reset warning if limit changed
                    }
                } else {
                    if (currentLimitedAppDetails != null) {
                        Log.d(TAG, "Active app $currentPkg is no longer limited. Stopping continuous tracking for limiter.")
                        currentLimitedAppDetails = null
                        continuousUsageStartTimeForLimiterMillis = null
                        warningShownForCurrentSessionApp = null
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load limited app settings", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand, action: ${intent?.action}")
        startForeground(FOREGROUND_NOTIFICATION_ID, createForegroundServiceNotification()) // Renamed method

        when (intent?.action) {
            ACTION_HANDLE_SCREEN_OFF -> serviceScope.launch { handleScreenOff() }
            ACTION_RELOAD_LIMIT_SETTINGS -> serviceScope.launch { loadLimitedAppSettings() }
            else -> {
                if (serviceScope.coroutineContext[Job]?.isActive != true || !isPollingJobActive()) {
                    serviceScope.launch(CoroutineName("AppUsagePollingJob")) {
                        Log.d(TAG, "Starting app usage polling loop.")
                        while (isActive) {
                            pollAppUsage()
                            delay(POLLING_INTERVAL_MS)
                        }
                        Log.d(TAG, "Exited app usage polling loop.")
                    }
                }
            }
        }
        return START_STICKY
    }

    private fun isPollingJobActive(): Boolean { /* ... same as before ... */
        return serviceScope.coroutineContext[Job]?.children?.any { it.toString().contains("AppUsagePollingJob") } == true
    }

    private fun createNotificationChannels() { // Renamed from createNotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, "App Usage Tracking Service Channel", NotificationManager.IMPORTANCE_DEFAULT
            )
            manager?.createNotificationChannel(serviceChannel)

            val warningChannel = NotificationChannel(
                WARNING_NOTIFICATION_CHANNEL_ID, "App Usage Limit Warnings", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for when app usage limits are reached."
                enableVibration(true) // Example: add vibration
                lightColor = Color.RED
            }
            manager?.createNotificationChannel(warningChannel)
        }
    }

    private fun createForegroundServiceNotification(): Notification { // Renamed
        /* ... same as createNotification() before ... */
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
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

    private suspend fun pollAppUsage() {
        val currentTime = System.currentTimeMillis()
        val usageEvents = usageStatsManager.queryEvents(lastTrackedEventTimeForPolling, currentTime)
        var latestForegroundEvent: UsageEvents.Event? = null
        val tempEvent = UsageEvents.Event()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(tempEvent)
            if (tempEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED || tempEvent.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                if (latestForegroundEvent == null || tempEvent.timeStamp > latestForegroundEvent!!.timeStamp) {
                    latestForegroundEvent = UsageEvents.Event(); latestForegroundEvent!!.copyFrom(tempEvent)
                }
            }
        }
        lastTrackedEventTimeForPolling = currentTime

        val foregroundPackageName = latestForegroundEvent?.packageName

        if (foregroundPackageName != null && foregroundPackageName != currentSessionPackageName) {
            finalizeCurrentSession(latestForegroundEvent!!.timeStamp)
            startNewSession(foregroundPackageName, latestForegroundEvent.timeStamp)
        } else if (foregroundPackageName == null && currentSessionPackageName != null) {
            finalizeCurrentSession(currentTime)
        }

        currentLimitedAppDetails?.let { limitedApp ->
            if (limitedApp.packageName == currentSessionPackageName && continuousUsageStartTimeForLimiterMillis != null) {
                val continuousDuration = currentTime - continuousUsageStartTimeForLimiterMillis!!
                Log.i(TAG, "LIMITED APP ACTIVE: ${limitedApp.packageName}, Continuous Duration: ${TimeUnit.MILLISECONDS.toSeconds(continuousDuration)}s, Limit: ${TimeUnit.MILLISECONDS.toMinutes(limitedApp.timeLimitMillis)}min")

                if (continuousDuration >= limitedApp.timeLimitMillis && warningShownForCurrentSessionApp != limitedApp.packageName) {
                    showUsageLimitWarningNotification(limitedApp, continuousDuration)
                    warningShownForCurrentSessionApp = limitedApp.packageName
                }
            }
        }
    }

    private fun handleScreenOff() {
        Log.d(TAG, "Handling screen off. Current session: $currentSessionPackageName")
        finalizeCurrentSession(System.currentTimeMillis())
    }

    private fun startNewSession(packageName: String, startTime: Long) {
        currentSessionPackageName = packageName
        currentSessionStartTimeMillis = startTime
        warningShownForCurrentSessionApp = null // Reset warning flag for new session
        Log.i(TAG, "SESSION START (Analytics): $packageName at $startTime")

        serviceScope.launch { /* ... (legacy opened event) ... */
            try {
                recordAppUsageEventUseCase(packageName, "opened", startTime)
                Log.d(TAG, "Legacy 'opened' event saved for $packageName")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving legacy 'opened' event for $packageName", e)
            }
        }

        val appLimit = limitedAppSettings.find { it.packageName == packageName }
        if (appLimit != null) {
            currentLimitedAppDetails = appLimit
            continuousUsageStartTimeForLimiterMillis = startTime
            Log.i(TAG, "LIMITER: Continuous tracking started for ${appLimit.packageName}, Limit: ${TimeUnit.MILLISECONDS.toMinutes(appLimit.timeLimitMillis)}min")
        } else {
            currentLimitedAppDetails = null
            continuousUsageStartTimeForLimiterMillis = null
        }
    }

    private fun finalizeCurrentSession(sessionEndTimeMillis: Long) {
        val pkgName = currentSessionPackageName
        val sessionStartTime = currentSessionStartTimeMillis
        warningShownForCurrentSessionApp = null // Reset warning on any session finalization

        if (pkgName != null && sessionStartTime != null) { /* ... (save session event) ... */
            val actualEndTime = if (sessionEndTimeMillis < sessionStartTime) sessionStartTime else sessionEndTimeMillis
            val duration = actualEndTime - sessionStartTime

            if (duration > 1000) {
                val sessionEvent = AppSessionEvent(
                    packageName = pkgName,
                    startTimeMillis = sessionStartTime,
                    endTimeMillis = actualEndTime,
                    durationMillis = duration
                )
                serviceScope.launch {
                    try {
                        recordAppSessionUseCase(sessionEvent)
                        Log.i(TAG, "SESSION END (Analytics): $pkgName, Duration: ${TimeUnit.MILLISECONDS.toSeconds(duration)}s. Event: $sessionEvent")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error saving app session event for $pkgName", e)
                    }
                }
            } else {
                Log.d(TAG, "Session for $pkgName (Analytics) was too short ($duration ms), not recording.")
            }
        }
        currentSessionPackageName = null
        currentSessionStartTimeMillis = null

        if (currentLimitedAppDetails != null) {
             Log.i(TAG, "LIMITER: Continuous tracking stopped for ${currentLimitedAppDetails!!.packageName} due to app switch or screen off.")
            currentLimitedAppDetails = null
            continuousUsageStartTimeForLimiterMillis = null
        }
    }

    private fun showUsageLimitWarningNotification(limitedApp: LimitedApp, continuousDurationMillis: Long) {
        val notificationManager = NotificationManagerCompat.from(this)

        val appName = try {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(limitedApp.packageName, 0)).toString()
        } catch (e: Exception) {
            limitedApp.packageName
        }

        val durationMinutes = TimeUnit.MILLISECONDS.toMinutes(continuousDurationMillis)
        val limitMinutes = TimeUnit.MILLISECONDS.toMinutes(limitedApp.timeLimitMillis)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // putExtra("action", "limit_warning") // Optional: for specific handling in MainActivity
            // putExtra("packageName", limitedApp.packageName)
        }
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(this, limitedApp.packageName.hashCode(), intent, pendingIntentFlags)

        val notificationIcon = R.drawable.ic_launcher_foreground // Replace with a dedicated warning icon

        val notification = NotificationCompat.Builder(this, WARNING_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Usage Limit Reached")
            .setContentText("$appName used for $durationMinutes minutes (Limit: $limitMinutes min). Consider a break.")
            .setSmallIcon(notificationIcon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationId = WARNING_NOTIFICATION_ID_BASE + limitedApp.packageName.hashCode()
        try {
            notificationManager.notify(notificationId, notification)
            Log.i(TAG, "Usage limit warning notification shown for ${limitedApp.packageName}.")
        } catch (e: SecurityException) {
            Log.e(TAG, "Missing POST_NOTIFICATIONS permission for warning.", e)
            // This should ideally not happen if permission is in manifest and it's a foreground service.
            // But good to catch if there are edge cases or platform issues.
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() { /* ... same as before ... */
        Log.d(TAG, "AppUsageTrackingService destroying. Finalizing current session.")
        GlobalScope.launch(Dispatchers.IO) {
            finalizeCurrentSession(System.currentTimeMillis())
        }
        serviceJob.cancel()
        super.onDestroy()
    }
}
