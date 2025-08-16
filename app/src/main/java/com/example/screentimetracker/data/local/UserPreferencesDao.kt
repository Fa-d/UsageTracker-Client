package com.example.screentimetracker.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferencesDao {
    
    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    fun getUserPreferences(): Flow<UserPreferences?>
    
    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    suspend fun getUserPreferencesSync(): UserPreferences?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserPreferences(preferences: UserPreferences)
    
    @Query("UPDATE user_preferences SET theme_mode = :themeMode, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateThemeMode(themeMode: String, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET color_scheme = :colorScheme, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateColorScheme(colorScheme: String, updatedAt: Long = System.currentTimeMillis())
    
    
    @Query("UPDATE user_preferences SET notification_sound = :notificationSound, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateNotificationSound(notificationSound: String, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET motivational_messages_enabled = :enabled, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateMotivationalMessagesEnabled(enabled: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET achievement_celebrations_enabled = :enabled, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateAchievementCelebrationsEnabled(enabled: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET break_reminders_enabled = :enabled, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateBreakRemindersEnabled(enabled: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET wellness_coaching_enabled = :enabled, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateWellnessCoachingEnabled(enabled: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET default_focus_duration_minutes = :duration, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateDefaultFocusDurationMinutes(duration: Int?, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET focus_mode_enabled = :enabled, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateFocusModeEnabled(enabled: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    // AI Features
    @Query("UPDATE user_preferences SET ai_features_enabled = :enabled, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateAIFeaturesEnabled(enabled: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET ai_insights_enabled = :enabled, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateAIInsightsEnabled(enabled: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET ai_goal_recommendations_enabled = :enabled, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateAIGoalRecommendationsEnabled(enabled: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET ai_predictive_coaching_enabled = :enabled, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateAIPredictiveCoachingEnabled(enabled: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET ai_usage_predictions_enabled = :enabled, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateAIUsagePredictionsEnabled(enabled: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET ai_module_downloaded = :downloaded, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateAIModuleDownloaded(downloaded: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET ai_onboarding_completed = :completed, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateAIOnboardingCompleted(completed: Boolean, updatedAt: Long = System.currentTimeMillis())
}