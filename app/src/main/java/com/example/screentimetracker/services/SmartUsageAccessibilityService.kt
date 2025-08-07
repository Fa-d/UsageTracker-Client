package com.example.screentimetracker.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.screentimetracker.domain.repository.TrackerRepository
import com.example.screentimetracker.domain.usecases.RecordAppSessionUseCase
import com.example.screentimetracker.services.content.ContentBlockingManager
import com.example.screentimetracker.services.limiter.AppUsageLimiter
import com.example.screentimetracker.utils.logger.AppLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmartUsageAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var recordAppSessionUseCase: RecordAppSessionUseCase
    @Inject
    lateinit var repository: TrackerRepository
    @Inject
    lateinit var appUsageLimiter: AppUsageLimiter
    @Inject
    lateinit var contentBlockingManager: ContentBlockingManager
    @Inject
    lateinit var appLogger: AppLogger

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private var currentSessionPackageName: String? = null
    private var currentSessionStartTimeMillis: Long? = null
    private var lastEventTime: Long = 0L

    companion object {
        private const val TAG = "SmartAccessibilityService"
        private const val MIN_EVENT_INTERVAL_MS = 1000L // Prevent event spam
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        appLogger.i(TAG, "Smart Usage Accessibility Service connected")
        serviceScope.launch { 
            appUsageLimiter.loadLimitedAppSettings() 
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastEventTime < MIN_EVENT_INTERVAL_MS) return
        lastEventTime = currentTime

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                handleWindowStateChanged(event, currentTime)
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                handleContentChanged(event, currentTime)
            }
        }
    }

    private fun handleWindowStateChanged(event: AccessibilityEvent, currentTime: Long) {
        val packageName = event.packageName?.toString()
        appLogger.d(TAG, "Window state changed to: $packageName")

        if (packageName != null && packageName != currentSessionPackageName) {
            // Finalize previous session
            finalizeCurrentSession(currentTime)
            
            // Start new session
            startNewSession(packageName, currentTime)
        }
    }

    private fun handleContentChanged(event: AccessibilityEvent, currentTime: Long) {
        val packageName = event.packageName?.toString() ?: return
        
        // Only check content for apps we're currently tracking
        if (packageName == currentSessionPackageName) {
            serviceScope.launch {
                checkForBlockedContent(packageName, event)
            }
            // Update usage limits check
            appUsageLimiter.checkUsageLimits(packageName, currentTime)
        }
    }

    private suspend fun checkForBlockedContent(packageName: String, event: AccessibilityEvent) {
        try {
            val rootNode = rootInActiveWindow
            if (rootNode != null && contentBlockingManager.shouldBlockContent(packageName, rootNode)) {
                val blockedFeature = contentBlockingManager.getBlockedFeatureName(packageName, rootNode)
                appLogger.i(TAG, "Blocked content detected: $blockedFeature in $packageName")
                
                // Block the content
                contentBlockingManager.blockContent(packageName, blockedFeature)
            }
        } catch (e: Exception) {
            appLogger.e(TAG, "Error checking blocked content for $packageName", e)
        }
    }

    private fun startNewSession(packageName: String, startTime: Long) {
        currentSessionPackageName = packageName
        currentSessionStartTimeMillis = startTime
        appLogger.i(TAG, "SESSION START: $packageName at $startTime")
        
        // Notify usage limiter
        appUsageLimiter.onNewSession(packageName, startTime)
        
        // Start smart tracking service if needed for this app
        serviceScope.launch {
            if (appUsageLimiter.isAppLimited(packageName)) {
                startSmartTrackingService()
            }
        }
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

    private fun startSmartTrackingService() {
        val intent = Intent(this, SmartUsageTrackingService::class.java)
        intent.action = SmartUsageTrackingService.ACTION_START_SMART_TRACKING
        startService(intent)
    }

    override fun onInterrupt() {
        appLogger.w(TAG, "Accessibility service interrupted")
        finalizeCurrentSession(System.currentTimeMillis())
    }

    override fun onDestroy() {
        appLogger.d(TAG, "SmartUsageAccessibilityService destroying")
        finalizeCurrentSession(System.currentTimeMillis())
        serviceJob.cancel()
        super.onDestroy()
    }
}