package dev.sadakat.screentimetracker.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferences(
    @PrimaryKey
    val id: Int = 1, // Single row for user preferences
    
    @ColumnInfo(name = "theme_mode")
    val themeMode: String = "SYSTEM", // LIGHT, DARK, SYSTEM
    
    @ColumnInfo(name = "color_scheme")
    val colorScheme: String = "DEFAULT", // DEFAULT, COLORFUL, MINIMAL
    
    @ColumnInfo(name = "notification_sound")
    val notificationSound: String = "DEFAULT",
    
    @ColumnInfo(name = "motivational_messages_enabled")
    val motivationalMessagesEnabled: Boolean = true,
    
    @ColumnInfo(name = "achievement_celebrations_enabled")
    val achievementCelebrationsEnabled: Boolean = true,
    
    @ColumnInfo(name = "break_reminders_enabled")
    val breakRemindersEnabled: Boolean = true,
    
    @ColumnInfo(name = "wellness_coaching_enabled")
    val wellnessCoachingEnabled: Boolean = true,
    
    @ColumnInfo(name = "default_focus_duration_minutes")
    val defaultFocusDurationMinutes: Int? = 25, // Default Pomodoro duration
    
    @ColumnInfo(name = "focus_mode_enabled")
    val focusModeEnabled: Boolean = true,
    
    // AI Features
    @ColumnInfo(name = "ai_features_enabled")
    val aiFeaturesEnabled: Boolean = false,
    
    @ColumnInfo(name = "ai_insights_enabled")
    val aiInsightsEnabled: Boolean = false,
    
    @ColumnInfo(name = "ai_goal_recommendations_enabled")
    val aiGoalRecommendationsEnabled: Boolean = false,
    
    @ColumnInfo(name = "ai_predictive_coaching_enabled")
    val aiPredictiveCoachingEnabled: Boolean = false,
    
    @ColumnInfo(name = "ai_usage_predictions_enabled")
    val aiUsagePredictionsEnabled: Boolean = false,
    
    @ColumnInfo(name = "ai_module_downloaded")
    val aiModuleDownloaded: Boolean = false,
    
    @ColumnInfo(name = "ai_onboarding_completed")
    val aiOnboardingCompleted: Boolean = false,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

// Enums for type safety
enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

enum class ColorScheme {
    DEFAULT, COLORFUL, MINIMAL
}

