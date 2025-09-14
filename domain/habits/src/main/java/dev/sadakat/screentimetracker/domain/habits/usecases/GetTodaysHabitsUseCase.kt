package dev.sadakat.screentimetracker.domain.habits.usecases

import dev.sadakat.screentimetracker.core.database.entities.HabitTracker
import dev.sadakat.screentimetracker.domain.habits.repository.HabitsRepository
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject

class GetTodaysHabitsUseCase @Inject constructor(
    private val repository: HabitsRepository
) {
    operator fun invoke(): Flow<List<HabitTracker>> {
        return repository.getHabitsForDate(getTodayStart())
    }

    private fun getTodayStart(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}