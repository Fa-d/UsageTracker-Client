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
        ReplacementActivity::class
    ],
    version = 10, // Incremented version for Privacy Settings entity
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
    }
}
