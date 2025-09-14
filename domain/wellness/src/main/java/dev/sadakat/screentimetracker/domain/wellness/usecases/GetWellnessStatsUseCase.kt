package dev.sadakat.screentimetracker.domain.wellness.usecases

import dev.sadakat.screentimetracker.domain.wellness.repository.WellnessRepository
import dev.sadakat.screentimetracker.domain.wellness.repository.WellnessTrend
import android.util.Log
import javax.inject.Inject

class GetWellnessStatsUseCase @Inject constructor(
    private val repository: WellnessRepository
) {
    companion object {
        private const val TAG = "GetWellnessStatsUseCase"
    }

    suspend fun getAverageScore(days: Int = 7): Double {
        return try {
            repository.getAverageWellnessScore(days)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get average wellness score", e)
            0.0
        }
    }

    suspend fun getWellnessTrend(days: Int = 14): WellnessTrend {
        return try {
            repository.getWellnessTrend(days)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get wellness trend", e)
            WellnessTrend(
                trend = dev.sadakat.screentimetracker.domain.wellness.repository.TrendDirection.STABLE,
                changePercentage = 0f,
                periodDays = days
            )
        }
    }

    suspend fun getBestStreak(): Int {
        return try {
            repository.getBestWellnessStreak()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get best wellness streak", e)
            0
        }
    }

    suspend fun getCurrentStreak(): Int {
        return try {
            repository.getCurrentWellnessStreak()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get current wellness streak", e)
            0
        }
    }
}