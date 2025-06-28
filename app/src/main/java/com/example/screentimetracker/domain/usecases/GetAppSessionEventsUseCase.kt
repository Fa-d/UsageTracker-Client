package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.AppSessionEvent
import com.example.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAppSessionEventsUseCase @Inject constructor(
    private val repository: TrackerRepository
) {
    operator fun invoke(startTime: Long, endTime: Long): Flow<List<AppSessionEvent>> {
        return repository.getAllSessionsInRange(startTime, endTime)
    }
}
