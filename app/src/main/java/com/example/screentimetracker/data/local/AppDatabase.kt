package com.example.screentimetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
// import androidx.room.migration.Migration
// import androidx.sqlite.db.SupportSQLiteDatabase

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

        // Placeholder for MIGRATION_4_5 if needed:
        // val MIGRATION_4_5 = object : Migration(4, 5) {
        //     override fun migrate(db: SupportSQLiteDatabase) {
        //         db.execSQL("CREATE TABLE IF NOT EXISTS `limited_apps` (`packageName` TEXT NOT NULL, `timeLimitMillis` INTEGER NOT NULL, PRIMARY KEY(`packageName`))")
        //     }
        // }
    }
}
