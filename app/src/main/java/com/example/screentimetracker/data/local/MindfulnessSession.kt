package com.example.screentimetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mindfulness_sessions")
data class MindfulnessSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionType: String, // "breathing", "meditation", "gratitude"
    val durationMillis: Long,
    val startTime: Long,
    val endTime: Long,
    val completionRate: Float, // 0.0 to 1.0
    val userRating: Int = 0, // 1-5 stars
    val notes: String = "",
    val triggeredByAppBlock: Boolean = false,
    val appThatWasBlocked: String = ""
)