package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.AppSessionDataAggregate
import com.example.screentimetracker.data.local.DailyAppSummary
import com.example.screentimetracker.data.local.DailyScreenUnlockSummary
import com.example.screentimetracker.domain.repository.TrackerRepository
import com.example.screentimetracker.utils.logger.AppLogger
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor // Import ArgumentCaptor
import org.mockito.Captor // Import Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat // Import argThat
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.never
import java.util.Calendar
import org.mockito.ArgumentMatcher // Import ArgumentMatcher
import org.mockito.kotlin.whenever

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

    // Custom ArgumentMatcher for String content
    class StringContainsMatcher(private val substring: String) : ArgumentMatcher<String> {
        override fun matches(argument: String?): Boolean {
            return argument != null && argument.contains(substring)
        }
    }

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
        whenever(mockRepository.getAggregatedSessionDataForDay(startOfYesterdayMillis, endOfYesterdayMillis))
            .thenReturn(mockAppSessionAggregates)

        val mockUnlockCount = 15
        whenever(mockRepository.getUnlockCountForDay(startOfYesterdayMillis, endOfYesterdayMillis))
            .thenReturn(mockUnlockCount)

        // When
        aggregateDailyUsageUseCase()

        // Then
        verify(mockAppLogger).d(anyString(), argThat(StringContainsMatcher("Starting daily aggregation use case.")))
        verify(mockRepository).getAggregatedSessionDataForDay(startOfYesterdayMillis, endOfYesterdayMillis)
        verify(mockRepository).insertDailyAppSummaries(dailyAppSummaryListCaptor.capture())
        verify(mockRepository).getUnlockCountForDay(startOfYesterdayMillis, endOfYesterdayMillis)
        verify(mockRepository).insertDailyScreenUnlockSummary(dailyScreenUnlockSummaryCaptor.capture())
        verify(mockAppLogger).d(anyString(), argThat(StringContainsMatcher("Daily aggregation use case finished successfully.")))

        // Verify captured DailyAppSummaries
        val capturedAppSummaries = dailyAppSummaryListCaptor.value
        assert(capturedAppSummaries.size == 2)
        assert(capturedAppSummaries[0].packageName == "com.app1")
        assert(capturedAppSummaries[0].totalDurationMillis == 10000L)
        assert(capturedAppSummaries[0].openCount == 5)
        assert(capturedAppSummaries[0].dateMillis == startOfYesterdayMillis)

        assert(capturedAppSummaries[1].packageName == "com.app2")
        assert(capturedAppSummaries[1].totalDurationMillis == 20000L)
        assert(capturedAppSummaries[1].openCount == 10)
        assert(capturedAppSummaries[1].dateMillis == startOfYesterdayMillis)

        // Verify captured DailyScreenUnlockSummary
        val capturedUnlockSummary = dailyScreenUnlockSummaryCaptor.value
        assert(capturedUnlockSummary.dateMillis == startOfYesterdayMillis)
        assert(capturedUnlockSummary.unlockCount == mockUnlockCount)
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

        whenever(mockRepository.getAggregatedSessionDataForDay(startOfYesterdayMillis, endOfYesterdayMillis))
            .thenReturn(emptyList())

        val mockUnlockCount = 5
        whenever(mockRepository.getUnlockCountForDay(startOfYesterdayMillis, endOfYesterdayMillis))
            .thenReturn(mockUnlockCount)

        // When
        aggregateDailyUsageUseCase()

        // Then
        verify(mockAppLogger).d(anyString(), argThat(StringContainsMatcher("No app session data to aggregate for yesterday.")))
        verify(mockRepository, never()).insertDailyAppSummaries(any())
        verify(mockRepository).insertDailyScreenUnlockSummary(dailyScreenUnlockSummaryCaptor.capture())
    }
}