package dev.sadakat.screentimetracker.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long,
    val targetDurationMillis: Long,
    val actualDurationMillis: Long,
    val appsBlocked: String, // JSON array of package names
    val wasSuccessful: Boolean,
    val interruptionCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)