package dev.sadakat.screentimetracker.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime

/**
 * Database entity for app usage events
 */
@Entity(tableName = "app_usage_events")
data class AppUsageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val eventType: String, // "open", "close"
    val timestamp: LocalDateTime,
    val dayOfYear: Int,
    val year: Int
)