package dev.sadakat.screentimetracker.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.sadakat.screentimetracker.core.database.entities.*
import dev.sadakat.screentimetracker.core.database.dao.*

@Database(
    entities = [
        ScreenUnlockEvent::class,
        AppUsageEvent::class,
        AppSessionEvent::class,
        DailyAppSummary::class,
        DailyScreenUnlockSummary::class,
        LimitedApp::class,
        Achievement::class,
        WellnessScore::class,
        UserGoal::class,
        Challenge::class,
        FocusSession::class,
        HabitTracker::class,
        TimeRestriction::class,
        ProgressiveLimit::class,
        ProgressiveMilestone::class,
        UserPreferences::class,
        PrivacySettings::class,
        MindfulnessSession::class,
        ReplacementActivity::class,
        AppCategory::class,
        DigitalPet::class
    ],
    version = 15, // Incremented version to remove personality_mode and dashboard_layout columns
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun screenUnlockDao(): ScreenUnlockDao
    abstract fun appUsageDao(): AppUsageDao
    abstract fun appSessionDao(): AppSessionDao
    abstract fun dailyAppSummaryDao(): DailyAppSummaryDao
    abstract fun dailyScreenUnlockSummaryDao(): DailyScreenUnlockSummaryDao
    abstract fun limitedAppDao(): LimitedAppDao
    abstract fun achievementDao(): AchievementDao
    abstract fun wellnessScoreDao(): WellnessScoreDao
    abstract fun userGoalDao(): UserGoalDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun habitTrackerDao(): HabitTrackerDao
    abstract fun timeRestrictionDao(): TimeRestrictionDao
    abstract fun progressiveLimitDao(): ProgressiveLimitDao
    abstract fun progressiveMilestoneDao(): ProgressiveMilestoneDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun privacySettingsDao(): PrivacySettingsDao
    abstract fun mindfulnessSessionDao(): MindfulnessSessionDao
    abstract fun replacementActivityDao(): ReplacementActivityDao
    abstract fun appCategoryDao(): AppCategoryDao
    abstract fun digitalPetDao(): DigitalPetDao

    companion object {
        const val DATABASE_NAME = "screen_time_tracker_db"

        // Migration from version 9 to 10: Add TimeRestriction and PrivacySettings tables
        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create TimeRestriction table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `time_restrictions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `restrictionType` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `startTimeMinutes` INTEGER NOT NULL,
                        `endTimeMinutes` INTEGER NOT NULL,
                        `appsBlocked` TEXT NOT NULL,
                        `daysOfWeek` TEXT NOT NULL,
                        `isEnabled` INTEGER NOT NULL DEFAULT 1,
                        `allowEmergencyApps` INTEGER NOT NULL DEFAULT 1,
                        `showNotifications` INTEGER NOT NULL DEFAULT 1,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                """)

                // Create PrivacySettings table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `privacy_settings` (
                        `id` INTEGER PRIMARY KEY NOT NULL DEFAULT 1,
                        `isStealthModeEnabled` INTEGER NOT NULL DEFAULT 0,
                        `stealthModePassword` TEXT NOT NULL DEFAULT '',
                        `isGuestModeEnabled` INTEGER NOT NULL DEFAULT 0,
                        `guestModeStartTime` INTEGER NOT NULL DEFAULT 0,
                        `guestModeEndTime` INTEGER NOT NULL DEFAULT 0,
                        `hiddenAppsPackages` TEXT NOT NULL DEFAULT '[]',
                        `excludedAppsFromTracking` TEXT NOT NULL DEFAULT '[]',
                        `dataExportEnabled` INTEGER NOT NULL DEFAULT 1,
                        `lastDataExportTime` INTEGER NOT NULL DEFAULT 0
                    )
                """)

                // Insert default privacy settings
                database.execSQL("""
                    INSERT OR IGNORE INTO `privacy_settings` (
                        `id`, `isStealthModeEnabled`, `stealthModePassword`, `isGuestModeEnabled`,
                        `guestModeStartTime`, `guestModeEndTime`, `hiddenAppsPackages`,
                        `excludedAppsFromTracking`, `dataExportEnabled`, `lastDataExportTime`
                    ) VALUES (
                        1, 0, '', 0, 0, 0, '[]', '[]', 1, 0
                    )
                """)
            }
        }
        
        // Migration from version 10 to 11: Add AppCategory table
        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create AppCategory table (no indices in migration)
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `app_categories` (
                        `packageName` TEXT PRIMARY KEY NOT NULL,
                        `category` TEXT NOT NULL,
                        `confidence` REAL NOT NULL,
                        `source` TEXT NOT NULL,
                        `lastUpdated` INTEGER NOT NULL,
                        `appName` TEXT NOT NULL
                    )
                """)
                
                // Create MindfulnessSession table if it doesn't exist
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `mindfulness_sessions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sessionType` TEXT NOT NULL,
                        `durationMillis` INTEGER NOT NULL,
                        `startTime` INTEGER NOT NULL,
                        `endTime` INTEGER NOT NULL,
                        `completionRate` REAL NOT NULL,
                        `userRating` INTEGER NOT NULL,
                        `notes` TEXT NOT NULL,
                        `triggeredByAppBlock` INTEGER NOT NULL,
                        `appThatWasBlocked` TEXT NOT NULL
                    )
                """)
                
                // Create ReplacementActivity table if it doesn't exist
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `replacement_activities` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `activityType` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `emoji` TEXT NOT NULL,
                        `estimatedDurationMinutes` INTEGER NOT NULL,
                        `category` TEXT NOT NULL,
                        `difficultyLevel` INTEGER NOT NULL,
                        `isCustom` INTEGER NOT NULL,
                        `timesCompleted` INTEGER NOT NULL,
                        `averageRating` REAL NOT NULL,
                        `lastCompletedAt` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """)
            }
        }
        
        // Migration from version 11 to 12: Add focus mode columns to UserPreferences
        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to user_preferences table
                database.execSQL("""
                    ALTER TABLE `user_preferences` 
                    ADD COLUMN `default_focus_duration_minutes` INTEGER DEFAULT 25
                """)
                
                database.execSQL("""
                    ALTER TABLE `user_preferences` 
                    ADD COLUMN `focus_mode_enabled` INTEGER NOT NULL DEFAULT 1
                """)
            }
        }
        
        // Migration from version 12 to 13: Add AI features columns to UserPreferences
        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add AI feature columns to user_preferences table
                database.execSQL("""
                    ALTER TABLE `user_preferences` 
                    ADD COLUMN `ai_features_enabled` INTEGER NOT NULL DEFAULT 0
                """)
                
                database.execSQL("""
                    ALTER TABLE `user_preferences` 
                    ADD COLUMN `ai_insights_enabled` INTEGER NOT NULL DEFAULT 0
                """)
                
                database.execSQL("""
                    ALTER TABLE `user_preferences` 
                    ADD COLUMN `ai_goal_recommendations_enabled` INTEGER NOT NULL DEFAULT 0
                """)
                
                database.execSQL("""
                    ALTER TABLE `user_preferences` 
                    ADD COLUMN `ai_predictive_coaching_enabled` INTEGER NOT NULL DEFAULT 0
                """)
                
                database.execSQL("""
                    ALTER TABLE `user_preferences` 
                    ADD COLUMN `ai_usage_predictions_enabled` INTEGER NOT NULL DEFAULT 0
                """)
                
                database.execSQL("""
                    ALTER TABLE `user_preferences` 
                    ADD COLUMN `ai_module_downloaded` INTEGER NOT NULL DEFAULT 0
                """)
                
                database.execSQL("""
                    ALTER TABLE `user_preferences` 
                    ADD COLUMN `ai_onboarding_completed` INTEGER NOT NULL DEFAULT 0
                """)
            }
        }
        
        // Migration from version 13 to 14: Add DigitalPet table
        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create DigitalPet table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `digital_pet` (
                        `id` INTEGER PRIMARY KEY NOT NULL DEFAULT 1,
                        `name` TEXT NOT NULL DEFAULT 'Zen',
                        `pet_type` TEXT NOT NULL DEFAULT 'TREE',
                        `level` INTEGER NOT NULL DEFAULT 1,
                        `experience_points` INTEGER NOT NULL DEFAULT 0,
                        `health` INTEGER NOT NULL DEFAULT 100,
                        `happiness` INTEGER NOT NULL DEFAULT 100,
                        `energy` INTEGER NOT NULL DEFAULT 100,
                        `wellness_streak_days` INTEGER NOT NULL DEFAULT 0,
                        `last_fed_timestamp` INTEGER NOT NULL,
                        `last_wellness_check` INTEGER NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL
                    )
                """)
                
                // Insert default digital pet
                val currentTime = System.currentTimeMillis()
                database.execSQL("""
                    INSERT OR IGNORE INTO `digital_pet` (
                        `id`, `name`, `pet_type`, `level`, `experience_points`,
                        `health`, `happiness`, `energy`, `wellness_streak_days`,
                        `last_fed_timestamp`, `last_wellness_check`, `created_at`, `updated_at`
                    ) VALUES (
                        1, 'Zen', 'TREE', 1, 0, 100, 100, 100, 0, 
                        $currentTime, $currentTime, $currentTime, $currentTime
                    )
                """)
            }
        }
        
        // Migration from version 14 to 15: Remove personality_mode and dashboard_layout columns
        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create a new table without the removed columns
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `user_preferences_new` (
                        `id` INTEGER PRIMARY KEY NOT NULL DEFAULT 1,
                        `theme_mode` TEXT NOT NULL DEFAULT 'SYSTEM',
                        `color_scheme` TEXT NOT NULL DEFAULT 'DEFAULT',
                        `notification_sound` TEXT NOT NULL DEFAULT 'DEFAULT',
                        `motivational_messages_enabled` INTEGER NOT NULL DEFAULT 1,
                        `achievement_celebrations_enabled` INTEGER NOT NULL DEFAULT 1,
                        `break_reminders_enabled` INTEGER NOT NULL DEFAULT 1,
                        `wellness_coaching_enabled` INTEGER NOT NULL DEFAULT 1,
                        `default_focus_duration_minutes` INTEGER DEFAULT 25,
                        `focus_mode_enabled` INTEGER NOT NULL DEFAULT 1,
                        `ai_features_enabled` INTEGER NOT NULL DEFAULT 0,
                        `ai_insights_enabled` INTEGER NOT NULL DEFAULT 0,
                        `ai_goal_recommendations_enabled` INTEGER NOT NULL DEFAULT 0,
                        `ai_predictive_coaching_enabled` INTEGER NOT NULL DEFAULT 0,
                        `ai_usage_predictions_enabled` INTEGER NOT NULL DEFAULT 0,
                        `ai_module_downloaded` INTEGER NOT NULL DEFAULT 0,
                        `ai_onboarding_completed` INTEGER NOT NULL DEFAULT 0,
                        `updated_at` INTEGER NOT NULL
                    )
                """)
                
                // Copy data from old table to new table (excluding personality_mode and dashboard_layout)
                database.execSQL("""
                    INSERT INTO `user_preferences_new` (
                        `id`, `theme_mode`, `color_scheme`, `notification_sound`,
                        `motivational_messages_enabled`, `achievement_celebrations_enabled`,
                        `break_reminders_enabled`, `wellness_coaching_enabled`,
                        `default_focus_duration_minutes`, `focus_mode_enabled`,
                        `ai_features_enabled`, `ai_insights_enabled`, `ai_goal_recommendations_enabled`,
                        `ai_predictive_coaching_enabled`, `ai_usage_predictions_enabled`,
                        `ai_module_downloaded`, `ai_onboarding_completed`, `updated_at`
                    )
                    SELECT 
                        `id`, `theme_mode`, `color_scheme`, `notification_sound`,
                        `motivational_messages_enabled`, `achievement_celebrations_enabled`,
                        `break_reminders_enabled`, `wellness_coaching_enabled`,
                        `default_focus_duration_minutes`, `focus_mode_enabled`,
                        `ai_features_enabled`, `ai_insights_enabled`, `ai_goal_recommendations_enabled`,
                        `ai_predictive_coaching_enabled`, `ai_usage_predictions_enabled`,
                        `ai_module_downloaded`, `ai_onboarding_completed`, `updated_at`
                    FROM `user_preferences`
                """)
                
                // Drop the old table
                database.execSQL("DROP TABLE `user_preferences`")
                
                // Rename the new table
                database.execSQL("ALTER TABLE `user_preferences_new` RENAME TO `user_preferences`")
            }
        }
    }
}
