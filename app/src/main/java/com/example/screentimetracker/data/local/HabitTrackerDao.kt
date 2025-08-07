package com.example.screentimetracker.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitTrackerDao {
    @Query("SELECT * FROM habit_tracker ORDER BY date DESC")
    fun getAllHabits(): Flow<List<HabitTracker>>

    @Query("SELECT * FROM habit_tracker WHERE date = :date")
    fun getHabitsForDate(date: Long): Flow<List<HabitTracker>>

    @Query("SELECT * FROM habit_tracker WHERE habitId = :habitId ORDER BY date DESC")
    fun getHabitHistory(habitId: String): Flow<List<HabitTracker>>

    @Query("SELECT * FROM habit_tracker WHERE habitId = :habitId AND date = :date")
    suspend fun getHabitForDate(habitId: String, date: Long): HabitTracker?

    @Query("SELECT MAX(currentStreak) FROM habit_tracker WHERE habitId = :habitId")
    suspend fun getBestStreakForHabit(habitId: String): Int?

    @Query("SELECT COUNT(*) FROM habit_tracker WHERE habitId = :habitId AND isCompleted = 1 AND date >= :startDate")
    suspend fun getCompletedHabitsCount(habitId: String, startDate: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitTracker): Long

    @Update
    suspend fun updateHabit(habit: HabitTracker)

    @Query("UPDATE habit_tracker SET isCompleted = :isCompleted, completedAt = :completedAt, currentStreak = :currentStreak WHERE id = :id")
    suspend fun updateHabitCompletion(id: Long, isCompleted: Boolean, completedAt: Long?, currentStreak: Int)

    @Delete
    suspend fun deleteHabit(habit: HabitTracker)

    // Export methods
    @Query("SELECT * FROM habit_tracker ORDER BY date ASC, habitId ASC")
    suspend fun getAllHabitTrackersForExport(): List<HabitTracker>
}