package dev.sadakat.screentimetracker.domain.habits.usecases

import dev.sadakat.screentimetracker.core.database.entities.HabitTracker
import dev.sadakat.screentimetracker.domain.habits.repository.HabitsRepository
import android.util.Log
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject

class InitializeDigitalWellnessHabitsUseCase @Inject constructor(
    private val repository: HabitsRepository
) {
    companion object {
        private const val TAG = "InitializeHabits"

        // Digital wellness habits
        const val MORNING_NO_PHONE = "morning_no_phone"
        const val BEDTIME_ROUTINE = "bedtime_routine"
        const val BREAK_EVERY_HOUR = "break_every_hour"
        const val MINDFUL_EATING = "mindful_eating"
        const val FOCUS_TIME = "focus_time"
        const val DIGITAL_SUNSET = "digital_sunset"
        const val PHONE_FREE_SOCIAL = "phone_free_social"
    }

    suspend operator fun invoke() {
        val today = getTodayStart()

        val digitalWellnessHabits = listOf(
            HabitTracker(
                habitId = MORNING_NO_PHONE,
                habitName = "Morning Phone-Free",
                description = "No phone for first hour after waking up",
                emoji = "🌅",
                date = today
            ),
            HabitTracker(
                habitId = BEDTIME_ROUTINE,
                habitName = "Digital Sunset",
                description = "No screens 1 hour before bed",
                emoji = "🌙",
                date = today
            ),
            HabitTracker(
                habitId = BREAK_EVERY_HOUR,
                habitName = "Hourly Breaks",
                description = "Take a break every hour from screen",
                emoji = "⏰",
                date = today
            ),
            HabitTracker(
                habitId = MINDFUL_EATING,
                habitName = "Mindful Meals",
                description = "Phone-free during all meals",
                emoji = "🍽️",
                date = today
            ),
            HabitTracker(
                habitId = FOCUS_TIME,
                habitName = "Focus Session",
                description = "Complete at least one 25-minute focus session",
                emoji = "🎯",
                date = today
            ),
            HabitTracker(
                habitId = PHONE_FREE_SOCIAL,
                habitName = "Phone-Free Social Time",
                description = "No phone during social interactions",
                emoji = "👥",
                date = today
            )
        )

        try {
            // Check if habits already exist for today
            val existingHabits = repository.getHabitsForDate(today).first()
            val existingHabitIds = existingHabits.map { it.habitId }.toSet()

            digitalWellnessHabits.forEach { habit ->
                if (!existingHabitIds.contains(habit.habitId)) {
                    repository.insertHabit(habit)
                }
            }

            Log.i(TAG, "Digital wellness habits initialized for today")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize digital wellness habits", e)
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