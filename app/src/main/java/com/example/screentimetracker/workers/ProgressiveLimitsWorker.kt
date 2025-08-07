package com.example.screentimetracker.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.screentimetracker.domain.usecases.ProgressiveLimitsUseCase
import com.example.screentimetracker.utils.ui.AppNotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ProgressiveLimitsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val progressiveLimitsUseCase: ProgressiveLimitsUseCase,
    private val notificationManager: AppNotificationManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Process weekly reductions for all active progressive limits
            progressiveLimitsUseCase.processWeeklyReductions()
            
            // Check for newly achieved milestones and show celebrations
            val uncelebratedMilestones = progressiveLimitsUseCase.getUncelebratedMilestones()
            
            uncelebratedMilestones.forEach { milestone ->
                // Show milestone achievement notification
                notificationManager.showMilestoneNotification(
                    milestone.rewardTitle,
                    milestone.rewardDescription
                )
                
                // Mark celebration as shown
                progressiveLimitsUseCase.markCelebrationShown(milestone.id)
            }
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}