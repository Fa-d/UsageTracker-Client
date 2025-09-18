<<<<<<<< HEAD:core/database/src/main/java/dev/sadakat/screentimetracker/core/database/entities/TimeRestriction.kt
package dev.sadakat.screentimetracker.core.database.entities
========
package dev.sadakat.screentimetracker.data.local.entities
>>>>>>>> origin/detached3:backup_src_20250918_005155/main/java/dev/sadakat/screentimetracker/data/local/entities/TimeRestriction.kt

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_restrictions")
data class TimeRestriction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val restrictionType: String, // bedtime_mode, work_hours_focus, meal_time_protection, morning_routine
    val name: String,
    val description: String,
    val startTimeMinutes: Int, // Minutes from midnight (e.g., 22:00 = 22 * 60 = 1320)
    val endTimeMinutes: Int, // Minutes from midnight (e.g., 08:00 = 8 * 60 = 480)
    val appsBlocked: String, // JSON array of package names, empty means all apps
    val daysOfWeek: String, // JSON array of day indices (0=Sunday, 1=Monday, etc.)
    val isEnabled: Boolean = true,
    val allowEmergencyApps: Boolean = true, // Allow phone, messages, etc.
    val showNotifications: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)