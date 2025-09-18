<<<<<<<< HEAD:core/database/src/main/java/dev/sadakat/screentimetracker/core/database/entities/DailyAppSummary.kt
package dev.sadakat.screentimetracker.core.database.entities
========
package dev.sadakat.screentimetracker.data.local.entities
>>>>>>>> origin/detached3:backup_src_20250918_005155/main/java/dev/sadakat/screentimetracker/data/local/entities/DailyAppSummary.kt

import androidx.room.Entity
import androidx.room.Index

// Define a composite primary key for date and package name
@Entity(
    tableName = "daily_app_summary",
    primaryKeys = ["dateMillis", "packageName"],
    indices = [Index(value = ["dateMillis"]), Index(value = ["packageName"])]
)
data class DailyAppSummary(
    val dateMillis: Long, // Timestamp for the start of the day (00:00:00)
    val packageName: String,
    val totalDurationMillis: Long,
    val openCount: Int // Number of sessions for that app on that day
)