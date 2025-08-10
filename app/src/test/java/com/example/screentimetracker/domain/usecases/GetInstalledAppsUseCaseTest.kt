package com.example.screentimetracker.domain.usecases

import android.app.Application
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import io.mockk.*

class GetInstalledAppsUseCaseTest {

    private val mockApplication = mockk<Application>()
    private val mockPackageManager = mockk<PackageManager>()

    private lateinit var getInstalledAppsUseCase: GetInstalledAppsUseCase

    @Before
    fun setup() {
        every { mockApplication.packageManager } returns mockPackageManager
        
        // Mock static Intent creation
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().addCategory(any()) } returns mockk<Intent>()
        
        getInstalledAppsUseCase = GetInstalledAppsUseCase(mockApplication)
    }
    
    @After
    fun cleanup() {
        unmockkConstructor(Intent::class)
    }

    @Test
    fun `invoke should return sorted list of installed apps`() = runTest {
        // Given
        val mockResolveInfo1 = createMockResolveInfo("com.app.beta", "Beta App")
        val mockResolveInfo2 = createMockResolveInfo("com.app.alpha", "Alpha App")
        val mockResolveInfo3 = createMockResolveInfo("com.app.gamma", "Gamma App")
        
        val mockResolveInfoList = listOf(mockResolveInfo1, mockResolveInfo2, mockResolveInfo3)
        
        every { mockPackageManager.queryIntentActivities(any<Intent>(), any<Int>()) } returns mockResolveInfoList

        // When
        val result = getInstalledAppsUseCase()

        // Then
        assert(result.size == 3)
        // Should be sorted alphabetically
        assert(result[0].appName == "Alpha App")
        assert(result[0].packageName == "com.app.alpha")
        assert(result[1].appName == "Beta App")
        assert(result[1].packageName == "com.app.beta")
        assert(result[2].appName == "Gamma App")
        assert(result[2].packageName == "com.app.gamma")
    }

    @Test
    fun `invoke should handle empty app list`() = runTest {
        // Given
        every { mockPackageManager.queryIntentActivities(any<Intent>(), any<Int>()) } returns emptyList()

        // When
        val result = getInstalledAppsUseCase()

        // Then
        assert(result.isEmpty())
    }

    @Test
    fun `invoke should remove duplicate packages`() = runTest {
        // Given
        val mockResolveInfo1 = createMockResolveInfo("com.app.duplicate", "Duplicate App")
        val mockResolveInfo2 = createMockResolveInfo("com.app.duplicate", "Duplicate App")
        val mockResolveInfo3 = createMockResolveInfo("com.app.unique", "Unique App")
        
        val mockResolveInfoList = listOf(mockResolveInfo1, mockResolveInfo2, mockResolveInfo3)
        
        every { mockPackageManager.queryIntentActivities(any<Intent>(), any<Int>()) } returns mockResolveInfoList

        // When
        val result = getInstalledAppsUseCase()

        // Then
        assert(result.size == 2) // Duplicates should be removed
        assert(result.any { it.packageName == "com.app.duplicate" })
        assert(result.any { it.packageName == "com.app.unique" })
    }

    @Test
    fun `invoke should query package manager correctly`() = runTest {
        // Given
        every { mockPackageManager.queryIntentActivities(any<Intent>(), any<Int>()) } returns emptyList()

        // When
        getInstalledAppsUseCase()

        // Then
        verify { mockPackageManager.queryIntentActivities(any<Intent>(), any<Int>()) }
    }

    private fun createMockResolveInfo(packageName: String, appName: String): ResolveInfo {
        val mockResolveInfo = mockk<ResolveInfo>()
        val mockActivityInfo = mockk<ActivityInfo>()
        val mockApplicationInfo = mockk<ApplicationInfo>()
        
        mockResolveInfo.activityInfo = mockActivityInfo
        mockActivityInfo.packageName = packageName
        mockActivityInfo.applicationInfo = mockApplicationInfo
        
        every { mockPackageManager.getApplicationLabel(mockApplicationInfo) } returns appName
        
        return mockResolveInfo
    }
}