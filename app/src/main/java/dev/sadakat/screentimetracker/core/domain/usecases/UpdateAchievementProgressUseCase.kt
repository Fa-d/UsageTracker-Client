package dev.sadakat.screentimetracker.core.domain.usecases

import dev.sadakat.screentimetracker.core.domain.repository.TrackerRepository
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import dev.sadakat.screentimetracker.utils.ui.AppNotificationManager
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

class UpdateAchievementProgressUseCase(
    private val repository: TrackerRepository,
    private val notificationManager: AppNotificationManager,
    private val appLogger: AppLogger
) {
    companion object {
        private const val TAG = "UpdateAchievementProgress"
        
        // Achievement IDs from InitializeAchievementsUseCase
        const val DAILY_STREAK_3 = "daily_streak_3"
        const val MINDFUL_MOMENTS_5 = "mindful_moments_5"
        const val FOCUS_CHAMPION_3 = "focus_champion_3"
        const val APP_CLEANER_5 = "app_cleaner_5"
        const val WEEKEND_WARRIOR_2 = "weekend_warrior_2"
        const val EARLY_BIRD_7 = "early_bird_7"
        const val DIGITAL_SUNSET_5 = "digital_sunset_5"
    }
    
    suspend operator fun invoke() {
        try {
            val achievements = repository.getAllAchievements().first()
            
            achievements.forEach { achievement ->
                when (achievement.id) {
                    DAILY_STREAK_3 -> updateDailyStreakProgress(achievement.id)
                    MINDFUL_MOMENTS_5 -> updateMindfulMomentsProgress(achievement.id)
                    FOCUS_CHAMPION_3 -> updateFocusChampionProgress(achievement.id)
                    APP_CLEANER_5 -> updateAppCleanerProgress(achievement.id)
                    WEEKEND_WARRIOR_2 -> updateWeekendWarriorProgress(achievement.id)
                    EARLY_BIRD_7 -> updateEarlyBirdProgress(achievement.id)
                    DIGITAL_SUNSET_5 -> updateDigitalSunsetProgress(achievement.id)
                    else -> {
                        appLogger.w(TAG, "Unknown achievement ID: ${achievement.id}")
                    }
                }
            }
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to update achievement progress", e)
        }
    }
    
    private suspend fun updateDailyStreakProgress(achievementId: String) {
        try {
            // Check consecutive days of meeting screen time goals
            val activeGoals = repository.getActiveGoals().first()
            val dailyScreenTimeGoals = activeGoals.filter { it.goalType == "daily_screen_time" }
            
            if (dailyScreenTimeGoals.isEmpty()) {
                appLogger.d(TAG, "No daily screen time goals set for streak tracking")
                return
            }
            
            val targetMillis = dailyScreenTimeGoals.minOfOrNull { it.targetValue } ?: return
            val currentTime = System.currentTimeMillis()
            
            var currentStreak = 0
            var dateToCheck = getStartOfDay(currentTime)
            
            // Count consecutive days from today backwards
            for (i in 0 until 7) { // Check up to 7 days back
                val dayStart = dateToCheck - (i * 24 * 60 * 60 * 1000L)
                val dayEnd = dayStart + (24 * 60 * 60 * 1000L) - 1
                
                val dayUsage = repository.getAggregatedSessionDataForDay(dayStart, dayEnd)
                val totalUsage = dayUsage.sumOf { it.totalDuration }
                
                if (totalUsage <= targetMillis) {
                    currentStreak++
                } else {
                    break // Streak broken
                }
            }
            
            repository.updateAchievementProgress(achievementId, currentStreak)
            
            if (currentStreak >= 3) {
                unlockAchievementIfNotAlready(achievementId, "🔥 Amazing! 3-day streak achieved!")
            }
            
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to update daily streak progress", e)
        }
    }
    
    private suspend fun updateMindfulMomentsProgress(achievementId: String) {
        try {
            // Count breaks between app sessions today (session gaps > 5 minutes)
            val today = getStartOfDay(System.currentTimeMillis())
            val tomorrow = today + (24 * 60 * 60 * 1000L)
            
            val todaysSessions = repository.getAllSessionsInRange(today, tomorrow).first()
            val sortedSessions = todaysSessions.sortedBy { it.startTimeMillis }
            
            var mindfulBreaks = 0
            
            for (i in 1 until sortedSessions.size) {
                val previousEnd = sortedSessions[i-1].startTimeMillis + sortedSessions[i-1].durationMillis
                val currentStart = sortedSessions[i].startTimeMillis
                val breakDuration = currentStart - previousEnd
                
                // Count as mindful break if gap is 5+ minutes
                if (breakDuration >= TimeUnit.MINUTES.toMillis(5)) {
                    mindfulBreaks++
                }
            }
            
            repository.updateAchievementProgress(achievementId, mindfulBreaks)
            
            if (mindfulBreaks >= 5) {
                unlockAchievementIfNotAlready(achievementId, "🧘 Mindful Moments mastered! 5 breaks taken today!")
            }
            
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to update mindful moments progress", e)
        }
    }
    
    private suspend fun updateFocusChampionProgress(achievementId: String) {
        try {
            // Count successful focus sessions today
            val today = getStartOfDay(System.currentTimeMillis())
            val focusSessions = repository.getFocusSessionsForDate(today)
            
            val todaysSessions = focusSessions.filter { 
                it.startTime >= today && it.startTime < today + (24 * 60 * 60 * 1000L)
            }
            
            val successfulSessions = todaysSessions.count { it.wasSuccessful }
            
            repository.updateAchievementProgress(achievementId, successfulSessions)
            
            if (successfulSessions >= 3) {
                unlockAchievementIfNotAlready(achievementId, "🎯 Focus Champion! 3 successful focus sessions today!")
            }
            
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to update focus champion progress", e)
        }
    }
    
    private suspend fun updateAppCleanerProgress(achievementId: String) {
        try {
            // Count number of apps with limits set
            val limitedApps = repository.getAllLimitedAppsOnce()
            val appCount = limitedApps.size
            
            repository.updateAchievementProgress(achievementId, appCount)
            
            if (appCount >= 5) {
                unlockAchievementIfNotAlready(achievementId, "🧹 App Cleaner! 5 apps limited for better focus!")
            }
            
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to update app cleaner progress", e)
        }
    }
    
    private suspend fun updateWeekendWarriorProgress(achievementId: String) {
        try {
            // Check if user maintained healthy habits for consecutive weekends
            val currentTime = System.currentTimeMillis()
            val calendar = Calendar.getInstance()
            
            var consecutiveHealthyWeekends = 0
            
            // Check last 4 weekends (2 months back)
            for (week in 0 until 4) {
                calendar.timeInMillis = currentTime
                calendar.add(Calendar.WEEK_OF_YEAR, -week)
                
                // Get Saturday and Sunday of this week
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
                val saturday = getStartOfDay(calendar.timeInMillis)
                
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                val sunday = getStartOfDay(calendar.timeInMillis)
                
                val weekendHealthy = isWeekendHealthy(saturday, sunday)
                
                if (weekendHealthy) {
                    consecutiveHealthyWeekends++
                } else {
                    break // Streak broken
                }
            }
            
            repository.updateAchievementProgress(achievementId, consecutiveHealthyWeekends)
            
            if (consecutiveHealthyWeekends >= 2) {
                unlockAchievementIfNotAlready(achievementId, "🌟 Weekend Warrior! 2 consecutive healthy weekends!")
            }
            
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to update weekend warrior progress", e)
        }
    }
    
    private suspend fun updateEarlyBirdProgress(achievementId: String) {
        try {
            // Check consecutive days of first app usage after 8 AM
            val currentTime = System.currentTimeMillis()
            var consecutiveEarlyBirdDays = 0
            
            for (day in 0 until 7) { // Check last 7 days
                val dayStart = getStartOfDay(currentTime - (day * 24 * 60 * 60 * 1000L))
                val dayEnd = dayStart + (24 * 60 * 60 * 1000L) - 1
                
                val daySessions = repository.getAllSessionsInRange(dayStart, dayEnd).first()
                val firstSession = daySessions.minByOrNull { it.startTimeMillis }
                
                if (firstSession != null) {
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = firstSession.startTimeMillis
                    val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
                    
                    if (hourOfDay >= 8) { // First usage after 8 AM
                        consecutiveEarlyBirdDays++
                    } else {
                        break // Streak broken
                    }
                } else {
                    // No usage that day - counts as early bird day
                    consecutiveEarlyBirdDays++
                }
            }
            
            repository.updateAchievementProgress(achievementId, consecutiveEarlyBirdDays)
            
            if (consecutiveEarlyBirdDays >= 7) {
                unlockAchievementIfNotAlready(achievementId, "🌅 Early Bird! 7 days of healthy morning routines!")
            }
            
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to update early bird progress", e)
        }
    }
    
    private suspend fun updateDigitalSunsetProgress(achievementId: String) {
        try {
            // Check consecutive days with no screen time 1 hour before bedtime (10 PM)
            val currentTime = System.currentTimeMillis()
            var consecutiveSunsetDays = 0
            
            for (day in 0 until 7) { // Check last 7 days
                val dayStart = getStartOfDay(currentTime - (day * 24 * 60 * 60 * 1000L))
                
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = dayStart
                calendar.set(Calendar.HOUR_OF_DAY, 22) // 10 PM
                val sunsetStart = calendar.timeInMillis
                
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                val sunsetEnd = calendar.timeInMillis
                
                val sunsetSessions = repository.getAllSessionsInRange(sunsetStart, sunsetEnd).first()
                
                if (sunsetSessions.isEmpty()) {
                    consecutiveSunsetDays++
                } else {
                    break // Streak broken
                }
            }
            
            repository.updateAchievementProgress(achievementId, consecutiveSunsetDays)
            
            if (consecutiveSunsetDays >= 5) {
                unlockAchievementIfNotAlready(achievementId, "🌇 Digital Sunset! 5 days of healthy bedtime routines!")
            }
            
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to update digital sunset progress", e)
        }
    }
    
    private suspend fun unlockAchievementIfNotAlready(achievementId: String, message: String) {
        val achievement = repository.getAchievementById(achievementId)
        if (achievement != null && !achievement.isUnlocked) {
            repository.unlockAchievement(achievementId, System.currentTimeMillis())
            notificationManager.showMotivationBoost(message)
            appLogger.i(TAG, "Achievement unlocked: $achievementId")
        }
    }
    
    private suspend fun isWeekendHealthy(saturday: Long, sunday: Long): Boolean {
        // Define healthy weekend as limited screen time on both days
        val saturdayEnd = saturday + (24 * 60 * 60 * 1000L) - 1
        val sundayEnd = sunday + (24 * 60 * 60 * 1000L) - 1
        
        val saturdayUsage = repository.getAggregatedSessionDataForDay(saturday, saturdayEnd)
        val sundayUsage = repository.getAggregatedSessionDataForDay(sunday, sundayEnd)
        
        val saturdayTotal = saturdayUsage.sumOf { it.totalDuration }
        val sundayTotal = sundayUsage.sumOf { it.totalDuration }
        
        // Consider healthy if both days under 4 hours of screen time
        val healthyLimit = TimeUnit.HOURS.toMillis(4)
        return saturdayTotal <= healthyLimit && sundayTotal <= healthyLimit
    }
    
    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}