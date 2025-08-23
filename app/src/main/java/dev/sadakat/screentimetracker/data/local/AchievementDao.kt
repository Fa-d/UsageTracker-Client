package dev.sadakat.screentimetracker.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY isUnlocked DESC, achievementId ASC")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE isUnlocked = 1 ORDER BY unlockedDate DESC")
    fun getUnlockedAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE achievementId = :id")
    suspend fun getAchievementById(id: String): Achievement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<Achievement>)

    @Update
    suspend fun updateAchievement(achievement: Achievement)

    @Query("UPDATE achievements SET currentProgress = :progress WHERE achievementId = :id")
    suspend fun updateAchievementProgress(id: String, progress: Int)

    @Query("UPDATE achievements SET isUnlocked = 1, unlockedDate = :unlockedDate WHERE achievementId = :id")
    suspend fun unlockAchievement(id: String, unlockedDate: Long)

    // Export methods
    @Query("SELECT * FROM achievements ORDER BY achievementId ASC")
    suspend fun getAllAchievementsForExport(): List<Achievement>

    @Query("SELECT COUNT(*) FROM achievements WHERE isUnlocked = 1")
    suspend fun getUnlockedAchievementsCount(): Int
}