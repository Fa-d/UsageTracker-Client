<<<<<<<< HEAD:core/database/src/main/java/dev/sadakat/screentimetracker/core/database/entities/LimitedApp.kt
package dev.sadakat.screentimetracker.core.database.entities
========
package dev.sadakat.screentimetracker.data.local.entities
>>>>>>>> origin/detached3:backup_src_20250918_005155/main/java/dev/sadakat/screentimetracker/data/local/entities/LimitedApp.kt

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "limited_apps")
data class LimitedApp(
    @PrimaryKey
    val packageName: String, // Package name of the app to be limited
    val timeLimitMillis: Long // Continuous usage time limit in milliseconds
)
