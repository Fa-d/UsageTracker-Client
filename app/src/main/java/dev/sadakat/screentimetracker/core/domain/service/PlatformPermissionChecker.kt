package dev.sadakat.screentimetracker.core.domain.service

import dev.sadakat.screentimetracker.core.domain.error.DomainResult
import kotlinx.coroutines.flow.Flow

/**
 * Domain abstraction for platform-specific permission management.
 * This interface removes Android dependencies from the domain layer.
 */
interface PlatformPermissionChecker {

    /**
     * Observable permission state
     */
    val permissionState: Flow<PermissionState>

    /**
     * Checks all required permissions
     */
    suspend fun checkAllPermissions(): DomainResult<PermissionState>

    /**
     * Requests usage statistics permission
     */
    suspend fun requestUsageStatsPermission(): DomainResult<Unit>

    /**
     * Requests notification permission
     */
    suspend fun requestNotificationPermission(): DomainResult<Unit>

    /**
     * Requests accessibility permission
     */
    suspend fun requestAccessibilityPermission(): DomainResult<Unit>
}

/**
 * Domain representation of app permission states
 */
data class PermissionState(
    val hasUsageStatsPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val hasAccessibilityPermission: Boolean = false
) {
    /**
     * Checks if all required permissions are granted
     * Note: Accessibility is optional, so not included in this check
     */
    val allRequiredPermissionsGranted: Boolean
        get() = hasUsageStatsPermission && hasNotificationPermission

    /**
     * Checks if all permissions (including optional ones) are granted
     */
    val allPermissionsGranted: Boolean
        get() = hasUsageStatsPermission && hasNotificationPermission && hasAccessibilityPermission

    /**
     * Gets the list of missing required permissions
     */
    val missingRequiredPermissions: List<PermissionType>
        get() = buildList {
            if (!hasUsageStatsPermission) add(PermissionType.USAGE_STATS)
            if (!hasNotificationPermission) add(PermissionType.NOTIFICATION)
        }

    /**
     * Gets the list of all missing permissions
     */
    val missingPermissions: List<PermissionType>
        get() = buildList {
            if (!hasUsageStatsPermission) add(PermissionType.USAGE_STATS)
            if (!hasNotificationPermission) add(PermissionType.NOTIFICATION)
            if (!hasAccessibilityPermission) add(PermissionType.ACCESSIBILITY)
        }
}

/**
 * Types of permissions used by the app
 */
enum class PermissionType(val displayName: String, val required: Boolean) {
    USAGE_STATS("Usage Access", true),
    NOTIFICATION("Notifications", true),
    ACCESSIBILITY("Accessibility", false)
}