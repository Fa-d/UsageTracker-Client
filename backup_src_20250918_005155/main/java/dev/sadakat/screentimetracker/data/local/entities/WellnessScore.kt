<<<<<<<< HEAD:core/database/src/main/java/dev/sadakat/screentimetracker/core/database/entities/WellnessScore.kt
package dev.sadakat.screentimetracker.core.database.entities
========
package dev.sadakat.screentimetracker.data.local.entities
>>>>>>>> origin/detached3:backup_src_20250918_005155/main/java/dev/sadakat/screentimetracker/data/local/entities/WellnessScore.kt

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