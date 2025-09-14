package dev.sadakat.screentimetracker.domain.tracking.usecases

import dev.sadakat.screentimetracker.core.database.entities.DailyAppSummary
import dev.sadakat.screentimetracker.core.database.entities.DailyScreenUnlockSummary
import dev.sadakat.screentimetracker.domain.tracking.repository.TrackerRepository
import android.util.Log
import java.util.Calendar
import javax.inject.Inject

class AggregateDailyUsageUseCase @Inject constructor(
    private val repository: TrackerRepository
) {

    companion object {
        private const val TAG = "AggregateDailyUsageUseCase"
    }

    suspend operator fun invoke() {
        Log.d(TAG, "Starting daily aggregation use case.")

        val calendar = Calendar.getInstance()
        // Set calendar to yesterday
        calendar.add(Calendar.DAY_OF_YEAR, -1)

        // Get start of yesterday
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfYesterdayMillis = calendar.timeInMillis

        // Get end of yesterday (start of today)
        calendar.add(Calendar.DAY_OF_YEAR, 1) // Back to today
        val endOfYesterdayMillis = calendar.timeInMillis // This is effectively start of today

        Log.d(TAG, "Aggregating data for day starting at: $startOfYesterdayMillis until $endOfYesterdayMillis")

        // 1. Aggregate App Session Data
        val appSessionAggregates = repository.getAggregatedSessionDataForDay(startOfYesterdayMillis, endOfYesterdayMillis)

        if (appSessionAggregates.isNotEmpty()) {
            val dailyAppSummaries = appSessionAggregates.map { aggregate ->
                DailyAppSummary(
                    dateMillis = startOfYesterdayMillis,
                    packageName = aggregate.packageName,
                    totalDurationMillis = aggregate.totalDuration,
                    openCount = aggregate.sessionCount
                )
            }
            repository.insertDailyAppSummaries(dailyAppSummaries)
            Log.d(TAG, "Inserted ${dailyAppSummaries.size} daily app summaries.")
        } else {
            Log.d(TAG, "No app session data to aggregate for yesterday.")
        }

        // 2. Aggregate Screen Unlock Data
        val screenUnlocksYesterday = repository.getUnlockCountForDay(startOfYesterdayMillis, endOfYesterdayMillis)

        val dailyUnlockSummary = DailyScreenUnlockSummary(
            dateMillis = startOfYesterdayMillis,
            unlockCount = screenUnlocksYesterday
        )
        repository.insertDailyScreenUnlockSummary(dailyUnlockSummary)
        Log.d(TAG, "Inserted daily unlock summary: $screenUnlocksYesterday unlocks.")

        Log.d(TAG, "Daily aggregation use case finished successfully.")
    }
}