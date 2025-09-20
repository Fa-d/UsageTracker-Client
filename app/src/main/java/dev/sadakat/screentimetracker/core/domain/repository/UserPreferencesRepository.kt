package dev.sadakat.screentimetracker.core.domain.repository

import dev.sadakat.screentimetracker.core.data.local.entities.UserPreferences
import dev.sadakat.screentimetracker.core.domain.service.InsightPreferences
import dev.sadakat.screentimetracker.core.domain.service.UserBehaviorProfile
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository for user preferences and settings.
 * Contains pure domain operations without framework dependencies.
 */
interface UserPreferencesRepository {

    fun getUserPreferences(): Flow<UserPreferences?>
    /**
     * Gets user's insight generation preferences
     */
    suspend fun getInsightPreferences(): InsightPreferences

    /**
     * Updates user's insight generation preferences
     */
    suspend fun updateInsightPreferences(preferences: InsightPreferences)

    /**
     * Observes changes to insight preferences
     */
    fun observeInsightPreferences(): Flow<InsightPreferences>

    /**
     * Gets user's behavior profile for personalized experiences
     */
    suspend fun getUserBehaviorProfile(): UserBehaviorProfile

    /**
     * Updates user's behavior profile
     */
    suspend fun updateUserBehaviorProfile(profile: UserBehaviorProfile)

    /**
     * Gets notification preferences
     */
    suspend fun getNotificationSettings(): NotificationSettings

    /**
     * Updates notification preferences
     */
    suspend fun updateNotificationSettings(settings: NotificationSettings)

    /**
     * Observes notification settings changes
     */
    fun observeNotificationSettings(): Flow<NotificationSettings>

    /**
     * Gets app appearance preferences
     */
    suspend fun getAppearanceSettings(): AppearanceSettings

    /**
     * Updates app appearance preferences
     */
    suspend fun updateAppearanceSettings(settings: AppearanceSettings)

    /**
     * Gets privacy and data settings
     */
    suspend fun getPrivacySettings(): PrivacySettings

    /**
     * Updates privacy and data settings
     */
    suspend fun updatePrivacySettings(settings: PrivacySettings)

    /**
     * Gets focus session preferences
     */
    suspend fun getFocusSessionSettings(): FocusSessionSettings

    /**
     * Updates focus session preferences
     */
    suspend fun updateFocusSessionSettings(settings: FocusSessionSettings)

    /**
     * Resets all preferences to default values
     */
    suspend fun resetToDefaults()

    /**
     * Exports all preferences for backup
     */
    suspend fun exportPreferences(): PreferencesBackup

    /**
     * Imports preferences from backup
     */
    suspend fun importPreferences(backup: PreferencesBackup)

    suspend fun updateAIFeaturesEnabled(enabled: Boolean)
    suspend fun updateAIModuleDownloaded(downloaded: Boolean)
    suspend fun updateAIInsightsEnabled(enabled: Boolean)
    suspend fun updateAIGoalRecommendationsEnabled(enabled: Boolean)
    suspend fun updateAIPredictiveCoachingEnabled(enabled: Boolean)
    suspend fun updateAIUsagePredictionsEnabled(enabled: Boolean)
}

/**
 * Notification preferences for the user
 */
data class NotificationSettings(
    val limitWarningsEnabled: Boolean = true,
    val timeWarningsEnabled: Boolean = true,
    val breakRemindersEnabled: Boolean = true,
    val achievementNotificationsEnabled: Boolean = true,
    val motivationBoostsEnabled: Boolean = true,
    val weeklyReportsEnabled: Boolean = true,
    val focusSessionNotificationsEnabled: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: Int = 22, // Hour of day (0-23)
    val quietHoursEnd: Int = 8,    // Hour of day (0-23)
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = true
)

/**
 * App appearance and UI preferences
 */
data class AppearanceSettings(
    val darkModeEnabled: Boolean = false,
    val useSystemTheme: Boolean = true,
    val primaryColor: String = "#6200EE",
    val chartStyle: ChartStyle = ChartStyle.MODERN,
    val compactMode: Boolean = false,
    val animationsEnabled: Boolean = true,
    val cardStyle: CardStyle = CardStyle.ELEVATED
)

enum class ChartStyle {
    MODERN,
    CLASSIC,
    MINIMAL
}

enum class CardStyle {
    ELEVATED,
    OUTLINED,
    FILLED
}

/**
 * Privacy and data handling preferences
 */
data class PrivacySettings(
    val analyticsEnabled: Boolean = true,
    val crashReportingEnabled: Boolean = true,
    val dataBackupEnabled: Boolean = true,
    val shareUsageStatsEnabled: Boolean = false,
    val personalizedRecommendationsEnabled: Boolean = true,
    val locationBasedInsightsEnabled: Boolean = false,
    val cloudSyncEnabled: Boolean = true,
    val dataRetentionDays: Int = 365
)

/**
 * Focus session and productivity preferences
 */
data class FocusSessionSettings(
    val defaultSessionDurationMinutes: Int = 25,
    val shortBreakDurationMinutes: Int = 5,
    val longBreakDurationMinutes: Int = 15,
    val sessionsBeforeLongBreak: Int = 4,
    val strictModeEnabled: Boolean = false,
    val allowedAppsInFocus: Set<String> = emptySet(),
    val focusRemindersEnabled: Boolean = true,
    val autoStartBreaksEnabled: Boolean = false,
    val focusSessionSoundEnabled: Boolean = true,
    val tickingSoundEnabled: Boolean = false
)

/**
 * Complete preferences backup for export/import
 */
data class PreferencesBackup(
    val notificationSettings: NotificationSettings,
    val appearanceSettings: AppearanceSettings,
    val privacySettings: PrivacySettings,
    val focusSessionSettings: FocusSessionSettings,
    val insightPreferences: InsightPreferences,
    val userBehaviorProfile: UserBehaviorProfile,
    val exportTimestamp: Long = System.currentTimeMillis(),
    val appVersion: String
)