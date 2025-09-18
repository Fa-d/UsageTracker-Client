<<<<<<<< HEAD:core/database/src/main/java/dev/sadakat/screentimetracker/core/database/entities/DailyScreenUnlockSummary.kt
package dev.sadakat.screentimetracker.core.database.entities
========
package dev.sadakat.screentimetracker.data.local.entities
>>>>>>>> origin/detached3:backup_src_20250918_005155/main/java/dev/sadakat/screentimetracker/data/local/entities/DailyScreenUnlockSummary.kt

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_screen_unlock_summary")
data class DailyScreenUnlockSummary(
    @PrimaryKey // Date will be unique for unlock summaries
    val dateMillis: Long, // Timestamp for the start of the day (00:00:00)
    val unlockCount: Int
)