package dev.sadakat.screentimetracker.core.domain.usecase

import dev.sadakat.screentimetracker.core.domain.model.AppSession
import dev.sadakat.screentimetracker.core.domain.model.TimeRange
import dev.sadakat.screentimetracker.core.domain.repository.ScreenTimeRepository
import dev.sadakat.screentimetracker.core.domain.service.WellnessCalculationService

class TrackAppUsageUseCase(
    private val screenTimeRepository: ScreenTimeRepository,
    private val wellnessService: WellnessCalculationService
) {
    suspend fun startTracking() {
        // TODO: Implement tracking logic
    }

    suspend fun recordAppSession(session: AppSession) {
        screenTimeRepository.saveAppSession(session)
    }

    suspend fun recordScreenUnlock(timestamp: Long = System.currentTimeMillis()) {
        screenTimeRepository.recordScreenUnlock(timestamp)
    }

    suspend fun getUsagePatterns(timeRange: TimeRange) =
        screenTimeRepository.getUsagePatterns(timeRange)
}