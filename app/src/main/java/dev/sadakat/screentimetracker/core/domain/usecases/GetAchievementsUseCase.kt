package dev.sadakat.screentimetracker.core.domain.usecases

import dev.sadakat.screentimetracker.core.domain.model.Achievement
import dev.sadakat.screentimetracker.core.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow

class GetAchievementsUseCase(
    private val repository: TrackerRepository
) {
    operator fun invoke(): Flow<List<Achievement>> {
        return repository.getAllAchievements()
    }
}