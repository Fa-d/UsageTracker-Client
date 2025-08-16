package com.example.screentimetracker.data.local

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
    
    @ColumnInfo(name = "personality_mode")
    val personalityMode: String = "GENTLE_GUIDE", // STRICT_COACH, GENTLE_GUIDE, MOTIVATIONAL_BUDDY
    
    @ColumnInfo(name = "notification_sound")
    val notificationSound: String = "DEFAULT",
    
    @ColumnInfo(name = "dashboard_layout")
    val dashboardLayout: String = "STANDARD", // STANDARD, COMPACT, DETAILED
    
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

enum class PersonalityMode(
    val displayName: String,
    val description: String,
    val motivationStyle: String
) {
    STRICT_COACH(
        "Strict Coach", 
        "Direct and firm guidance to keep you on track",
        "FIRM"
    ),
    GENTLE_GUIDE(
        "Gentle Guide", 
        "Supportive and understanding approach",
        "SUPPORTIVE"
    ),
    MOTIVATIONAL_BUDDY(
        "Motivational Buddy", 
        "Energetic and encouraging companion",
        "ENTHUSIASTIC"
    )
}

enum class DashboardLayout {
    STANDARD, // Current layout
    COMPACT,  // Minimal cards with essential info
    DETAILED  // Expanded cards with more metrics
}