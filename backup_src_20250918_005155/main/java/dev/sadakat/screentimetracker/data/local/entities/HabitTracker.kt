<<<<<<<< HEAD:core/database/src/main/java/dev/sadakat/screentimetracker/core/database/entities/HabitTracker.kt
package dev.sadakat.screentimetracker.core.database.entities
========
package dev.sadakat.screentimetracker.data.local.entities
>>>>>>>> origin/detached3:backup_src_20250918_005155/main/java/dev/sadakat/screentimetracker/data/local/entities/HabitTracker.kt

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_tracker")
data class HabitTracker(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: String, // unique identifier for habit type
    val habitName: String,
    val description: String,
    val emoji: String,
    val date: Long, // Date in milliseconds (start of day)
    val isCompleted: Boolean = false,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)