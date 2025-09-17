package dev.sadakat.screentimetracker.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_screen_unlock_summary")
data class DailyScreenUnlockSummary(
    @PrimaryKey // Date will be unique for unlock summaries
    val dateMillis: Long, // Timestamp for the start of the day (00:00:00)
    val unlockCount: Int
)
