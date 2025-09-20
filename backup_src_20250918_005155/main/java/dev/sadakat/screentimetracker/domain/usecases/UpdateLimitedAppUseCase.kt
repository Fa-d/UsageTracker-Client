package dev.sadakat.screentimetracker.domain.usecases

import dev.sadakat.screentimetracker.core.data.local.LimitedApp
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import javax.inject.Inject

class UpdateLimitedAppUseCase @Inject constructor(
    private val repository: TrackerRepository
) {
    suspend operator fun invoke(limitedApp: LimitedApp) {
        repository.insertLimitedApp(limitedApp) // insert with REPLACE strategy acts as update
    }
}
