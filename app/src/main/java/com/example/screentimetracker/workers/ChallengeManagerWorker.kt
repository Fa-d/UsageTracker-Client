package com.example.screentimetracker.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.screentimetracker.domain.usecases.ChallengeManagerUseCase
import com.example.screentimetracker.utils.logger.AppLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ChallengeManagerWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val challengeManager: ChallengeManagerUseCase,
    private val appLogger: AppLogger
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "ChallengeManagerWorker"
        private const val TAG = "ChallengeManagerWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            appLogger.i(TAG, "Running challenge management tasks...")
            
            // Expire old challenges first
            challengeManager.expireOldChallenges()
            
            // Create new weekly challenges if needed
            challengeManager.createWeeklyChallenges()
            
            appLogger.i(TAG, "Challenge management completed successfully")
            Result.success()
        } catch (e: Exception) {
            appLogger.e(TAG, "Challenge management failed", e)
            Result.failure()
        }
    }
}