package dev.sadakat.screentimetracker.core.domain.service

import dev.sadakat.screentimetracker.core.domain.error.DomainResult
import kotlinx.coroutines.flow.Flow

/**
 * Domain abstraction for platform-specific service management.
 * This interface removes Android dependencies from the domain layer.
 */
interface PlatformServiceManager {

    /**
     * Observable service state
     */
    val serviceState: Flow<ServiceState>

    /**
     * Starts the usage tracking service
     */
    suspend fun startTrackingService(): DomainResult<Unit>

    /**
     * Stops the usage tracking service
     */
    suspend fun stopTrackingService(): DomainResult<Unit>

    /**
     * Restarts the usage tracking service
     */
    suspend fun restartTrackingService(): DomainResult<Unit>

    /**
     * Checks if the tracking service is currently running
     */
    fun isServiceRunning(): Boolean
}

/**
 * Domain representation of service states
 */
sealed class ServiceState {
    object Stopped : ServiceState()
    object Starting : ServiceState()
    object Running : ServiceState()
    object Error : ServiceState()
}