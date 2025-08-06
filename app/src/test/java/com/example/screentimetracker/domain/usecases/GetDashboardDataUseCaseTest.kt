package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.AppLastOpenedData
import com.example.screentimetracker.data.local.AppSessionDataAggregate
import com.example.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.first
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.flow.first
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.util.Calendar

@RunWith(MockitoJUnitRunner::class)
class GetDashboardDataUseCaseTest {

    @Mock
    private lateinit var mockRepository: TrackerRepository

    private lateinit var getDashboardDataUseCase: GetDashboardDataUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        getDashboardDataUseCase = GetDashboardDataUseCase(mockRepository)
    }

    @Test
    fun `invoke should return correct DashboardData`() = runTest {
        // Given
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val endOfToday = Calendar.getInstance().timeInMillis

        val mockUnlockCount = 5
        val mockSessionAggregates = listOf(
            AppSessionDataAggregate("com.app1", 10000L, 2),
            AppSessionDataAggregate("com.app2", 5000L, 1)
        )
        val mockLastOpenedTimestamps = listOf(
            AppLastOpenedData("com.app1", 1500L),
            AppLastOpenedData("com.app2", 2500L)
        )

        `when`(mockRepository.getUnlockCountForDayFlow(anyLong(), anyLong()))
            .thenReturn(kotlinx.coroutines.flow.flowOf(mockUnlockCount))
        `when`(mockRepository.getAggregatedSessionDataForDayFlow(anyLong(), anyLong()))
            .thenReturn(kotlinx.coroutines.flow.flowOf(mockSessionAggregates))
        `when`(mockRepository.getLastOpenedTimestampsForAppsInRangeFlow(anyLong(), anyLong()))
            .thenReturn(kotlinx.coroutines.flow.flowOf(mockLastOpenedTimestamps))

        // When
        val result = getDashboardDataUseCase().first()

        // Then
        assert(result.totalScreenUnlocksToday == mockUnlockCount)
        assert(result.appDetailsToday.size == 2)
        assert(result.appDetailsToday[0].packageName == "com.app1")
        assert(result.appDetailsToday[0].totalDurationMillis == 10000L)
        assert(result.appDetailsToday[0].sessionCount == 2)
        assert(result.appDetailsToday[0].lastOpenedTimestamp == 1500L)
        assert(result.appDetailsToday[1].packageName == "com.app2")
        assert(result.appDetailsToday[1].totalDurationMillis == 5000L)
        assert(result.appDetailsToday[1].sessionCount == 1)
        assert(result.appDetailsToday[1].lastOpenedTimestamp == 2500L)
        assert(result.totalScreenTimeFromSessionsToday == 15000L)
    }
}