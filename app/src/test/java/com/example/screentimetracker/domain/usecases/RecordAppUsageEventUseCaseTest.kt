package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.AppUsageEvent
import com.example.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import io.mockk.*
import io.mockk.impl.annotations.MockK

class RecordAppUsageEventUseCaseTest {

    @MockK
    private lateinit var mockRepository: TrackerRepository

    private lateinit var recordAppUsageEventUseCase: RecordAppUsageEventUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        recordAppUsageEventUseCase = RecordAppUsageEventUseCase(mockRepository)
    }

    @Test
    fun `invoke should insert an AppUsageEvent with provided data`() = runTest {
        // Given
        val packageName = "com.example.app"
        val eventName = "app_opened"
        val timestamp = System.currentTimeMillis()

        // When
        recordAppUsageEventUseCase(packageName, eventName, timestamp)

        // Then
        coVerify { mockRepository.insertAppUsageEvent(any<AppUsageEvent>()) }
    }
}