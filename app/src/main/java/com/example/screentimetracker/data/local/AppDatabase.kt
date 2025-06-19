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
        DailyScreenUnlockSummary::class // Added DailyScreenUnlockSummary
    ],
    version = 4, // Incremented version
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun screenUnlockDao(): ScreenUnlockDao
    abstract fun appUsageDao(): AppUsageDao
    abstract fun appSessionDao(): AppSessionDao
    abstract fun dailyAppSummaryDao(): DailyAppSummaryDao
    abstract fun dailyScreenUnlockSummaryDao(): DailyScreenUnlockSummaryDao // Added abstract method

    companion object {
        const val DATABASE_NAME = "screen_time_tracker_db"

        // Placeholder for MIGRATION_3_4 if needed:
        // val MIGRATION_3_4 = object : Migration(3, 4) {
        //     override fun migrate(db: SupportSQLiteDatabase) {
        //         db.execSQL("CREATE TABLE IF NOT EXISTS `daily_screen_unlock_summary` (`dateMillis` INTEGER NOT NULL, `unlockCount` INTEGER NOT NULL, PRIMARY KEY(`dateMillis`))")
        //     }
        // }
    }
}
