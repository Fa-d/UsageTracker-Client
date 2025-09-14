package dev.sadakat.screentimetracker.domain.limits.usecases

import dev.sadakat.screentimetracker.domain.limits.repository.LimitsRepository
import android.util.Log
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AppBlockingUseCase @Inject constructor(
    private val repository: LimitsRepository
) {
    companion object {
        private const val TAG = "AppBlockingUseCase"
    }

    suspend fun isAppBlocked(packageName: String): Boolean {
        return try {
            repository.isAppBlocked(packageName)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check if app is blocked: $packageName", e)
            false
        }
    }

    suspend fun isCategoryBlocked(categoryId: String): Boolean {
        return try {
            repository.isCategoryBlocked(categoryId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check if category is blocked: $categoryId", e)
            false
        }
    }

    suspend fun getBlockedApps(): List<String> {
        return try {
            repository.getBlockedApps()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get blocked apps", e)
            emptyList()
        }
    }

    suspend fun getBlockedCategories(): List<String> {
        return try {
            repository.getBlockedCategories()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get blocked categories", e)
            emptyList()
        }
    }

    suspend fun shouldBlockAppUsage(packageName: String, currentUsageMinutes: Int): BlockingDecision {
        return try {
            // Check app-specific limit
            val appLimit = repository.getAppLimit(packageName)
            if (appLimit != null && appLimit.isActive) {
                val exceedsLimit = currentUsageMinutes > appLimit.dailyLimitMinutes
                if (exceedsLimit && appLimit.blockWhenExceeded) {
                    val exceededBy = currentUsageMinutes - appLimit.dailyLimitMinutes
                    Log.i(TAG, "Blocking $packageName - exceeded app limit by ${exceededBy}min")
                    return BlockingDecision.Block(
                        reason = "App limit exceeded by ${exceededBy} minutes",
                        limitType = "app_limit",
                        limitName = appLimit.appName,
                        exceededBy = exceededBy
                    )
                }
            }

            // TODO: Check category-specific limits (would need app categorization data)
            // For now, return allow
            BlockingDecision.Allow

        } catch (e: Exception) {
            Log.e(TAG, "Failed to determine blocking decision for $packageName", e)
            BlockingDecision.Error(e)
        }
    }

    suspend fun requestLimitBreak(
        packageName: String,
        reasonMessage: String? = null
    ): BreakRequestResult {
        return try {
            val appLimit = repository.getAppLimit(packageName)
            if (appLimit == null || !appLimit.isActive) {
                return BreakRequestResult.NoLimitFound
            }

            // TODO: Implement break tracking logic
            // For now, check if breaks are allowed
            if (appLimit.allowedBreaksPerDay <= 0) {
                Log.w(TAG, "No breaks allowed for ${appLimit.appName}")
                return BreakRequestResult.NoBreaksAllowed
            }

            // TODO: Check if daily break limit has been reached
            // TODO: Grant temporary break access
            Log.i(TAG, "Break requested for ${appLimit.appName}: $reasonMessage")

            BreakRequestResult.BreakGranted(
                durationMinutes = 15, // Default 15-minute break
                reason = reasonMessage ?: "User requested break"
            )

        } catch (e: Exception) {
            Log.e(TAG, "Failed to process break request for $packageName", e)
            BreakRequestResult.Error(e)
        }
    }

    suspend fun getAllBlockingSettings(): BlockingSettings {
        return try {
            val appLimits = repository.getActiveAppLimits().first()
            val categoryLimits = repository.getActiveCategoryLimits().first()
            val blockedApps = repository.getBlockedApps()
            val blockedCategories = repository.getBlockedCategories()

            BlockingSettings(
                activeAppLimits = appLimits.size,
                activeCategoryLimits = categoryLimits.size,
                blockedAppsCount = blockedApps.size,
                blockedCategoriesCount = blockedCategories.size,
                totalBlockingRules = appLimits.count { it.blockWhenExceeded } +
                                   categoryLimits.count { it.blockWhenExceeded }
            )

        } catch (e: Exception) {
            Log.e(TAG, "Failed to get blocking settings", e)
            BlockingSettings()
        }
    }
}

sealed class BlockingDecision {
    object Allow : BlockingDecision()
    data class Block(
        val reason: String,
        val limitType: String,
        val limitName: String,
        val exceededBy: Int
    ) : BlockingDecision()
    data class Error(val exception: Exception) : BlockingDecision()
}

sealed class BreakRequestResult {
    object NoLimitFound : BreakRequestResult()
    object NoBreaksAllowed : BreakRequestResult()
    object BreakLimitExceeded : BreakRequestResult()
    data class BreakGranted(
        val durationMinutes: Int,
        val reason: String
    ) : BreakRequestResult()
    data class Error(val exception: Exception) : BreakRequestResult()
}

data class BlockingSettings(
    val activeAppLimits: Int = 0,
    val activeCategoryLimits: Int = 0,
    val blockedAppsCount: Int = 0,
    val blockedCategoriesCount: Int = 0,
    val totalBlockingRules: Int = 0
)