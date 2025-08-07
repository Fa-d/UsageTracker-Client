package com.example.screentimetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "replacement_activities")
data class ReplacementActivity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val activityType: String, // "reading", "walking", "water", "breathing", "journaling"
    val title: String,
    val description: String,
    val emoji: String,
    val estimatedDurationMinutes: Int,
    val category: String, // "physical", "mental", "wellness", "productivity"
    val difficultyLevel: Int, // 1-3 (easy, medium, hard)
    val isCustom: Boolean = false,
    val timesCompleted: Int = 0,
    val averageRating: Float = 0f,
    val lastCompletedAt: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_completions")
data class ActivityCompletion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val activityId: Long,
    val completedAt: Long,
    val actualDurationMinutes: Int,
    val userRating: Int, // 1-5 stars
    val notes: String = "",
    val contextTrigger: String = "", // e.g., "blocked_instagram", "timer_expired"
    val locationCompleted: String = "" // Optional: "home", "office", etc.
)