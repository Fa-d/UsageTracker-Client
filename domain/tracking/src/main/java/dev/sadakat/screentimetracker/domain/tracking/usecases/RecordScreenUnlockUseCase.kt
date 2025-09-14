package dev.sadakat.screentimetracker.domain.tracking.usecases

import dev.sadakat.screentimetracker.core.database.entities.ScreenUnlockEvent
import dev.sadakat.screentimetracker.domain.tracking.repository.TrackerRepository
import javax.inject.Inject

class RecordScreenUnlockUseCase @Inject constructor(
    private val repository: TrackerRepository
) {
    suspend operator fun invoke() {
        val event = ScreenUnlockEvent(timestamp = System.currentTimeMillis())
        repository.insertScreenUnlockEvent(event)
    }
}