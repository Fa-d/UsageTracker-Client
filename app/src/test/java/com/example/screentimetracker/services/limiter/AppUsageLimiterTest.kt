package com.example.screentimetracker.services.limiter

import android.content.Context
import android.content.pm.PackageManager
import com.example.screentimetracker.domain.model.LimitedApp
import com.example.screentimetracker.domain.repository.TrackerRepository
import com.example.screentimetracker.utils.logger.AppLogger
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.anyOrNull // Import anyOrNull
import com.example.screentimetracker.data.local.LimitedApp as DataLimitedApp
import com.example.screentimetracker.data.local.toEntity // Import toEntity
import android.app.Notification // Import Notification
import com.example.screentimetracker.utils.ui.AppNotificationManager
import com.example.screentimetracker.utils.ui.AppToastManager
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

class AppUsageLimiterTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockRepository: TrackerRepository

    @Mock
    private lateinit var mockAppLogger: AppLogger

    @Mock
    private lateinit var mockPackageManager: PackageManager

    @Mock
    private lateinit var mockAppNotificationManager: AppNotificationManager // Mock the new interface

    @Mock
    private lateinit var mockAppToastManager: AppToastManager // Mock the new interface

    private lateinit var appUsageLimiter: AppUsageLimiter

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        whenever(mockContext.packageManager).thenReturn(mockPackageManager)

        appUsageLimiter = AppUsageLimiter(mockContext, mockRepository, mockAppLogger, mockAppNotificationManager, mockAppToastManager)
    }

    @Test
    fun `loadLimitedAppSettings should load settings from repository and map to domain`() = runTest {
        // Given
        val dataLimitedApps = listOf(
            DataLimitedApp("com.app1", 60000L),
            DataLimitedApp("com.app2", 120000L)
        )
        whenever(mockRepository.getAllLimitedAppsOnce()).thenReturn(dataLimitedApps)

        // When
        appUsageLimiter.loadLimitedAppSettings()

        // Then
        verify(mockRepository).getAllLimitedAppsOnce()
        verify(mockAppLogger).d(anyString(), contains("Loaded limited app settings"))
        // Internal state is private, so we can't directly assert on limitedAppSettings
        // We rely on subsequent calls to checkUsageLimits to verify correct loading
    }

    @Test
    fun `onNewSession should set currentLimitedAppDetails if app is limited`() = runTest {
        // Given
        val packageName = "com.app1"
        val startTime = System.currentTimeMillis()
        val limitedApp = LimitedApp(packageName, 60000L)
        val dataLimitedApps = listOf(limitedApp.toEntity())
        whenever(mockRepository.getAllLimitedAppsOnce()).thenReturn(dataLimitedApps)
        appUsageLimiter.loadLimitedAppSettings() // Load settings first

        // When
        appUsageLimiter.onNewSession(packageName, startTime)

        // Then
        verify(mockAppLogger).i(anyString(), contains("Continuous tracking started"))
        // Cannot directly assert on currentLimitedAppDetails due to private access
    }

    @Test
    fun `onNewSession should clear currentLimitedAppDetails if app is not limited`() = runTest {
        // Given
        val packageName = "com.app3" // Not a limited app
        val startTime = System.currentTimeMillis()
        val dataLimitedApps = listOf(
            DataLimitedApp("com.app1", 60000L)
        )
        whenever(mockRepository.getAllLimitedAppsOnce()).thenReturn(dataLimitedApps)
        appUsageLimiter.loadLimitedAppSettings()

        // When
        appUsageLimiter.onNewSession(packageName, startTime)

        // Then
        verify(mockAppLogger, never()).i(anyString(), contains("Continuous tracking started"))
        // Cannot directly assert on currentLimitedAppDetails due to private access
    }

    @Test
    fun `onSessionFinalized should clear current limited app details`() = runTest {
        // Given - set up a current limited app session
        val packageName = "com.app1"
        val startTime = System.currentTimeMillis()
        val limitedApp = LimitedApp(packageName, 60000L)
        val dataLimitedApps = listOf(limitedApp.toEntity())
        whenever(mockRepository.getAllLimitedAppsOnce()).thenReturn(dataLimitedApps)
        appUsageLimiter.loadLimitedAppSettings()
        appUsageLimiter.onNewSession(packageName, startTime)

        // When
        appUsageLimiter.onSessionFinalized()

        // Then
        verify(mockAppLogger).i(anyString(), contains("Continuous tracking stopped"))
        // Cannot directly assert on currentLimitedAppDetails due to private access
    }

    @Test
    fun `checkUsageLimits should show warning notification if 1x limit exceeded`() = runTest {
        // Given
        val packageName = "com.app1"
        val limitMillis = 60000L // 1 minute
        val startTime = System.currentTimeMillis() - limitMillis - 1000 // Exceed limit by 1 second
        val limitedApp = LimitedApp(packageName, limitMillis)
        val dataLimitedApps = listOf(limitedApp.toEntity())
        whenever(mockRepository.getAllLimitedAppsOnce()).thenReturn(dataLimitedApps)
        appUsageLimiter.loadLimitedAppSettings()
        appUsageLimiter.onNewSession(packageName, startTime)

        // Mock getApplicationLabel to return a dummy app name
        whenever(mockPackageManager.getApplicationLabel(any())).thenReturn("Test App")

        // When
        appUsageLimiter.checkUsageLimits(packageName, System.currentTimeMillis())

        // Then
        verify(mockAppLogger).i(anyString(), contains("Usage limit warning notification shown"))
        verify(mockAppNotificationManager).showWarningNotification(any(LimitedApp::class.java), anyLong())
    }

    @Test
    fun `checkUsageLimits should execute dissuasion action if 3x limit exceeded`() = runTest {
        // Given
        val packageName = "com.app1"
        val limitMillis = 60000L // 1 minute
        val startTime = System.currentTimeMillis() - (limitMillis * 3) - 1000 // Exceed 3x limit
        val limitedApp = LimitedApp(packageName, limitMillis)
        val dataLimitedApps = listOf(limitedApp.toEntity())
        whenever(mockRepository.getAllLimitedAppsOnce()).thenReturn(dataLimitedApps)
        appUsageLimiter.loadLimitedAppSettings()
        appUsageLimiter.onNewSession(packageName, startTime)

        // Mock getApplicationLabel to return a dummy app name
        whenever(mockPackageManager.getApplicationLabel(any())).thenReturn("Test App")

        // When
        appUsageLimiter.checkUsageLimits(packageName, System.currentTimeMillis())

        // Then
        verify(mockAppLogger).i(anyString(), contains("Executing dissuasion action"))
        verify(mockAppToastManager).bringAppToForeground(eq(packageName)) // Verify new interface call
        verify(mockAppToastManager).showDissuasionToast(eq("Test App")) // Verify new interface call
    }

    @Test
    fun `checkUsageLimits should not show warning if limit not exceeded`() = runTest {
        // Given
        val packageName = "com.app1"
        val limitMillis = 60000L // 1 minute
        val startTime = System.currentTimeMillis() - (limitMillis / 2) // Half limit
        val limitedApp = LimitedApp(packageName, limitMillis)
        val dataLimitedApps = listOf(limitedApp.toEntity())
        whenever(mockRepository.getAllLimitedAppsOnce()).thenReturn(dataLimitedApps)
        appUsageLimiter.loadLimitedAppSettings()
        appUsageLimiter.onNewSession(packageName, startTime)

        // When
        appUsageLimiter.checkUsageLimits(packageName, System.currentTimeMillis())

        // Then
        verify(mockAppLogger, never()).i(anyString(), contains("Usage limit warning notification shown"))
        verify(mockAppNotificationManager, never()).showWarningNotification(any(LimitedApp::class.java), anyLong()) // Verify no notification
    }

    @Test
    fun `checkUsageLimits should not execute dissuasion if 3x limit not exceeded`() = runTest {
        // Given
        val packageName = "com.app1"
        val limitMillis = 60000L // 1 minute
        val startTime = System.currentTimeMillis() - (limitMillis * 2) // Between 1x and 3x
        val limitedApp = LimitedApp(packageName, limitMillis)
        val dataLimitedApps = listOf(limitedApp.toEntity())
        whenever(mockRepository.getAllLimitedAppsOnce()).thenReturn(dataLimitedApps)
        appUsageLimiter.loadLimitedAppSettings()
        appUsageLimiter.onNewSession(packageName, startTime)

        // When
        appUsageLimiter.checkUsageLimits(packageName, System.currentTimeMillis())

        // Then
        verify(mockAppLogger, never()).i(anyString(), contains("Executing dissuasion action"))
        verify(mockAppToastManager, never()).bringAppToForeground(any()) // Verify no startActivity
        verify(mockAppToastManager, never()).showDissuasionToast(any()) // Verify no toast
    }

    @Test
    fun `checkUsageLimits should not re-show warning if already shown for current session`() = runTest {
        // Given
        val packageName = "com.app1"
        val limitMillis = 60000L // 1 minute
        val startTime = System.currentTimeMillis() - limitMillis - 1000 // Exceed limit
        val limitedApp = LimitedApp(packageName, limitMillis)
        val dataLimitedApps = listOf(limitedApp.toEntity())
        whenever(mockRepository.getAllLimitedAppsOnce()).thenReturn(dataLimitedApps)
        appUsageLimiter.loadLimitedAppSettings()
        appUsageLimiter.onNewSession(packageName, startTime)

        whenever(mockPackageManager.getApplicationLabel(any())).thenReturn("Test App")

        // First check - warning should be shown
        appUsageLimiter.checkUsageLimits(packageName, System.currentTimeMillis())
        verify(mockAppLogger, times(1)).i(anyString(), contains("Usage limit warning notification shown"))
        verify(mockAppNotificationManager, times(1)).showWarningNotification(
            any(LimitedApp::class.java),
            anyLong()
        )

        // When - check again without new session
        appUsageLimiter.checkUsageLimits(packageName, System.currentTimeMillis() + 1000)

        // Then - warning should not be re-shown
        verify(mockAppLogger, times(1)).i(anyString(), contains("Usage limit warning notification shown"))
        verify(mockAppNotificationManager, times(1)).showWarningNotification(
            any(LimitedApp::class.java),
            anyLong()
        )
    }

    @Test
    fun `checkUsageLimits should not re-execute dissuasion if already taken for current session`() = runTest {
        // Given
        val packageName = "com.app1"
        val limitMillis = 60000L // 1 minute
        val startTime = System.currentTimeMillis() - (limitMillis * 3) - 1000 // Exceed 3x limit
        val limitedApp = LimitedApp(packageName, limitMillis)
        val dataLimitedApps = listOf(limitedApp.toEntity())
        whenever(mockRepository.getAllLimitedAppsOnce()).thenReturn(dataLimitedApps)
        appUsageLimiter.loadLimitedAppSettings()
        appUsageLimiter.onNewSession(packageName, startTime)

        whenever(mockPackageManager.getApplicationLabel(any())).thenReturn("Test App")

        // First check - dissuasion should be executed
        appUsageLimiter.checkUsageLimits(packageName, System.currentTimeMillis())
        verify(mockAppLogger, times(1)).i(anyString(), contains("Executing dissuasion action"))
        verify(mockAppToastManager, times(1)).bringAppToForeground(eq(packageName))
        verify(mockAppToastManager, times(1)).showDissuasionToast(eq("Test App"))

        // When - check again without new session
        appUsageLimiter.checkUsageLimits(packageName, System.currentTimeMillis() + 1000)

        // Then - dissuasion should not be re-executed
        verify(mockAppLogger, times(1)).i(anyString(), contains("Executing dissuasion action"))
        verify(mockAppToastManager, times(1)).bringAppToForeground(eq(packageName))
        verify(mockAppToastManager, times(1)).showDissuasionToast(eq("Test App"))
    }

    // Removed direct getAppName tests as it's a private method and its behavior is covered by public methods.
}