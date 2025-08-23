package dev.sadakat.screentimetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_session_events")
data class AppSessionEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val durationMillis: Long // Calculated as endTimeMillis - startTimeMillis
)
