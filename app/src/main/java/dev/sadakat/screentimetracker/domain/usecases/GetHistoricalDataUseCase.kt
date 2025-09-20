package dev.sadakat.screentimetracker.domain.usecases

import dev.sadakat.screentimetracker.core.data.local.entities.DailyAppSummary
import dev.sadakat.screentimetracker.core.data.local.entities.DailyScreenUnlockSummary
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

data class HistoricalData(
    val appSummaries: List<DailyAppSummary>,
    val unlockSummaries: List<DailyScreenUnlockSummary>
    // We can add more processed data here, like overall daily screen time
)

class GetHistoricalDataUseCase @Inject constructor(
    private val repository: TrackerRepository
) {
    operator fun invoke(daysAgo: Int = 7): Flow<HistoricalData> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val endDateMillis = calendar.timeInMillis // Start of today (exclusive for "last N days")
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        val startDateMillis = calendar.timeInMillis // Start of N days ago

        val appSummariesFlow = repository.getDailyAppSummaries(startDateMillis, endDateMillis -1 ) // -1 to make endDate exclusive for summaries
        val unlockSummariesFlow = repository.getDailyScreenUnlockSummaries(startDateMillis, endDateMillis -1)

        return combine(appSummariesFlow, unlockSummariesFlow) { apps, unlocks ->
            HistoricalData(appSummaries = apps, unlockSummaries = unlocks)
        }
    }
}
