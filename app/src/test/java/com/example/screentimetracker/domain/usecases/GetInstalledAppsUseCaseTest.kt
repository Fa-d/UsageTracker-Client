package com.example.screentimetracker.domain.usecases

import android.app.Application
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GetInstalledAppsUseCaseTest {

    @Mock
    private lateinit var mockApplication: Application

    @Mock
    private lateinit var mockPackageManager: PackageManager

    private lateinit var getInstalledAppsUseCase: GetInstalledAppsUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        whenever(mockApplication.packageManager).thenReturn(mockPackageManager)
        getInstalledAppsUseCase = GetInstalledAppsUseCase(mockApplication)
    }

    @Test
    fun `invoke should return sorted list of installed apps`() = runTest {
        // Given
        val mockResolveInfo1 = createMockResolveInfo("com.app.beta", "Beta App")
        val mockResolveInfo2 = createMockResolveInfo("com.app.alpha", "Alpha App")
        val mockResolveInfo3 = createMockResolveInfo("com.app.gamma", "Gamma App")
        
        val mockResolveInfoList = listOf(mockResolveInfo1, mockResolveInfo2, mockResolveInfo3)
        
        whenever(mockPackageManager.queryIntentActivities(any<Intent>(), any<Int>()))
            .thenReturn(mockResolveInfoList)

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
        whenever(mockPackageManager.queryIntentActivities(any<Intent>(), any<Int>()))
            .thenReturn(emptyList())

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
        
        whenever(mockPackageManager.queryIntentActivities(any<Intent>(), any<Int>()))
            .thenReturn(mockResolveInfoList)

        // When
        val result = getInstalledAppsUseCase()

        // Then
        assert(result.size == 2) // Duplicates should be removed
        assert(result.any { it.packageName == "com.app.duplicate" })
        assert(result.any { it.packageName == "com.app.unique" })
    }

    @Test
    fun `invoke should query with correct intent and flags`() = runTest {
        // Given
        whenever(mockPackageManager.queryIntentActivities(any<Intent>(), any<Int>()))
            .thenReturn(emptyList())

        // When
        getInstalledAppsUseCase()

        // Then
        verify(mockPackageManager).queryIntentActivities(
            argThat { intent ->
                intent.action == Intent.ACTION_MAIN && 
                intent.categories?.contains(Intent.CATEGORY_LAUNCHER) == true
            },
            eq(PackageManager.MATCH_ALL)
        )
    }

    private fun createMockResolveInfo(packageName: String, appName: String): ResolveInfo {
        val mockResolveInfo = mock<ResolveInfo>()
        val mockActivityInfo = mock<ActivityInfo>()
        val mockApplicationInfo = mock<ApplicationInfo>()
        
        mockResolveInfo.activityInfo = mockActivityInfo
        mockActivityInfo.packageName = packageName
        mockActivityInfo.applicationInfo = mockApplicationInfo
        
        whenever(mockPackageManager.getApplicationLabel(mockApplicationInfo)).thenReturn(appName)
        
        return mockResolveInfo
    }
}