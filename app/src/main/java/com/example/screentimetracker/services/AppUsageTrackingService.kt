package com.example.screentimetracker.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
// import android.content.Context // Not directly used in this manner
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.screentimetracker.R
import com.example.screentimetracker.data.local.AppSessionEvent // Import the new entity
import com.example.screentimetracker.domain.usecases.RecordAppUsageEventUseCase // Still here for now, will be re-evaluated
import com.example.screentimetracker.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit // For time calculation clarity
import javax.inject.Inject

@AndroidEntryPoint
class AppUsageTrackingService : Service() {

    @Inject
    lateinit var usageStatsManager: UsageStatsManager
    @Inject
    lateinit var recordAppUsageEventUseCase: RecordAppUsageEventUseCase // Will be replaced by RecordAppSessionUseCase later

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    // --- Session Tracking State ---
    private var currentSessionPackageName: String? = null
    private var currentSessionStartTimeMillis: Long? = null
    // --- End Session Tracking State ---

    private var lastTrackedEventTimeForPolling: Long = 0L // Renamed for clarity

    companion object {
        private const val TAG = "AppUsageService"
        private const val POLLING_INTERVAL_MS = 3000L // Maybe reduce polling interval for faster fg detection
        const val NOTIFICATION_CHANNEL_ID = "AppUsageTrackingChannel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_HANDLE_SCREEN_OFF = "com.example.screentimetracker.ACTION_HANDLE_SCREEN_OFF"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AppUsageTrackingService created.")
        createNotificationChannel()
        lastTrackedEventTimeForPolling = System.currentTimeMillis() - POLLING_INTERVAL_MS
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand, action: ${intent?.action}")
        startForeground(NOTIFICATION_ID, createNotification())

        if (intent?.action == ACTION_HANDLE_SCREEN_OFF) {
            serviceScope.launch { // Ensure this is on a background thread
                handleScreenOff()
            }
        } else if (serviceScope.coroutineContext[Job]?.isActive != true || !isPollingJobActive()) {
             // Start polling only if it's not already running from a previous onStartCommand
            serviceScope.launch(CoroutineName("AppUsagePollingJob")) {
                Log.d(TAG, "Starting app usage polling loop.")
                while (isActive) {
                    pollAppUsage()
                    delay(POLLING_INTERVAL_MS)
                }
                Log.d(TAG, "Exited app usage polling loop.")
            }
        }
        return START_STICKY
    }

    // Helper to check if the specific polling job is active
    private fun isPollingJobActive(): Boolean {
        return serviceScope.coroutineContext[Job]?.children?.any { it.name == "AppUsagePollingJob" } == true
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "App Usage Tracking Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, pendingIntentFlags
        )
        val notificationIcon = R.drawable.ic_launcher_foreground

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Screen Time Tracker")
            .setContentText("Tracking app usage in the background.")
            .setSmallIcon(notificationIcon)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private suspend fun pollAppUsage() { // Renamed from trackAppUsage
        val currentTime = System.currentTimeMillis()
        val usageEvents = usageStatsManager.queryEvents(lastTrackedEventTimeForPolling, currentTime)
        var latestForegroundEvent: UsageEvents.Event? = null

        val tempEvent = UsageEvents.Event()
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(tempEvent)
            if (tempEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED || tempEvent.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                if (latestForegroundEvent == null || tempEvent.timeStamp > latestForegroundEvent!!.timeStamp) {
                    latestForegroundEvent = UsageEvents.Event()
                    latestForegroundEvent!!.copyFrom(tempEvent)
                }
            }
        }

        lastTrackedEventTimeForPolling = currentTime

        latestForegroundEvent?.let { event ->
            if (event.packageName != null && event.packageName != currentSessionPackageName) {
                Log.d(TAG, "New app to foreground: ${event.packageName} at ${event.timeStamp}. Previous: $currentSessionPackageName")
                finalizeCurrentSession(event.timeStamp)
                startNewSession(event.packageName, event.timeStamp)
            } else if (event.packageName != null && event.packageName == currentSessionPackageName) {
                Log.d(TAG, "App ${event.packageName} still in foreground or resumed.")
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
        Log.i(TAG, "SESSION START: $packageName at $startTime")
        // TODO: Later, also save the "app open" event using RecordAppUsageEventUseCase if still needed for open counts
        // This might be where you call recordAppUsageEventUseCase(packageName, "opened", startTime) if you want an explicit "open" event.
        // However, session data itself can imply an open. This depends on how you want to count "opens".
        // For now, let's assume an explicit "opened" event is still desired for the AppUsageEvent table
        // and for the existing dashboard functionality that relies on "opened" events.
        // This will be re-evaluated when RecordAppSessionUseCase is implemented.
        serviceScope.launch { // Added serviceScope.launch for suspend function
            try {
                recordAppUsageEventUseCase(packageName, "opened", startTime)
                Log.d(TAG, "Legacy 'opened' event saved for $packageName")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving legacy 'opened' event for $packageName", e)
            }
        }
    }

    private fun finalizeCurrentSession(sessionEndTimeMillis: Long) {
        val pkgName = currentSessionPackageName
        val sessionStartTime = currentSessionStartTimeMillis

        if (pkgName != null && sessionStartTime != null) {
            val actualEndTime = if (sessionEndTimeMillis < sessionStartTime) sessionStartTime else sessionEndTimeMillis
            val duration = actualEndTime - sessionStartTime

            if (duration > 1000) {
                val sessionEvent = AppSessionEvent(
                    packageName = pkgName,
                    startTimeMillis = sessionStartTime,
                    endTimeMillis = actualEndTime,
                    durationMillis = duration
                )
                Log.i(TAG, "SESSION END: $pkgName, Start: $sessionStartTime, End: $actualEndTime, Duration: ${TimeUnit.MILLISECONDS.toSeconds(duration)}s. Event: $sessionEvent")
                // TODO: Save sessionEvent using a UseCase (e.g., recordAppSessionUseCase(sessionEvent)) - this is for Step 5
            } else {
                Log.d(TAG, "Session for $pkgName was too short ($duration ms), not recording.")
            }
        }
        currentSessionPackageName = null
        currentSessionStartTimeMillis = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d(TAG, "AppUsageTrackingService destroying. Finalizing current session.")
        serviceScope.launch {
             finalizeCurrentSession(System.currentTimeMillis())
        }
        serviceJob.cancel() // Cancel all children of the job
        // Wait for the finalization job to complete if necessary, though usually cancellation is enough
        // and Android gives limited time in onDestroy.
        super.onDestroy()
    }
}
