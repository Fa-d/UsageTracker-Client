package dev.sadakat.screentimetracker.core.domain.service

import dev.sadakat.screentimetracker.core.domain.error.DomainResult
import kotlinx.coroutines.flow.Flow

/**
 * Domain service for managing app services.
 * This is now just an alias to PlatformServiceManager for backward compatibility.
 * Use PlatformServiceManager directly in new code.
 *
 * @deprecated Use PlatformServiceManager instead
 */
@Deprecated(
    message = "Use PlatformServiceManager instead",
    replaceWith = ReplaceWith("PlatformServiceManager")
)
interface ServiceManager : PlatformServiceManager

/**
 * Service states that the app can be in
 * This is moved to PlatformServiceManager.kt
 *
 * @deprecated Use ServiceState from PlatformServiceManager instead
 */
@Deprecated(
    message = "Use ServiceState from PlatformServiceManager instead",
    replaceWith = ReplaceWith("ServiceState", "dev.sadakat.screentimetracker.core.domain.service.ServiceState")
)
sealed class LegacyServiceState {
    object Stopped : LegacyServiceState()
    object Starting : LegacyServiceState()
    object Running : LegacyServiceState()
    object Error : LegacyServiceState()
}
