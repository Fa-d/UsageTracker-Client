package com.example.screentimetracker

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory // Import for Hilt with WorkManager
import androidx.work.Configuration // Import for WorkManager configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.screentimetracker.workers.DailyAggregationWorker
import com.example.screentimetracker.workers.HistoricalDataWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import androidx.core.content.edit

@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider { // Implement Configuration.Provider

    @Inject
    lateinit var workerFactory: HiltWorkerFactory // Inject HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO) // Optional: for debugging
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleDailyAggregationWork()
        enqueueHistoricalDataWorker()
    }

    private fun enqueueHistoricalDataWorker() {
        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isFirstLaunch = sharedPrefs.getBoolean("is_first_launch", true)

        if (isFirstLaunch) {
            val historicalDataWorkRequest = OneTimeWorkRequestBuilder<HistoricalDataWorker>()
                .build()
            WorkManager.getInstance(this).enqueue(historicalDataWorkRequest)
            sharedPrefs.edit { putBoolean("is_first_launch", false) }
            Log.d("MainApplication", "Historical data worker enqueued for first launch.")
        }
    }

    private fun scheduleDailyAggregationWork() {
        val workRequest = PeriodicWorkRequestBuilder<DailyAggregationWorker>(
            repeatInterval = 1, // Repeat once per day
            repeatIntervalTimeUnit = TimeUnit.DAYS
        )
        // .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS) // Optional: delay to run at specific time like midnight
        .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            DailyAggregationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing work if it's already scheduled
            workRequest
        )
        Log.d("MainApplication", "Daily aggregation worker scheduled.")
    }

    // Optional: Helper to calculate delay to run worker around midnight
    // private fun calculateInitialDelay(): Long { ... }
}
