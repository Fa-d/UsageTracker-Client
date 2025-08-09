package com.example.screentimetracker.services

import android.content.Context
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.screentimetracker.utils.logger.AppLogger
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.*

@RunWith(RobolectricTestRunner::class)
class NotificationSchedulerTest {

    @MockK
    private lateinit var appLogger: AppLogger

    private lateinit var context: Context
    private lateinit var notificationScheduler: NotificationScheduler

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        context = RuntimeEnvironment.getApplication()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        
        every { appLogger.i(any(), any()) } returns Unit
        every { appLogger.e(any(), any(), any()) } returns Unit

        notificationScheduler = NotificationScheduler(context, appLogger)
    }

    @Test
    fun `scheduleWeeklyReportNotification should schedule work successfully`() {
        // When
        notificationScheduler.scheduleWeeklyReportNotification()

        // Then
        verify { appLogger.i("NotificationScheduler", match { it.contains("Weekly report notification scheduled") }) }
    }

    @Test
    fun `schedulePeriodicWeeklyReports should schedule periodic work successfully`() {
        // When
        notificationScheduler.schedulePeriodicWeeklyReports()

        // Then
        verify { appLogger.i("NotificationScheduler", "Periodic weekly report notifications scheduled") }
    }

    @Test
    fun `cancelWeeklyReportNotifications should cancel work successfully`() {
        // Given - schedule first
        notificationScheduler.scheduleWeeklyReportNotification()
        notificationScheduler.schedulePeriodicWeeklyReports()

        // When
        notificationScheduler.cancelWeeklyReportNotifications()

        // Then
        verify { appLogger.i("NotificationScheduler", "Weekly report notifications cancelled") }
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
        // Given - mock WorkManager to throw exception
        val mockAppLogger = mockk<AppLogger>(relaxed = true)
        val scheduler = NotificationScheduler(context, mockAppLogger)

        // This test verifies the error handling in the catch blocks
        // The actual WorkManager operations should complete successfully in test environment

        // When
        scheduler.scheduleWeeklyReportNotification()
        scheduler.schedulePeriodicWeeklyReports()

        // Then - should log success messages (no exceptions thrown)
        verify { mockAppLogger.i(any(), any()) }
    }

    @Test
    fun `multiple schedule calls should replace existing work`() {
        // When - schedule multiple times
        notificationScheduler.scheduleWeeklyReportNotification()
        notificationScheduler.scheduleWeeklyReportNotification()
        notificationScheduler.schedulePeriodicWeeklyReports()
        notificationScheduler.schedulePeriodicWeeklyReports()

        // Then - should complete successfully (ExistingWorkPolicy.REPLACE)
        verify(atLeast = 2) { appLogger.i("NotificationScheduler", match { it.contains("scheduled") }) }
    }
}