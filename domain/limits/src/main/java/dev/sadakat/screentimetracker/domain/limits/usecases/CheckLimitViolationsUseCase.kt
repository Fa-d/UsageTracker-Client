package dev.sadakat.screentimetracker.domain.limits.usecases

import dev.sadakat.screentimetracker.domain.limits.repository.LimitType
import dev.sadakat.screentimetracker.domain.limits.repository.LimitViolation
import dev.sadakat.screentimetracker.domain.limits.repository.LimitsRepository
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject

class CheckLimitViolationsUseCase @Inject constructor(
    private val repository: LimitsRepository
) {
    companion object {
        private const val TAG = "CheckLimitViolationsUseCase"
    }

    suspend fun checkAppLimitViolation(
        packageName: String,
        currentUsageMinutes: Int
    ): LimitViolationResult {
        return try {
            val appLimit = repository.getAppLimit(packageName)
            if (appLimit == null || !appLimit.isActive) {
                return LimitViolationResult.NoLimit
            }

            val exceedsLimit = currentUsageMinutes > appLimit.dailyLimitMinutes
            if (!exceedsLimit) {
                return LimitViolationResult.WithinLimit
            }

            val exceededBy = currentUsageMinutes - appLimit.dailyLimitMinutes
            val shouldBlock = appLimit.blockWhenExceeded

            // Check if violation already recorded today
            val today = getTodayStart()
            val todayViolations = repository.getLimitViolationsForDate(today).first()
            val existingViolation = todayViolations.find {
                it.limitType == LimitType.APP_LIMIT && it.limitId == packageName
            }

            val violation = LimitViolation(
                limitType = LimitType.APP_LIMIT,
                limitId = packageName,
                limitName = appLimit.appName,
                violationDate = today,
                exceededByMinutes = exceededBy,
                wasBlocked = shouldBlock,
                breaksUsed = 0 // TODO: Track actual breaks used
            )

            // Log new violation or update existing one
            if (existingViolation == null) {
                repository.logLimitViolation(violation)
                Log.w(TAG, "App limit violated: ${appLimit.appName} exceeded by ${exceededBy}min")
            }

            LimitViolationResult.Violation(
                violation = violation,
                shouldBlock = shouldBlock,
                canUseBreak = false // TODO: Implement break logic
            )

        } catch (e: Exception) {
            Log.e(TAG, "Failed to check app limit violation for $packageName", e)
            LimitViolationResult.Error(e)
        }
    }

    suspend fun checkCategoryLimitViolation(
        categoryId: String,
        currentUsageMinutes: Int
    ): LimitViolationResult {
        return try {
            val categoryLimit = repository.getCategoryLimit(categoryId)
            if (categoryLimit == null || !categoryLimit.isActive) {
                return LimitViolationResult.NoLimit
            }

            val exceedsLimit = currentUsageMinutes > categoryLimit.dailyLimitMinutes
            if (!exceedsLimit) {
                return LimitViolationResult.WithinLimit
            }

            val exceededBy = currentUsageMinutes - categoryLimit.dailyLimitMinutes
            val shouldBlock = categoryLimit.blockWhenExceeded

            // Check if violation already recorded today
            val today = getTodayStart()
            val todayViolations = repository.getLimitViolationsForDate(today).first()
            val existingViolation = todayViolations.find {
                it.limitType == LimitType.CATEGORY_LIMIT && it.limitId == categoryId
            }

            val violation = LimitViolation(
                limitType = LimitType.CATEGORY_LIMIT,
                limitId = categoryId,
                limitName = categoryLimit.categoryName,
                violationDate = today,
                exceededByMinutes = exceededBy,
                wasBlocked = shouldBlock,
                breaksUsed = 0 // TODO: Track actual breaks used
            )

            // Log new violation or update existing one
            if (existingViolation == null) {
                repository.logLimitViolation(violation)
                Log.w(TAG, "Category limit violated: ${categoryLimit.categoryName} exceeded by ${exceededBy}min")
            }

            LimitViolationResult.Violation(
                violation = violation,
                shouldBlock = shouldBlock,
                canUseBreak = false // TODO: Implement break logic
            )

        } catch (e: Exception) {
            Log.e(TAG, "Failed to check category limit violation for $categoryId", e)
            LimitViolationResult.Error(e)
        }
    }

    fun getLimitViolationsForDate(date: Long): Flow<List<LimitViolation>> {
        return repository.getLimitViolationsForDate(date)
    }

    fun getLimitViolationsInRange(startDate: Long, endDate: Long): Flow<List<LimitViolation>> {
        return repository.getLimitViolationsInRange(startDate, endDate)
    }

    suspend fun getTodaysViolations(): List<LimitViolation> {
        return try {
            val today = getTodayStart()
            repository.getLimitViolationsForDate(today).first()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get today's violations", e)
            emptyList()
        }
    }

    private fun getTodayStart(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}

sealed class LimitViolationResult {
    object NoLimit : LimitViolationResult()
    object WithinLimit : LimitViolationResult()
    data class Violation(
        val violation: LimitViolation,
        val shouldBlock: Boolean,
        val canUseBreak: Boolean
    ) : LimitViolationResult()
    data class Error(val exception: Exception) : LimitViolationResult()
}