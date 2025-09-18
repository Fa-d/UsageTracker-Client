package dev.sadakat.screentimetracker.core.domain.usecase

import dev.sadakat.screentimetracker.core.domain.model.UserGoal
import dev.sadakat.screentimetracker.core.domain.model.GoalType
import dev.sadakat.screentimetracker.core.domain.repository.UserGoalRepository
import dev.sadakat.screentimetracker.core.domain.service.GoalProgressService

class ManageUsageLimitsUseCase(
    private val userGoalRepository: UserGoalRepository,
    private val goalProgressService: GoalProgressService
) {
    suspend fun setScreenTimeLimit(limitMillis: Long): UserGoal {
        val goal = UserGoal.screenTimeLimit(limitMillis)
        userGoalRepository.saveGoal(goal)
        return goal
    }

    suspend fun setUnlockLimit(maxUnlocks: Int): UserGoal {
        val goal = UserGoal.unlockLimit(maxUnlocks)
        userGoalRepository.saveGoal(goal)
        return goal
    }

    suspend fun getActiveLimit(goalType: GoalType): UserGoal? {
        return userGoalRepository.getGoalsByType(goalType).firstOrNull { it.isActive }
    }

    suspend fun updateLimit(goalId: String, newTarget: Long) {
        val goal = userGoalRepository.getGoalById(goalId)
        goal?.let {
            val updatedGoal = it.copy(targetValue = newTarget)
            userGoalRepository.updateGoal(updatedGoal)
        }
    }

    suspend fun removeLimit(goalId: String) {
        userGoalRepository.deleteGoal(goalId)
    }

    suspend fun checkLimitViolations(): List<UserGoal> {
        // TODO: Implement limit violation checking
        return emptyList()
    }
}