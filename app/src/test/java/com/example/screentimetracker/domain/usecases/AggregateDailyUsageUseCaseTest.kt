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
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatcher
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Calendar

class AggregateDailyUsageUseCaseTest {

    @Mock
    private lateinit var mockRepository: TrackerRepository

    @Mock
    private lateinit var mockAppLogger: AppLogger

    @Captor
    private lateinit var dailyAppSummaryListCaptor: ArgumentCaptor<List<DailyAppSummary>>

    @Captor
    private lateinit var dailyScreenUnlockSummaryCaptor: ArgumentCaptor<DailyScreenUnlockSummary>

    private lateinit var aggregateDailyUsageUseCase: AggregateDailyUsageUseCase


    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
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
        whenever(
            mockRepository.getAggregatedSessionDataForDay(
                startOfYesterdayMillis, endOfYesterdayMillis
            )
        ).thenReturn(mockAppSessionAggregates)

        val mockUnlockCount = 15
        whenever(
            mockRepository.getUnlockCountForDay(
                startOfYesterdayMillis, endOfYesterdayMillis
            )
        ).thenReturn(mockUnlockCount)

        // When
        aggregateDailyUsageUseCase()

        // Then
        verify(mockAppLogger).d(any(), any())
        verify(mockAppLogger, org.mockito.kotlin.atLeastOnce()).d(any(), any())

        // Verify repository methods were called
        verify(mockRepository).insertDailyAppSummaries(any())
        verify(mockRepository).insertDailyScreenUnlockSummary(any())
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

        whenever(
            mockRepository.getAggregatedSessionDataForDay(
                startOfYesterdayMillis, endOfYesterdayMillis
            )
        ).thenReturn(emptyList())

        val mockUnlockCount = 5
        whenever(
            mockRepository.getUnlockCountForDay(
                startOfYesterdayMillis, endOfYesterdayMillis
            )
        ).thenReturn(mockUnlockCount)

        // When
        aggregateDailyUsageUseCase()

        // Then
        verify(mockAppLogger, org.mockito.kotlin.atLeastOnce()).d(any(), any())
        verify(mockRepository, never()).insertDailyAppSummaries(any())
        verify(mockRepository).insertDailyScreenUnlockSummary(any())
    }
}