package dev.sadakat.screentimetracker.data.local.dao
import dev.sadakat.screentimetracker.data.local.entities.*

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MindfulnessSessionDao {
    
    @Query("SELECT * FROM mindfulness_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<MindfulnessSession>>
    
    @Query("SELECT * FROM mindfulness_sessions WHERE startTime >= :startTime ORDER BY startTime DESC")
    fun getSessionsSince(startTime: Long): Flow<List<MindfulnessSession>>
    
    @Query("SELECT * FROM mindfulness_sessions WHERE sessionType = :type ORDER BY startTime DESC LIMIT :limit")
    fun getSessionsByType(type: String, limit: Int = 10): Flow<List<MindfulnessSession>>
    
    @Query("SELECT COUNT(*) FROM mindfulness_sessions WHERE startTime >= :startTime")
    suspend fun getSessionCountSince(startTime: Long): Int
    
    @Query("SELECT SUM(durationMillis) FROM mindfulness_sessions WHERE startTime >= :startTime")
    suspend fun getTotalDurationSince(startTime: Long): Long?
    
    @Query("SELECT AVG(userRating) FROM mindfulness_sessions WHERE userRating > 0 AND startTime >= :startTime")
    suspend fun getAverageRatingSince(startTime: Long): Float?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: MindfulnessSession): Long
    
    @Update
    suspend fun updateSession(session: MindfulnessSession)
    
    @Query("UPDATE mindfulness_sessions SET userRating = :rating, notes = :notes WHERE id = :sessionId")
    suspend fun updateSessionFeedback(sessionId: Long, rating: Int, notes: String)
    
    @Delete
    suspend fun deleteSession(session: MindfulnessSession)
    
    // Export method
    @Query("SELECT * FROM mindfulness_sessions ORDER BY startTime ASC")
    suspend fun getAllMindfulnessSessionsForExport(): List<MindfulnessSession>
}