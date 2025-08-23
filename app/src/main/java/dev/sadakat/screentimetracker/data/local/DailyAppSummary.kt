package dev.sadakat.screentimetracker.data.local

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
