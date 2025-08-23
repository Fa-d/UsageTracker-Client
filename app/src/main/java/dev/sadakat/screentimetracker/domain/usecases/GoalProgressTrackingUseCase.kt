package dev.sadakat.screentimetracker.domain.usecases

import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import dev.sadakat.screentimetracker.utils.ui.AppNotificationManager
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalProgressTrackingUseCase @Inject constructor(
    private val repository: TrackerRepository,
    private val notificationManager: AppNotificationManager,
    private val appLogger: AppLogger
) {
    companion object {
        private const val TAG = "GoalProgressTracking"
    }

    suspend fun updateAllGoalProgress() {
        try {
            val activeGoals = repository.getActiveGoals().first()
            val today = System.currentTimeMillis()
            val dayStart = today - (today % TimeUnit.DAYS.toMillis(1))
            val dayEnd = dayStart + TimeUnit.DAYS.toMillis(1)

            activeGoals.forEach { goal ->
                when (goal.goalType) {
                    SmartGoalSettingUseCase.DAILY_SCREEN_TIME -> {
                        val currentScreenTime = repository.getTotalScreenTimeFromSessionsInRange(dayStart, dayEnd).first() ?: 0L
                        repository.updateGoalProgress(goal.id, currentScreenTime)
                        
                        // Check if goal is exceeded
                        if (currentScreenTime > goal.targetValue) {
                            notificationManager.showGoalExceededWarning(
                                "⚠️ Screen time goal exceeded! You've used more than your daily limit."
                            )
                        } else if (currentScreenTime > goal.targetValue * 0.8) {
                            notificationManager.showGoalWarning(
                                "⏰ You're at 80% of your daily screen time goal. Consider taking a break!"
                            )
                        }
                    }
                    
                    SmartGoalSettingUseCase.UNLOCK_FREQUENCY -> {
                        val currentUnlocks = repository.getUnlockCountForDay(dayStart, dayEnd)
                        repository.updateGoalProgress(goal.id, currentUnlocks.toLong())
                        
                        // Check if goal is exceeded
                        if (currentUnlocks > goal.targetValue) {
                            notificationManager.showGoalExceededWarning(
                                "📱 You've unlocked your phone more than your daily limit!"
                            )
                        } else if (currentUnlocks > goal.targetValue * 0.8) {
                            notificationManager.showGoalWarning(
                                "📱 You're at 80% of your daily unlock limit. Try to be more mindful!"
                            )
                        }
                    }
                    
                    SmartGoalSettingUseCase.FOCUS_SESSIONS -> {
                        val focusSessions = repository.getFocusSessionsForDate(dayStart)
                        val completedSessions = focusSessions.count { it.wasSuccessful }
                        repository.updateGoalProgress(goal.id, completedSessions.toLong())
                        
                        // Encourage if goal is reached
                        if (completedSessions >= goal.targetValue) {
                            notificationManager.showMotivationBoost(
                                "🎯 Great job! You've completed your daily focus session goal!"
                            )
                        }
                    }
                    
                    SmartGoalSettingUseCase.APP_SPECIFIC_LIMIT -> {
                        val packageName = goal.packageName
                        if (!packageName.isNullOrBlank()) {
                            val appUsage = repository.getTotalDurationForAppInRange(packageName, dayStart, dayEnd).first() ?: 0L
                            repository.updateGoalProgress(goal.id, appUsage)
                            
                            // Check if app limit is exceeded
                            if (appUsage > goal.targetValue) {
                                notificationManager.showGoalExceededWarning(
                                    "📱 You've exceeded your daily limit for ${getAppDisplayName(packageName)}!"
                                )
                            } else if (appUsage > goal.targetValue * 0.8) {
                                notificationManager.showGoalWarning(
                                    "📱 You're at 80% of your limit for ${getAppDisplayName(packageName)}."
                                )
                            }
                        }
                    }
                }
            }
            
            appLogger.i(TAG, "Updated progress for ${activeGoals.size} goals")
            
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to update goal progress", e)
        }
    }

    suspend fun checkGoalAchievements() {
        try {
            val activeGoals = repository.getActiveGoals().first()
            val today = System.currentTimeMillis()
            val dayStart = today - (today % TimeUnit.DAYS.toMillis(1))
            val dayEnd = dayStart + TimeUnit.DAYS.toMillis(1)

            activeGoals.forEach { goal ->
                val progressPercentage = if (goal.targetValue > 0) {
                    (goal.currentProgress.toFloat() / goal.targetValue.toFloat()) * 100
                } else 0f

                when {
                    progressPercentage >= 100f && goal.goalType != SmartGoalSettingUseCase.DAILY_SCREEN_TIME && goal.goalType != SmartGoalSettingUseCase.UNLOCK_FREQUENCY -> {
                        // For positive goals (focus sessions, etc.), celebrate completion
                        notificationManager.showMotivationBoost(
                            "🎉 Goal achieved! You've completed your ${getGoalTypeDisplayName(goal.goalType)} goal!"
                        )
                    }
                    progressPercentage <= 80f && (goal.goalType == SmartGoalSettingUseCase.DAILY_SCREEN_TIME || goal.goalType == SmartGoalSettingUseCase.UNLOCK_FREQUENCY) -> {
                        // For limit goals, celebrate staying under the limit
                        if (isEndOfDay()) {
                            notificationManager.showMotivationBoost(
                                "✨ Great job staying within your ${getGoalTypeDisplayName(goal.goalType)} limit today!"
                            )
                        }
                    }
                }
            }
            
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to check goal achievements", e)
        }
    }

    private fun getAppDisplayName(packageName: String): String {
        return packageName.split(".").lastOrNull()?.replaceFirstChar { it.uppercase() } ?: packageName
    }

    private fun getGoalTypeDisplayName(goalType: String): String {
        return when (goalType) {
            SmartGoalSettingUseCase.DAILY_SCREEN_TIME -> "screen time"
            SmartGoalSettingUseCase.UNLOCK_FREQUENCY -> "unlock frequency"
            SmartGoalSettingUseCase.FOCUS_SESSIONS -> "focus sessions"
            SmartGoalSettingUseCase.APP_SPECIFIC_LIMIT -> "app limit"
            SmartGoalSettingUseCase.SESSION_LIMIT -> "session limit"
            SmartGoalSettingUseCase.BREAK_GOALS -> "break goals"
            else -> goalType
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