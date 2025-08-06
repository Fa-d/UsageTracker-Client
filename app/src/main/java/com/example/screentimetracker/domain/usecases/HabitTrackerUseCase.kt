package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.HabitTracker
import com.example.screentimetracker.domain.repository.TrackerRepository
import com.example.screentimetracker.utils.logger.AppLogger
import com.example.screentimetracker.utils.ui.AppNotificationManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitTrackerUseCase @Inject constructor(
    private val repository: TrackerRepository,
    private val notificationManager: AppNotificationManager,
    private val appLogger: AppLogger
) {
    companion object {
        private const val TAG = "HabitTrackerUseCase"
        
        // Digital wellness habits from requirements.md
        const val MORNING_NO_PHONE = "morning_no_phone"
        const val BEDTIME_ROUTINE = "bedtime_routine"
        const val BREAK_EVERY_HOUR = "break_every_hour"
        const val MINDFUL_EATING = "mindful_eating"
        const val FOCUS_TIME = "focus_time"
        const val DIGITAL_SUNSET = "digital_sunset"
        const val PHONE_FREE_SOCIAL = "phone_free_social"
    }

    suspend fun initializeDigitalWellnessHabits() {
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
            
            appLogger.i(TAG, "Digital wellness habits initialized for today")
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to initialize digital wellness habits", e)
        }
    }

    suspend fun completeHabit(habitId: String): Boolean {
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
                
                // Show celebration notification for streaks
                when {
                    newStreak == 1 -> notificationManager.showMotivationBoost("🎉 Great start! You completed '${it.habitName}' today!")
                    newStreak % 7 == 0 -> notificationManager.showMotivationBoost("🔥 Amazing! ${newStreak} days streak for '${it.habitName}'!")
                    newStreak % 3 == 0 -> notificationManager.showMotivationBoost("💪 ${newStreak} days in a row! Keep up the great work!")
                }
                
                appLogger.i(TAG, "Habit completed: $habitId, new streak: $newStreak")
                true
            } ?: false
            
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to complete habit: $habitId", e)
            false
        }
    }

    suspend fun resetHabitStreak(habitId: String): Boolean {
        return try {
            val today = getTodayStart()
            val todaysHabits = repository.getHabitsForDate(today).first()
            val habit = todaysHabits.find { it.habitId == habitId }
            
            habit?.let {
                val updatedHabit = it.copy(
                    currentStreak = 0,
                    isCompleted = false,
                    completedAt = null
                )
                
                repository.updateHabit(updatedHabit)
                appLogger.i(TAG, "Habit streak reset: $habitId")
                true
            } ?: false
            
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to reset habit streak: $habitId", e)
            false
        }
    }

    fun getTodaysHabits(): Flow<List<HabitTracker>> {
        return repository.getHabitsForDate(getTodayStart())
    }

    fun getAllHabits(): Flow<List<HabitTracker>> {
        return repository.getAllHabits()
    }

    suspend fun getHabitStats(habitId: String, days: Int = 30): HabitStats {
        return try {
            val endDate = getTodayStart()
            val startDate = endDate - (days * 24 * 60 * 60 * 1000L)
            
            val allHabits = repository.getAllHabits().first()
            val habitHistory = allHabits.filter { 
                it.habitId == habitId && it.date >= startDate && it.date <= endDate 
            }.sortedBy { it.date }
            
            val completedDays = habitHistory.count { it.isCompleted }
            val totalDays = habitHistory.size
            val completionRate = if (totalDays > 0) (completedDays.toFloat() / totalDays.toFloat()) * 100 else 0f
            val currentStreak = habitHistory.lastOrNull()?.currentStreak ?: 0
            val bestStreak = habitHistory.maxOfOrNull { it.bestStreak } ?: 0
            
            HabitStats(
                habitId = habitId,
                habitName = habitHistory.firstOrNull()?.habitName ?: "",
                completedDays = completedDays,
                totalTrackedDays = totalDays,
                completionRate = completionRate,
                currentStreak = currentStreak,
                bestStreak = bestStreak
            )
            
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to get habit stats for: $habitId", e)
            HabitStats(habitId = habitId, habitName = "Unknown")
        }
    }

    suspend fun checkMissedHabits() {
        try {
            val yesterday = getTodayStart() - (24 * 60 * 60 * 1000L)
            val yesterdaysHabits = repository.getHabitsForDate(yesterday).first()
            
            yesterdaysHabits.filter { !it.isCompleted }.forEach { missedHabit ->
                // Reset streak for missed habits
                val updatedHabit = missedHabit.copy(currentStreak = 0)
                repository.updateHabit(updatedHabit)
                
                // Create today's habit entry with reset streak
                val todaysHabit = missedHabit.copy(
                    id = 0, // New ID will be generated
                    date = getTodayStart(),
                    isCompleted = false,
                    currentStreak = 0,
                    completedAt = null,
                    createdAt = System.currentTimeMillis()
                )
                repository.insertHabit(todaysHabit)
            }
            
            appLogger.i(TAG, "Checked and updated missed habits")
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to check missed habits", e)
        }
    }

    suspend fun createCustomHabit(
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
            appLogger.i(TAG, "Custom habit created: $habitName")
            id
            
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to create custom habit: $habitName", e)
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

    data class HabitStats(
        val habitId: String,
        val habitName: String,
        val completedDays: Int = 0,
        val totalTrackedDays: Int = 0,
        val completionRate: Float = 0f,
        val currentStreak: Int = 0,
        val bestStreak: Int = 0
    )
}