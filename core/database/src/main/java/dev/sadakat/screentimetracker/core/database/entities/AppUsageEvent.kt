package dev.sadakat.screentimetracker.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_usage_events")
data class AppUsageEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val eventName: String, // e.g., "opened", "closed", "in_foreground"
    val timestamp: Long
    // Consider adding duration if tracking app session lengths directly in this event
)