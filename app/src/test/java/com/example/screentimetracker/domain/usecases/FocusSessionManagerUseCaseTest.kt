package dev.sadakat.screentimetracker.domain.usecases

import dev.sadakat.screentimetracker.data.local.FocusSession
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import dev.sadakat.screentimetracker.utils.ui.AppNotificationManager
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
class FocusSessionManagerUseCaseTest {

    private val mockRepository = mockk<TrackerRepository>(relaxed = true)
    private val mockNotificationManager = mockk<AppNotificationManager>(relaxed = true)
    private val mockAppLogger = mockk<AppLogger>(relaxed = true)
    private lateinit var focusSessionManager: FocusSessionManagerUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        focusSessionManager = FocusSessionManagerUseCase(mockRepository, mockNotificationManager, mockAppLogger)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `startFocusSession should create session and show notification`() = runTest {
        val durationMinutes = 25
        val appsToBlock = listOf("com.instagram.android", "com.facebook.katana")
        val sessionId = 1L

        coEvery { mockRepository.insertFocusSession(any()) } returns sessionId
        coEvery { mockNotificationManager.showFocusSessionStart(any()) } just Runs

        val result = focusSessionManager.startFocusSession(durationMinutes, appsToBlock)

        assertEquals(sessionId, result)
        
        // Verify focus session was inserted
        coVerify { mockRepository.insertFocusSession(any()) }
        
        // Verify notification was shown
        coVerify { mockNotificationManager.showFocusSessionStart(durationMinutes) }
        
        // Verify session is now active
        assertTrue(focusSessionManager.isSessionActive())
        assertEquals(sessionId, focusSessionManager.getCurrentSessionId())
    }

    @Test
    fun `completeFocusSession should update session and show notification`() = runTest {
        // Start a session first
        coEvery { mockRepository.insertFocusSession(any()) } returns 1L
        focusSessionManager.startFocusSession(25)

        // Mock completion
        coEvery { mockRepository.completeFocusSession(any(), any(), any(), any(), any()) } just Runs
        coEvery { mockRepository.getFocusSessionsForDate(any()) } returns emptyList()

        val result = focusSessionManager.completeFocusSession(wasSuccessful = true, interruptionCount = 0)

        assertTrue(result)
        
        // Verify session was completed in repository
        coVerify { mockRepository.completeFocusSession(1L, any(), any(), true, 0) }
        
        // Verify completion notification was shown
        coVerify { mockNotificationManager.showFocusSessionComplete(any(), true) }
        
        // Verify session is no longer active
        assertFalse(focusSessionManager.isSessionActive())
        assertNull(focusSessionManager.getCurrentSessionId())
    }

    @Test
    fun `completeFocusSession should handle unsuccessful completion`() = runTest {
        // Start a session first
        coEvery { mockRepository.insertFocusSession(any()) } returns 1L
        focusSessionManager.startFocusSession(25)

        coEvery { mockRepository.completeFocusSession(any(), any(), any(), any(), any()) } just Runs
        coEvery { mockRepository.getFocusSessionsForDate(any()) } returns emptyList()

        val result = focusSessionManager.completeFocusSession(wasSuccessful = false, interruptionCount = 2)

        assertTrue(result)
        
        // Verify session was marked as unsuccessful
        coVerify { mockRepository.completeFocusSession(1L, any(), any(), false, 2) }
        
        // Verify failure notification was shown
        coVerify { mockNotificationManager.showFocusSessionComplete(any(), false) }
    }

    @Test
    fun `cancelCurrentFocusSession should complete session as unsuccessful`() = runTest {
        // Start a session first
        coEvery { mockRepository.insertFocusSession(any()) } returns 1L
        focusSessionManager.startFocusSession(25)

        coEvery { mockRepository.completeFocusSession(any(), any(), any(), any(), any()) } just Runs
        coEvery { mockRepository.getFocusSessionsForDate(any()) } returns emptyList()

        val result = focusSessionManager.cancelCurrentFocusSession()

        assertTrue(result)
        
        // Verify session was completed as unsuccessful with 1 interruption
        coVerify { mockRepository.completeFocusSession(1L, any(), any(), false, 1) }
    }

    @Test
    fun `isSessionActive should return false when no session is active`() {
        assertFalse(focusSessionManager.isSessionActive())
    }

    @Test
    fun `getCurrentSessionDuration should return 0 when no session is active`() = runTest {
        val duration = focusSessionManager.getCurrentSessionDuration()
        assertEquals(0L, duration)
    }

    @Test
    fun `getCurrentSessionDuration should return elapsed time for active session`() = runTest {
        // Start a session
        coEvery { mockRepository.insertFocusSession(any()) } returns 1L
        val startTime = System.currentTimeMillis()
        
        focusSessionManager.startFocusSession(25)
        
        // Wait a bit to simulate elapsed time
        Thread.sleep(100) // 100ms
        
        val duration = focusSessionManager.getCurrentSessionDuration()
        assertTrue("Duration should be greater than 0", duration > 0)
        assertTrue("Duration should be reasonable", duration < 1000) // Less than 1 second for test
    }

    @Test
    fun `getFocusStats should calculate statistics correctly`() = runTest {
        val testSessions = listOf(
            FocusSession(
                id = 1,
                startTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2),
                endTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1) - TimeUnit.MINUTES.toMillis(35),
                targetDurationMillis = TimeUnit.MINUTES.toMillis(25),
                actualDurationMillis = TimeUnit.MINUTES.toMillis(25),
                appsBlocked = "[]",
                wasSuccessful = true
            ),
            FocusSession(
                id = 2,
                startTime = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1),
                endTime = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(45),
                targetDurationMillis = TimeUnit.MINUTES.toMillis(25),
                actualDurationMillis = TimeUnit.MINUTES.toMillis(15),
                appsBlocked = "[]",
                wasSuccessful = false
            ),
            FocusSession(
                id = 3,
                startTime = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(30),
                endTime = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5),
                targetDurationMillis = TimeUnit.MINUTES.toMillis(25),
                actualDurationMillis = TimeUnit.MINUTES.toMillis(25),
                appsBlocked = "[]",
                wasSuccessful = true
            )
        )

        coEvery { mockRepository.getFocusSessionsForDate(any()) } returns testSessions

        val stats = focusSessionManager.getFocusStats(System.currentTimeMillis())

        assertEquals(3, stats.totalSessions)
        assertEquals(2, stats.successfulSessions)
        assertEquals(TimeUnit.MINUTES.toMillis(50), stats.totalFocusTime) // 25 + 25 successful minutes
        assertEquals(0.67f, stats.successRate, 0.01f) // 2/3 ≈ 0.67
    }

    @Test
    fun `getAllFocusSessions should return flow from repository`() {
        val mockSessions = flowOf(emptyList<FocusSession>())
        every { mockRepository.getAllFocusSessions() } returns mockSessions

        val result = focusSessionManager.getAllFocusSessions()

        verify { mockRepository.getAllFocusSessions() }
        assertEquals(mockSessions, result)
    }

    @Test
    fun `getFocusSessionsForDate should return sessions from repository`() = runTest {
        val testDate = System.currentTimeMillis()
        val mockSessions = emptyList<FocusSession>()
        coEvery { mockRepository.getFocusSessionsForDate(testDate) } returns mockSessions

        val result = focusSessionManager.getFocusSessionsForDate(testDate)

        coVerify { mockRepository.getFocusSessionsForDate(testDate) }
        assertEquals(mockSessions, result)
    }

    @Test
    fun `isAppBlocked should return false when no session is active`() = runTest {
        val result = focusSessionManager.isAppBlocked("com.instagram.android")
        assertFalse(result)
    }

    @Test
    fun `isAppBlocked should return true when session is active`() = runTest {
        // Start a session to make it active
        coEvery { mockRepository.insertFocusSession(any()) } returns 1L
        every { mockRepository.getAllFocusSessions() } returns flowOf(emptyList())
        
        focusSessionManager.startFocusSession(25)

        val result = focusSessionManager.isAppBlocked("com.instagram.android")
        
        // Based on current simplified implementation, it returns true for any app during active session
        assertTrue(result)
    }

    @Test
    fun `completeFocusSession should return false when no session is active`() = runTest {
        val result = focusSessionManager.completeFocusSession(wasSuccessful = true)
        assertFalse(result)
        
        // Verify no repository calls were made
        coVerify(exactly = 0) { mockRepository.completeFocusSession(any(), any(), any(), any(), any()) }
    }
}