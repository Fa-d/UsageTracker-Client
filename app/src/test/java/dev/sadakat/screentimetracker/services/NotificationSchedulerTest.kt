package dev.sadakat.screentimetracker.services

import android.content.Context
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

class NotificationSchedulerTest {

    private val appLogger = mockk<AppLogger>()
    private val context = mockk<Context>(relaxed = true)
    private lateinit var notificationScheduler: NotificationScheduler

    @Before
    fun setup() {
        every { appLogger.i(any(), any()) } returns Unit
        every { appLogger.e(any(), any(), any()) } returns Unit

        notificationScheduler = NotificationScheduler(context, appLogger)
    }

    @Test
    fun `scheduleWeeklyReportNotification should handle WorkManager unavailability`() {
        // When
        notificationScheduler.scheduleWeeklyReportNotification()

        // Then - Should log error when WorkManager is not available
        verify { appLogger.e("NotificationScheduler", "Failed to schedule weekly report notification", any()) }
    }

    @Test
    fun `schedulePeriodicWeeklyReports should handle WorkManager unavailability`() {
        // When
        notificationScheduler.schedulePeriodicWeeklyReports()

        // Then - Should log error when WorkManager is not available
        verify { appLogger.e("NotificationScheduler", "Failed to schedule periodic weekly report notifications", any()) }
    }

    @Test
    fun `cancelWeeklyReportNotifications should handle WorkManager unavailability`() {
        // When
        notificationScheduler.cancelWeeklyReportNotifications()

        // Then - Should log error when WorkManager is not available
        verify { appLogger.e("NotificationScheduler", "Failed to cancel weekly report notifications", any()) }
    }

    @Test
    fun `getNextSundayAt9AM should return correct timestamp`() {
        val scheduler = NotificationScheduler(context, appLogger)
        
        // Using reflection to access private method for testing
        val method = scheduler::class.java.getDeclaredMethod("getNextSundayAt9AM")
        method.isAccessible = true
        val nextSunday = method.invoke(scheduler) as Long

        // Verify it's a future timestamp
        assertTrue("Next Sunday should be in the future", nextSunday > System.currentTimeMillis())
        
        // Verify it's on a Sunday at 9 AM
        val calendar = Calendar.getInstance().apply { timeInMillis = nextSunday }
        assertEquals("Should be Sunday", Calendar.SUNDAY, calendar.get(Calendar.DAY_OF_WEEK))
        assertEquals("Should be 9 AM", 9, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals("Should be 0 minutes", 0, calendar.get(Calendar.MINUTE))
    }

    @Test
    fun `calculateInitialDelay should return positive delay`() {
        val scheduler = NotificationScheduler(context, appLogger)
        
        // Using reflection to access private method for testing
        val method = scheduler::class.java.getDeclaredMethod("calculateInitialDelay")
        method.isAccessible = true
        val delay = method.invoke(scheduler) as Long

        // Verify delay is positive (future time)
        assertTrue("Initial delay should be positive", delay > 0)
    }

    @Test
    fun `schedule operations should handle exceptions gracefully`() {
        // Given - WorkManager is not available in test environment
        val mockAppLogger = mockk<AppLogger>(relaxed = true)
        val scheduler = NotificationScheduler(context, mockAppLogger)

        // When - operations fail due to WorkManager unavailability
        scheduler.scheduleWeeklyReportNotification()
        scheduler.schedulePeriodicWeeklyReports()

        // Then - should log error messages for both operations
        verify { mockAppLogger.e("NotificationScheduler", "Failed to schedule weekly report notification", any()) }
        verify { mockAppLogger.e("NotificationScheduler", "Failed to schedule periodic weekly report notifications", any()) }
    }

    @Test
    fun `multiple schedule calls should handle WorkManager unavailability consistently`() {
        // When - schedule multiple times (each will fail due to WorkManager unavailability)
        notificationScheduler.scheduleWeeklyReportNotification()
        notificationScheduler.scheduleWeeklyReportNotification()
        notificationScheduler.schedulePeriodicWeeklyReports()
        notificationScheduler.schedulePeriodicWeeklyReports()

        // Then - should log error messages for each failed operation
        verify(exactly = 2) { appLogger.e("NotificationScheduler", "Failed to schedule weekly report notification", any()) }
        verify(exactly = 2) { appLogger.e("NotificationScheduler", "Failed to schedule periodic weekly report notifications", any()) }
    }
}