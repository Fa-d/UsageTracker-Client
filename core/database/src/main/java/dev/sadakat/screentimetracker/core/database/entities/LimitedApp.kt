package dev.sadakat.screentimetracker.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "limited_apps")
data class LimitedApp(
    @PrimaryKey
    val packageName: String, // Package name of the app to be limited
    val timeLimitMillis: Long // Continuous usage time limit in milliseconds
)
