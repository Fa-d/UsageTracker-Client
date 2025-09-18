<<<<<<<< HEAD:core/database/src/main/java/dev/sadakat/screentimetracker/core/database/entities/MindfulnessSession.kt
package dev.sadakat.screentimetracker.core.database.entities
========
package dev.sadakat.screentimetracker.data.local.entities
>>>>>>>> origin/detached3:backup_src_20250918_005155/main/java/dev/sadakat/screentimetracker/data/local/entities/MindfulnessSession.kt

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