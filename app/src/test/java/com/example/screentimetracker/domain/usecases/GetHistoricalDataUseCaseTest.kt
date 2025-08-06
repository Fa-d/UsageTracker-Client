package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.DailyAppSummary
import com.example.screentimetracker.data.local.DailyScreenUnlockSummary
import com.example.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.ArgumentMatchers.anyLong
import kotlinx.coroutines.flow.first

class GetHistoricalDataUseCaseTest {

    @Mock
    private lateinit var mockRepository: TrackerRepository

    private lateinit var getHistoricalDataUseCase: GetHistoricalDataUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        getHistoricalDataUseCase = GetHistoricalDataUseCase(mockRepository)
    }

    @Test
    fun `invoke should return correct HistoricalData for default daysAgo`() = runTest {
        // Given
        val mockAppSummaries = listOf(DailyAppSummary(1L, "pkg1", 100L, 1))
        val mockUnlockSummaries = listOf(DailyScreenUnlockSummary(1L, 5))

        `when`(mockRepository.getDailyAppSummaries(anyLong(), anyLong()))
            .thenReturn(flowOf(mockAppSummaries))
        `when`(mockRepository.getDailyScreenUnlockSummaries(anyLong(), anyLong()))
            .thenReturn(flowOf(mockUnlockSummaries))

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

        `when`(mockRepository.getDailyAppSummaries(anyLong(), anyLong()))
            .thenReturn(flowOf(mockAppSummaries))
        `when`(mockRepository.getDailyScreenUnlockSummaries(anyLong(), anyLong()))
            .thenReturn(flowOf(mockUnlockSummaries))

        // When
        val result = getHistoricalDataUseCase(daysAgo).first()

        // Then
        assert(result.appSummaries == mockAppSummaries)
        assert(result.unlockSummaries == mockUnlockSummaries)
    }
}