package dev.sadakat.screentimetracker.core.common

/**
 * Application-wide constants
 */
object Constants {

    // Database
    const val DATABASE_NAME = "screen_time_tracker_db"
    const val DATABASE_VERSION = 1

    // Preferences
    const val PREFERENCES_NAME = "screen_time_tracker_prefs"
    const val ENCRYPTED_PREFERENCES_NAME = "encrypted_prefs"

    // Time constants
    const val MILLISECONDS_IN_SECOND = 1000L
    const val SECONDS_IN_MINUTE = 60L
    const val MINUTES_IN_HOUR = 60L
    const val HOURS_IN_DAY = 24L

    const val MILLISECONDS_IN_MINUTE = MILLISECONDS_IN_SECOND * SECONDS_IN_MINUTE
    const val MILLISECONDS_IN_HOUR = MILLISECONDS_IN_MINUTE * MINUTES_IN_HOUR
    const val MILLISECONDS_IN_DAY = MILLISECONDS_IN_HOUR * HOURS_IN_DAY

    // App Categories
    object AppCategory {
        const val SOCIAL_MEDIA = "Social Media"
        const val PRODUCTIVITY = "Productivity"
        const val ENTERTAINMENT = "Entertainment"
        const val GAMES = "Games"
        const val EDUCATION = "Education"
        const val HEALTH_FITNESS = "Health & Fitness"
        const val NEWS = "News"
        const val COMMUNICATION = "Communication"
        const val SHOPPING = "Shopping"
        const val FINANCE = "Finance"
        const val TRAVEL = "Travel"
        const val UTILITIES = "Utilities"
        const val OTHER = "Other"
    }

    // Notification
    object Notification {
        const val USAGE_TRACKING_CHANNEL_ID = "usage_tracking"
        const val WELLNESS_REMINDER_CHANNEL_ID = "wellness_reminders"
        const val LIMIT_EXCEEDED_CHANNEL_ID = "limit_exceeded"
        const val ACHIEVEMENT_CHANNEL_ID = "achievements"

        const val USAGE_TRACKING_NOTIFICATION_ID = 1001
        const val WELLNESS_REMINDER_NOTIFICATION_ID = 1002
        const val LIMIT_EXCEEDED_NOTIFICATION_ID = 1003
        const val ACHIEVEMENT_NOTIFICATION_ID = 1004
    }

    // Work Manager
    object WorkManager {
        const val USAGE_SYNC_WORK = "usage_sync_work"
        const val WELLNESS_REMINDER_WORK = "wellness_reminder_work"
        const val CLEANUP_WORK = "cleanup_work"
    }

    // Limits
    const val DEFAULT_DAILY_LIMIT_MINUTES = 480 // 8 hours
    const val MIN_APP_LIMIT_MINUTES = 5
    const val MAX_APP_LIMIT_MINUTES = 1440 // 24 hours

    // Wellness Scoring
    const val MAX_WELLNESS_SCORE = 100
    const val MIN_WELLNESS_SCORE = 0
}