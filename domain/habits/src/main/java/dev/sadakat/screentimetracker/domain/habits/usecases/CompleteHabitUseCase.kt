package dev.sadakat.screentimetracker.domain.habits.usecases

import dev.sadakat.screentimetracker.domain.habits.repository.HabitsRepository
import android.util.Log
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject

class CompleteHabitUseCase @Inject constructor(
    private val repository: HabitsRepository
) {
    companion object {
        private const val TAG = "CompleteHabitUseCase"
    }

    suspend operator fun invoke(habitId: String): Boolean {
        return try {
            val today = getTodayStart()
            val todaysHabits = repository.getHabitsForDate(today).first()
            val habit = todaysHabits.find { it.habitId == habitId && !it.isCompleted }

            habit?.let {
                val newStreak = it.currentStreak + 1
                val newBestStreak = maxOf(newStreak, it.bestStreak)

                val updatedHabit = it.copy(
                    isCompleted = true,
                    currentStreak = newStreak,
                    bestStreak = newBestStreak,
                    completedAt = System.currentTimeMillis()
                )

                repository.updateHabit(updatedHabit)

                Log.i(TAG, "Habit completed: $habitId, new streak: $newStreak")
                true
            } ?: false

        } catch (e: Exception) {
            Log.e(TAG, "Failed to complete habit: $habitId", e)
            false
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