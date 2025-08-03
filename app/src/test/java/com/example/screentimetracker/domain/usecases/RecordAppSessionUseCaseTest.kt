package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.AppSessionEvent
import com.example.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.never
import org.mockito.kotlin.argThat

class RecordAppSessionUseCaseTest {

    @Mock
    private lateinit var mockRepository: TrackerRepository

    private lateinit var recordAppSessionUseCase: RecordAppSessionUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        recordAppSessionUseCase = RecordAppSessionUseCase(mockRepository)
    }

    @Test
    fun `invoke with packageName, startTime, endTime should insert AppSessionEvent`() = runTest {
        // Given
        val packageName = "com.example.testapp"
        val startTime = 1000L
        val endTime = 2000L

        // When
        recordAppSessionUseCase(packageName, startTime, endTime)

        // Then
        verify(mockRepository).insertAppSession(any<AppSessionEvent>())
    }

    @Test
    fun `invoke with AppSessionEvent should insert AppSessionEvent`() = runTest {
        // Given
        val sessionEvent = AppSessionEvent(
            packageName = "com.example.testapp",
            startTimeMillis = 1000L,
            endTimeMillis = 2000L,
            durationMillis = 1000L
        )

        // When
        recordAppSessionUseCase(sessionEvent)

        // Then
        verify(mockRepository).insertAppSession(sessionEvent)
    }

    @Test
    fun `invoke with endTime less than startTime should correct and insert AppSessionEvent`() = runTest {
        // Given
        val packageName = "com.example.testapp"
        val startTime = 2000L
        val endTime = 1000L // Invalid end time

        // When
        recordAppSessionUseCase(packageName, startTime, endTime)

        // Then
        verify(mockRepository).insertAppSession(argThat { event ->
            event.packageName == packageName &&
            event.startTimeMillis == startTime &&
            event.endTimeMillis == startTime &&
            event.durationMillis == 0L
        })
    }

    @Test
    fun `invoke with AppSessionEvent and invalid times should correct and insert`() = runTest {
        // Given
        val sessionEvent = AppSessionEvent(
            packageName = "com.example.testapp",
            startTimeMillis = 2000L,
            endTimeMillis = 1000L,
            durationMillis = 500L // Incorrect duration
        )

        // When
        recordAppSessionUseCase(sessionEvent)

        // Then
        verify(mockRepository).insertAppSession(argThat { event ->
            event.packageName == sessionEvent.packageName &&
            event.startTimeMillis == sessionEvent.startTimeMillis &&
            event.endTimeMillis == sessionEvent.startTimeMillis &&
            event.durationMillis == 0L
        })
    }

    @Test
    fun `invoke with AppSessionEvent and inconsistent duration should correct and insert`() = runTest {
        // Given
        val sessionEvent = AppSessionEvent(
            packageName = "com.example.testapp",
            startTimeMillis = 1000L,
            endTimeMillis = 3000L,
            durationMillis = 500L // Inconsistent duration
        )

        // When
        recordAppSessionUseCase(sessionEvent)

        // Then
        verify(mockRepository).insertAppSession(argThat { event ->
            event.packageName == sessionEvent.packageName &&
            event.startTimeMillis == sessionEvent.startTimeMillis &&
            event.endTimeMillis == sessionEvent.endTimeMillis &&
            event.durationMillis == 2000L
        })
    }
}