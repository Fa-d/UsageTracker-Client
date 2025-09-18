<<<<<<<< HEAD:core/database/src/main/java/dev/sadakat/screentimetracker/core/database/query/AppOpenData.kt
package dev.sadakat.screentimetracker.core.database.query
========
package dev.sadakat.screentimetracker.data.local.dto
>>>>>>>> origin/detached3:backup_src_20250918_005155/main/java/dev/sadakat/screentimetracker/data/local/dto/AppOpenData.kt

// Data class for app open count information
data class AppOpenData(
    val packageName: String,
    val openCount: Int,
    val lastOpenedTimestamp: Long = 0L
)