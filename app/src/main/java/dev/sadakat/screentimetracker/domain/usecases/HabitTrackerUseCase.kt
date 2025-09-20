package dev.sadakat.screentimetracker.domain.usecases
import dev.sadakat.screentimetracker.core.domain.usecase.GetDashboardDataUseCase

import dev.sadakat.screentimetracker.data.local.entities.HabitTracker
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import dev.sadakat.screentimetracker.utils.ui.AppNotificationManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitTrackerUseCase @Inject constructor(
    private val repository: TrackerRepository,
    private val notificationManager: AppNotificationManager,
    private val appLogger: AppLogger,
    private val focusSessionManagerUseCase: FocusSessionManagerUseCase,
    private val getDashboardDataUseCase: GetDashboardDataUseCase
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

    /**
     * NEW: Automatic habit detection based on actual user behavior
     * This method should be called periodically (e.g., every hour or when app resumes)
     * to automatically complete habits when user achieves the behavior
     */
    suspend fun checkAndCompleteHabitsAutomatically() {
        try {
            val today = getTodayStart()
            val todaysHabits = repository.getHabitsForDate(today).first()
            
            // Check each habit type for automatic completion
            todaysHabits.forEach { habit ->
                if (!habit.isCompleted) {
                    when (habit.habitId) {
                        FOCUS_TIME -> checkFocusTimeHabit(habit)
                        MORNING_NO_PHONE -> checkMorningNoPhoneHabit(habit)
                        BEDTIME_ROUTINE -> checkBedtimeRoutineHabit(habit)
                        BREAK_EVERY_HOUR -> checkBreakEveryHourHabit(habit)
                        MINDFUL_EATING -> checkMindfulEatingHabit(habit)
                        // PHONE_FREE_SOCIAL -> manual for now, could be location-based
                    }
                }
            }
            
            appLogger.i(TAG, "Completed automatic habit checking")
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to check habits automatically", e)
        }
    }

    /**
     * Auto-complete FOCUS_TIME habit when user completes 25+ minute focus session
     */
    private suspend fun checkFocusTimeHabit(habit: HabitTracker) {
        try {
            val today = getTodayStart()
            val todaysFocusSessions = focusSessionManagerUseCase.getFocusSessionsForDate(today)
            
            // Check if user has completed at least one successful focus session of 25+ minutes
            val hasSuccessfulSession = todaysFocusSessions.any { session ->
                session.wasSuccessful && session.actualDurationMillis >= (25 * 60 * 1000L)
            }
            
            if (hasSuccessfulSession) {
                autoCompleteHabit(habit, "Completed 25+ minute focus session")
            }
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to check focus time habit", e)
        }
    }

    /**
     * Auto-complete MORNING_NO_PHONE habit if no phone usage in first hour after typical wake time
     */
    private suspend fun checkMorningNoPhoneHabit(habit: HabitTracker) {
        try {
            val today = getTodayStart()
            
            // Assume typical wake time is 7 AM (could be made configurable)
            val wakeTime = today + (7 * 60 * 60 * 1000L) // 7 AM
            val firstHourEnd = wakeTime + (60 * 60 * 1000L) // 8 AM
            
            // If it's past 8 AM, check if there was usage in the first hour
            val now = System.currentTimeMillis()
            if (now >= firstHourEnd) {
                val dashboardData = getDashboardDataUseCase()
                
                // Check if any app was used between 7-8 AM
                val hadMorningUsage = dashboardData.topAppsToday.any { appSession ->
                    appSession.timeRange.startMillis in wakeTime..firstHourEnd && appSession.durationMillis > 0
                }
                
                if (!hadMorningUsage) {
                    autoCompleteHabit(habit, "No phone usage detected in first hour after waking")
                }
            }
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to check morning no phone habit", e)
        }
    }

    /**
     * Auto-complete BEDTIME_ROUTINE habit if no phone usage 1 hour before typical bedtime
     */
    private suspend fun checkBedtimeRoutineHabit(habit: HabitTracker) {
        try {
            val today = getTodayStart()
            
            // Assume typical bedtime is 10 PM (could be made configurable)
            val bedtime = today + (22 * 60 * 60 * 1000L) // 10 PM
            val digitalSunsetStart = bedtime - (60 * 60 * 1000L) // 9 PM
            
            // If it's past bedtime, check if there was usage in the hour before
            val now = System.currentTimeMillis()
            if (now >= bedtime) {
                val dashboardData = getDashboardDataUseCase()
                
                // Check if any app was used between 9-10 PM
                val hadPreBedtimeUsage = dashboardData.topAppsToday.any { appSession ->
                    appSession.timeRange.startMillis in digitalSunsetStart..bedtime && appSession.durationMillis > 0
                }
                
                if (!hadPreBedtimeUsage) {
                    autoCompleteHabit(habit, "No phone usage detected 1 hour before bedtime")
                }
            }
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to check bedtime routine habit", e)
        }
    }

    /**
     * Auto-complete BREAK_EVERY_HOUR habit if usage shows regular hourly breaks
     */
    private suspend fun checkBreakEveryHourHabit(habit: HabitTracker) {
        try {
            // This is more complex - need to analyze usage patterns throughout the day
            // For now, simplified: check if user has had at least 4 breaks of 5+ minutes
            val today = getTodayStart()
            val now = System.currentTimeMillis()
            
            // Only check after 5 PM to see full day pattern
            val afternoonCheck = today + (17 * 60 * 60 * 1000L) // 5 PM
            if (now >= afternoonCheck) {
                val dashboardData = getDashboardDataUseCase()
                val totalUsageTime = dashboardData.totalScreenTimeToday
                val totalAppsUsed = dashboardData.topAppsToday.size
                
                // Heuristic: If total usage < 6 hours and used multiple apps (indicating breaks)
                // This is simplified - real implementation would analyze session gaps
                if (totalUsageTime < (6 * 60 * 60 * 1000L) && totalAppsUsed >= 3) {
                    autoCompleteHabit(habit, "Usage pattern suggests regular breaks taken")
                }
            }
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to check break every hour habit", e)
        }
    }

    /**
     * Auto-complete MINDFUL_EATING habit if no phone usage during typical meal times
     */
    private suspend fun checkMindfulEatingHabit(habit: HabitTracker) {
        try {
            val today = getTodayStart()
            val now = System.currentTimeMillis()
            
            // Check after dinner time (8 PM) to see if all meals were phone-free
            val dinnerEnd = today + (20 * 60 * 60 * 1000L) // 8 PM
            if (now >= dinnerEnd) {
                val dashboardData = getDashboardDataUseCase()
                
                // Define meal time ranges (could be made configurable)
                val breakfastRange = (today + 8 * 60 * 60 * 1000L) to (today + 9 * 60 * 60 * 1000L) // 8-9 AM
                val lunchRange = (today + 12 * 60 * 60 * 1000L) to (today + 13 * 60 * 60 * 1000L) // 12-1 PM  
                val dinnerRange = (today + 18 * 60 * 60 * 1000L) to (today + 19 * 60 * 60 * 1000L) // 6-7 PM
                
                val mealRanges = listOf(breakfastRange, lunchRange, dinnerRange)
                
                // Check if any app was used during meal times
                val hadMealTimeUsage = dashboardData.topAppsToday.any { appSession ->
                    mealRanges.any { (start, end) ->
                        appSession.timeRange.startMillis in start..end && appSession.durationMillis > 0
                    }
                }
                
                if (!hadMealTimeUsage) {
                    autoCompleteHabit(habit, "No phone usage detected during meal times")
                }
            }
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to check mindful eating habit", e)
        }
    }

    /**
     * Internal method to automatically complete a habit with celebration
     */
    private suspend fun autoCompleteHabit(habit: HabitTracker, reason: String) {
        try {
            val newStreak = habit.currentStreak + 1
            val newBestStreak = maxOf(newStreak, habit.bestStreak)
            
            val updatedHabit = habit.copy(
                isCompleted = true,
                currentStreak = newStreak,
                bestStreak = newBestStreak,
                completedAt = System.currentTimeMillis()
            )
            
            repository.updateHabit(updatedHabit)
            
            // Show automatic completion notification
            notificationManager.showMotivationBoost(
                "🎉 Habit Auto-Completed: '${habit.habitName}'! " +
                "Streak: $newStreak days. Reason: $reason"
            )
            
            appLogger.i(TAG, "Auto-completed habit: ${habit.habitId}, reason: $reason, new streak: $newStreak")
            
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to auto-complete habit: ${habit.habitId}", e)
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