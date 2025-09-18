package dev.sadakat.screentimetracker.data.repository

import dev.sadakat.screentimetracker.data.local.entities.UserPreferences
import dev.sadakat.screentimetracker.data.local.dao.UserPreferencesDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val userPreferencesDao: UserPreferencesDao
) {
    
    fun getUserPreferences(): Flow<UserPreferences?> {
        return userPreferencesDao.getUserPreferences()
    }
    
    suspend fun getUserPreferencesSync(): UserPreferences? {
        return userPreferencesDao.getUserPreferencesSync()
    }
    
    suspend fun insertOrUpdateUserPreferences(preferences: UserPreferences) {
        userPreferencesDao.insertOrUpdateUserPreferences(preferences)
    }
    
    suspend fun updateThemeMode(themeMode: String) {
        userPreferencesDao.updateThemeMode(themeMode, System.currentTimeMillis())
    }
    
    suspend fun updateColorScheme(colorScheme: String) {
        userPreferencesDao.updateColorScheme(colorScheme, System.currentTimeMillis())
    }
    
    suspend fun updateNotificationSound(notificationSound: String) {
        userPreferencesDao.updateNotificationSound(notificationSound, System.currentTimeMillis())
    }
    
    suspend fun updateMotivationalMessagesEnabled(enabled: Boolean) {
        userPreferencesDao.updateMotivationalMessagesEnabled(enabled, System.currentTimeMillis())
    }
    
    suspend fun updateAchievementCelebrationsEnabled(enabled: Boolean) {
        userPreferencesDao.updateAchievementCelebrationsEnabled(enabled, System.currentTimeMillis())
    }
    
    suspend fun updateBreakRemindersEnabled(enabled: Boolean) {
        userPreferencesDao.updateBreakRemindersEnabled(enabled, System.currentTimeMillis())
    }
    
    suspend fun updateWellnessCoachingEnabled(enabled: Boolean) {
        userPreferencesDao.updateWellnessCoachingEnabled(enabled, System.currentTimeMillis())
    }
    
    suspend fun updateDefaultFocusDurationMinutes(duration: Int?) {
        userPreferencesDao.updateDefaultFocusDurationMinutes(duration, System.currentTimeMillis())
    }
    
    suspend fun updateFocusModeEnabled(enabled: Boolean) {
        userPreferencesDao.updateFocusModeEnabled(enabled, System.currentTimeMillis())
    }
    
    // AI Features
    suspend fun updateAIFeaturesEnabled(enabled: Boolean) {
        userPreferencesDao.updateAIFeaturesEnabled(enabled, System.currentTimeMillis())
    }
    
    suspend fun updateAIInsightsEnabled(enabled: Boolean) {
        userPreferencesDao.updateAIInsightsEnabled(enabled, System.currentTimeMillis())
    }
    
    suspend fun updateAIGoalRecommendationsEnabled(enabled: Boolean) {
        userPreferencesDao.updateAIGoalRecommendationsEnabled(enabled, System.currentTimeMillis())
    }
    
    suspend fun updateAIPredictiveCoachingEnabled(enabled: Boolean) {
        userPreferencesDao.updateAIPredictiveCoachingEnabled(enabled, System.currentTimeMillis())
    }
    
    suspend fun updateAIUsagePredictionsEnabled(enabled: Boolean) {
        userPreferencesDao.updateAIUsagePredictionsEnabled(enabled, System.currentTimeMillis())
    }
    
    suspend fun updateAIModuleDownloaded(downloaded: Boolean) {
        userPreferencesDao.updateAIModuleDownloaded(downloaded, System.currentTimeMillis())
    }
    
    suspend fun updateAIOnboardingCompleted(completed: Boolean) {
        userPreferencesDao.updateAIOnboardingCompleted(completed, System.currentTimeMillis())
    }
    
    suspend fun getAIFeatureFlags(): Map<String, Boolean> {
        val prefs = getUserPreferencesSync()
        return if (prefs != null) {
            mapOf(
                "AI Features" to prefs.aiFeaturesEnabled,
                "AI Insights" to prefs.aiInsightsEnabled,
                "Goal Recommendations" to prefs.aiGoalRecommendationsEnabled,
                "Predictive Coaching" to prefs.aiPredictiveCoachingEnabled,
                "Usage Predictions" to prefs.aiUsagePredictionsEnabled,
                "Module Downloaded" to prefs.aiModuleDownloaded,
                "Onboarding Completed" to prefs.aiOnboardingCompleted
            )
        } else {
            emptyMap()
        }
    }
    
    suspend fun createDefaultPreferencesIfNeeded() {
        val existing = getUserPreferencesSync()
        if (existing == null) {
            val defaultPrefs = UserPreferences()
            insertOrUpdateUserPreferences(defaultPrefs)
        }
    }
}