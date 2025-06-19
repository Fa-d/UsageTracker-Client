package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.DailyAppSummary
import com.example.screentimetracker.data.local.DailyScreenUnlockSummary
import com.example.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.Calendar

class GetHistoricalDataUseCaseTest {

    private lateinit var mockRepository: TrackerRepository
    private lateinit var getHistoricalDataUseCase: GetHistoricalDataUseCase

    @Before
    fun setup() {
        mockRepository = mock(TrackerRepository::class.java)
        getHistoricalDataUseCase = GetHistoricalDataUseCase(mockRepository)
    }

    @Test
    fun invoke_fetchesAndCombinesHistoricalData() = runBlocking {
        val daysAgo = 7
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
        val endDateMillis = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        val startDateMillis = calendar.timeInMillis

        val appSummaries = listOf(DailyAppSummary(startDateMillis, "app1", 1000L, 1))
        val unlockSummaries = listOf(DailyScreenUnlockSummary(startDateMillis, 10))

        `when`(mockRepository.getDailyAppSummaries(startDateMillis, endDateMillis - 1)).thenReturn(flowOf(appSummaries))
        `when`(mockRepository.getDailyScreenUnlockSummaries(startDateMillis, endDateMillis - 1)).thenReturn(flowOf(unlockSummaries))

        val result = getHistoricalDataUseCase.invoke(daysAgo = daysAgo).first()

        assertEquals(appSummaries, result.appSummaries)
        assertEquals(unlockSummaries, result.unlockSummaries)
    }
}
