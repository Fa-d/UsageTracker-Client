package com.example.screentimetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
        AppCategory::class
    ],
    version = 13, // Incremented version for UserPreferences AI features columns
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
    }
}
