package com.example.screentimetracker.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.screentimetracker.data.local.DailyAppSummary
import com.example.screentimetracker.data.local.DailyScreenUnlockSummary
import com.example.screentimetracker.domain.repository.TrackerRepository // Corrected import path
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

@HiltWorker
class DailyAggregationWorker @AssistedInject constructor(
    @Assisted  appContext: Context,
    @Assisted   workerParams: WorkerParameters,
    private val repository: TrackerRepository // Inject repository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "DailyAggregationWorker"
        private const val TAG = "DailyAggregationWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting daily aggregation work.")
        return try {
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
            val appSessionAggregates = repository.getAggregatedSessionDataForDay(startOfYesterdayMillis, endOfYesterdayMillis).firstOrNull()

            if (appSessionAggregates != null && appSessionAggregates.isNotEmpty()) {
                val dailyAppSummaries = appSessionAggregates.map { aggregate ->
                    DailyAppSummary(
                        dateMillis = startOfYesterdayMillis,
                        packageName = aggregate.packageName,
                        totalDurationMillis = aggregate.totalDuration,
                        openCount = aggregate.sessionCount
                    )
                }
                repository.insertDailyAppSummaries(dailyAppSummaries) // New method in repository
                Log.d(TAG, "Inserted ${dailyAppSummaries.size} daily app summaries.")
            } else {
                Log.d(TAG, "No app session data to aggregate for yesterday.")
            }

            // 2. Aggregate Screen Unlock Data
            // We need a way to get unlock count for a specific day.
            val screenUnlocksYesterday = repository.getUnlockCountForDay(startOfYesterdayMillis, endOfYesterdayMillis).firstOrNull() ?: 0 // New method in repository

            val dailyUnlockSummary = DailyScreenUnlockSummary(
                dateMillis = startOfYesterdayMillis,
                unlockCount = screenUnlocksYesterday
            )
            repository.insertDailyScreenUnlockSummary(dailyUnlockSummary) // New method in repository
            Log.d(TAG, "Inserted daily unlock summary: $screenUnlocksYesterday unlocks.")

            Log.d(TAG, "Daily aggregation work finished successfully.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during daily aggregation work", e)
            Result.failure()
        }
    }
}
