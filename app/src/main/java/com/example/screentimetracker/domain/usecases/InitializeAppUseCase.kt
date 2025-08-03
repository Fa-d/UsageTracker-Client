package com.example.screentimetracker.domain.usecases

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.screentimetracker.workers.HistoricalDataWorker
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InitializeAppUseCase @Inject constructor(
    private val application: Application,
    private val workManager: WorkManager
) {
    operator fun invoke() {
        val sharedPrefs = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isFirstLaunch = sharedPrefs.getBoolean("is_first_launch", true)

        if (isFirstLaunch) {
            val historicalDataWorkRequest = OneTimeWorkRequestBuilder<HistoricalDataWorker>()
                .build()
            workManager.enqueue(historicalDataWorkRequest)
            sharedPrefs.edit { putBoolean("is_first_launch", false) }
            // Log.d("InitializeAppUseCase", "Historical data worker enqueued for first launch.")
        }
    }
}