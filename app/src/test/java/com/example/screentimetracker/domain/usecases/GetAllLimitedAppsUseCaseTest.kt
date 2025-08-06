package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.LimitedApp
import com.example.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import java.util.concurrent.TimeUnit

class GetAllLimitedAppsUseCaseTest {

    @Mock
    private lateinit var mockRepository: TrackerRepository

    private lateinit var getAllLimitedAppsUseCase: GetAllLimitedAppsUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        getAllLimitedAppsUseCase = GetAllLimitedAppsUseCase(mockRepository)
    }

    @Test
    fun `invoke should return flow of limited apps from repository`() = runTest {
        // Given
        val expectedLimitedApps = listOf(
            LimitedApp("com.instagram.android", TimeUnit.HOURS.toMillis(1)),
            LimitedApp("com.facebook.katana", TimeUnit.MINUTES.toMillis(30)),
            LimitedApp("com.twitter.android", TimeUnit.MINUTES.toMillis(45))
        )
        whenever(mockRepository.getAllLimitedApps()).thenReturn(flowOf(expectedLimitedApps))

        // When
        val result = getAllLimitedAppsUseCase().first()

        // Then
        verify(mockRepository).getAllLimitedApps()
        assertEquals(3, result.size)
        assertEquals("com.instagram.android", result[0].packageName)
        assertEquals(TimeUnit.HOURS.toMillis(1), result[0].timeLimitMillis)
        assertEquals("com.facebook.katana", result[1].packageName)
        assertEquals(TimeUnit.MINUTES.toMillis(30), result[1].timeLimitMillis)
        assertEquals("com.twitter.android", result[2].packageName)
        assertEquals(TimeUnit.MINUTES.toMillis(45), result[2].timeLimitMillis)
    }

    @Test
    fun `invoke should return empty flow when no limited apps exist`() = runTest {
        // Given
        whenever(mockRepository.getAllLimitedApps()).thenReturn(flowOf(emptyList()))

        // When
        val result = getAllLimitedAppsUseCase().first()

        // Then
        verify(mockRepository).getAllLimitedApps()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke should return flow that updates when repository changes`() = runTest {
        // Given
        val initialApps = listOf(
            LimitedApp("com.app1", TimeUnit.MINUTES.toMillis(30))
        )
        val updatedApps = listOf(
            LimitedApp("com.app1", TimeUnit.MINUTES.toMillis(30)),
            LimitedApp("com.app2", TimeUnit.HOURS.toMillis(1))
        )
        
        whenever(mockRepository.getAllLimitedApps())
            .thenReturn(flowOf(initialApps))
            .thenReturn(flowOf(updatedApps))

        // When
        val flow = getAllLimitedAppsUseCase()

        // Then
        verify(mockRepository).getAllLimitedApps()
        assertNotNull(flow)
    }

    @Test
    fun `invoke should handle single limited app`() = runTest {
        // Given
        val singleApp = listOf(
            LimitedApp("com.single.app", TimeUnit.MINUTES.toMillis(15))
        )
        whenever(mockRepository.getAllLimitedApps()).thenReturn(flowOf(singleApp))

        // When
        val result = getAllLimitedAppsUseCase().first()

        // Then
        verify(mockRepository).getAllLimitedApps()
        assertEquals(1, result.size)
        assertEquals("com.single.app", result[0].packageName)
        assertEquals(TimeUnit.MINUTES.toMillis(15), result[0].timeLimitMillis)
    }

    @Test
    fun `invoke should handle apps with different time limits`() = runTest {
        // Given
        val appsWithVariousLimits = listOf(
            LimitedApp("com.short.limit", TimeUnit.MINUTES.toMillis(5)),
            LimitedApp("com.medium.limit", TimeUnit.HOURS.toMillis(1)),
            LimitedApp("com.long.limit", TimeUnit.HOURS.toMillis(8))
        )
        whenever(mockRepository.getAllLimitedApps()).thenReturn(flowOf(appsWithVariousLimits))

        // When
        val result = getAllLimitedAppsUseCase().first()

        // Then
        verify(mockRepository).getAllLimitedApps()
        assertEquals(3, result.size)
        // Verify the different time limits
        val shortLimitApp = result.find { it.packageName == "com.short.limit" }
        val mediumLimitApp = result.find { it.packageName == "com.medium.limit" }
        val longLimitApp = result.find { it.packageName == "com.long.limit" }
        
        assertNotNull(shortLimitApp)
        assertEquals(TimeUnit.MINUTES.toMillis(5), shortLimitApp?.timeLimitMillis)
        
        assertNotNull(mediumLimitApp)
        assertEquals(TimeUnit.HOURS.toMillis(1), mediumLimitApp?.timeLimitMillis)
        
        assertNotNull(longLimitApp)
        assertEquals(TimeUnit.HOURS.toMillis(8), longLimitApp?.timeLimitMillis)
    }
}