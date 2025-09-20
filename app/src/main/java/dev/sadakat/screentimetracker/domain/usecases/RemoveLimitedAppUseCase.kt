package dev.sadakat.screentimetracker.domain.usecases

import dev.sadakat.screentimetracker.core.data.local.entities.LimitedApp
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import javax.inject.Inject

class RemoveLimitedAppUseCase @Inject constructor(
    private val repository: TrackerRepository
) {
    suspend operator fun invoke(limitedApp: LimitedApp) {
        repository.deleteLimitedApp(limitedApp)
    }
}
