package dev.sadakat.screentimetracker.core.data.local.dao
import dev.sadakat.screentimetracker.core.data.local.entities.*

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReplacementActivityDao {
    
    // Replacement Activities
    @Query("SELECT * FROM replacement_activities ORDER BY averageRating DESC, timesCompleted DESC")
    fun getAllActivities(): Flow<List<ReplacementActivity>>
    
    @Query("SELECT * FROM replacement_activities WHERE category = :category ORDER BY averageRating DESC")
    fun getActivitiesByCategory(category: String): Flow<List<ReplacementActivity>>
    
    @Query("SELECT * FROM replacement_activities WHERE difficultyLevel <= :maxDifficulty ORDER BY averageRating DESC")
    fun getActivitiesByDifficulty(maxDifficulty: Int): Flow<List<ReplacementActivity>>
    
    @Query("SELECT * FROM replacement_activities WHERE estimatedDurationMinutes <= :maxMinutes ORDER BY averageRating DESC")
    fun getActivitiesByDuration(maxMinutes: Int): Flow<List<ReplacementActivity>>
    
    @Query("SELECT * FROM replacement_activities WHERE id = :id")
    suspend fun getActivityById(id: Long): ReplacementActivity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ReplacementActivity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<ReplacementActivity>)
    
    @Update
    suspend fun updateActivity(activity: ReplacementActivity)
    
    @Delete
    suspend fun deleteActivity(activity: ReplacementActivity)
    
    @Query("UPDATE replacement_activities SET timesCompleted = timesCompleted + 1, lastCompletedAt = :timestamp WHERE id = :activityId")
    suspend fun incrementCompletionCount(activityId: Long, timestamp: Long)
    
    @Query("UPDATE replacement_activities SET averageRating = :rating WHERE id = :activityId")
    suspend fun updateAverageRating(activityId: Long, rating: Float)
    
    // Smart suggestions based on context
    @Query("""
        SELECT * FROM replacement_activities 
        WHERE estimatedDurationMinutes <= :availableMinutes 
        AND difficultyLevel <= :maxDifficulty
        AND category = :preferredCategory
        ORDER BY averageRating DESC, timesCompleted DESC
        LIMIT :limit
    """)
    suspend fun getSmartSuggestions(
        availableMinutes: Int, 
        maxDifficulty: Int, 
        preferredCategory: String, 
        limit: Int
    ): List<ReplacementActivity>
    
    // Export methods
    @Query("SELECT * FROM replacement_activities ORDER BY createdAt ASC")
    suspend fun getAllReplacementActivitiesForExport(): List<ReplacementActivity>
}

data class CategoryStat(
    val category: String,
    val count: Int
)