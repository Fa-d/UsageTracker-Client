package dev.sadakat.screentimetracker.core.domain.usecases

import dev.sadakat.screentimetracker.core.data.local.entities.LimitedApp
import dev.sadakat.screentimetracker.core.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow

class GetAllLimitedAppsUseCase(
    private val repository: TrackerRepository
) {
    operator fun invoke(): Flow<List<LimitedApp>> {
        return repository.getAllLimitedApps()
    }
}
