package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.AppSessionEvent
import com.example.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.mockito.kotlin.eq

class GetAppSessionEventsUseCaseTest {

    @Mock
    private lateinit var mockRepository: TrackerRepository

    private lateinit var getAppSessionEventsUseCase: GetAppSessionEventsUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        getAppSessionEventsUseCase = GetAppSessionEventsUseCase(mockRepository)
    }

    @Test
    fun `invoke should return a flow of AppSessionEvents`() = runTest {
        // Given
        val startTime = 1000L
        val endTime = 2000L
        val mockEvents = listOf(
            AppSessionEvent(
                1000L, "com.1=", 1500L, 500L, durationMillis = 300L
            ), AppSessionEvent(1600L, "com.app2", 1800L, 200L, durationMillis = 600L)
        )
        whenever(mockRepository.getAllSessionsInRange(eq(startTime), eq(endTime))).thenReturn(
            flowOf(
                mockEvents
            )
        )

        // When
        val resultFlow = getAppSessionEventsUseCase(startTime, endTime)

        // Then
        val result = resultFlow.first()
        assert(result == mockEvents)
    }
}