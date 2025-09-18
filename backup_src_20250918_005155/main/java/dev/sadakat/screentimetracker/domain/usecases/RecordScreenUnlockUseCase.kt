package dev.sadakat.screentimetracker.domain.usecases

import dev.sadakat.screentimetracker.data.local.ScreenUnlockEvent
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import javax.inject.Inject

class RecordScreenUnlockUseCase @Inject constructor(
    private val repository: TrackerRepository
) {
    suspend operator fun invoke() {
        val event = ScreenUnlockEvent(timestamp = System.currentTimeMillis())
        repository.insertScreenUnlockEvent(event)
    }
}
