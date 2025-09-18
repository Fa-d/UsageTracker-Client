<<<<<<<< HEAD:core/database/src/main/java/dev/sadakat/screentimetracker/core/database/query/AppLastOpenedData.kt
package dev.sadakat.screentimetracker.core.database.query
========
package dev.sadakat.screentimetracker.data.local.dto
>>>>>>>> origin/detached3:backup_src_20250918_005155/main/java/dev/sadakat/screentimetracker/data/local/dto/AppLastOpenedData.kt

// Data class for last opened timestamp data
data class AppLastOpenedData(
    val packageName: String,
    val lastOpenedTimestamp: Long
)