package dev.sadakat.screentimetracker.workers

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.sadakat.screentimetracker.core.data.local.entities.DailyAppSummary
import dev.sadakat.screentimetracker.core.data.local.entities.DailyScreenUnlockSummary
import dev.sadakat.screentimetracker.core.domain.repository.TrackerRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlinx.coroutines.flow.first

@HiltWorker
class HistoricalDataWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val usageStatsManager: UsageStatsManager,
    private val repository: TrackerRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting HistoricalDataWorker")
        try {
            val calendar = Calendar.getInstance()
            // Go back 7 days from today for historical data
            for (i in 1..7) {
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.add(Calendar.DAY_OF_YEAR, -i)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDayMillis = calendar.timeInMillis

                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val endOfDayMillis = calendar.timeInMillis

                Log.d(
                    TAG, "Fetching data for day $i (start: $startOfDayMillis, end: $endOfDayMillis)"
                )

                // Fetch app usage stats for the day
                val usageStatsList = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY, startOfDayMillis, endOfDayMillis
                )

                // Fetch usage events to calculate open counts
                val usageEvents = usageStatsManager.queryEvents(startOfDayMillis, endOfDayMillis)
                val appOpenCounts = mutableMapOf<String, Int>()
                val event = UsageEvents.Event()
                while (usageEvents.hasNextEvent()) {
                    usageEvents.getNextEvent(event)
                    if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED || event.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                        appOpenCounts[event.packageName] =
                            appOpenCounts.getOrDefault(event.packageName, 0) + 1
                    }
                }

                val dailyAppSummaries = mutableListOf<DailyAppSummary>()
                var totalDurationForDay = 0L

                for (usageStats in usageStatsList) {
                    val packageName = usageStats.packageName
                    val totalTimeInForeground = usageStats.totalTimeInForeground
                    val openCount = appOpenCounts.getOrDefault(packageName, 0)

                    if (totalTimeInForeground > 0) {
                        dailyAppSummaries.add(
                            DailyAppSummary(
                                dateMillis = startOfDayMillis,
                                packageName = packageName,
                                totalDurationMillis = totalTimeInForeground,
                                openCount = openCount
                            )
                        )
                        totalDurationForDay += totalTimeInForeground
                    } else if (openCount > 0 && !dailyAppSummaries.any { it.packageName == packageName }) {
                        // If an app was opened but has 0 foreground time (e.g., quickly closed), still record the open.
                        dailyAppSummaries.add(
                            DailyAppSummary(
                                dateMillis = startOfDayMillis,
                                packageName = packageName,
                                totalDurationMillis = 0, // Explicitly set to 0
                                openCount = openCount
                            )
                        )
                        totalDurationForDay += totalTimeInForeground
                    }
                }

                // Insert daily app summaries
                if (dailyAppSummaries.isNotEmpty()) {
                    repository.insertDailyAppSummaries(dailyAppSummaries)
                    Log.d(TAG, "Inserted ${dailyAppSummaries.size} app summaries for day $i")
                }

                // For screen unlocks, UsageStatsManager doesn't directly provide unlock counts.
                // This would typically come from a BroadcastReceiver listening for ACTION_USER_PRESENT.
                // For historical data, we might need to estimate or leave it as 0 if not tracked historically.
                // For now, we'll insert a placeholder for unlocks.
                repository.insertDailyScreenUnlockSummary(
                    DailyScreenUnlockSummary(
                        dateMillis = startOfDayMillis,
                        unlockCount = 0 // Placeholder, as historical unlocks are not directly available from UsageStatsManager
                    )
                )
                Log.d(TAG, "Inserted unlock summary for day $i")
            }
            Log.d(TAG, "HistoricalDataWorker finished successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in HistoricalDataWorker", e)
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "HistoricalDataWorker"
    }
}