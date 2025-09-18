package dev.sadakat.screentimetracker.domain.usecases

import dev.sadakat.screentimetracker.data.local.entities.AppSessionEvent
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAppSessionEventsUseCase @Inject constructor(
    private val repository: TrackerRepository
) {
    operator fun invoke(startTime: Long, endTime: Long): Flow<List<AppSessionEvent>> {
        return repository.getAllSessionsInRange(startTime, endTime)
    }
}
