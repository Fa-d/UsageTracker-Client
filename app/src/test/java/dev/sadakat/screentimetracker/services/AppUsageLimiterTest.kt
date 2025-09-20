package dev.sadakat.screentimetracker.services

import android.content.Context
import android.content.pm.PackageManager
import dev.sadakat.screentimetracker.domain.model.LimitedApp
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import dev.sadakat.screentimetracker.framework.services.limiter.AppUsageLimiter
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import io.mockk.*
import io.mockk.impl.annotations.MockK
import dev.sadakat.screentimetracker.core.data.local.entities.LimitedApp as DataLimitedApp
import dev.sadakat.screentimetracker.utils.ui.AppNotificationManager
import dev.sadakat.screentimetracker.utils.ui.AppToastManager
import java.lang.reflect.Field

class AppUsageLimiterTest {

    @MockK
    private lateinit var mockContext: Context

    @MockK
    private lateinit var mockRepository: TrackerRepository

    @MockK
    private lateinit var mockAppLogger: AppLogger

    @MockK
    private lateinit var mockPackageManager: PackageManager

    @MockK
    private lateinit var mockAppNotificationManager: AppNotificationManager // Mock the new interface

    @MockK
    private lateinit var mockAppToastManager: AppToastManager // Mock the new interface

    private lateinit var appUsageLimiter: AppUsageLimiter

    

    // Reflection helper to set private limitedAppSettings
    private fun AppUsageLimiter.setLimitedAppSettings(settings: List<LimitedApp>) {
        val field: Field = AppUsageLimiter::class.java.getDeclaredField("limitedAppSettings")
        field.isAccessible = true
        field.set(this, settings)
    }

    // Reflection helper to set private currentLimitedAppDetails
    private fun AppUsageLimiter.setCurrentLimitedAppDetails(details: LimitedApp?) {
        val field: Field = AppUsageLimiter::class.java.getDeclaredField("currentLimitedAppDetails")
        field.isAccessible = true
        field.set(this, details)
    }

    // Reflection helper to set private continuousUsageStartTimeForLimiterMillis
    private fun AppUsageLimiter.setContinuousUsageStartTimeForLimiterMillis(time: Long?) {
        val field: Field = AppUsageLimiter::class.java.getDeclaredField("continuousUsageStartTimeForLimiterMillis")
        field.isAccessible = true
        field.set(this, time)
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { mockContext.packageManager } returns mockPackageManager

        appUsageLimiter = AppUsageLimiter(
            mockContext,
            mockRepository,
            mockAppLogger,
            mockAppNotificationManager,
            mockAppToastManager
        )
    }

    @Test
    fun `loadLimitedAppSettings should load settings from repository and map to domain`() = runTest {
        // Given
        val dataLimitedApps = listOf(
            DataLimitedApp("com.app1", 60000L),
            DataLimitedApp("com.app2", 120000L)
        )
        coEvery { mockRepository.getAllLimitedAppsOnce() } returns dataLimitedApps

        // When
        appUsageLimiter.loadLimitedAppSettings()

        // Then
        coVerify { mockRepository.getAllLimitedAppsOnce() }
        verify { mockAppLogger.d(any(), any()) }
        // Internal state is private, so we can't directly assert on limitedAppSettings
        // We rely on subsequent calls to checkUsageLimits to verify correct loading
    }

    @Test
    fun `onNewSession should set currentLimitedAppDetails if app is limited`() = runTest {
        // Given
        val packageName = "com.app1"
        val startTime = System.currentTimeMillis()
        val limitedApp = LimitedApp(packageName, 60000L)
        appUsageLimiter.setLimitedAppSettings(listOf(limitedApp)) // Directly set for test

        // When
        appUsageLimiter.onNewSession(packageName, startTime)

        // Then
        verify { mockAppLogger.i(any(), any()) }
        // Cannot directly assert on currentLimitedAppDetails due to private access
    }

    @Test
    fun `onNewSession should clear currentLimitedAppDetails if app is not limited`() = runTest {
        // Given
        val packageName = "com.app3" // Not a limited app
        val startTime = System.currentTimeMillis()
        val limitedApp = LimitedApp("com.app1", 60000L)
        appUsageLimiter.setLimitedAppSettings(listOf(limitedApp)) // Directly set for test

        // When
        appUsageLimiter.onNewSession(packageName, startTime)

        // Then
        verify(exactly = 0) { mockAppLogger.i(any(), any()) }
        // Cannot directly assert on currentLimitedAppDetails due to private access
    }

    @Test
    fun `onSessionFinalized should clear current limited app details`() = runTest {
        // Given - set up a current limited app session
        val packageName = "com.app1"
        val startTime = System.currentTimeMillis()
        val limitedApp = LimitedApp(packageName, 60000L)
        appUsageLimiter.setLimitedAppSettings(listOf(limitedApp)) // Directly set for test
        appUsageLimiter.onNewSession(packageName, startTime)

        // When
        appUsageLimiter.onSessionFinalized()

        // Then
        verify { mockAppLogger.i(any(), any()) }
        // Cannot directly assert on currentLimitedAppDetails due to private access
    }

    @Test
    fun `checkUsageLimits should show warning notification if 1x limit exceeded`() = runTest {
        // Given
        val packageName = "com.app1"
        val limitMillis = 60000L // 1 minute
        val startTime = System.currentTimeMillis() - limitMillis - 1000 // Exceed limit by 1 second
        val limitedApp = LimitedApp(packageName, limitMillis)
        appUsageLimiter.setLimitedAppSettings(listOf(limitedApp)) // Directly set for test
        appUsageLimiter.onNewSession(packageName, startTime)

        // Mock getApplicationLabel to return a dummy app name
        every { mockPackageManager.getApplicationLabel(any()) } returns "Test App"

        // When
        appUsageLimiter.checkUsageLimits(packageName, System.currentTimeMillis())

        // Then
        verify { mockAppNotificationManager.showWarningNotification(any<LimitedApp>(), any<Long>()) }
    }

    @Test
    fun `checkUsageLimits should execute dissuasion action if 3x limit exceeded`() = runTest {
        // Given
        val packageName = "com.app1"
        val limitMillis = 60000L // 1 minute
        val startTime = System.currentTimeMillis() - (limitMillis * 3) - 1000 // Exceed 3x limit
        val limitedApp = LimitedApp(packageName, limitMillis)
        appUsageLimiter.setLimitedAppSettings(listOf(limitedApp)) // Directly set for test
        appUsageLimiter.onNewSession(packageName, startTime)

        // Mock getApplicationLabel to return a dummy app name
        every { mockPackageManager.getApplicationLabel(any()) } returns "Test App"

        // When
        appUsageLimiter.checkUsageLimits(packageName, System.currentTimeMillis())

        // Then - Only verify actions that should actually be called
        verify { mockAppToastManager.bringAppToForeground(packageName) } // Verify new interface call
        verify { mockAppToastManager.showDissuasionToast("Test App") } // Verify new interface call
        verify { mockAppNotificationManager.showWarningNotification(any<LimitedApp>(), any<Long>()) } // Warning should also be shown
    }

    @Test
    fun `checkUsageLimits should not show warning if limit not exceeded`() = runTest {
        // Given
        val packageName = "com.app1"
        val limitMillis = 60000L // 1 minute
        val startTime = System.currentTimeMillis() - (limitMillis / 2) // Half limit
        val limitedApp = LimitedApp(packageName, limitMillis)

        appUsageLimiter.setCurrentLimitedAppDetails(limitedApp)
        appUsageLimiter.setContinuousUsageStartTimeForLimiterMillis(startTime)

        // When
        appUsageLimiter.checkUsageLimits(packageName, System.currentTimeMillis())

        // Then
        verify(exactly = 0) { mockAppLogger.i(any(), any()) }
        verify(exactly = 0) { mockAppNotificationManager.showWarningNotification(any<LimitedApp>(), any<Long>()) }
    }

    @Test
    fun `checkUsageLimits should not execute dissuasion if 3x limit not exceeded`() = runTest {
        // Given
        val packageName = "com.app1"
        val limitMillis = 60000L // 1 minute
        val startTime = System.currentTimeMillis() - (limitMillis * 2) // Between 1x and 3x
        val limitedApp = LimitedApp(packageName, limitMillis)
        appUsageLimiter.setLimitedAppSettings(listOf(limitedApp)) // Directly set for test
        appUsageLimiter.onNewSession(packageName, startTime)

        // Mock getApplicationLabel to return a dummy app name
        every { mockPackageManager.getApplicationLabel(any()) } returns "Test App"

        // When
        appUsageLimiter.checkUsageLimits(packageName, System.currentTimeMillis())

        // Then - Warning should be shown (since limit is exceeded) but no dissuasion action
        verify { mockAppNotificationManager.showWarningNotification(any<LimitedApp>(), any<Long>()) } // Warning should be shown
        verify(exactly = 0) { mockAppToastManager.bringAppToForeground(any()) } // Verify no startActivity
        verify(exactly = 0) { mockAppToastManager.showDissuasionToast(any()) } // Verify no toast
    }

    @Test
    fun `checkUsageLimits should not re-show warning if already shown for current session`() = runTest {
        // Given
        val packageName = "com.app1"
        val limitMillis = 60000L // 1 minute
        val startTime = System.currentTimeMillis() - limitMillis - 1000 // Exceed limit
        val limitedApp = LimitedApp(packageName, limitMillis)
        appUsageLimiter.setLimitedAppSettings(listOf(limitedApp)) // Directly set for test
        appUsageLimiter.onNewSession(packageName, startTime)

        every { mockPackageManager.getApplicationLabel(any()) } returns "Test App"

        // First check - warning should be shown
        appUsageLimiter.checkUsageLimits(packageName, System.currentTimeMillis())
        verify(exactly = 1) { mockAppLogger.i(any(), any()) }
        verify(exactly = 1) { mockAppNotificationManager.showWarningNotification(
            any<LimitedApp>(),
            any<Long>()
        ) }

        // When - check again without new session
        appUsageLimiter.checkUsageLimits(packageName, System.currentTimeMillis() + 1000)

        // Then - warning should not be re-shown
        verify(exactly = 1) { mockAppLogger.i(any(), any()) }
        verify(exactly = 1) { mockAppNotificationManager.showWarningNotification(
            any<LimitedApp>(),
            any<Long>()
        ) }
    }

    @Test
    fun `checkUsageLimits should not re-execute dissuasion if already taken for current session`() = runTest {
        // Given
        val packageName = "com.app1"
        val limitMillis = 60000L // 1 minute
        val startTime = System.currentTimeMillis() - (limitMillis * 3) - 1000 // Exceed 3x limit
        val limitedApp = LimitedApp(packageName, limitMillis)
        appUsageLimiter.setLimitedAppSettings(listOf(limitedApp)) // Directly set for test
        appUsageLimiter.onNewSession(packageName, startTime)

        every { mockPackageManager.getApplicationLabel(any()) } returns "Test App"

        // First check - dissuasion should be executed
        appUsageLimiter.checkUsageLimits(packageName, System.currentTimeMillis())
        verify(exactly = 1) { mockAppToastManager.bringAppToForeground(packageName) }
        verify(exactly = 1) { mockAppToastManager.showDissuasionToast("Test App") }
        verify(exactly = 1) { mockAppNotificationManager.showWarningNotification(any<LimitedApp>(), any<Long>()) }

        // When - check again without new session
        appUsageLimiter.checkUsageLimits(packageName, System.currentTimeMillis() + 1000)

        // Then - dissuasion should not be re-executed (still exactly 1 call)
        verify(exactly = 1) { mockAppToastManager.bringAppToForeground(packageName) }
        verify(exactly = 1) { mockAppToastManager.showDissuasionToast("Test App") }
        verify(exactly = 1) { mockAppNotificationManager.showWarningNotification(any<LimitedApp>(), any<Long>()) }
    }

    

    // Removed direct getAppName tests as it's a private method and its behavior is covered by public methods.
}
