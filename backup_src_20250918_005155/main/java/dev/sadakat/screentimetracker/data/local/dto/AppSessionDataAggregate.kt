<<<<<<<< HEAD:core/database/src/main/java/dev/sadakat/screentimetracker/core/database/query/AppSessionDataAggregate.kt
package dev.sadakat.screentimetracker.core.database.query
========
package dev.sadakat.screentimetracker.data.local.dto
>>>>>>>> origin/detached3:backup_src_20250918_005155/main/java/dev/sadakat/screentimetracker/data/local/dto/AppSessionDataAggregate.kt

// Data class for aggregated session data
data class AppSessionDataAggregate(
    val packageName: String,
    val totalDuration: Long,
    val sessionCount: Int
)