package dev.sadakat.screentimetracker.core.domain.usecases

import dev.sadakat.screentimetracker.core.data.local.dao.MindfulnessSessionDao
import dev.sadakat.screentimetracker.core.data.local.entities.MindfulnessSession
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId

class MindfulnessUseCase(
    private val mindfulnessSessionDao: MindfulnessSessionDao
) {
    
    suspend fun startSession(
        sessionType: String,
        durationMillis: Long,
        triggeredByAppBlock: Boolean = false,
        appThatWasBlocked: String = ""
    ): Long {
        val session = MindfulnessSession(
            sessionType = sessionType,
            durationMillis = durationMillis,
            startTime = System.currentTimeMillis(),
            endTime = 0, // Will be set when session ends
            completionRate = 0f,
            triggeredByAppBlock = triggeredByAppBlock,
            appThatWasBlocked = appThatWasBlocked
        )
        
        return mindfulnessSessionDao.insertSession(session)
    }
    
    suspend fun endSession(
        sessionId: Long,
        completionRate: Float,
        endTime: Long
    ): MindfulnessSession {
        val sessions = mindfulnessSessionDao.getAllSessions()
        // Note: In a real implementation, you'd want to get the specific session by ID
        // For now, we'll create and return the completed session
        
        val completedSession = MindfulnessSession(
            id = sessionId,
            sessionType = "breathing", // This should come from the stored session
            durationMillis = endTime - System.currentTimeMillis(), // This should be the actual duration
            startTime = System.currentTimeMillis() - (endTime - System.currentTimeMillis()),
            endTime = endTime,
            completionRate = completionRate
        )
        
        mindfulnessSessionDao.updateSession(completedSession)
        return completedSession
    }
    
    suspend fun updateSessionFeedback(
        sessionId: Long,
        rating: Int,
        notes: String
    ) {
        mindfulnessSessionDao.updateSessionFeedback(sessionId, rating, notes)
    }
    
    fun getAllSessions(): Flow<List<MindfulnessSession>> {
        return mindfulnessSessionDao.getAllSessions()
    }
    
    fun getSessionsForToday(): Flow<List<MindfulnessSession>> {
        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return mindfulnessSessionDao.getSessionsSince(todayStart)
    }
    
    fun getSessionsForWeek(): Flow<List<MindfulnessSession>> {
        val weekStart = LocalDate.now().minusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return mindfulnessSessionDao.getSessionsSince(weekStart)
    }
    
    suspend fun getTodaySessionCount(): Int {
        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return mindfulnessSessionDao.getSessionCountSince(todayStart)
    }
    
    suspend fun getTodayTotalDuration(): Long {
        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return mindfulnessSessionDao.getTotalDurationSince(todayStart) ?: 0L
    }
    
    suspend fun getWeeklyAverageRating(): Float {
        val weekStart = LocalDate.now().minusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return mindfulnessSessionDao.getAverageRatingSince(weekStart) ?: 0f
    }
    
    suspend fun getSessionStats(days: Int): SessionStats {
        val startTime = LocalDate.now().minusDays(days.toLong()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        return SessionStats(
            totalSessions = mindfulnessSessionDao.getSessionCountSince(startTime),
            totalDurationMillis = mindfulnessSessionDao.getTotalDurationSince(startTime) ?: 0L,
            averageRating = mindfulnessSessionDao.getAverageRatingSince(startTime) ?: 0f
        )
    }
    
    fun getSessionsByType(type: String, limit: Int = 10): Flow<List<MindfulnessSession>> {
        return mindfulnessSessionDao.getSessionsByType(type, limit)
    }
}

data class SessionStats(
    val totalSessions: Int,
    val totalDurationMillis: Long,
    val averageRating: Float
)