package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.AppOpenData
import com.example.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when` // Using Mockito for mocking
import org.mockito.Mockito.mock // Using Mockito for mocking

// --- IMPORTANT ---
// This test uses Mockito for mocking the repository.
// Ensure testImplementation "org.mockito:mockito-core:4.+" is in app/build.gradle
// For simplicity, this example uses basic Mockito. More advanced setups might use mockito-kotlin.

class GetDashboardDataUseCaseTest {

    private lateinit var mockRepository: TrackerRepository
    private lateinit var getDashboardDataUseCase: GetDashboardDataUseCase

    @Before
    fun setup() {
        mockRepository = mock(TrackerRepository::class.java)
        getDashboardDataUseCase = GetDashboardDataUseCase(mockRepository)
    }

    @Test
    fun invoke_combinesDataCorrectly() = runBlocking {
        val todayStart = getDashboardDataUseCase.getStartOfTodayMillis() // Use the internal helper

        val unlockCountFlow = flowOf(5)
        val appOpenDataList = listOf(
            AppOpenData("app1", 3, 12345L),
            AppOpenData("app2", 2, 67890L)
        )
        val appUsagesFlow = flowOf(appOpenDataList)

        // Stub repository methods
        `when`(mockRepository.getUnlockCountSince(todayStart)).thenReturn(unlockCountFlow)
        `when`(mockRepository.getAppOpenCountsSince(todayStart)).thenReturn(appUsagesFlow)

        val result = getDashboardDataUseCase.invoke().first()

        assertEquals(5, result.totalScreenUnlocksToday)
        assertEquals(2, result.appUsagesToday.size)
        assertEquals("app1", result.appUsagesToday[0].packageName)
        assertEquals(3, result.appUsagesToday[0].openCount)
        assertEquals("app2", result.appUsagesToday[1].packageName)
    }
}
