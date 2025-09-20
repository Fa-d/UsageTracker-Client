package dev.sadakat.screentimetracker.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dev.sadakat.screentimetracker.core.data.local.entities.UserPreferences
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

data class AIAvailabilityStatus(
    val isDeviceCompatible: Boolean,
    val isModuleDownloaded: Boolean,
    val isUserEnabled: Boolean,
    val canUseAI: Boolean,
    val reasonIfUnavailable: String? = null
)

object AIUtils {
    private const val AI_MODULE_NAME = "ai_insights"
    const val MIN_SDK_VERSION = 24
    const val MIN_RAM_MB = 2048

    fun checkDeviceCompatibility(context: Context): Boolean {
        return checkSDKVersion() && 
               checkRAMAvailability(context) && 
               checkPlayServices(context)
    }

    fun checkSDKVersion(): Boolean {
        return Build.VERSION.SDK_INT >= MIN_SDK_VERSION
    }

    fun checkRAMAvailability(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val memoryInfo = android.app.ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val totalMemoryMB = memoryInfo.totalMem / (1024 * 1024)
        return totalMemoryMB >= MIN_RAM_MB
    }

    fun checkPlayServices(context: Context): Boolean {
        return try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo("com.google.android.gms", 0)
            packageInfo != null
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isAIModuleInstalled(context: Context): Boolean {
        val splitInstallManager = SplitInstallManagerFactory.create(context)
        return splitInstallManager.installedModules.contains(AI_MODULE_NAME)
    }

    fun getAIAvailabilityStatus(
        context: Context,
        userPreferences: UserPreferences
    ): AIAvailabilityStatus {
        val isDeviceCompatible = checkDeviceCompatibility(context)
        val isModuleDownloaded = isAIModuleInstalled(context) && userPreferences.aiModuleDownloaded
        val isUserEnabled = userPreferences.aiFeaturesEnabled
        
        val canUseAI = isDeviceCompatible && isModuleDownloaded && isUserEnabled
        
        val reasonIfUnavailable = when {
            !isDeviceCompatible -> "Device doesn't meet minimum requirements"
            !isModuleDownloaded -> "AI module not downloaded"
            !isUserEnabled -> "AI features disabled by user"
            else -> null
        }
        
        return AIAvailabilityStatus(
            isDeviceCompatible = isDeviceCompatible,
            isModuleDownloaded = isModuleDownloaded,
            isUserEnabled = isUserEnabled,
            canUseAI = canUseAI,
            reasonIfUnavailable = reasonIfUnavailable
        )
    }

    fun getDetailedCompatibilityReport(context: Context): Map<String, Boolean> {
        return mapOf(
            "SDK Version >= $MIN_SDK_VERSION" to checkSDKVersion(),
            "RAM >= ${MIN_RAM_MB}MB" to checkRAMAvailability(context),
            "Google Play Services" to checkPlayServices(context),
            "AI Module Installed" to isAIModuleInstalled(context)
        )
    }

    fun canDownloadAIModule(context: Context): Boolean {
        return checkDeviceCompatibility(context) && !isAIModuleInstalled(context)
    }

    fun getAIFeatureFlags(userPreferences: UserPreferences): Map<String, Boolean> {
        return mapOf(
            "AI Features" to userPreferences.aiFeaturesEnabled,
            "AI Insights" to userPreferences.aiInsightsEnabled,
            "Goal Recommendations" to userPreferences.aiGoalRecommendationsEnabled,
            "Predictive Coaching" to userPreferences.aiPredictiveCoachingEnabled,
            "Usage Predictions" to userPreferences.aiUsagePredictionsEnabled,
            "Module Downloaded" to userPreferences.aiModuleDownloaded,
            "Onboarding Completed" to userPreferences.aiOnboardingCompleted
        )
    }

    fun createAIAvailabilityFlow(
        context: Context,
        userPreferencesFlow: Flow<UserPreferences>
    ): Flow<AIAvailabilityStatus> {
        return combine(
            userPreferencesFlow,
            flow { emit(checkDeviceCompatibility(context)) }
        ) { prefs, deviceCompatible ->
            AIAvailabilityStatus(
                isDeviceCompatible = deviceCompatible,
                isModuleDownloaded = isAIModuleInstalled(context) && prefs.aiModuleDownloaded,
                isUserEnabled = prefs.aiFeaturesEnabled,
                canUseAI = deviceCompatible && 
                          isAIModuleInstalled(context) && 
                          prefs.aiModuleDownloaded && 
                          prefs.aiFeaturesEnabled,
                reasonIfUnavailable = when {
                    !deviceCompatible -> "Device doesn't meet minimum requirements"
                    !isAIModuleInstalled(context) -> "AI module not installed"
                    !prefs.aiModuleDownloaded -> "AI module not downloaded"
                    !prefs.aiFeaturesEnabled -> "AI features disabled"
                    else -> null
                }
            )
        }
    }
}