package dev.sadakat.screentimetracker.domain.goals.usecases

import dev.sadakat.screentimetracker.domain.goals.repository.GoalsRepository
import dev.sadakat.screentimetracker.core.common.model.Achievement
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAchievementsUseCase @Inject constructor(
    private val repository: GoalsRepository
) {
    operator fun invoke(): Flow<List<Achievement>> {
        return repository.getAllAchievements()
    }
}