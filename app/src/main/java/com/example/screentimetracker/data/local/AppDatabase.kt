package com.example.screentimetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
// import androidx.room.migration.Migration
// import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ScreenUnlockEvent::class,
        AppUsageEvent::class,
        AppSessionEvent::class,
        DailyAppSummary::class,
        DailyScreenUnlockSummary::class,
        LimitedApp::class // Added LimitedApp
    ],
    version = 5, // Incremented version
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun screenUnlockDao(): ScreenUnlockDao
    abstract fun appUsageDao(): AppUsageDao
    abstract fun appSessionDao(): AppSessionDao
    abstract fun dailyAppSummaryDao(): DailyAppSummaryDao
    abstract fun dailyScreenUnlockSummaryDao(): DailyScreenUnlockSummaryDao
    abstract fun limitedAppDao(): LimitedAppDao // Added abstract method for the new DAO

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
