package com.example.screentimetracker.services

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import com.example.screentimetracker.utils.logger.AppLogger

@Singleton
class UsageStatsPoller @Inject constructor(
    private val usageStatsManager: UsageStatsManager,
    private val appLogger: AppLogger
) {
    private val _foregroundEvents = MutableSharedFlow<ForegroundEventInfo>()
    val foregroundEvents: SharedFlow<ForegroundEventInfo> = _foregroundEvents

    private var pollingJob: Job? = null

    // Helper data class to store event info
    data class ForegroundEventInfo(
        val eventType: Int,
        val packageName: String?,
        val className: String?,
        val timeStamp: Long
    )

    companion object {
        private const val TAG = "UsageStatsPoller"
        private const val POLLING_INTERVAL_MS = 3000L
    }

    fun startPolling(scope: CoroutineScope) {
        if (pollingJob?.isActive == true) {
            appLogger.d(TAG, "Polling job already active.")
            return
        }
        val lastTrackedEventTimeForPolling = System.currentTimeMillis() - POLLING_INTERVAL_MS
        pollingJob = scope.launch(Dispatchers.IO) {
            appLogger.d(TAG, "Starting usage stats polling loop.")
            var lastTrackedEventTime = System.currentTimeMillis() - POLLING_INTERVAL_MS
            while (isActive) {
                val currentTime = System.currentTimeMillis()
                lastTrackedEventTime = pollUsageStats(lastTrackedEventTime, currentTime)
                lastTrackedEventTime = currentTime
                delay(POLLING_INTERVAL_MS)
            }
            appLogger.d(TAG, "Exited usage stats polling loop.")
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
        appLogger.d(TAG, "Usage stats polling stopped.")
    }

    private suspend fun pollUsageStats(startTime: Long, endTime: Long): Long {
        var newLastTrackedEventTime = startTime
        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        var latestForegroundEvent: ForegroundEventInfo? = null
        val tempEvent = UsageEvents.Event()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(tempEvent)
            val eventType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tempEvent.eventType
            } else {
                @Suppress("DEPRECATION") tempEvent.eventType
            }
            if (eventType == UsageEvents.Event.ACTIVITY_RESUMED /*|| eventType == UsageEvents.Event.MOVE_TO_FOREGROUND*/) {
                if (latestForegroundEvent == null || tempEvent.timeStamp > latestForegroundEvent.timeStamp) {
                    latestForegroundEvent = ForegroundEventInfo(
                        eventType = tempEvent.eventType,
                        packageName = tempEvent.packageName,
                        className = tempEvent.className,
                        timeStamp = tempEvent.timeStamp
                    )
                    appLogger.d(TAG, "New foreground event detected by poller: ${tempEvent.packageName}")
                }
            }
            newLastTrackedEventTime = tempEvent.timeStamp
        }

        latestForegroundEvent?.let {
            _foregroundEvents.emit(it)
        }
        return newLastTrackedEventTime
    }
}