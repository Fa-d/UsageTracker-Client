package dev.sadakat.screentimetracker.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserGoalDao {
    @Query("SELECT * FROM user_goals WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveGoals(): Flow<List<UserGoal>>

    @Query("SELECT * FROM user_goals WHERE goalType = :goalType AND isActive = 1")
    fun getGoalsByType(goalType: String): Flow<List<UserGoal>>

    @Query("SELECT * FROM user_goals WHERE packageName = :packageName AND isActive = 1")
    fun getGoalsForApp(packageName: String): Flow<List<UserGoal>>

    @Query("SELECT * FROM user_goals WHERE id = :id")
    suspend fun getGoalById(id: Long): UserGoal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: UserGoal): Long

    @Update
    suspend fun updateGoal(goal: UserGoal)

    @Query("UPDATE user_goals SET currentProgress = :progress, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateGoalProgress(id: Long, progress: Long, updatedAt: Long)

    @Query("UPDATE user_goals SET isActive = 0 WHERE id = :id")
    suspend fun deactivateGoal(id: Long)

    @Delete
    suspend fun deleteGoal(goal: UserGoal)

    @Query("SELECT * FROM user_goals WHERE deadline < :currentTime AND isActive = 1")
    suspend fun getExpiredGoals(currentTime: Long): List<UserGoal>

    // Export methods
    @Query("SELECT * FROM user_goals ORDER BY createdAt ASC")
    suspend fun getAllUserGoalsForExport(): List<UserGoal>
}