<<<<<<<< HEAD:core/database/src/main/java/dev/sadakat/screentimetracker/core/database/entities/Challenge.kt
package dev.sadakat.screentimetracker.core.database.entities
========
package dev.sadakat.screentimetracker.data.local.entities
>>>>>>>> origin/detached3:backup_src_20250918_005155/main/java/dev/sadakat/screentimetracker/data/local/entities/Challenge.kt

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