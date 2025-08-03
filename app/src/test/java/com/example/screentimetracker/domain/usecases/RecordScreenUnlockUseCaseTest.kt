package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.ScreenUnlockEvent
import com.example.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

class RecordScreenUnlockUseCaseTest {

    @Mock
    private lateinit var mockRepository: TrackerRepository

    private lateinit var recordScreenUnlockUseCase: RecordScreenUnlockUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        recordScreenUnlockUseCase = RecordScreenUnlockUseCase(mockRepository)
    }

    @Test
    fun `invoke should insert a ScreenUnlockEvent with current timestamp`() = runTest {
        // When
        recordScreenUnlockUseCase()

        // Then
        verify(mockRepository).insertScreenUnlockEvent(any<ScreenUnlockEvent>())
    }
}