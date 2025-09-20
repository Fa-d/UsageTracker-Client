package dev.sadakat.screentimetracker.core.domain.usecases

import dev.sadakat.screentimetracker.core.data.local.entities.LimitedApp
import dev.sadakat.screentimetracker.core.domain.repository.TrackerRepository

class UpdateLimitedAppUseCase(
    private val repository: TrackerRepository
) {
    suspend operator fun invoke(limitedApp: LimitedApp) {
        repository.insertLimitedApp(limitedApp) // insert with REPLACE strategy acts as update
    }
}
