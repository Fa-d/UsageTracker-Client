package dev.sadakat.screentimetracker.domain.habits.usecases

import dev.sadakat.screentimetracker.domain.habits.repository.HabitStats
import dev.sadakat.screentimetracker.domain.habits.repository.HabitsRepository
import android.util.Log
import javax.inject.Inject

class GetHabitStatsUseCase @Inject constructor(
    private val repository: HabitsRepository
) {
    companion object {
        private const val TAG = "GetHabitStatsUseCase"
    }

    suspend operator fun invoke(habitId: String, days: Int = 30): HabitStats {
        return try {
            repository.getHabitCompletionStats(habitId, days)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get habit stats for: $habitId", e)
            HabitStats(habitId = habitId, habitName = "Unknown")
        }
    }
}