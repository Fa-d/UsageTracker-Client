package com.example.screentimetracker.domain.usecases


import com.example.screentimetracker.data.local.AppSessionDataAggregate
import com.example.screentimetracker.data.local.DailyAppSummary
import com.example.screentimetracker.data.local.DailyScreenUnlockSummary
import com.example.screentimetracker.domain.repository.TrackerRepository
import com.example.screentimetracker.utils.logger.AppLogger
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import io.mockk.*
import io.mockk.impl.annotations.MockK
import java.util.Calendar

class AggregateDailyUsageUseCaseTest {

    @MockK
    private lateinit var mockRepository: TrackerRepository

    @MockK
    private lateinit var mockAppLogger: AppLogger

    private lateinit var aggregateDailyUsageUseCase: AggregateDailyUsageUseCase


    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        aggregateDailyUsageUseCase = AggregateDailyUsageUseCase(mockRepository, mockAppLogger)
    }

    @Test
    fun `invoke should aggregate app session and screen unlock data for yesterday`() = runTest {
        // Given
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfYesterdayMillis = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfYesterdayMillis = calendar.timeInMillis

        val mockAppSessionAggregates = listOf(
            AppSessionDataAggregate("com.app1", 10000L, 5),
            AppSessionDataAggregate("com.app2", 20000L, 10)
        )
        coEvery {
            mockRepository.getAggregatedSessionDataForDay(
                startOfYesterdayMillis, endOfYesterdayMillis
            )
        } returns mockAppSessionAggregates

        val mockUnlockCount = 15
        coEvery {
            mockRepository.getUnlockCountForDay(
                startOfYesterdayMillis, endOfYesterdayMillis
            )
        } returns mockUnlockCount

        // When
        aggregateDailyUsageUseCase()

        // Then
        verify { mockAppLogger.d(any(), any()) }
        verify(atLeast = 1) { mockAppLogger.d(any(), any()) }

        // Verify repository methods were called
        coVerify { mockRepository.insertDailyAppSummaries(any()) }
        coVerify { mockRepository.insertDailyScreenUnlockSummary(any()) }
    }

    @Test
    fun `invoke should handle no app session data gracefully`() = runTest {
        // Given
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfYesterdayMillis = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfYesterdayMillis = calendar.timeInMillis

        coEvery {
            mockRepository.getAggregatedSessionDataForDay(
                startOfYesterdayMillis, endOfYesterdayMillis
            )
        } returns emptyList()

        val mockUnlockCount = 5
        coEvery {
            mockRepository.getUnlockCountForDay(
                startOfYesterdayMillis, endOfYesterdayMillis
            )
        } returns mockUnlockCount

        // When
        aggregateDailyUsageUseCase()

        // Then
        verify(atLeast = 1) { mockAppLogger.d(any(), any()) }
        coVerify(exactly = 0) { mockRepository.insertDailyAppSummaries(any()) }
        coVerify { mockRepository.insertDailyScreenUnlockSummary(any()) }
    }
}