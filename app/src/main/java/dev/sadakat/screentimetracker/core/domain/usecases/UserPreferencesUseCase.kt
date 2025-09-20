package dev.sadakat.screentimetracker.core.domain.usecases

import dev.sadakat.screentimetracker.core.data.local.entities.UserPreferences
import dev.sadakat.screentimetracker.core.data.local.dao.UserPreferencesDao
import dev.sadakat.screentimetracker.core.data.local.entities.ThemeMode
import dev.sadakat.screentimetracker.core.data.local.entities.ColorScheme
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
    
}