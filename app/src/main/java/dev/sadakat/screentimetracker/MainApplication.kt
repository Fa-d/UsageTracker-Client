package dev.sadakat.screentimetracker

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider { // Implement Configuration.Provider

    @Inject
    lateinit var workerFactory: HiltWorkerFactory // Inject HiltWorkerFactory



    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(Log.INFO) // Optional: for debugging
            .build()

    override fun onCreate() {
        super.onCreate()

    }

}