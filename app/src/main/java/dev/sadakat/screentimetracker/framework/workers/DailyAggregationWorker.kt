package dev.sadakat.screentimetracker.framework.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.sadakat.screentimetracker.core.domain.usecases.AggregateDailyUsageUseCase
import dev.sadakat.screentimetracker.utils.logger.AppLogger

@HiltWorker
class DailyAggregationWorker @AssistedInject constructor(
    @Assisted  appContext: Context,
    @Assisted   workerParams: WorkerParameters,
    private val aggregateDailyUsageUseCase: AggregateDailyUsageUseCase, // Inject the new use case
    private val appLogger: AppLogger // Inject AppLogger
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "DailyAggregationWorker"
        private const val TAG = "DailyAggregationWorker"
    }

    override suspend fun doWork(): Result {
        appLogger.d(TAG, "Starting daily aggregation work.")
        return try {
            aggregateDailyUsageUseCase() // Execute the use case
            appLogger.d(TAG, "Daily aggregation work finished successfully.")
            Result.success()
        } catch (e: Exception) {
            appLogger.e(TAG, "Error during daily aggregation work", e)
            Result.failure()
        }
    }
}