package dev.sadakat.screentimetracker.core.presentation.temp

import android.content.Context
import dev.sadakat.screentimetracker.core.domain.service.ServiceManager
import dev.sadakat.screentimetracker.core.domain.service.ServiceState
import dev.sadakat.screentimetracker.core.domain.error.DomainResult
import dev.sadakat.screentimetracker.framework.services.AppUsageTrackingService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Temporary implementation of ServiceManager for Phase 1.
 * This will be replaced with proper infrastructure implementation in Phase 5.
 */
class TempServiceManager(
    private val context: Context
) : ServiceManager {

    private val _serviceState = MutableStateFlow<ServiceState>(ServiceState.Stopped)
    override val serviceState: Flow<ServiceState> = _serviceState.asStateFlow()

    override suspend fun startTrackingService(): DomainResult<Unit> {
        return try {
            _serviceState.value = ServiceState.Starting

            val intent = android.content.Intent(context, AppUsageTrackingService::class.java)
            context.startService(intent)

            _serviceState.value = ServiceState.Running
            DomainResult.success(Unit)
        } catch (e: Exception) {
            _serviceState.value = ServiceState.Error
            DomainResult.failure(
                dev.sadakat.screentimetracker.core.domain.error.DomainError.SystemError(
                    "Failed to start tracking service",
                    e.message ?: "Unknown error"
                )
            )
        }
    }

    override suspend fun stopTrackingService(): DomainResult<Unit> {
        return try {
            val intent = android.content.Intent(context, AppUsageTrackingService::class.java)
            context.stopService(intent)

            _serviceState.value = ServiceState.Stopped
            DomainResult.success(Unit)
        } catch (e: Exception) {
            _serviceState.value = ServiceState.Error
            DomainResult.failure(
                dev.sadakat.screentimetracker.core.domain.error.DomainError.SystemError(
                    "Failed to stop tracking service",
                    e.message ?: "Unknown error"
                )
            )
        }
    }

    override suspend fun restartTrackingService(): DomainResult<Unit> {
        stopTrackingService()
        return startTrackingService()
    }

    override fun isServiceRunning(): Boolean {
        return _serviceState.value == ServiceState.Running
    }
}