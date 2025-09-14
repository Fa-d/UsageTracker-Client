package dev.sadakat.screentimetracker.domain.habits.usecases

import dev.sadakat.screentimetracker.core.database.entities.HabitTracker
import dev.sadakat.screentimetracker.domain.habits.repository.HabitsRepository
import android.util.Log
import java.util.*
import javax.inject.Inject

class CreateCustomHabitUseCase @Inject constructor(
    private val repository: HabitsRepository
) {
    companion object {
        private const val TAG = "CreateCustomHabitUseCase"
    }

    suspend operator fun invoke(
        habitName: String,
        description: String,
        emoji: String
    ): Long {
        return try {
            val habitId = habitName.lowercase().replace(" ", "_").replace("[^a-z0-9_]".toRegex(), "")
            val today = getTodayStart()

            val habit = HabitTracker(
                habitId = habitId,
                habitName = habitName,
                description = description,
                emoji = emoji,
                date = today
            )

            val id = repository.insertHabit(habit)
            Log.i(TAG, "Custom habit created: $habitName")
            id

        } catch (e: Exception) {
            Log.e(TAG, "Failed to create custom habit: $habitName", e)
            throw e
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