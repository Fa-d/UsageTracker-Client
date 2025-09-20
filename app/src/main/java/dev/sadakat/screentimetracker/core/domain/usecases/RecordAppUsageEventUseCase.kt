package dev.sadakat.screentimetracker.core.domain.usecases

import dev.sadakat.screentimetracker.core.data.local.entities.AppUsageEvent
import dev.sadakat.screentimetracker.core.domain.repository.TrackerRepository

class RecordAppUsageEventUseCase(
    private val repository: TrackerRepository
) {
    suspend operator fun invoke(packageName: String, eventName: String, timestamp: Long) {
        val event = AppUsageEvent(
            packageName = packageName,
            eventName = eventName,
            timestamp = timestamp
        )
        repository.insertAppUsageEvent(event)
    }
}
