package dev.sadakat.screentimetracker.domain.usecases

import dev.sadakat.screentimetracker.data.local.entities.AppSessionEvent
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import io.mockk.*
import io.mockk.impl.annotations.MockK

class GetAppSessionEventsUseCaseTest {

    @MockK
    private lateinit var mockRepository: TrackerRepository

    private lateinit var getAppSessionEventsUseCase: GetAppSessionEventsUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        getAppSessionEventsUseCase = GetAppSessionEventsUseCase(mockRepository)
    }

    @Test
    fun `invoke should return a flow of AppSessionEvents`() = runTest {
        // Given
        val startTime = 1000L
        val endTime = 2000L
        val mockEvents = listOf(
            AppSessionEvent(
                1000L, "com.app1", 1500L, 500L, durationMillis = 300L
            ), AppSessionEvent(1600L, "com.app2", 1800L, 200L, durationMillis = 600L)
        )
        every { mockRepository.getAllSessionsInRange(startTime, endTime) } returns flowOf(mockEvents)

        // When
        val resultFlow = getAppSessionEventsUseCase(startTime, endTime)

        // Then
        val result = resultFlow.first()
        assert(result == mockEvents)
    }
}