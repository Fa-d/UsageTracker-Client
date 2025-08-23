package dev.sadakat.screentimetracker.domain.usecases

import dev.sadakat.screentimetracker.data.local.ScreenUnlockEvent
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import io.mockk.*
import io.mockk.impl.annotations.MockK

class RecordScreenUnlockUseCaseTest {

    @MockK
    private lateinit var mockRepository: TrackerRepository

    private lateinit var recordScreenUnlockUseCase: RecordScreenUnlockUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        recordScreenUnlockUseCase = RecordScreenUnlockUseCase(mockRepository)
    }

    @Test
    fun `invoke should insert a ScreenUnlockEvent with current timestamp`() = runTest {
        // When
        recordScreenUnlockUseCase()

        // Then
        coVerify { mockRepository.insertScreenUnlockEvent(any<ScreenUnlockEvent>()) }
    }
}