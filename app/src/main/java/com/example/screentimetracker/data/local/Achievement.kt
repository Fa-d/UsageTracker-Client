package com.example.screentimetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey
    val achievementId: String,
    val name: String,
    val description: String,
    val emoji: String,
    val category: String, // streak, mindful, focus, cleaner, warrior, early_bird, digital_sunset
    val targetValue: Int,
    val isUnlocked: Boolean = false,
    val unlockedDate: Long? = null,
    val currentProgress: Int = 0
)