package dev.sadakat.screentimetracker.domain.goals.repository

import dev.sadakat.screentimetracker.core.common.model.Achievement
import dev.sadakat.screentimetracker.core.database.entities.UserGoal
import kotlinx.coroutines.flow.Flow

interface GoalsRepository {
    // --- Achievement Methods ---
    fun getAllAchievements(): Flow<List<Achievement>>
    fun getUnlockedAchievements(): Flow<List<Achievement>>
    suspend fun getAchievementById(id: String): Achievement?
    suspend fun insertAchievements(achievements: List<Achievement>)
    suspend fun updateAchievementProgress(id: String, progress: Int)
    suspend fun unlockAchievement(id: String, unlockedDate: Long)

    // --- User Goal Methods ---
    fun getActiveGoals(): Flow<List<UserGoal>>
    fun getGoalsByType(goalType: String): Flow<List<UserGoal>>
    suspend fun insertGoal(goal: UserGoal): Long
    suspend fun updateGoalProgress(id: Long, progress: Long)
}