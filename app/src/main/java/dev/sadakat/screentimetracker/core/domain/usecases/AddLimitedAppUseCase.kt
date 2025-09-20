package dev.sadakat.screentimetracker.core.domain.usecases

import dev.sadakat.screentimetracker.core.data.local.entities.LimitedApp
import dev.sadakat.screentimetracker.core.domain.repository.TrackerRepository

class AddLimitedAppUseCase(
    private val repository: TrackerRepository
) {
    suspend operator fun invoke(limitedApp: LimitedApp) {
        // Basic validation: ensure timeLimitMillis is positive
        if (limitedApp.timeLimitMillis <= 0) {
            throw IllegalArgumentException("Time limit must be positive.")
        }
        repository.insertLimitedApp(limitedApp)
    }
}
