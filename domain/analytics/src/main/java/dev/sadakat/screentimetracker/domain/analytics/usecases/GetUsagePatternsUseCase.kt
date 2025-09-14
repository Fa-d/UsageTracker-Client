package dev.sadakat.screentimetracker.domain.analytics.usecases

import dev.sadakat.screentimetracker.domain.analytics.repository.AnalyticsRepository
import dev.sadakat.screentimetracker.domain.analytics.repository.UsagePatterns
import android.util.Log
import javax.inject.Inject

class GetUsagePatternsUseCase @Inject constructor(
    private val repository: AnalyticsRepository
) {
    companion object {
        private const val TAG = "GetUsagePatternsUseCase"
    }

    suspend operator fun invoke(days: Int = 30): Result<UsagePatterns> {
        return try {
            val patterns = repository.getUsagePatterns(days)
            Log.i(TAG, "Retrieved usage patterns for $days days - Peak hours: ${patterns.peakUsageHours.size}")
            Result.success(patterns)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get usage patterns", e)
            Result.failure(e)
        }
    }

    suspend fun getWeeklyPatterns(): Result<UsagePatterns> {
        return invoke(7)
    }

    suspend fun getMonthlyPatterns(): Result<UsagePatterns> {
        return invoke(30)
    }

    suspend fun getQuarterlyPatterns(): Result<UsagePatterns> {
        return invoke(90)
    }

    suspend fun identifyPeakUsageTime(patterns: UsagePatterns): String {
        return try {
            val peakHour = patterns.peakUsageHours.maxByOrNull { it.totalMinutes }?.hour ?: 12
            when (peakHour) {
                in 6..11 -> "Morning (${peakHour}:00)"
                in 12..17 -> "Afternoon (${peakHour}:00)"
                in 18..22 -> "Evening (${peakHour}:00)"
                else -> "Night/Early Morning (${peakHour}:00)"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to identify peak usage time", e)
            "Unknown"
        }
    }

    suspend fun analyzeWeekendBehavior(patterns: UsagePatterns): String {
        return try {
            val comparison = patterns.weekdayVsWeekendUsage
            val increase = comparison.weekendAverage > comparison.weekdayAverage
            val percentage = kotlin.math.abs(comparison.differencePercentage)

            when {
                percentage < 10 -> "Similar usage on weekdays and weekends"
                increase -> "Increased usage on weekends (+${percentage.toInt()}%)"
                else -> "Reduced usage on weekends (-${percentage.toInt()}%)"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to analyze weekend behavior", e)
            "Unable to analyze weekend behavior"
        }
    }
}