<<<<<<<< HEAD:core/database/src/main/java/dev/sadakat/screentimetracker/core/database/entities/ScreenUnlockEvent.kt
package dev.sadakat.screentimetracker.core.database.entities
========
package dev.sadakat.screentimetracker.data.local.entities
>>>>>>>> origin/detached3:backup_src_20250918_005155/main/java/dev/sadakat/screentimetracker/data/local/entities/ScreenUnlockEvent.kt

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "screen_unlock_events")
data class ScreenUnlockEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long
)