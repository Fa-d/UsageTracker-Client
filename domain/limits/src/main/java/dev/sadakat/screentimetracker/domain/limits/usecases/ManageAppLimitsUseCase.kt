package dev.sadakat.screentimetracker.domain.limits.usecases

import dev.sadakat.screentimetracker.domain.limits.repository.AppLimit
import dev.sadakat.screentimetracker.domain.limits.repository.LimitsRepository
import android.util.Log
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageAppLimitsUseCase @Inject constructor(
    private val repository: LimitsRepository
) {
    companion object {
        private const val TAG = "ManageAppLimitsUseCase"
    }

    suspend fun createAppLimit(
        packageName: String,
        appName: String,
        dailyLimitMinutes: Int,
        blockWhenExceeded: Boolean = false,
        allowedBreaksPerDay: Int = 0
    ): Result<AppLimit> {
        return try {
            val existingLimit = repository.getAppLimit(packageName)
            if (existingLimit != null) {
                Log.w(TAG, "App limit already exists for $packageName")
                return Result.failure(Exception("App limit already exists for this app"))
            }

            val appLimit = AppLimit(
                packageName = packageName,
                appName = appName,
                dailyLimitMinutes = dailyLimitMinutes,
                blockWhenExceeded = blockWhenExceeded,
                allowedBreaksPerDay = allowedBreaksPerDay
            )

            repository.insertAppLimit(appLimit)
            Log.i(TAG, "Created app limit for $appName: ${dailyLimitMinutes}min")
            Result.success(appLimit)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to create app limit for $packageName", e)
            Result.failure(e)
        }
    }

    suspend fun updateAppLimit(
        packageName: String,
        dailyLimitMinutes: Int? = null,
        blockWhenExceeded: Boolean? = null,
        allowedBreaksPerDay: Int? = null,
        isActive: Boolean? = null
    ): Result<AppLimit> {
        return try {
            val existingLimit = repository.getAppLimit(packageName)
                ?: return Result.failure(Exception("App limit not found for $packageName"))

            val updatedLimit = existingLimit.copy(
                dailyLimitMinutes = dailyLimitMinutes ?: existingLimit.dailyLimitMinutes,
                blockWhenExceeded = blockWhenExceeded ?: existingLimit.blockWhenExceeded,
                allowedBreaksPerDay = allowedBreaksPerDay ?: existingLimit.allowedBreaksPerDay,
                isActive = isActive ?: existingLimit.isActive,
                modifiedAt = System.currentTimeMillis()
            )

            repository.updateAppLimit(updatedLimit)
            Log.i(TAG, "Updated app limit for ${existingLimit.appName}")
            Result.success(updatedLimit)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to update app limit for $packageName", e)
            Result.failure(e)
        }
    }

    suspend fun deleteAppLimit(packageName: String): Result<Unit> {
        return try {
            val existingLimit = repository.getAppLimit(packageName)
            if (existingLimit == null) {
                Log.w(TAG, "App limit not found for $packageName")
                return Result.failure(Exception("App limit not found"))
            }

            repository.deleteAppLimit(packageName)
            Log.i(TAG, "Deleted app limit for ${existingLimit.appName}")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete app limit for $packageName", e)
            Result.failure(e)
        }
    }

    suspend fun getAppLimit(packageName: String): AppLimit? {
        return try {
            repository.getAppLimit(packageName)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get app limit for $packageName", e)
            null
        }
    }

    fun getAllAppLimits(): Flow<List<AppLimit>> {
        return repository.getAllAppLimits()
    }

    fun getActiveAppLimits(): Flow<List<AppLimit>> {
        return repository.getActiveAppLimits()
    }

    suspend fun toggleAppLimitStatus(packageName: String): Result<AppLimit> {
        return try {
            val existingLimit = repository.getAppLimit(packageName)
                ?: return Result.failure(Exception("App limit not found for $packageName"))

            val updatedLimit = existingLimit.copy(
                isActive = !existingLimit.isActive,
                modifiedAt = System.currentTimeMillis()
            )

            repository.updateAppLimit(updatedLimit)
            val status = if (updatedLimit.isActive) "enabled" else "disabled"
            Log.i(TAG, "App limit $status for ${existingLimit.appName}")
            Result.success(updatedLimit)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle app limit status for $packageName", e)
            Result.failure(e)
        }
    }
}