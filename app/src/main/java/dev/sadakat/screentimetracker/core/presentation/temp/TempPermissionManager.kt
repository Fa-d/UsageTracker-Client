package dev.sadakat.screentimetracker.core.presentation.temp

import android.content.Context
import dev.sadakat.screentimetracker.core.domain.permissions.PermissionManager
import dev.sadakat.screentimetracker.core.domain.service.PermissionState
import dev.sadakat.screentimetracker.core.domain.error.DomainResult
import dev.sadakat.screentimetracker.utils.PermissionUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

/**
 * Temporary implementation of PermissionManager for Phase 1.
 * This will be replaced with proper infrastructure implementation in Phase 5.
 */
class TempPermissionManager(
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
            hasAccessibilityPermission = accessibility
        )
    }

    override suspend fun checkAllPermissions(): DomainResult<PermissionState> {
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
                hasAccessibilityPermission = hasAccessibility
            )

            DomainResult.success(state)
        } catch (e: Exception) {
            DomainResult.failure(
                dev.sadakat.screentimetracker.core.domain.error.DomainError.SystemError(
                    "Permission check failed",
                    e.message ?: "Unknown error"
                )
            )
        }
    }

    override suspend fun requestUsageStatsPermission(): DomainResult<Unit> {
        return try {
            PermissionUtils.requestUsageStatsPermission(context)
            DomainResult.success(Unit)
        } catch (e: Exception) {
            DomainResult.failure(
                dev.sadakat.screentimetracker.core.domain.error.DomainError.SystemError(
                    "Usage stats permission request failed",
                    e.message ?: "Unknown error"
                )
            )
        }
    }

    override suspend fun requestNotificationPermission(): DomainResult<Unit> {
        return try {
            PermissionUtils.requestNotificationPermission(context)
            DomainResult.success(Unit)
        } catch (e: Exception) {
            DomainResult.failure(
                dev.sadakat.screentimetracker.core.domain.error.DomainError.SystemError(
                    "Notification permission request failed",
                    e.message ?: "Unknown error"
                )
            )
        }
    }

    override suspend fun requestAccessibilityPermission(): DomainResult<Unit> {
        return try {
            PermissionUtils.requestAccessibilityPermission(context)
            DomainResult.success(Unit)
        } catch (e: Exception) {
            DomainResult.failure(
                dev.sadakat.screentimetracker.core.domain.error.DomainError.SystemError(
                    "Accessibility permission request failed",
                    e.message ?: "Unknown error"
                )
            )
        }
    }
}