package dev.sadakat.screentimetracker.domain.analytics.usecases

import dev.sadakat.screentimetracker.domain.analytics.repository.AnalyticsRepository
import dev.sadakat.screentimetracker.domain.analytics.repository.AppUsageInsights
import android.util.Log
import javax.inject.Inject

class GetAppUsageInsightsUseCase @Inject constructor(
    private val repository: AnalyticsRepository
) {
    companion object {
        private const val TAG = "GetAppUsageInsightsUseCase"
    }

    suspend operator fun invoke(days: Int = 7): Result<AppUsageInsights> {
        return try {
            val insights = repository.getAppUsageInsights(days)
            Log.i(TAG, "Retrieved app usage insights for $days days: ${insights.totalScreenTime}ms total screen time")
            Result.success(insights)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get app usage insights", e)
            Result.failure(e)
        }
    }

    suspend fun getWeeklyInsights(): Result<AppUsageInsights> {
        return invoke(7)
    }

    suspend fun getMonthlyInsights(): Result<AppUsageInsights> {
        return invoke(30)
    }

    suspend fun getCustomPeriodInsights(startDate: Long, endDate: Long): Result<AppUsageInsights> {
        val daysDiff = ((endDate - startDate) / (24 * 60 * 60 * 1000)).toInt()
        return invoke(daysDiff.coerceAtLeast(1))
    }
}