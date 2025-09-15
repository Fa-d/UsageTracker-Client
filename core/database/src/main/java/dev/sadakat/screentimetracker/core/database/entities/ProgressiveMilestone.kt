package dev.sadakat.screentimetracker.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "progressive_milestones",
    foreignKeys = [
        ForeignKey(
            entity = ProgressiveLimit::class,
            parentColumns = ["id"],
            childColumns = ["limit_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["limit_id"])]
)
data class ProgressiveMilestone(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "limit_id")
    val limitId: Long,
    
    @ColumnInfo(name = "milestone_percentage")
    val milestonePercentage: Int, // 25, 50, 75, 100
    
    @ColumnInfo(name = "is_achieved")
    val isAchieved: Boolean = false,
    
    @ColumnInfo(name = "achieved_date")
    val achievedDate: String? = null, // LocalDate.toString()
    
    @ColumnInfo(name = "reward_title")
    val rewardTitle: String,
    
    @ColumnInfo(name = "reward_description") 
    val rewardDescription: String,
    
    @ColumnInfo(name = "celebration_shown")
    val celebrationShown: Boolean = false
)