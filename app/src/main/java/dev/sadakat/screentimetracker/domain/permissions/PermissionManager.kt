package dev.sadakat.screentimetracker.domain.permissions

import android.content.Context
import dev.sadakat.screentimetracker.core.presentation.ui.common.error.AppError
import dev.sadakat.screentimetracker.core.presentation.ui.common.error.Result
import dev.sadakat.screentimetracker.utils.PermissionUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

/**
 * Represents the state of app permissions
 */
data class PermissionState(
    val hasUsageStatsPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val hasAccessibilityPermission: Boolean = false,
    val allRequiredPermissionsGranted: Boolean = false
)

/**
 * Interface for managing app permissions
 */
interface PermissionManager {
    val permissionState: Flow<PermissionState>

    suspend fun checkAllPermissions(): Result<PermissionState>
    suspend fun requestUsageStatsPermission(): Result<Unit>
    suspend fun requestNotificationPermission(): Result<Unit>
    suspend fun requestAccessibilityPermission(): Result<Unit>
}

/**
 * Implementation of PermissionManager
 */
class AppPermissionManager(
    private val context: Context
) : PermissionManager {

    private val _usageStatsPermission = MutableStateFlow(false)
    private val _notificationPermission = MutableStateFlow(false)
    private val _accessibilityPermission = MutableStateFlow(false)

    override val permissionState: Flow<PermissionState> = combine(
        _usageStatsPermission,
        _notificationPermission,
        _accessibilityPermission
    ) { usage, notification, accessibility ->
        PermissionState(
            hasUsageStatsPermission = usage,
            hasNotificationPermission = notification,
            hasAccessibilityPermission = accessibility,
            allRequiredPermissionsGranted = usage && notification // Accessibility is optional
        )
    }

    override suspend fun checkAllPermissions(): Result<PermissionState> {
        return try {
            val hasUsageStats = PermissionUtils.hasUsageStatsPermission(context)
            val hasNotification = PermissionUtils.hasNotificationPermission(context)
            val hasAccessibility = PermissionUtils.hasAccessibilityPermission(context)

            _usageStatsPermission.value = hasUsageStats
            _notificationPermission.value = hasNotification
            _accessibilityPermission.value = hasAccessibility

            val state = PermissionState(
                hasUsageStatsPermission = hasUsageStats,
                hasNotificationPermission = hasNotification,
                hasAccessibilityPermission = hasAccessibility,
                allRequiredPermissionsGranted = hasUsageStats && hasNotification
            )

            Result.Success(state)
        } catch (e: Exception) {
            Result.Error(AppError.PermissionError("Failed to check permissions", e))
        }
    }

    override suspend fun requestUsageStatsPermission(): Result<Unit> {
        return try {
            PermissionUtils.requestUsageStatsPermission(context)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(AppError.PermissionError("Failed to request usage stats permission", e))
        }
    }

    override suspend fun requestNotificationPermission(): Result<Unit> {
        return try {
            PermissionUtils.requestNotificationPermission(context)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(AppError.PermissionError("Failed to request notification permission", e))
        }
    }

    override suspend fun requestAccessibilityPermission(): Result<Unit> {
        return try {
            PermissionUtils.requestAccessibilityPermission(context)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(AppError.PermissionError("Failed to request accessibility permission", e))
        }
    }
}