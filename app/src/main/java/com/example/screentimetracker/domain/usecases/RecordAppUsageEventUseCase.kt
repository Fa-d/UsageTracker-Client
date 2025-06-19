package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.AppUsageEvent
import com.example.screentimetracker.domain.repository.TrackerRepository
import javax.inject.Inject

class RecordAppUsageEventUseCase @Inject constructor(
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
