package dev.sadakat.screentimetracker.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.sadakat.screentimetracker.core.database.dao.*
import dev.sadakat.screentimetracker.core.database.entities.*
import dev.sadakat.screentimetracker.core.database.model.AppUsageEntity
import dev.sadakat.screentimetracker.core.database.util.Converters

/**
 * Core Screen Time Tracker database
 */
@Database(
    entities = [
        AppUsageEntity::class,
        AppSessionEvent::class,
        ScreenUnlockEvent::class,
        DailyAppSummary::class,
        DailyScreenUnlockSummary::class,
        UserGoal::class,
        HabitTracker::class,
        WellnessScore::class,
        Achievement::class,
        AppCategory::class,
        Challenge::class,
        DigitalPet::class,
        FocusSession::class,
        LimitedApp::class,
        MindfulnessSession::class,
        PrivacySettings::class,
        ProgressiveLimit::class,
        ProgressiveMilestone::class,
        ReplacementActivity::class,
        TimeRestriction::class,
        UserPreferences::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ScreenTimeDatabase : RoomDatabase() {
    abstract fun appUsageDao(): AppUsageDao
    abstract fun appCategoryDao(): AppCategoryDao
    abstract fun achievementDao(): AchievementDao
    abstract fun appSessionDao(): AppSessionDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun dailyAppSummaryDao(): DailyAppSummaryDao
    abstract fun dailyScreenUnlockSummaryDao(): DailyScreenUnlockSummaryDao
    abstract fun digitalPetDao(): DigitalPetDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun habitTrackerDao(): HabitTrackerDao
    abstract fun limitedAppDao(): LimitedAppDao
    abstract fun mindfulnessSessionDao(): MindfulnessSessionDao
    abstract fun privacySettingsDao(): PrivacySettingsDao
    abstract fun progressiveLimitDao(): ProgressiveLimitDao
    abstract fun progressiveMilestoneDao(): ProgressiveMilestoneDao
    abstract fun replacementActivityDao(): ReplacementActivityDao
    abstract fun screenUnlockDao(): ScreenUnlockDao
    abstract fun timeRestrictionDao(): TimeRestrictionDao
    abstract fun userGoalDao(): UserGoalDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun wellnessScoreDao(): WellnessScoreDao
}