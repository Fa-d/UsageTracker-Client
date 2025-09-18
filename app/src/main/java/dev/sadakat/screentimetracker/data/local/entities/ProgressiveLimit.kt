package dev.sadakat.screentimetracker.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "progressive_limits")
data class ProgressiveLimit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "app_package_name")
    val appPackageName: String,
    
    @ColumnInfo(name = "original_limit_millis")
    val originalLimitMillis: Long, // User's current usage + 10% buffer
    
    @ColumnInfo(name = "target_limit_millis") 
    val targetLimitMillis: Long, // Final goal
    
    @ColumnInfo(name = "current_limit_millis")
    val currentLimitMillis: Long, // Current week's limit
    
    @ColumnInfo(name = "reduction_percentage")
    val reductionPercentage: Int = 10, // Weekly reduction %
    
    @ColumnInfo(name = "start_date")
    val startDate: String, // LocalDate.toString()
    
    @ColumnInfo(name = "next_reduction_date")
    val nextReductionDate: String, // LocalDate.toString()
    
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    
    @ColumnInfo(name = "progress_percentage")
    val progressPercentage: Float = 0f, // 0-100%
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)