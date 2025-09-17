package dev.sadakat.screentimetracker.domain.usecases

import dev.sadakat.screentimetracker.data.local.entities.MindfulnessSession
import dev.sadakat.screentimetracker.data.local.dao.MindfulnessSessionDao
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
import java.time.ZoneId

class MindfulnessUseCaseTest {

    private val mockMindfulnessSessionDao = mockk<MindfulnessSessionDao>()
    private lateinit var mindfulnessUseCase: MindfulnessUseCase

    @Before
    fun setup() {
        mindfulnessUseCase = MindfulnessUseCase(mockMindfulnessSessionDao)
        MockKAnnotations.init(this)
    }

    // Test session start functionality
    @Test
    fun `startSession should create and insert new mindfulness session`() = runTest {
        // Given
        val sessionType = "breathing"
        val durationMillis = 300000L // 5 minutes
        val triggeredByAppBlock = false
        val appThatWasBlocked = ""
        val expectedSessionId = 123L

        coEvery { mockMindfulnessSessionDao.insertSession(any()) } returns expectedSessionId

        // When
        val result = mindfulnessUseCase.startSession(
            sessionType = sessionType,
            durationMillis = durationMillis,
            triggeredByAppBlock = triggeredByAppBlock,
            appThatWasBlocked = appThatWasBlocked
        )

        // Then
        assertEquals(expectedSessionId, result)
        coVerify {
            mockMindfulnessSessionDao.insertSession(
                match { session ->
                    session.sessionType == sessionType &&
                    session.durationMillis == durationMillis &&
                    session.startTime > 0 &&
                    session.endTime == 0L &&
                    session.completionRate == 0f &&
                    session.triggeredByAppBlock == triggeredByAppBlock &&
                    session.appThatWasBlocked == appThatWasBlocked
                }
            )
        }
    }

    @Test
    fun `startSession should handle app-blocked triggered sessions`() = runTest {
        // Given
        val sessionType = "meditation"
        val durationMillis = 600000L // 10 minutes
        val triggeredByAppBlock = true
        val appThatWasBlocked = "com.instagram.android"
        val expectedSessionId = 456L

        coEvery { mockMindfulnessSessionDao.insertSession(any()) } returns expectedSessionId

        // When
        val result = mindfulnessUseCase.startSession(
            sessionType = sessionType,
            durationMillis = durationMillis,
            triggeredByAppBlock = triggeredByAppBlock,
            appThatWasBlocked = appThatWasBlocked
        )

        // Then
        assertEquals(expectedSessionId, result)
        coVerify {
            mockMindfulnessSessionDao.insertSession(
                match { session ->
                    session.sessionType == sessionType &&
                    session.durationMillis == durationMillis &&
                    session.triggeredByAppBlock == true &&
                    session.appThatWasBlocked == appThatWasBlocked
                }
            )
        }
    }

    @Test
    fun `startSession should set correct start time`() = runTest {
        // Given
        val sessionType = "gratitude"
        val durationMillis = 180000L // 3 minutes
        coEvery { mockMindfulnessSessionDao.insertSession(any()) } returns 1L

        val beforeStart = System.currentTimeMillis()

        // When
        mindfulnessUseCase.startSession(sessionType, durationMillis)

        val afterStart = System.currentTimeMillis()

        // Then
        coVerify {
            mockMindfulnessSessionDao.insertSession(
                match { session ->
                    session.startTime >= beforeStart && session.startTime <= afterStart
                }
            )
        }
    }

    // Test session end functionality
    @Test
    fun `endSession should create and update completed session`() = runTest {
        // Given
        val sessionId = 123L
        val completionRate = 0.95f
        val endTime = System.currentTimeMillis()
        val existingSessions = listOf<MindfulnessSession>()

        every { mockMindfulnessSessionDao.getAllSessions() } returns flowOf(existingSessions)
        coEvery { mockMindfulnessSessionDao.updateSession(any()) } just Runs

        // When
        val result = mindfulnessUseCase.endSession(sessionId, completionRate, endTime)

        // Then
        assertEquals(sessionId, result.id)
        assertEquals(completionRate, result.completionRate, 0.001f)
        assertEquals(endTime, result.endTime)
        assertEquals("breathing", result.sessionType) // Default type from implementation
        coVerify { mockMindfulnessSessionDao.updateSession(any()) }
    }

    @Test
    fun `endSession should calculate duration correctly`() = runTest {
        // Given
        val sessionId = 456L
        val completionRate = 1.0f
        val currentTime = System.currentTimeMillis()
        val endTime = currentTime + 300000L // 5 minutes later

        every { mockMindfulnessSessionDao.getAllSessions() } returns flowOf(emptyList())
        coEvery { mockMindfulnessSessionDao.updateSession(any()) } just Runs

        // When
        val result = mindfulnessUseCase.endSession(sessionId, completionRate, endTime)

        // Then
        assertTrue("Duration should be positive", result.durationMillis > 0)
        coVerify { mockMindfulnessSessionDao.updateSession(result) }
    }

    // Test session feedback functionality
    @Test
    fun `updateSessionFeedback should call dao with correct parameters`() = runTest {
        // Given
        val sessionId = 789L
        val rating = 4
        val notes = "Great session, felt very relaxed"

        coEvery { mockMindfulnessSessionDao.updateSessionFeedback(any(), any(), any()) } just Runs

        // When
        mindfulnessUseCase.updateSessionFeedback(sessionId, rating, notes)

        // Then
        coVerify { mockMindfulnessSessionDao.updateSessionFeedback(sessionId, rating, notes) }
    }

    @Test
    fun `updateSessionFeedback should handle empty notes`() = runTest {
        // Given
        val sessionId = 101L
        val rating = 3
        val notes = ""

        coEvery { mockMindfulnessSessionDao.updateSessionFeedback(any(), any(), any()) } just Runs

        // When
        mindfulnessUseCase.updateSessionFeedback(sessionId, rating, notes)

        // Then
        coVerify { mockMindfulnessSessionDao.updateSessionFeedback(sessionId, rating, notes) }
    }

    // Test session retrieval functionality
    @Test
    fun `getAllSessions should return flow from dao`() = runTest {
        // Given
        val mockSessions = listOf(
            createMockMindfulnessSession(1L, "breathing", 300000L),
            createMockMindfulnessSession(2L, "meditation", 600000L)
        )
        every { mockMindfulnessSessionDao.getAllSessions() } returns flowOf(mockSessions)

        // When
        val result = mindfulnessUseCase.getAllSessions().first()

        // Then
        assertEquals(mockSessions, result)
        verify { mockMindfulnessSessionDao.getAllSessions() }
    }

    @Test
    fun `getSessionsForToday should query sessions since start of today`() = runTest {
        // Given
        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val mockSessions = listOf(createMockMindfulnessSession(1L, "breathing", 300000L))
        
        every { mockMindfulnessSessionDao.getSessionsSince(any()) } returns flowOf(mockSessions)

        // When
        val result = mindfulnessUseCase.getSessionsForToday().first()

        // Then
        assertEquals(mockSessions, result)
        verify { 
            mockMindfulnessSessionDao.getSessionsSince(
                match { timestamp -> 
                    Math.abs(timestamp - todayStart) < 1000L // Allow 1 second tolerance for timing
                }
            )
        }
    }

    @Test
    fun `getSessionsForWeek should query sessions since 7 days ago`() = runTest {
        // Given
        val weekStart = LocalDate.now().minusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val mockSessions = listOf(
            createMockMindfulnessSession(1L, "breathing", 300000L),
            createMockMindfulnessSession(2L, "meditation", 600000L)
        )
        
        every { mockMindfulnessSessionDao.getSessionsSince(any()) } returns flowOf(mockSessions)

        // When
        val result = mindfulnessUseCase.getSessionsForWeek().first()

        // Then
        assertEquals(mockSessions, result)
        verify { 
            mockMindfulnessSessionDao.getSessionsSince(
                match { timestamp -> 
                    Math.abs(timestamp - weekStart) < 1000L // Allow 1 second tolerance for timing
                }
            )
        }
    }

    // Test session statistics functionality
    @Test
    fun `getTodaySessionCount should return count from dao`() = runTest {
        // Given
        val expectedCount = 3
        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        coEvery { mockMindfulnessSessionDao.getSessionCountSince(any()) } returns expectedCount

        // When
        val result = mindfulnessUseCase.getTodaySessionCount()

        // Then
        assertEquals(expectedCount, result)
        coVerify { 
            mockMindfulnessSessionDao.getSessionCountSince(
                match { timestamp -> 
                    Math.abs(timestamp - todayStart) < 1000L // Allow 1 second tolerance for timing
                }
            )
        }
    }

    @Test
    fun `getTodayTotalDuration should return duration from dao`() = runTest {
        // Given
        val expectedDuration = 900000L // 15 minutes
        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        coEvery { mockMindfulnessSessionDao.getTotalDurationSince(any()) } returns expectedDuration

        // When
        val result = mindfulnessUseCase.getTodayTotalDuration()

        // Then
        assertEquals(expectedDuration, result)
        coVerify { 
            mockMindfulnessSessionDao.getTotalDurationSince(
                match { timestamp -> 
                    Math.abs(timestamp - todayStart) < 1000L // Allow 1 second tolerance for timing
                }
            )
        }
    }

    @Test
    fun `getTodayTotalDuration should return zero when dao returns null`() = runTest {
        // Given
        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        coEvery { mockMindfulnessSessionDao.getTotalDurationSince(any()) } returns null

        // When
        val result = mindfulnessUseCase.getTodayTotalDuration()

        // Then
        assertEquals(0L, result)
        coVerify { mockMindfulnessSessionDao.getTotalDurationSince(any()) }
    }

    @Test
    fun `getWeeklyAverageRating should return rating from dao`() = runTest {
        // Given
        val expectedRating = 4.2f
        val weekStart = LocalDate.now().minusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        coEvery { mockMindfulnessSessionDao.getAverageRatingSince(any()) } returns expectedRating

        // When
        val result = mindfulnessUseCase.getWeeklyAverageRating()

        // Then
        assertEquals(expectedRating, result, 0.001f)
        coVerify { 
            mockMindfulnessSessionDao.getAverageRatingSince(
                match { timestamp -> 
                    Math.abs(timestamp - weekStart) < 1000L // Allow 1 second tolerance for timing
                }
            )
        }
    }

    @Test
    fun `getWeeklyAverageRating should return zero when dao returns null`() = runTest {
        // Given
        val weekStart = LocalDate.now().minusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        coEvery { mockMindfulnessSessionDao.getAverageRatingSince(any()) } returns null

        // When
        val result = mindfulnessUseCase.getWeeklyAverageRating()

        // Then
        assertEquals(0f, result, 0.001f)
        coVerify { mockMindfulnessSessionDao.getAverageRatingSince(any()) }
    }

    @Test
    fun `getSessionStats should return complete statistics for given period`() = runTest {
        // Given
        val days = 30
        val expectedCount = 15
        val expectedDuration = 4500000L // 75 minutes total
        val expectedRating = 4.1f
        
        coEvery { mockMindfulnessSessionDao.getSessionCountSince(any()) } returns expectedCount
        coEvery { mockMindfulnessSessionDao.getTotalDurationSince(any()) } returns expectedDuration
        coEvery { mockMindfulnessSessionDao.getAverageRatingSince(any()) } returns expectedRating

        // When
        val result = mindfulnessUseCase.getSessionStats(days)

        // Then
        assertEquals(expectedCount, result.totalSessions)
        assertEquals(expectedDuration, result.totalDurationMillis)
        assertEquals(expectedRating, result.averageRating, 0.001f)
        
        val expectedStartTime = LocalDate.now().minusDays(days.toLong()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        coVerify { 
            mockMindfulnessSessionDao.getSessionCountSince(
                match { timestamp -> 
                    Math.abs(timestamp - expectedStartTime) < 1000L // Allow 1 second tolerance
                }
            )
        }
        coVerify { 
            mockMindfulnessSessionDao.getTotalDurationSince(
                match { timestamp -> 
                    Math.abs(timestamp - expectedStartTime) < 1000L // Allow 1 second tolerance
                }
            )
        }
        coVerify { 
            mockMindfulnessSessionDao.getAverageRatingSince(
                match { timestamp -> 
                    Math.abs(timestamp - expectedStartTime) < 1000L // Allow 1 second tolerance
                }
            )
        }
    }

    @Test
    fun `getSessionStats should handle null values from dao gracefully`() = runTest {
        // Given
        val days = 7
        val expectedCount = 5
        
        coEvery { mockMindfulnessSessionDao.getSessionCountSince(any()) } returns expectedCount
        coEvery { mockMindfulnessSessionDao.getTotalDurationSince(any()) } returns null
        coEvery { mockMindfulnessSessionDao.getAverageRatingSince(any()) } returns null

        // When
        val result = mindfulnessUseCase.getSessionStats(days)

        // Then
        assertEquals(expectedCount, result.totalSessions)
        assertEquals(0L, result.totalDurationMillis)
        assertEquals(0f, result.averageRating, 0.001f)
    }

    // Test session filtering functionality
    @Test
    fun `getSessionsByType should return sessions filtered by type`() = runTest {
        // Given
        val sessionType = "breathing"
        val limit = 5
        val mockSessions = listOf(
            createMockMindfulnessSession(1L, "breathing", 300000L),
            createMockMindfulnessSession(2L, "breathing", 420000L)
        )
        
        every { mockMindfulnessSessionDao.getSessionsByType(any(), any()) } returns flowOf(mockSessions)

        // When
        val result = mindfulnessUseCase.getSessionsByType(sessionType, limit).first()

        // Then
        assertEquals(mockSessions, result)
        verify { mockMindfulnessSessionDao.getSessionsByType(sessionType, limit) }
    }

    @Test
    fun `getSessionsByType should use default limit when not specified`() = runTest {
        // Given
        val sessionType = "meditation"
        val mockSessions = listOf(createMockMindfulnessSession(1L, "meditation", 600000L))
        
        every { mockMindfulnessSessionDao.getSessionsByType(any(), any()) } returns flowOf(mockSessions)

        // When
        mindfulnessUseCase.getSessionsByType(sessionType).first()

        // Then
        verify { mockMindfulnessSessionDao.getSessionsByType(sessionType, 10) } // Default limit should be 10
    }

    // Test edge cases
    @Test
    fun `startSession should handle zero duration`() = runTest {
        // Given
        val sessionType = "breathing"
        val durationMillis = 0L
        
        coEvery { mockMindfulnessSessionDao.insertSession(any()) } returns 1L

        // When
        val result = mindfulnessUseCase.startSession(sessionType, durationMillis)

        // Then
        assertEquals(1L, result)
        coVerify {
            mockMindfulnessSessionDao.insertSession(
                match { session -> session.durationMillis == 0L }
            )
        }
    }

    @Test
    fun `startSession should handle negative duration`() = runTest {
        // Given
        val sessionType = "breathing"
        val durationMillis = -1000L
        
        coEvery { mockMindfulnessSessionDao.insertSession(any()) } returns 1L

        // When
        val result = mindfulnessUseCase.startSession(sessionType, durationMillis)

        // Then
        assertEquals(1L, result)
        coVerify {
            mockMindfulnessSessionDao.insertSession(
                match { session -> session.durationMillis == -1000L }
            )
        }
    }

    @Test
    fun `endSession should handle completion rate greater than 1`() = runTest {
        // Given
        val sessionId = 1L
        val completionRate = 1.5f // Greater than 100%
        val endTime = System.currentTimeMillis()
        
        every { mockMindfulnessSessionDao.getAllSessions() } returns flowOf(emptyList())
        coEvery { mockMindfulnessSessionDao.updateSession(any()) } just Runs

        // When
        val result = mindfulnessUseCase.endSession(sessionId, completionRate, endTime)

        // Then
        assertEquals(completionRate, result.completionRate, 0.001f)
        coVerify { mockMindfulnessSessionDao.updateSession(any()) }
    }

    @Test
    fun `endSession should handle negative completion rate`() = runTest {
        // Given
        val sessionId = 1L
        val completionRate = -0.1f
        val endTime = System.currentTimeMillis()
        
        every { mockMindfulnessSessionDao.getAllSessions() } returns flowOf(emptyList())
        coEvery { mockMindfulnessSessionDao.updateSession(any()) } just Runs

        // When
        val result = mindfulnessUseCase.endSession(sessionId, completionRate, endTime)

        // Then
        assertEquals(completionRate, result.completionRate, 0.001f)
        coVerify { mockMindfulnessSessionDao.updateSession(any()) }
    }

    @Test
    fun `updateSessionFeedback should handle extreme rating values`() = runTest {
        // Given
        val sessionId = 1L
        val rating = 10 // Higher than typical 1-5 scale
        val notes = "Exceptional session"
        
        coEvery { mockMindfulnessSessionDao.updateSessionFeedback(any(), any(), any()) } just Runs

        // When
        mindfulnessUseCase.updateSessionFeedback(sessionId, rating, notes)

        // Then
        coVerify { mockMindfulnessSessionDao.updateSessionFeedback(sessionId, rating, notes) }
    }

    @Test
    fun `getSessionStats should handle zero days period`() = runTest {
        // Given
        val days = 0
        
        coEvery { mockMindfulnessSessionDao.getSessionCountSince(any()) } returns 0
        coEvery { mockMindfulnessSessionDao.getTotalDurationSince(any()) } returns 0L
        coEvery { mockMindfulnessSessionDao.getAverageRatingSince(any()) } returns 0f

        // When
        val result = mindfulnessUseCase.getSessionStats(days)

        // Then
        assertEquals(0, result.totalSessions)
        assertEquals(0L, result.totalDurationMillis)
        assertEquals(0f, result.averageRating, 0.001f)
    }

    // Helper function to create mock MindfulnessSession
    private fun createMockMindfulnessSession(
        id: Long,
        sessionType: String,
        durationMillis: Long,
        startTime: Long = System.currentTimeMillis(),
        endTime: Long = System.currentTimeMillis() + durationMillis,
        completionRate: Float = 1.0f,
        userRating: Int = 4,
        notes: String = "",
        triggeredByAppBlock: Boolean = false,
        appThatWasBlocked: String = ""
    ) = MindfulnessSession(
        id = id,
        sessionType = sessionType,
        durationMillis = durationMillis,
        startTime = startTime,
        endTime = endTime,
        completionRate = completionRate,
        userRating = userRating,
        notes = notes,
        triggeredByAppBlock = triggeredByAppBlock,
        appThatWasBlocked = appThatWasBlocked
    )
}