package dev.sadakat.screentimetracker.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.sadakat.screentimetracker.core.database.dao.AppUsageDao
import dev.sadakat.screentimetracker.core.database.model.AppUsageEntity
import dev.sadakat.screentimetracker.core.database.util.Converters

/**
 * Core Screen Time Tracker database
 */
@Database(
    entities = [
        AppUsageEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ScreenTimeDatabase : RoomDatabase() {
    abstract fun appUsageDao(): AppUsageDao
}