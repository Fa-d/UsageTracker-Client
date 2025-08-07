package com.example.screentimetracker.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressiveMilestoneDao {
    
    @Query("SELECT * FROM progressive_milestones WHERE limit_id = :limitId ORDER BY milestone_percentage ASC")
    suspend fun getMilestonesForLimit(limitId: Long): List<ProgressiveMilestone>
    
    @Query("SELECT * FROM progressive_milestones WHERE is_achieved = 1 AND celebration_shown = 0")
    suspend fun getUncelebratedMilestones(): List<ProgressiveMilestone>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: ProgressiveMilestone)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<ProgressiveMilestone>)
    
    @Update
    suspend fun updateMilestone(milestone: ProgressiveMilestone)
    
    @Query("UPDATE progressive_milestones SET is_achieved = 1, achieved_date = :achievedDate WHERE limit_id = :limitId AND milestone_percentage = :percentage")
    suspend fun markMilestoneAchieved(limitId: Long, percentage: Int, achievedDate: String)
    
    @Query("UPDATE progressive_milestones SET celebration_shown = 1 WHERE id = :milestoneId")
    suspend fun markCelebrationShown(milestoneId: Long)
    
    @Query("SELECT COUNT(*) FROM progressive_milestones WHERE limit_id = :limitId AND is_achieved = 1")
    suspend fun getAchievedMilestonesCount(limitId: Long): Int

    // Export methods
    @Query("SELECT * FROM progressive_milestones ORDER BY limit_id ASC, milestone_percentage ASC")
    suspend fun getAllProgressiveMilestonesForExport(): List<ProgressiveMilestone>
}