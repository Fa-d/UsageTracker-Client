package com.example.screentimetracker.domain.service

import android.content.Context
import com.example.screentimetracker.ui.common.error.AppError
import com.example.screentimetracker.ui.common.error.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Service states that the app can be in
 */
sealed class ServiceState {
    object Stopped : ServiceState()
    object Starting : ServiceState()
    object Running : ServiceState()
    object Error : ServiceState()
}

/**
 * Abstract interface for managing app services
 */
interface ServiceManager {
    val serviceState: Flow<ServiceState>

    suspend fun startTrackingService(): Result<Unit>
    suspend fun stopTrackingService(): Result<Unit>
    suspend fun restartTrackingService(): Result<Unit>

    fun isServiceRunning(): Boolean
}

/**
 * Implementation of ServiceManager that handles the app's tracking services
 */
class ScreenTimeServiceManager(
    private val context: Context
) : ServiceManager {

    private val _serviceState = kotlinx.coroutines.flow.MutableStateFlow<ServiceState>(ServiceState.Stopped)
    override val serviceState: Flow<ServiceState> = _serviceState.asStateFlow()

    override suspend fun startTrackingService(): Result<Unit> {
        return try {
            _serviceState.value = ServiceState.Starting

            val intent = android.content.Intent(context, com.example.screentimetracker.services.AppUsageTrackingService::class.java)
            context.startService(intent)

            _serviceState.value = ServiceState.Running
            Result.Success(Unit)
        } catch (e: Exception) {
            _serviceState.value = ServiceState.Error
            Result.Error(AppError.ServiceError("Failed to start tracking service", e))
        }
    }

    override suspend fun stopTrackingService(): Result<Unit> {
        return try {
            val intent = android.content.Intent(context, com.example.screentimetracker.services.AppUsageTrackingService::class.java)
            context.stopService(intent)

            _serviceState.value = ServiceState.Stopped
            Result.Success(Unit)
        } catch (e: Exception) {
            _serviceState.value = ServiceState.Error
            Result.Error(AppError.ServiceError("Failed to stop tracking service", e))
        }
    }

    override suspend fun restartTrackingService(): Result<Unit> {
        stopTrackingService()
        return startTrackingService()
    }

    override fun isServiceRunning(): Boolean {
        return _serviceState.value == ServiceState.Running
    }
}
