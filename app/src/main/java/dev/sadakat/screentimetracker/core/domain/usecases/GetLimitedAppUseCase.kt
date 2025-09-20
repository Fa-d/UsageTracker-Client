package dev.sadakat.screentimetracker.core.domain.usecases

import dev.sadakat.screentimetracker.core.data.local.entities.LimitedApp
import dev.sadakat.screentimetracker.core.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLimitedAppUseCase @Inject constructor(
    private val repository: TrackerRepository
) {
    operator fun invoke(packageName: String): Flow<LimitedApp?> {
        return repository.getLimitedApp(packageName)
    }
}
