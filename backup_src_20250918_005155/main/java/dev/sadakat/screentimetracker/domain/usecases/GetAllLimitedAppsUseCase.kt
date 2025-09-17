package dev.sadakat.screentimetracker.domain.usecases

import dev.sadakat.screentimetracker.data.local.LimitedApp
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllLimitedAppsUseCase @Inject constructor(
    private val repository: TrackerRepository
) {
    operator fun invoke(): Flow<List<LimitedApp>> {
        return repository.getAllLimitedApps()
    }
}
