package dev.sadakat.screentimetracker.domain.usecases

import dev.sadakat.screentimetracker.core.data.local.TimeRestriction
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import dev.sadakat.screentimetracker.utils.ui.AppNotificationManager
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeRestrictionManagerUseCase @Inject constructor(
    private val repository: TrackerRepository,
    private val notificationManager: AppNotificationManager,
    private val appLogger: AppLogger
) {
    companion object {
        private const val TAG = "TimeRestrictionManager"
        
        // Restriction types from requirements.md
        const val BEDTIME_MODE = "bedtime_mode"
        const val WORK_HOURS_FOCUS = "work_hours_focus"
        const val MEAL_TIME_PROTECTION = "meal_time_protection"
        const val MORNING_ROUTINE = "morning_routine"
        
        // Emergency apps that are usually allowed even during restrictions
        val EMERGENCY_APPS = listOf(
            "com.android.dialer",
            "com.google.android.dialer",
            "com.android.mms",
            "com.google.android.apps.messaging",
            "com.android.emergency",
            "com.google.android.contacts"
        )
    }

    suspend fun createDefaultTimeRestrictions() {
        val bedtimeRestriction = TimeRestriction(
            restrictionType = BEDTIME_MODE,
            name = "Digital Sunset",
            description = "Block distracting apps 1 hour before bedtime",
            startTimeMinutes = 22 * 60, // 10 PM
            endTimeMinutes = 8 * 60, // 8 AM
            appsBlocked = "", // Empty means all non-emergency apps
            daysOfWeek = "0,1,2,3,4,5,6", // All days
            allowEmergencyApps = true,
            showNotifications = true
        )
        
        val workHoursRestriction = TimeRestriction(
            restrictionType = WORK_HOURS_FOCUS,
            name = "Work Focus Mode",
            description = "Block entertainment apps during work hours",
            startTimeMinutes = 9 * 60, // 9 AM
            endTimeMinutes = 17 * 60, // 5 PM
            appsBlocked = "com.instagram.android,com.twitter.android,com.facebook.katana,com.snapchat.android,com.tiktok.android,com.netflix.mediaclient,com.spotify.music",
            daysOfWeek = "1,2,3,4,5", // Weekdays only
            allowEmergencyApps = true,
            showNotifications = false,
            isEnabled = false // Disabled by default
        )
        
        val morningRoutineRestriction = TimeRestriction(
            restrictionType = MORNING_ROUTINE,
            name = "Morning Routine",
            description = "Delayed social media access until morning tasks completed",
            startTimeMinutes = 6 * 60, // 6 AM
            endTimeMinutes = 9 * 60, // 9 AM
            appsBlocked = "com.instagram.android,com.twitter.android,com.facebook.katana,com.snapchat.android,com.tiktok.android",
            daysOfWeek = "1,2,3,4,5", // Weekdays only
            allowEmergencyApps = true,
            showNotifications = true,
            isEnabled = false // Disabled by default
        )
        
        val mealTimeRestriction = TimeRestriction(
            restrictionType = MEAL_TIME_PROTECTION,
            name = "Mindful Meals",
            description = "Block all apps during designated meal times",
            startTimeMinutes = 12 * 60, // 12 PM lunch
            endTimeMinutes = 13 * 60, // 1 PM
            appsBlocked = "", // All apps except emergency
            daysOfWeek = "0,1,2,3,4,5,6", // All days
            allowEmergencyApps = true,
            showNotifications = true,
            isEnabled = false // Disabled by default
        )
        
        try {
            repository.insertTimeRestriction(bedtimeRestriction)
            repository.insertTimeRestriction(workHoursRestriction)
            repository.insertTimeRestriction(morningRoutineRestriction)
            repository.insertTimeRestriction(mealTimeRestriction)
            
            appLogger.i(TAG, "Default time restrictions created successfully")
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to create default time restrictions", e)
        }
    }

    fun getAllTimeRestrictions(): Flow<List<TimeRestriction>> {
        return repository.getAllTimeRestrictions()
    }

    fun getActiveTimeRestrictions(): Flow<List<TimeRestriction>> {
        return repository.getActiveTimeRestrictions()
    }

    suspend fun isAppBlockedByTimeRestriction(packageName: String): Boolean {
        val calendar = Calendar.getInstance()
        val currentTimeMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // Convert to 0-based (0=Sunday)
        
        return try {
            val activeRestrictions = repository.getActiveRestrictionsForTime(currentTimeMinutes, dayOfWeek)
            
            for (restriction in activeRestrictions) {
                // Check if this is an emergency app and emergency apps are allowed
                if (restriction.allowEmergencyApps && EMERGENCY_APPS.contains(packageName)) {
                    continue
                }
                
                // Parse blocked apps from comma-separated string
                val blockedApps = if (restriction.appsBlocked.isEmpty()) {
                    emptyList<String>()
                } else {
                    restriction.appsBlocked.split(",")
                }
                
                // If blockedApps is empty, block all apps (except emergency if allowed)
                if (blockedApps.isEmpty()) {
                    appLogger.d(TAG, "App $packageName blocked by ${restriction.name} (blocks all apps)")
                    return true
                }
                
                // If the app is in the blocked list
                if (blockedApps.contains(packageName)) {
                    appLogger.d(TAG, "App $packageName blocked by ${restriction.name}")
                    return true
                }
            }
            
            false
        } catch (e: Exception) {
            appLogger.e(TAG, "Error checking time restrictions for $packageName", e)
            false
        }
    }

    suspend fun updateRestrictionEnabled(id: Long, isEnabled: Boolean) {
        try {
            repository.updateRestrictionEnabled(id, isEnabled, System.currentTimeMillis())
            appLogger.i(TAG, "Time restriction $id ${if (isEnabled) "enabled" else "disabled"}")
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to update restriction $id", e)
        }
    }

    suspend fun createCustomRestriction(
        name: String,
        description: String,
        startTimeMinutes: Int,
        endTimeMinutes: Int,
        blockedApps: List<String>,
        daysOfWeek: List<Int>,
        allowEmergencyApps: Boolean = true,
        showNotifications: Boolean = true
    ): Long {
        val restriction = TimeRestriction(
            restrictionType = "custom",
            name = name,
            description = description,
            startTimeMinutes = startTimeMinutes,
            endTimeMinutes = endTimeMinutes,
            appsBlocked = blockedApps.joinToString(","),
            daysOfWeek = daysOfWeek.joinToString(","),
            allowEmergencyApps = allowEmergencyApps,
            showNotifications = showNotifications
        )
        
        return try {
            val id = repository.insertTimeRestriction(restriction)
            appLogger.i(TAG, "Custom time restriction created: $name")
            id
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to create custom restriction: $name", e)
            throw e
        }
    }

    suspend fun getCurrentActiveRestrictions(): List<TimeRestriction> {
        val calendar = Calendar.getInstance()
        val currentTimeMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        
        return try {
            repository.getActiveRestrictionsForTime(currentTimeMinutes, dayOfWeek)
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to get current active restrictions", e)
            emptyList()
        }
    }

    suspend fun checkAndNotifyRestrictionChanges() {
        try {
            val activeRestrictions = getCurrentActiveRestrictions()
            
            if (activeRestrictions.isNotEmpty()) {
                val restrictionNames = activeRestrictions.joinToString(", ") { it.name }
                notificationManager.showMotivationBoost(
                    "⏰ Time restriction active: $restrictionNames. Stay focused!"
                )
            }
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to check and notify restriction changes", e)
        }
    }

    data class RestrictionStatus(
        val restriction: TimeRestriction,
        val isCurrentlyActive: Boolean,
        val nextChangeTimeMinutes: Int? // When this restriction will start/end next
    )
}