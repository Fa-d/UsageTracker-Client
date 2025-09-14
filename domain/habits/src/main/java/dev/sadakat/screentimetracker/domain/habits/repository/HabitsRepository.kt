package dev.sadakat.screentimetracker.domain.habits.repository

import dev.sadakat.screentimetracker.core.database.entities.HabitTracker
import kotlinx.coroutines.flow.Flow

interface HabitsRepository {
    // --- Habit CRUD Methods ---
    suspend fun insertHabit(habit: HabitTracker): Long
    suspend fun updateHabit(habit: HabitTracker)
    suspend fun deleteHabit(habitId: String)

    // --- Query Methods ---
    fun getAllHabits(): Flow<List<HabitTracker>>
    fun getHabitsForDate(date: Long): Flow<List<HabitTracker>>
    fun getHabitsByHabitId(habitId: String): Flow<List<HabitTracker>>
    suspend fun getHabitByIdAndDate(habitId: String, date: Long): HabitTracker?

    // --- Statistics Methods ---
    suspend fun getHabitCompletionStats(habitId: String, days: Int): HabitStats
    suspend fun getTotalHabitsCompleted(startDate: Long, endDate: Long): Int
    suspend fun getBestStreakForHabit(habitId: String): Int
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