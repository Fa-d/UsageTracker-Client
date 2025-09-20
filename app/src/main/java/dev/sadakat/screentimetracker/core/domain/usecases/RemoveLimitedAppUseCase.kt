package dev.sadakat.screentimetracker.core.domain.usecases

import dev.sadakat.screentimetracker.core.data.local.entities.LimitedApp
import dev.sadakat.screentimetracker.core.domain.repository.TrackerRepository
import javax.inject.Inject

class RemoveLimitedAppUseCase @Inject constructor(
    private val repository: TrackerRepository
) {
    suspend operator fun invoke(limitedApp: LimitedApp) {
        repository.deleteLimitedApp(limitedApp)
    }
}
