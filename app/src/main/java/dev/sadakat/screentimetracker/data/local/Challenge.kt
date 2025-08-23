package dev.sadakat.screentimetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "challenges")
data class Challenge(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val challengeId: String, // phone_free_meal, digital_sunset, focus_marathon, app_minimalist, step_away
    val name: String,
    val description: String,
    val emoji: String,
    val targetValue: Int,
    val status: String, // active, completed, failed, pending
    val startDate: Long,
    val endDate: Long,
    val currentProgress: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)