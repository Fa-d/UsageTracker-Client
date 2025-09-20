package dev.sadakat.screentimetracker.core.data.mapper

import dev.sadakat.screentimetracker.core.domain.repository.*
import dev.sadakat.screentimetracker.core.domain.service.*
import dev.sadakat.screentimetracker.data.local.entities.UserPreferences as EntityUserPreferences

/**
 * Maps between UserPreferences entity and domain preference models.
 * Handles the complex mapping from single entity to multiple domain preference types.
 */
class UserPreferencesDataMapper {

    /**
     * Maps entity to domain InsightPreferences
     */
    fun mapToInsightPreferences(entity: EntityUserPreferences): InsightPreferences {
        return InsightPreferences(
            insightsEnabled = entity.aiInsightsEnabled,
            goalRecommendationsEnabled = entity.aiGoalRecommendationsEnabled,
            predictiveCoachingEnabled = entity.aiPredictiveCoachingEnabled,
            usagePredictionsEnabled = entity.aiUsagePredictionsEnabled,
            privacyLevel = if (entity.aiFeaturesEnabled) PrivacyLevel.ENHANCED else PrivacyLevel.BASIC,
            updateFrequency = UpdateFrequency.DAILY
        )
    }

    /**
     * Maps entity to domain UserBehaviorProfile (with defaults for missing fields)
     */
    fun mapToUserBehaviorProfile(entity: EntityUserPreferences): UserBehaviorProfile {
        return UserBehaviorProfile(
            motivationLevel = if (entity.motivationalMessagesEnabled) MotivationLevel.MODERATE else MotivationLevel.LOW,
            selfControlScore = 0.5f, // Default value - would need tracking
            preferredEnforcementStyle = if (entity.wellnessCoachingEnabled) EnforcementStyle.GENTLE_NUDGES else EnforcementStyle.FIRM_BOUNDARIES,
            responsesToWarnings = 0.5f, // Default value - would need tracking
            streakDays = 0, // Default value - would need tracking
            recentViolations = 0 // Default value - would need tracking
        )
    }

    /**
     * Maps entity to domain NotificationSettings
     */
    fun mapToNotificationSettings(entity: EntityUserPreferences): NotificationSettings {
        return NotificationSettings(
            limitWarningsEnabled = entity.breakRemindersEnabled,
            timeWarningsEnabled = entity.breakRemindersEnabled,
            breakRemindersEnabled = entity.breakRemindersEnabled,
            achievementNotificationsEnabled = entity.achievementCelebrationsEnabled,
            motivationBoostsEnabled = entity.motivationalMessagesEnabled,
            weeklyReportsEnabled = entity.wellnessCoachingEnabled,
            focusSessionNotificationsEnabled = entity.focusModeEnabled,
            quietHoursEnabled = false, // Not in entity
            quietHoursStart = 22,
            quietHoursEnd = 8,
            vibrationEnabled = entity.notificationSound != "NONE",
            soundEnabled = entity.notificationSound != "NONE"
        )
    }

    /**
     * Maps entity to domain AppearanceSettings
     */
    fun mapToAppearanceSettings(entity: EntityUserPreferences): AppearanceSettings {
        return AppearanceSettings(
            darkModeEnabled = entity.themeMode == "DARK",
            useSystemTheme = entity.themeMode == "SYSTEM",
            primaryColor = when (entity.colorScheme) {
                "COLORFUL" -> "#FF6B6B"
                "MINIMAL" -> "#000000"
                else -> "#6200EE"
            },
            chartStyle = when (entity.colorScheme) {
                "MINIMAL" -> ChartStyle.MINIMAL
                "COLORFUL" -> ChartStyle.MODERN
                else -> ChartStyle.CLASSIC
            },
            compactMode = entity.colorScheme == "MINIMAL",
            animationsEnabled = entity.achievementCelebrationsEnabled,
            cardStyle = if (entity.colorScheme == "MINIMAL") CardStyle.OUTLINED else CardStyle.ELEVATED
        )
    }

    /**
     * Maps entity to domain PrivacySettings
     */
    fun mapToPrivacySettings(entity: EntityUserPreferences): PrivacySettings {
        return PrivacySettings(
            analyticsEnabled = entity.aiFeaturesEnabled,
            crashReportingEnabled = true, // Default
            dataBackupEnabled = true, // Default
            shareUsageStatsEnabled = entity.aiFeaturesEnabled,
            personalizedRecommendationsEnabled = entity.aiGoalRecommendationsEnabled,
            locationBasedInsightsEnabled = false, // Default
            cloudSyncEnabled = entity.aiFeaturesEnabled,
            dataRetentionDays = 365
        )
    }

    /**
     * Maps entity to domain FocusSessionSettings
     */
    fun mapToFocusSessionSettings(entity: EntityUserPreferences): FocusSessionSettings {
        return FocusSessionSettings(
            defaultSessionDurationMinutes = entity.defaultFocusDurationMinutes ?: 25,
            shortBreakDurationMinutes = 5,
            longBreakDurationMinutes = 15,
            sessionsBeforeLongBreak = 4,
            strictModeEnabled = false, // Default
            allowedAppsInFocus = emptySet(),
            focusRemindersEnabled = entity.focusModeEnabled,
            autoStartBreaksEnabled = false,
            focusSessionSoundEnabled = entity.notificationSound != "NONE",
            tickingSoundEnabled = false
        )
    }

    /**
     * Maps domain preferences back to entity (partial update)
     */
    fun updateEntityWithInsightPreferences(
        entity: EntityUserPreferences,
        preferences: InsightPreferences
    ): EntityUserPreferences {
        return entity.copy(
            aiInsightsEnabled = preferences.insightsEnabled,
            aiGoalRecommendationsEnabled = preferences.goalRecommendationsEnabled,
            aiPredictiveCoachingEnabled = preferences.predictiveCoachingEnabled,
            aiUsagePredictionsEnabled = preferences.usagePredictionsEnabled,
            aiFeaturesEnabled = preferences.privacyLevel != PrivacyLevel.BASIC,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Maps domain notification settings back to entity (partial update)
     */
    fun updateEntityWithNotificationSettings(
        entity: EntityUserPreferences,
        settings: NotificationSettings
    ): EntityUserPreferences {
        return entity.copy(
            breakRemindersEnabled = settings.breakRemindersEnabled,
            achievementCelebrationsEnabled = settings.achievementNotificationsEnabled,
            motivationalMessagesEnabled = settings.motivationBoostsEnabled,
            wellnessCoachingEnabled = settings.weeklyReportsEnabled,
            focusModeEnabled = settings.focusSessionNotificationsEnabled,
            notificationSound = if (settings.soundEnabled) "DEFAULT" else "NONE",
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Maps domain appearance settings back to entity (partial update)
     */
    fun updateEntityWithAppearanceSettings(
        entity: EntityUserPreferences,
        settings: AppearanceSettings
    ): EntityUserPreferences {
        val themeMode = when {
            settings.useSystemTheme -> "SYSTEM"
            settings.darkModeEnabled -> "DARK"
            else -> "LIGHT"
        }

        val colorScheme = when (settings.chartStyle) {
            ChartStyle.MINIMAL -> "MINIMAL"
            ChartStyle.MODERN -> "COLORFUL"
            else -> "DEFAULT"
        }

        return entity.copy(
            themeMode = themeMode,
            colorScheme = colorScheme,
            achievementCelebrationsEnabled = settings.animationsEnabled,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Maps domain focus session settings back to entity (partial update)
     */
    fun updateEntityWithFocusSessionSettings(
        entity: EntityUserPreferences,
        settings: FocusSessionSettings
    ): EntityUserPreferences {
        return entity.copy(
            defaultFocusDurationMinutes = settings.defaultSessionDurationMinutes,
            focusModeEnabled = settings.focusRemindersEnabled,
            notificationSound = if (settings.focusSessionSoundEnabled) "DEFAULT" else "NONE",
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Creates default entity when none exists
     */
    fun createDefaultEntity(): EntityUserPreferences {
        return EntityUserPreferences()
    }

    /**
     * Creates backup from entity
     */
    fun mapToPreferencesBackup(
        entity: EntityUserPreferences,
        appVersion: String
    ): PreferencesBackup {
        return PreferencesBackup(
            notificationSettings = mapToNotificationSettings(entity),
            appearanceSettings = mapToAppearanceSettings(entity),
            privacySettings = mapToPrivacySettings(entity),
            focusSessionSettings = mapToFocusSessionSettings(entity),
            insightPreferences = mapToInsightPreferences(entity),
            userBehaviorProfile = mapToUserBehaviorProfile(entity),
            appVersion = appVersion
        )
    }

    /**
     * Validates entity data
     */
    fun validateEntity(entity: EntityUserPreferences): Boolean {
        return entity.themeMode in listOf("LIGHT", "DARK", "SYSTEM") &&
                entity.colorScheme in listOf("DEFAULT", "COLORFUL", "MINIMAL") &&
                (entity.defaultFocusDurationMinutes ?: 25) > 0
    }
}