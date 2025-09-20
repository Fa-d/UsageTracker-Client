package dev.sadakat.screentimetracker.core.domain.usecases

import dev.sadakat.screentimetracker.core.data.local.entities.ScreenUnlockEvent
import dev.sadakat.screentimetracker.core.domain.repository.TrackerRepository
import javax.inject.Inject

class RecordScreenUnlockUseCase @Inject constructor(
    private val repository: TrackerRepository
) {
    suspend operator fun invoke() {
        val event = ScreenUnlockEvent(timestamp = System.currentTimeMillis())
        repository.insertScreenUnlockEvent(event)
    }
}
