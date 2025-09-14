package dev.sadakat.screentimetracker.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.sadakat.screentimetracker.domain.usecases.HabitTrackerUseCase
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class HabitTrackerWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val habitTrackerUseCase: HabitTrackerUseCase,
    private val appLogger: AppLogger
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "HabitTrackerWorker"
        private const val TAG = "HabitTrackerWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            appLogger.i(TAG, "Running automatic habit checking...")
            
            // Initialize today's habits if needed
            habitTrackerUseCase.initializeDigitalWellnessHabits()
            
            // Check and complete habits automatically based on user behavior
            habitTrackerUseCase.checkAndCompleteHabitsAutomatically()
            
            // Check for missed habits from yesterday and update streaks
            habitTrackerUseCase.checkMissedHabits()
            
            appLogger.i(TAG, "Automatic habit checking completed successfully")
            Result.success()
        } catch (e: Exception) {
            appLogger.e(TAG, "Automatic habit checking failed", e)
            Result.failure()
        }
    }
}