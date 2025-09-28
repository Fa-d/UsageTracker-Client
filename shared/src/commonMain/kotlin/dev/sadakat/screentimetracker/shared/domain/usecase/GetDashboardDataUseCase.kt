package dev.sadakat.screentimetracker.shared.domain.usecase

import dev.sadakat.screentimetracker.shared.domain.model.DashboardData
import dev.sadakat.screentimetracker.shared.domain.repository.UsageRepository
import kotlinx.coroutines.flow.Flow

class GetDashboardDataUseCase(
    private val usageRepository: UsageRepository
) {
    suspend operator fun invoke(): Flow<DashboardData> {
        return usageRepository.getDashboardData()
    }
}