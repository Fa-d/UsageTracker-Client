package dev.sadakat.screentimetracker

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dev.sadakat.screentimetracker.domain.usecases.InitializeAppUseCase
import dev.sadakat.screentimetracker.receivers.ScreenUnlockReceiver
import dev.sadakat.screentimetracker.services.NotificationScheduler
import dev.sadakat.screentimetracker.workers.DailyAggregationWorker
import dev.sadakat.screentimetracker.workers.HabitTrackerWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider { // Implement Configuration.Provider

    @Inject
    lateinit var workerFactory: HiltWorkerFactory // Inject HiltWorkerFactory
    
    @Inject
    lateinit var notificationScheduler: NotificationScheduler


    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.INFO) // Optional: for debugging
            .build()

    override fun onCreate() {
        super.onCreate()
        val initializeAppUseCase = InitializeAppUseCase(this, WorkManager.getInstance(this))
        initializeAppUseCase()
        scheduleDailyAggregationWork()
        scheduleHabitTrackerWork()
        scheduleWeeklyReportNotifications()
        ScreenUnlockReceiver.register(this)
    }

    private fun scheduleDailyAggregationWork() {
        val workRequest = PeriodicWorkRequestBuilder<DailyAggregationWorker>(
            1L, // Repeat once per day
            TimeUnit.DAYS
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

    private fun scheduleHabitTrackerWork() {
        val workRequest = PeriodicWorkRequestBuilder<HabitTrackerWorker>(
            1L, // Repeat once per hour
            TimeUnit.HOURS
        )
        .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            HabitTrackerWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing work if it's already scheduled
            workRequest
        )
        Log.d("MainApplication", "Habit tracker worker scheduled (hourly).")
    }

    private fun scheduleWeeklyReportNotifications() {
        notificationScheduler.schedulePeriodicWeeklyReports()
        Log.d("MainApplication", "Weekly report notifications scheduled.")
    }

    // Optional: Helper to calculate delay to run worker around midnight
    // private fun calculateInitialDelay(): Long { ... }
}
