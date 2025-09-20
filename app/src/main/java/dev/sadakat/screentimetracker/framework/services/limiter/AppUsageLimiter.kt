package dev.sadakat.screentimetracker.framework.services.limiter

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.sadakat.screentimetracker.core.data.local.entities.LimitedApp
import dev.sadakat.screentimetracker.core.domain.repository.TrackerRepository
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import dev.sadakat.screentimetracker.utils.ui.AppNotificationManager
import dev.sadakat.screentimetracker.utils.ui.AppToastManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUsageLimiter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: TrackerRepository,
    private val appLogger: AppLogger, // Inject AppLogger
    private val appNotificationManager: AppNotificationManager, // Inject AppNotificationManager
    private val appToastManager: AppToastManager // Inject AppToastManager
) {
    private var limitedAppSettings: List<LimitedApp> = emptyList()
    private var currentLimitedAppDetails: LimitedApp? = null
    private var continuousUsageStartTimeForLimiterMillis: Long? = null
    private var warningShownForCurrentSessionApp: String? = null
    private var threeXActionTakenForCurrentSessionApp: String? = null

    companion object {
        private const val TAG = "AppUsageLimiter"
    }

    init {
        // Notification channel creation is now handled by AppNotificationManagerImpl
    }

    suspend fun loadLimitedAppSettings() {
        try {
            limitedAppSettings = repository.getAllLimitedAppsOnce()
            appLogger.d(TAG, "Loaded limited app settings: ${limitedAppSettings.size} apps.")
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to load limited app settings", e)
        }
    }

    fun onNewSession(packageName: String, startTime: Long) {
        warningShownForCurrentSessionApp = null
        threeXActionTakenForCurrentSessionApp = null

        val appLimit = limitedAppSettings.find { it.packageName == packageName }
        if (appLimit != null) {
            currentLimitedAppDetails = appLimit
            continuousUsageStartTimeForLimiterMillis = startTime
            appLogger.i(TAG, "LIMITER: Continuous tracking started for ${appLimit.packageName}, Limit: ${TimeUnit.MILLISECONDS.toMinutes(appLimit.timeLimitMillis)}min")
            appLogger.d(TAG, "onNewSession: currentLimitedAppDetails=${currentLimitedAppDetails?.packageName}, continuousUsageStartTimeForLimiterMillis=$continuousUsageStartTimeForLimiterMillis")
        } else {
            currentLimitedAppDetails = null
            continuousUsageStartTimeForLimiterMillis = null
            appLogger.d(TAG, "onNewSession: App $packageName is not limited. Clearing current details.")
        }
    }

    fun onSessionFinalized() {
        warningShownForCurrentSessionApp = null
        threeXActionTakenForCurrentSessionApp = null
        if (currentLimitedAppDetails != null) {
            appLogger.i(TAG, "LIMITER: Continuous tracking stopped for ${currentLimitedAppDetails!!.packageName}.")
            appLogger.d(TAG, "onSessionFinalized: Clearing currentLimitedAppDetails and continuousUsageStartTimeForLimiterMillis.")
            currentLimitedAppDetails = null
            continuousUsageStartTimeForLimiterMillis = null
        }
    }

    fun checkUsageLimits(currentSessionPackageName: String?, currentTime: Long) {
        appLogger.d(TAG, "checkUsageLimits: currentSessionPackageName=$currentSessionPackageName, currentTime=$currentTime")
        appLogger.d(TAG, "checkUsageLimits: currentLimitedAppDetails=${currentLimitedAppDetails?.packageName}, continuousUsageStartTimeForLimiterMillis=$continuousUsageStartTimeForLimiterMillis")

        currentLimitedAppDetails?.let { limitedApp ->
            if (limitedApp.packageName == currentSessionPackageName && continuousUsageStartTimeForLimiterMillis != null) {
                val continuousDuration = currentTime - continuousUsageStartTimeForLimiterMillis!!

                if (continuousDuration >= limitedApp.timeLimitMillis && warningShownForCurrentSessionApp != limitedApp.packageName) {
                    appNotificationManager.showWarningNotification(limitedApp, continuousDuration)
                    warningShownForCurrentSessionApp = limitedApp.packageName
                }

                if (continuousDuration >= (limitedApp.timeLimitMillis * 3) && threeXActionTakenForCurrentSessionApp != limitedApp.packageName) {
                    appToastManager.bringAppToForeground(limitedApp.packageName)
                    appToastManager.showDissuasionToast(getAppName(limitedApp.packageName)) // getAppName is still needed here
                    threeXActionTakenForCurrentSessionApp = limitedApp.packageName
                }
            }
        }
    }

    // New methods for smart tracking integration
    fun isAppLimited(packageName: String): Boolean {
        return limitedAppSettings.any { it.packageName == packageName }
    }

    fun hasActiveLimitedApps(): Boolean {
        return currentLimitedAppDetails != null
    }

    suspend fun performPeriodicLimitCheck() {
        currentLimitedAppDetails?.let { limitedApp ->
            continuousUsageStartTimeForLimiterMillis?.let { startTime ->
                val currentTime = System.currentTimeMillis()
                val continuousDuration = currentTime - startTime

                // Check if we need to show warning
                if (continuousDuration >= limitedApp.timeLimitMillis &&
                    warningShownForCurrentSessionApp != limitedApp.packageName) {
                    appNotificationManager.showWarningNotification(limitedApp, continuousDuration)
                    warningShownForCurrentSessionApp = limitedApp.packageName
                    appLogger.i(TAG, "Periodic check: Warning shown for ${limitedApp.packageName}")
                }

                // Check if we need to take action
                if (continuousDuration >= (limitedApp.timeLimitMillis * 3) &&
                    threeXActionTakenForCurrentSessionApp != limitedApp.packageName) {
                    appToastManager.bringAppToForeground(limitedApp.packageName)
                    appToastManager.showDissuasionToast(getAppName(limitedApp.packageName))
                    threeXActionTakenForCurrentSessionApp = limitedApp.packageName
                    appLogger.i(TAG, "Periodic check: 3X action taken for ${limitedApp.packageName}")
                }
            }
        }
    }

    fun getCurrentLimitedAppDetails(): LimitedApp? {
        return currentLimitedAppDetails
    }

    fun getRemainingTime(packageName: String): Long? {
        val limitedApp = currentLimitedAppDetails ?: return null
        val startTime = continuousUsageStartTimeForLimiterMillis ?: return null

        if (limitedApp.packageName != packageName) return null

        val currentTime = System.currentTimeMillis()
        val usedTime = currentTime - startTime
        val remainingTime = limitedApp.timeLimitMillis - usedTime

        return if (remainingTime > 0) remainingTime else 0
    }

    // This method is still needed here as it's used by AppToastManagerImpl
    private fun getAppName(packageName: String): String {
        return try {
            context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(packageName, 0)).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            appLogger.w(TAG, "App name not found for $packageName", e)
            packageName
        } catch (e: Exception) {
            appLogger.e(TAG, "Error getting app name for $packageName", e)
            packageName
        }
    }
}