package com.example.screentimetracker.workers

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.screentimetracker.services.WeeklyReportWorker
import javax.inject.Inject
import javax.inject.Provider

class WeeklyReportWorkerFactory @Inject constructor(
    private val weeklyReportWorkerFactory: WeeklyReportWorker.Factory
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            WeeklyReportWorker::class.java.name -> {
                weeklyReportWorkerFactory.create(appContext, workerParameters)
            }
            else -> null
        }
    }
}