package com.example.screentimetracker

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory // Import for Hilt with WorkManager
import androidx.work.Configuration // Import for WorkManager configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.screentimetracker.workers.DailyAggregationWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider { // Implement Configuration.Provider

    @Inject
    lateinit var workerFactory: HiltWorkerFactory // Inject HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration { // Provide WorkManager configuration
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO) // Optional: for debugging
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        scheduleDailyAggregationWork()
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
