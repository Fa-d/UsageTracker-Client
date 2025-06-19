package com.example.screentimetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "screen_unlock_events")
data class ScreenUnlockEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long
)
