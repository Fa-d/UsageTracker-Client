<<<<<<<< HEAD:core/database/src/main/java/dev/sadakat/screentimetracker/core/database/entities/AppSessionEvent.kt
package dev.sadakat.screentimetracker.core.database.entities
========
package dev.sadakat.screentimetracker.data.local.entities
>>>>>>>> origin/detached3:backup_src_20250918_005155/main/java/dev/sadakat/screentimetracker/data/local/entities/AppSessionEvent.kt

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_session_events")
data class AppSessionEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val durationMillis: Long // Calculated as endTimeMillis - startTimeMillis
)