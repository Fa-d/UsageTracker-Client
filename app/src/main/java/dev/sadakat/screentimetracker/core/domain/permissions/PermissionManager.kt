package dev.sadakat.screentimetracker.core.domain.permissions

import dev.sadakat.screentimetracker.core.domain.service.PlatformPermissionChecker
import dev.sadakat.screentimetracker.core.domain.service.PermissionState
import dev.sadakat.screentimetracker.core.domain.error.DomainResult
import kotlinx.coroutines.flow.Flow

/**
 * Domain interface for managing app permissions.
 * This is now just an alias to PlatformPermissionChecker for backward compatibility.
 * Use PlatformPermissionChecker directly in new code.
 *
 * @deprecated Use PlatformPermissionChecker instead
 */
@Deprecated(
    message = "Use PlatformPermissionChecker instead",
    replaceWith = ReplaceWith(
        "PlatformPermissionChecker",
        "dev.sadakat.screentimetracker.core.domain.service.PlatformPermissionChecker"
    )
)
interface PermissionManager : PlatformPermissionChecker

/**
 * Legacy permission state - moved to PlatformPermissionChecker
 *
 * @deprecated Use PermissionState from PlatformPermissionChecker instead
 */
@Deprecated(
    message = "Use PermissionState from PlatformPermissionChecker instead",
    replaceWith = ReplaceWith(
        "PermissionState",
        "dev.sadakat.screentimetracker.core.domain.service.PermissionState"
    )
)
data class LegacyPermissionState(
    val hasUsageStatsPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val hasAccessibilityPermission: Boolean = false,
    val allRequiredPermissionsGranted: Boolean = false
)