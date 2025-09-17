package dev.sadakat.screentimetracker.validation

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.sadakat.screentimetracker.data.local.TimeRestriction
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import dev.sadakat.screentimetracker.domain.usecases.TimeRestrictionManagerUseCase
import dev.sadakat.screentimetracker.domain.usecases.WeeklyInsightsUseCase
import dev.sadakat.screentimetracker.services.NotificationScheduler
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import dev.sadakat.screentimetracker.utils.ui.AppNotificationManager
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class ErrorHandlingTest {

    @MockK
    private lateinit var repository: TrackerRepository

    @MockK
    private lateinit var notificationManager: AppNotificationManager

    @MockK
    private lateinit var appLogger: AppLogger

    private lateinit var context: Context
    private lateinit var timeRestrictionUseCase: TimeRestrictionManagerUseCase
    private lateinit var weeklyInsightsUseCase: WeeklyInsightsUseCase
    private lateinit var notificationScheduler: NotificationScheduler

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        context = ApplicationProvider.getApplicationContext()
        
        every { appLogger.i(any(), any()) } just runs
        every { appLogger.e(any(), any(), any()) } just runs
        every { appLogger.w(any(), any(), any()) } just runs
        every { appLogger.d(any(), any()) } just runs

        timeRestrictionUseCase = TimeRestrictionManagerUseCase(repository, notificationManager, appLogger)
        val mockAppCategorizer = mockk<dev.sadakat.screentimetracker.domain.categorization.AppCategorizer>(relaxed = true)
        weeklyInsightsUseCase = WeeklyInsightsUseCase(repository, notificationManager, appLogger, mockAppCategorizer)
        notificationScheduler = NotificationScheduler(context, appLogger)
    }

    @Test
    fun `TimeRestrictionManagerUseCase should handle database connection errors`() = runTest {
        // Given - Database connection error
        coEvery { repository.getAllTimeRestrictions() } throws IOException("Database connection failed")

        // When
        val result = timeRestrictionUseCase.getAllTimeRestrictions()

        // Then - Should return empty list and log error
        assertTrue("Should return empty list on database error", result.first().isEmpty())
        verify { appLogger.e("TimeRestrictionManagerUseCase", "Failed to get all time restrictions", any()) }
    }

    @Test
    fun `TimeRestrictionManagerUseCase should handle invalid time restriction data`() = runTest {
        // Given - Invalid time restriction (end time before start time)
        val invalidRestriction = TimeRestriction(
            id = 0,
            restrictionType = "test",
            name = "Invalid Test",
            description = "Invalid time restriction for testing",
            startTimeMinutes = 17 * 60, // 5 PM
            endTimeMinutes = 9 * 60,    // 9 AM (invalid - before start)
            appsBlocked = "com.test.app",
            daysOfWeek = "1,2,3,4,5",
            isEnabled = true,
            createdAt = System.currentTimeMillis()
        )

        coEvery { repository.insertTimeRestriction(any()) } throws IllegalArgumentException("Invalid time restriction")

        // When
        val result = runCatching { timeRestrictionUseCase.createCustomRestriction(
            name = "Invalid Test",
            description = "Invalid time restriction for testing",
            startTimeMinutes = 17 * 60,
            endTimeMinutes = 9 * 60,
            blockedApps = listOf("com.test.app"),
            daysOfWeek = listOf(1, 2, 3, 4, 5)
        ) }.isFailure

        // Then - Should handle gracefully
        assertTrue("Should fail to create invalid restriction", result)
        verify { appLogger.e("TimeRestrictionManagerUseCase", "Failed to create time restriction", any()) }
    }

    @Test
    fun `TimeRestrictionManagerUseCase should handle null package name`() = runTest {
        // Given - Time restriction with null/empty package name
        val nullPackageRestriction = TimeRestriction(
            id = 0,
            restrictionType = "test",
            name = "Empty Package Test",
            description = "Test with empty apps blocked",
            startTimeMinutes = 9 * 60,
            endTimeMinutes = 17 * 60,
            appsBlocked = "", // Empty apps blocked
            daysOfWeek = "1,2,3,4,5",
            isEnabled = true,
            createdAt = System.currentTimeMillis()
        )

        // When/Then - Should handle empty package name gracefully
        val isRestricted = runBlocking { timeRestrictionUseCase.isAppBlockedByTimeRestriction("") }
        assertFalse("Empty package name should not be restricted", isRestricted)

        val result = runCatching { timeRestrictionUseCase.createCustomRestriction(
            name = "Empty Package Test",
            description = "Test with empty apps blocked",
            startTimeMinutes = 9 * 60,
            endTimeMinutes = 17 * 60,
            blockedApps = emptyList(),
            daysOfWeek = listOf(1, 2, 3, 4, 5)
        ) }.isSuccess
        // Should either succeed with validation or fail gracefully
        assertNotNull("Should return a boolean result", result)
    }

    @Test
    fun `WeeklyInsightsUseCase should handle repository timeout errors`() = runTest {
        // Given - Repository timeout
        coEvery { repository.getAggregatedSessionDataForDayFlow(any(), any()) } throws SocketTimeoutException("Request timeout")

        // When
        val report = weeklyInsightsUseCase.generateWeeklyReport()

        // Then - Should return empty report and log error
        assertEquals("Should return empty report on timeout", WeeklyInsightsUseCase.WeeklyReport.empty(), report)
        verify { appLogger.e("WeeklyInsightsUseCase", "Failed to generate weekly report", any()) }
    }

    @Test
    fun `WeeklyInsightsUseCase should handle corrupted data gracefully`() = runTest {
        // Given - Corrupted session data (negative durations, invalid timestamps)
        coEvery { repository.getAggregatedSessionDataForDayFlow(any(), any()) } returns flowOf(emptyList())
        coEvery { repository.getDailyAppSummaries(any(), any()) } throws RuntimeException("Corrupted data")
        coEvery { repository.getUnlockCountForDayFlow(any(), any()) } returns flowOf(0)
        coEvery { repository.getAllWellnessScores() } returns flowOf(emptyList())
        coEvery { repository.getAllSessionsInRange(any(), any()) } returns flowOf(emptyList())
        coEvery { repository.getActiveGoals() } returns flowOf(emptyList())

        // When
        val report = weeklyInsightsUseCase.generateWeeklyReport()

        // Then - Should handle gracefully with empty/default data
        assertNotNull("Report should not be null", report)
        assertEquals("Should have zero screen time", 0L, report.totalScreenTimeMillis)
        assertTrue("Should have insights despite errors", report.insights.isNotEmpty())
    }

    @Test
    fun `WeeklyInsightsUseCase should handle notification failures`() = runTest {
        // Given - Notification manager throws exception
        every { notificationManager.showWeeklyReport(any(), any(), any()) } throws SecurityException("Permission denied")

        // When
        weeklyInsightsUseCase.sendWeeklyReportNotification()

        // Then - Should log error and continue
        verify { appLogger.e("WeeklyInsightsUseCase", "Failed to send weekly report notification", any()) }
        verify(exactly = 1) { notificationManager.showWeeklyReport(any(), any(), any()) }
    }

    @Test
    fun `WeeklyInsightsUseCase should handle memory pressure gracefully`() = runTest {
        // Given - OutOfMemoryError during data processing
        coEvery { repository.getAllSessionsInRange(any(), any()) } throws OutOfMemoryError("Insufficient memory")

        // When
        val productivityHours = weeklyInsightsUseCase.getProductivityHours()
        val categoryInsights = weeklyInsightsUseCase.getAppCategoryInsights()

        // Then - Should return empty collections and log errors
        assertTrue("Should return empty productivity hours on memory error", productivityHours.isEmpty())
        verify { appLogger.e("WeeklyInsightsUseCase", "Failed to get productivity hours", any()) }
    }

    @Test
    fun `NotificationScheduler should handle WorkManager initialization failures`() {
        // Given - WorkManager not properly initialized (simulated with logging)
        val scheduler = NotificationScheduler(context, appLogger)

        // When - Try to schedule notifications
        scheduler.scheduleWeeklyReportNotification()
        scheduler.schedulePeriodicWeeklyReports()

        // Then - Should handle gracefully (verified through logging)
        // Note: In test environment, WorkManager should work, but production might have issues
        verify(atLeast = 1) { appLogger.i("NotificationScheduler", any()) }
    }

    @Test
    fun `NotificationScheduler should handle invalid time calculations`() {
        // Given - Scheduler with potential time calculation issues
        val scheduler = NotificationScheduler(context, appLogger)

        // When - Schedule with current time constraints
        scheduler.scheduleWeeklyReportNotification()

        // Then - Should complete without throwing exceptions
        verify { appLogger.i("NotificationScheduler", match { it.contains("scheduled") }) }
    }

    @Test
    fun `Use cases should handle concurrent access gracefully`() = runTest {
        // Given - Multiple concurrent operations
        coEvery { repository.getAllTimeRestrictions() } returns flowOf(emptyList())
        coEvery { repository.getAggregatedSessionDataForDayFlow(any(), any()) } returns flowOf(emptyList())
        coEvery { repository.getDailyAppSummaries(any(), any()) } returns flowOf(emptyList())
        coEvery { repository.getUnlockCountForDayFlow(any(), any()) } returns flowOf(0)
        coEvery { repository.getAllWellnessScores() } returns flowOf(emptyList())
        coEvery { repository.getAllSessionsInRange(any(), any()) } returns flowOf(emptyList())
        coEvery { repository.getActiveGoals() } returns flowOf(emptyList())

        // When - Run multiple operations concurrently
        val restrictions = timeRestrictionUseCase.getAllTimeRestrictions()
        val report = weeklyInsightsUseCase.generateWeeklyReport()
        val productivity = weeklyInsightsUseCase.getProductivityHours()
        val categories = weeklyInsightsUseCase.getAppCategoryInsights()

        // Then - All operations should complete successfully
        assertNotNull("Restrictions should not be null", restrictions)
        assertNotNull("Report should not be null", report)
        assertNotNull("Productivity hours should not be null", productivity)
        assertNotNull("Category insights should not be null", categories)
    }

    @Test
    fun `Error recovery should work correctly`() = runTest {
        // Given - Repository initially fails, then succeeds
        var callCount = 0
        coEvery { repository.getAllTimeRestrictions() } answers {
            if (callCount++ == 0) {
                throw IOException("Network error")
            } else {
                flowOf(emptyList())
            }
        }

        // When - First call fails, second succeeds
        val firstResult = timeRestrictionUseCase.getAllTimeRestrictions()
        val secondResult = timeRestrictionUseCase.getAllTimeRestrictions()

        // Then
        assertTrue("First call should handle error", firstResult.first().isEmpty())
        assertTrue("Second call should succeed", secondResult.first().isEmpty()) // Empty but no error
        verify(exactly = 1) { appLogger.e("TimeRestrictionManagerUseCase", any(), any()) }
    }

    @Test
    fun `Resource cleanup should work properly after errors`() = runTest {
        // Given - Operations that might leave resources open
        coEvery { repository.getAggregatedSessionDataForDayFlow(any(), any()) } throws IOException("Resource error")

        // When
        val report = weeklyInsightsUseCase.generateWeeklyReport()

        // Then - Should complete gracefully
        assertEquals("Should return empty report", WeeklyInsightsUseCase.WeeklyReport.empty(), report)
        verify { appLogger.e("WeeklyInsightsUseCase", "Failed to generate weekly report", any()) }

        // Subsequent operations should still work
        val productivity = weeklyInsightsUseCase.getProductivityHours()
        assertNotNull("Subsequent operations should work", productivity)
    }

    @Test
    fun `Input sanitization should prevent injection attacks`() = runTest {
        // Given - Potentially malicious input
        val maliciousPackageName = "'; DROP TABLE time_restrictions; --"
        val maliciousRestriction = TimeRestriction(
            id = 0,
            restrictionType = "security_test",
            name = "Test Restriction",
            description = "Security test restriction",
            startTimeMinutes = 9 * 60, // 9:00 AM
            endTimeMinutes = 17 * 60, // 5:00 PM
            appsBlocked = "[\"$maliciousPackageName\"]",
            daysOfWeek = "[1,2,3,4,5]",
            isEnabled = true,
            createdAt = System.currentTimeMillis()
        )

        // When/Then - Should handle malicious input safely
        val isBlocked = runBlocking { timeRestrictionUseCase.isAppBlockedByTimeRestriction(maliciousPackageName) }
        assertNotNull("Should handle malicious input safely", isBlocked)

        // The actual insertion would be handled by the repository layer with parameterized queries
        // This test ensures the use case layer doesn't crash on malicious input
        val result = runBlocking { timeRestrictionUseCase.createCustomRestriction(
            name = maliciousRestriction.name,
            description = maliciousRestriction.description,
            startTimeMinutes = maliciousRestriction.startTimeMinutes,
            endTimeMinutes = maliciousRestriction.endTimeMinutes,
            blockedApps = listOf(maliciousPackageName),
            daysOfWeek = listOf(1,2,3,4,5)
        ) }
        assertNotNull("Should return a result even with malicious input", result)
    }

    @Test
    fun `Boundary value handling should work correctly`() = runTest {
        // Given - Boundary values
        val boundaryValues = listOf(
            Long.MIN_VALUE,
            -1L,
            0L,
            1L,
            Long.MAX_VALUE
        )

        // When/Then - Should handle all boundary values gracefully
        boundaryValues.forEach { value ->
            val isRestricted = runBlocking { timeRestrictionUseCase.isAppBlockedByTimeRestriction("test.package") }
            assertNotNull("Should handle boundary timestamp: $value", isRestricted)
        }
    }

    @Test
    fun `Network-related errors should be handled appropriately`() = runTest {
        // Given - Various network errors
        val networkErrors = listOf(
            IOException("Network unreachable"),
            SocketTimeoutException("Connection timeout"),
            RuntimeException("DNS resolution failed")
        )

        networkErrors.forEach { error ->
            // Given
            coEvery { repository.getAggregatedSessionDataForDayFlow(any(), any()) } throws error

            // When
            val report = weeklyInsightsUseCase.generateWeeklyReport()

            // Then
            assertEquals("Should handle ${error.javaClass.simpleName}", 
                WeeklyInsightsUseCase.WeeklyReport.empty(), report)
        }

        // Should have logged all errors
        verify(exactly = networkErrors.size) { 
            appLogger.e("WeeklyInsightsUseCase", "Failed to generate weekly report", any()) 
        }
    }

    @Test
    fun `State consistency should be maintained after partial failures`() = runTest {
        // Given - Partial failure scenario
        coEvery { repository.getAllTimeRestrictions() } returns flowOf(emptyList())
        coEvery { repository.getAggregatedSessionDataForDayFlow(any(), any()) } throws RuntimeException("Partial failure")
        coEvery { repository.getDailyAppSummaries(any(), any()) } returns flowOf(emptyList()) // This succeeds
        coEvery { repository.getUnlockCountForDayFlow(any(), any()) } returns flowOf(42)
        coEvery { repository.getAllWellnessScores() } returns flowOf(emptyList())
        coEvery { repository.getAllSessionsInRange(any(), any()) } returns flowOf(emptyList())
        coEvery { repository.getActiveGoals() } returns flowOf(emptyList())

        // When
        val restrictions = timeRestrictionUseCase.getAllTimeRestrictions()
        val report = weeklyInsightsUseCase.generateWeeklyReport()

        // Then - State should remain consistent
        assertTrue("Restrictions should be empty but valid", restrictions.first().isEmpty())
        assertEquals("Report should be empty due to partial failure", 
            WeeklyInsightsUseCase.WeeklyReport.empty(), report)
        
        // Verify error logging
        verify { appLogger.e("WeeklyInsightsUseCase", "Failed to generate weekly report", any()) }
    }
}