package com.example.screentimetracker.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferencesDao {
    
    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    fun getUserPreferences(): Flow<UserPreferences?>
    
    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    suspend fun getUserPreferencesOnce(): UserPreferences?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePreferences(preferences: UserPreferences)
    
    @Query("UPDATE user_preferences SET theme_mode = :themeMode, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateThemeMode(themeMode: String, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET color_scheme = :colorScheme, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateColorScheme(colorScheme: String, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET personality_mode = :personalityMode, updated_at = :updatedAt WHERE id = 1")
    suspend fun updatePersonalityMode(personalityMode: String, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET dashboard_layout = :dashboardLayout, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateDashboardLayout(dashboardLayout: String, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET motivational_messages_enabled = :enabled, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateMotivationalMessages(enabled: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET achievement_celebrations_enabled = :enabled, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateAchievementCelebrations(enabled: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET break_reminders_enabled = :enabled, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateBreakReminders(enabled: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE user_preferences SET wellness_coaching_enabled = :enabled, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateWellnessCoaching(enabled: Boolean, updatedAt: Long = System.currentTimeMillis())
}