package com.example.screentimetracker.domain.usecases

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class) // Needed for Application context
@Config(sdk = [Config.OLDEST_SDK])
class GetInstalledAppsUseCaseTest {

    private lateinit var mockApplication: Application
    private lateinit var mockPackageManager: PackageManager
    private lateinit var getInstalledAppsUseCase: GetInstalledAppsUseCase

    @Before
    fun setup() {
        mockApplication = mock(Application::class.java)
        mockPackageManager = mock(PackageManager::class.java)
        `when`(mockApplication.packageManager).thenReturn(mockPackageManager)
        getInstalledAppsUseCase = GetInstalledAppsUseCase(mockApplication)
    }

    @Test
    fun invoke_returnsFilteredAndSortedApps() = runBlocking {
        // Mock ApplicationInfo objects
        val appInfo1 = ApplicationInfo().apply { packageName = "com.app1"; flags = 0; name = "App B" } // User app
        val appInfo2 = ApplicationInfo().apply { packageName = "com.app2"; flags = ApplicationInfo.FLAG_SYSTEM; name = "System App" } // System app
        val appInfo3 = ApplicationInfo().apply { packageName = "com.app3"; flags = 0; name = "App A" } // User app

        val installedAppsList = listOf(appInfo1, appInfo2, appInfo3)
        `when`(mockPackageManager.getInstalledApplications(PackageManager.GET_META_DATA)).thenReturn(installedAppsList)

        // Mock getApplicationLabel
        // Robolectric's ShadowPackageManager can also be used for more complex scenarios,
        // but direct mocking is fine for this structure.
        `when`(mockPackageManager.getApplicationLabel(appInfo1)).thenReturn("App B")
        `when`(mockPackageManager.getApplicationLabel(appInfo3)).thenReturn("App A")
        // No need to mock for appInfo2 as it should be filtered out.

        val result = getInstalledAppsUseCase()

        assertEquals(2, result.size) // System app should be filtered out
        assertEquals("App A", result[0].appName) // Check sorting
        assertEquals("com.app3", result[0].packageName)
        assertEquals("App B", result[1].appName)
        assertEquals("com.app1", result[1].packageName)
    }
}
