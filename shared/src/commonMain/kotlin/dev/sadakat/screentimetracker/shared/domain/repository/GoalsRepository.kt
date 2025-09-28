package dev.sadakat.screentimetracker.shared.domain.repository

import dev.sadakat.screentimetracker.shared.domain.model.Goal
import kotlinx.coroutines.flow.Flow

interface GoalsRepository {
    suspend fun getAllGoals(): Flow<List<Goal>>
    suspend fun getGoalById(id: String): Flow<Goal?>
    suspend fun createGoal(goal: Goal)
    suspend fun updateGoal(goal: Goal)
    suspend fun deleteGoal(id: String)
    suspend fun updateGoalProgress(goalId: String, currentValue: Long)
}