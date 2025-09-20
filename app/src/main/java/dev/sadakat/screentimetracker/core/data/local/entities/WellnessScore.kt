package dev.sadakat.screentimetracker.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wellness_scores")
data class WellnessScore(
    @PrimaryKey
    val date: Long, // Date in milliseconds (start of day)
    val totalScore: Int, // 0-100
    val timeLimitScore: Int, // 0-40 points
    val focusSessionScore: Int, // 0-20 points
    val breaksScore: Int, // 0-20 points
    val sleepHygieneScore: Int, // 0-20 points
    val level: String, // digital_sprout, mindful_explorer, balanced_user, wellness_master
    val calculatedAt: Long = System.currentTimeMillis()
)