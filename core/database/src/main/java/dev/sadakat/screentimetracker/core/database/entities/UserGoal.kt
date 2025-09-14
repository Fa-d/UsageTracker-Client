package dev.sadakat.screentimetracker.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_goals")
data class UserGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val goalType: String, // daily_screen_time, app_specific_limit, session_limit, unlock_frequency, focus_sessions, break_goals
    val targetValue: Long, // in milliseconds for time-based goals, count for others
    val currentProgress: Long = 0,
    val packageName: String? = null, // for app-specific goals
    val deadline: Long? = null, // optional deadline
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)