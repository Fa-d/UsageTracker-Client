package com.example.screentimetracker.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenges ORDER BY startDate DESC")
    fun getAllChallenges(): Flow<List<Challenge>>

    @Query("SELECT * FROM challenges WHERE status = :status ORDER BY startDate DESC")
    fun getChallengesByStatus(status: String): Flow<List<Challenge>>

    @Query("SELECT * FROM challenges WHERE status = 'active' AND endDate >= :currentTime")
    fun getActiveChallenges(currentTime: Long): Flow<List<Challenge>>

    @Query("SELECT * FROM challenges WHERE challengeId = :challengeId ORDER BY startDate DESC LIMIT 1")
    suspend fun getLatestChallengeByType(challengeId: String): Challenge?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: Challenge): Long

    @Update
    suspend fun updateChallenge(challenge: Challenge)

    @Query("UPDATE challenges SET currentProgress = :progress WHERE id = :id")
    suspend fun updateChallengeProgress(id: Long, progress: Int)

    @Query("UPDATE challenges SET status = :status WHERE id = :id")
    suspend fun updateChallengeStatus(id: Long, status: String)

    @Query("SELECT * FROM challenges WHERE endDate < :currentTime AND status = 'active'")
    suspend fun getExpiredChallenges(currentTime: Long): List<Challenge>

    // Export methods
    @Query("SELECT * FROM challenges ORDER BY startDate ASC")
    suspend fun getAllChallengesForExport(): List<Challenge>
}