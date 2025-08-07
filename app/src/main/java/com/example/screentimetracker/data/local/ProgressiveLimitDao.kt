package com.example.screentimetracker.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressiveLimitDao {
    
    @Query("SELECT * FROM progressive_limits WHERE is_active = 1")
    fun getAllActiveLimits(): Flow<List<ProgressiveLimit>>
    
    @Query("SELECT * FROM progressive_limits WHERE app_package_name = :packageName AND is_active = 1 LIMIT 1")
    suspend fun getActiveLimitForApp(packageName: String): ProgressiveLimit?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLimit(limit: ProgressiveLimit): Long
    
    @Update
    suspend fun updateLimit(limit: ProgressiveLimit)
    
    @Query("UPDATE progressive_limits SET is_active = 0 WHERE app_package_name = :packageName")
    suspend fun deactivateLimitForApp(packageName: String)
    
    @Query("SELECT * FROM progressive_limits WHERE next_reduction_date <= :currentDate AND is_active = 1")
    suspend fun getLimitsReadyForReduction(currentDate: String): List<ProgressiveLimit>
    
    @Query("DELETE FROM progressive_limits WHERE id = :limitId")
    suspend fun deleteLimit(limitId: Long)
    
    @Query("SELECT * FROM progressive_limits WHERE app_package_name = :packageName ORDER BY created_at DESC")
    fun getLimitHistoryForApp(packageName: String): Flow<List<ProgressiveLimit>>
    
    @Query("SELECT COUNT(*) FROM progressive_limits WHERE is_active = 1")
    suspend fun getActiveLimitsCount(): Int

    // Export methods
    @Query("SELECT * FROM progressive_limits ORDER BY created_at ASC")
    suspend fun getAllProgressiveLimitsForExport(): List<ProgressiveLimit>
}