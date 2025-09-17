package dev.sadakat.screentimetracker.domain.usecases

import dev.sadakat.screentimetracker.data.local.DailyAppSummary
import dev.sadakat.screentimetracker.data.local.DailyScreenUnlockSummary
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first

class GetHistoricalDataUseCaseTest {

    @MockK
    private lateinit var mockRepository: TrackerRepository

    private lateinit var getHistoricalDataUseCase: GetHistoricalDataUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        getHistoricalDataUseCase = GetHistoricalDataUseCase(mockRepository)
    }

    @Test
    fun `invoke should return correct HistoricalData for default daysAgo`() = runTest {
        // Given
        val mockAppSummaries = listOf(DailyAppSummary(1L, "pkg1", 100L, 1))
        val mockUnlockSummaries = listOf(DailyScreenUnlockSummary(1L, 5))

        every { mockRepository.getDailyAppSummaries(any(), any()) } returns flowOf(mockAppSummaries)
        every { mockRepository.getDailyScreenUnlockSummaries(any(), any()) } returns flowOf(mockUnlockSummaries)

        // When
        val result = getHistoricalDataUseCase().first()

        // Then
        assert(result.appSummaries == mockAppSummaries)
        assert(result.unlockSummaries == mockUnlockSummaries)
    }

    @Test
    fun `invoke should return correct HistoricalData for specified daysAgo`() = runTest {
        // Given
        val daysAgo = 3
        val mockAppSummaries = listOf(DailyAppSummary(2L, "pkg2", 200L, 2))
        val mockUnlockSummaries = listOf(DailyScreenUnlockSummary(2L, 10))

        every { mockRepository.getDailyAppSummaries(any(), any()) } returns flowOf(mockAppSummaries)
        every { mockRepository.getDailyScreenUnlockSummaries(any(), any()) } returns flowOf(mockUnlockSummaries)

        // When
        val result = getHistoricalDataUseCase(daysAgo).first()

        // Then
        assert(result.appSummaries == mockAppSummaries)
        assert(result.unlockSummaries == mockUnlockSummaries)
    }
}