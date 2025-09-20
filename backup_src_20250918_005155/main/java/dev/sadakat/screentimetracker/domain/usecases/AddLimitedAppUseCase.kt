package dev.sadakat.screentimetracker.domain.usecases

import dev.sadakat.screentimetracker.core.data.local.LimitedApp
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import javax.inject.Inject

class AddLimitedAppUseCase @Inject constructor(
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
