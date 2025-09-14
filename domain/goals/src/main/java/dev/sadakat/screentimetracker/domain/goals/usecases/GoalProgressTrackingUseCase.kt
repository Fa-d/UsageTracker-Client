package dev.sadakat.screentimetracker.domain.goals.usecases

import dev.sadakat.screentimetracker.domain.goals.repository.GoalsRepository
import android.util.Log
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalProgressTrackingUseCase @Inject constructor(
    private val repository: GoalsRepository
) {
    companion object {
        private const val TAG = "GoalProgressTracking"
    }

    suspend fun updateAllGoalProgress() {
        try {
            val activeGoals = repository.getActiveGoals().first()
            val today = System.currentTimeMillis()
            val dayStart = today - (today % TimeUnit.DAYS.toMillis(1))

            activeGoals.forEach { goal ->
                when (goal.goalType) {
                    "daily_screen_time" -> {
                        // Placeholder - would integrate with tracking data
                        val currentScreenTime = 0L // TODO: Get from tracking repository
                        repository.updateGoalProgress(goal.id, currentScreenTime)
                    }

                    "unlock_frequency" -> {
                        // Placeholder - would integrate with tracking data
                        val currentUnlocks = 0 // TODO: Get from tracking repository
                        repository.updateGoalProgress(goal.id, currentUnlocks.toLong())
                    }

                    "focus_sessions" -> {
                        // Placeholder - would integrate with focus session data
                        val completedSessions = 0 // TODO: Get from tracking repository
                        repository.updateGoalProgress(goal.id, completedSessions.toLong())
                    }
                }
            }

            Log.i(TAG, "Updated progress for ${activeGoals.size} goals")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to update goal progress", e)
        }
    }

    suspend fun checkGoalAchievements() {
        try {
            val activeGoals = repository.getActiveGoals().first()

            activeGoals.forEach { goal ->
                val progressPercentage = if (goal.targetValue > 0) {
                    (goal.currentProgress.toFloat() / goal.targetValue.toFloat()) * 100
                } else 0f

                when {
                    progressPercentage >= 100f && goal.goalType != "daily_screen_time" && goal.goalType != "unlock_frequency" -> {
                        Log.i(TAG, "Goal achieved: ${goal.goalType}")
                    }
                    progressPercentage <= 80f && (goal.goalType == "daily_screen_time" || goal.goalType == "unlock_frequency") -> {
                        if (isEndOfDay()) {
                            Log.i(TAG, "Stayed within limit for: ${goal.goalType}")
                        }
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to check goal achievements", e)
        }
    }

    private fun isEndOfDay(): Boolean {
        val now = System.currentTimeMillis()
        val dayStart = now - (now % TimeUnit.DAYS.toMillis(1))
        val dayEnd = dayStart + TimeUnit.DAYS.toMillis(1)
        val timeUntilMidnight = dayEnd - now

        // Consider it "end of day" if there's less than 2 hours until midnight
        return timeUntilMidnight < TimeUnit.HOURS.toMillis(2)
    }
}