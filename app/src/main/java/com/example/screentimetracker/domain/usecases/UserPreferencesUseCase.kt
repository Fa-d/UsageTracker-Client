package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.UserPreferences
import com.example.screentimetracker.data.local.UserPreferencesDao
import com.example.screentimetracker.data.local.ThemeMode
import com.example.screentimetracker.data.local.ColorScheme
import com.example.screentimetracker.data.local.PersonalityMode
import com.example.screentimetracker.data.local.DashboardLayout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesUseCase @Inject constructor(
    private val userPreferencesDao: UserPreferencesDao
) {
    
    fun getUserPreferences(): Flow<UserPreferences> {
        return userPreferencesDao.getUserPreferences()
            .map { preferences ->
                preferences ?: getDefaultPreferences()
            }
    }
    
    suspend fun getUserPreferencesOnce(): UserPreferences {
        return userPreferencesDao.getUserPreferencesSync() ?: getDefaultPreferences()
    }
    
    private fun getDefaultPreferences(): UserPreferences {
        return UserPreferences()
    }
    
    suspend fun updateThemeMode(themeMode: ThemeMode) {
        ensurePreferencesExist()
        userPreferencesDao.updateThemeMode(themeMode.name)
    }
    
    suspend fun updateColorScheme(colorScheme: ColorScheme) {
        ensurePreferencesExist()
        userPreferencesDao.updateColorScheme(colorScheme.name)
    }
    
    suspend fun updatePersonalityMode(personalityMode: PersonalityMode) {
        ensurePreferencesExist()
        userPreferencesDao.updatePersonalityMode(personalityMode.name)
    }
    
    suspend fun updateDashboardLayout(dashboardLayout: DashboardLayout) {
        ensurePreferencesExist()
        userPreferencesDao.updateDashboardLayout(dashboardLayout.name)
    }
    
    suspend fun updateMotivationalMessages(enabled: Boolean) {
        ensurePreferencesExist()
        userPreferencesDao.updateMotivationalMessagesEnabled(enabled)
    }
    
    suspend fun updateAchievementCelebrations(enabled: Boolean) {
        ensurePreferencesExist()
        userPreferencesDao.updateAchievementCelebrationsEnabled(enabled)
    }
    
    suspend fun updateBreakReminders(enabled: Boolean) {
        ensurePreferencesExist()
        userPreferencesDao.updateBreakRemindersEnabled(enabled)
    }
    
    suspend fun updateWellnessCoaching(enabled: Boolean) {
        ensurePreferencesExist()
        userPreferencesDao.updateWellnessCoachingEnabled(enabled)
    }
    
    suspend fun updateDefaultFocusDuration(minutes: Int) {
        ensurePreferencesExist()
        userPreferencesDao.insertOrUpdateUserPreferences(
            getUserPreferencesOnce().copy(
                defaultFocusDurationMinutes = minutes,
                updatedAt = System.currentTimeMillis()
            )
        )
    }
    
    suspend fun updateFocusModeEnabled(enabled: Boolean) {
        ensurePreferencesExist()
        userPreferencesDao.insertOrUpdateUserPreferences(
            getUserPreferencesOnce().copy(
                focusModeEnabled = enabled,
                updatedAt = System.currentTimeMillis()
            )
        )
    }
    
    suspend fun updateNotificationSound(soundName: String) {
        ensurePreferencesExist()
        userPreferencesDao.insertOrUpdateUserPreferences(
            getUserPreferencesOnce().copy(
                notificationSound = soundName,
                updatedAt = System.currentTimeMillis()
            )
        )
    }
    
    suspend fun saveAllPreferences(preferences: UserPreferences) {
        userPreferencesDao.insertOrUpdateUserPreferences(preferences.copy(updatedAt = System.currentTimeMillis()))
    }
    
    private suspend fun ensurePreferencesExist() {
        val existing = userPreferencesDao.getUserPreferencesSync()
        if (existing == null) {
            userPreferencesDao.insertOrUpdateUserPreferences(getDefaultPreferences())
        }
    }
    
    // Convenience methods for getting parsed enums
    suspend fun getThemeMode(): ThemeMode {
        val preferences = getUserPreferencesOnce()
        return try {
            ThemeMode.valueOf(preferences.themeMode)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }
    
    suspend fun getColorScheme(): ColorScheme {
        val preferences = getUserPreferencesOnce()
        return try {
            ColorScheme.valueOf(preferences.colorScheme)
        } catch (e: IllegalArgumentException) {
            ColorScheme.DEFAULT
        }
    }
    
    suspend fun getPersonalityMode(): PersonalityMode {
        val preferences = getUserPreferencesOnce()
        return try {
            PersonalityMode.valueOf(preferences.personalityMode)
        } catch (e: IllegalArgumentException) {
            PersonalityMode.GENTLE_GUIDE
        }
    }
    
    suspend fun getDashboardLayout(): DashboardLayout {
        val preferences = getUserPreferencesOnce()
        return try {
            DashboardLayout.valueOf(preferences.dashboardLayout)
        } catch (e: IllegalArgumentException) {
            DashboardLayout.STANDARD
        }
    }
    
    // Get motivational message based on personality mode
    suspend fun getMotivationalMessage(context: MotivationContext): String {
        val personalityMode = getPersonalityMode()
        return when (personalityMode) {
            PersonalityMode.STRICT_COACH -> getStrictCoachMessage(context)
            PersonalityMode.GENTLE_GUIDE -> getGentleGuideMessage(context)
            PersonalityMode.MOTIVATIONAL_BUDDY -> getMotivationalBuddyMessage(context)
        }
    }
    
    private fun getStrictCoachMessage(context: MotivationContext): String {
        return when (context) {
            MotivationContext.TIME_WARNING -> "Time's up! You need to stop using this app now."
            MotivationContext.BREAK_REMINDER -> "Take a break immediately. Your digital wellness depends on it."
            MotivationContext.GOAL_ACHIEVED -> "Good. You met your goal. Keep this discipline up."
            MotivationContext.GOAL_MISSED -> "You failed to meet your goal. Do better tomorrow."
            MotivationContext.ACHIEVEMENT_UNLOCK -> "Achievement unlocked. Stay focused on your goals."
        }
    }
    
    private fun getGentleGuideMessage(context: MotivationContext): String {
        return when (context) {
            MotivationContext.TIME_WARNING -> "You've been using this app for a while. Consider taking a break when you're ready."
            MotivationContext.BREAK_REMINDER -> "How about a short break? Your mind and body will thank you."
            MotivationContext.GOAL_ACHIEVED -> "Great job reaching your goal! You're building healthy digital habits."
            MotivationContext.GOAL_MISSED -> "That's okay, tomorrow is a new day to work toward your goals."
            MotivationContext.ACHIEVEMENT_UNLOCK -> "Wonderful! You've unlocked a new achievement on your wellness journey."
        }
    }
    
    private fun getMotivationalBuddyMessage(context: MotivationContext): String {
        return when (context) {
            MotivationContext.TIME_WARNING -> "Hey buddy! Time to switch gears and do something awesome offline! 🌟"
            MotivationContext.BREAK_REMINDER -> "Break time, champion! Let's get some fresh air or stretch those muscles! 💪"
            MotivationContext.GOAL_ACHIEVED -> "YES! You absolutely crushed that goal! I'm so proud of you! 🎉"
            MotivationContext.GOAL_MISSED -> "No worries, superstar! Every pro athlete has off days. Tomorrow we'll bounce back stronger! 🚀"
            MotivationContext.ACHIEVEMENT_UNLOCK -> "BOOM! New achievement unlocked! You're on fire! 🔥✨"
        }
    }
}

enum class MotivationContext {
    TIME_WARNING,
    BREAK_REMINDER,
    GOAL_ACHIEVED,
    GOAL_MISSED,
    ACHIEVEMENT_UNLOCK
}