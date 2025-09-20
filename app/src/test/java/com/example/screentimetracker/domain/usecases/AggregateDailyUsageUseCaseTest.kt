package dev.sadakat.screentimetracker.domain.usecases


import dev.sadakat.screentimetracker.domain.model.AppSessionDataAggregate
import dev.sadakat.screentimetracker.core.data.local.entities.DailyAppSummary
import dev.sadakat.screentimetracker.core.data.local.entities.DailyScreenUnlockSummary
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import io.mockk.*
import io.mockk.impl.annotations.MockK
import java.util.Calendar

class AggregateDailyUsageUseCaseTest {

    private val mockRepository = mockk<TrackerRepository>()
    private val mockAppLogger = mockk<AppLogger>()
    private lateinit var aggregateDailyUsageUseCase: AggregateDailyUsageUseCase

    @Before
    fun setup() {
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
            AppSessionDataAggregate("com.app1", 10000L, 5, 0L, 10000L),
            AppSessionDataAggregate("com.app2", 20000L, 10, 0L, 20000L)
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
        
        every { mockAppLogger.d(any(), any()) } just Runs
        coEvery { mockRepository.insertDailyAppSummaries(any()) } just Runs
        coEvery { mockRepository.insertDailyScreenUnlockSummary(any()) } just Runs

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
        
        every { mockAppLogger.d(any(), any()) } just Runs
        coEvery { mockRepository.insertDailyScreenUnlockSummary(any()) } just Runs

        // When
        aggregateDailyUsageUseCase()

        // Then
        verify(atLeast = 1) { mockAppLogger.d(any(), any()) }
        coVerify(exactly = 0) { mockRepository.insertDailyAppSummaries(any()) }
        coVerify { mockRepository.insertDailyScreenUnlockSummary(any()) }
    }
}