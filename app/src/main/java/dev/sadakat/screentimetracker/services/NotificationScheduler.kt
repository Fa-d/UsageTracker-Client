package dev.sadakat.screentimetracker.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.*
import dev.sadakat.screentimetracker.domain.usecases.WeeklyInsightsUseCase
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appLogger: AppLogger
) {
    companion object {
        private const val TAG = "NotificationScheduler"
        private const val WEEKLY_REPORT_WORK_NAME = "weekly_report_notification"
    }

    fun scheduleWeeklyReportNotification() {
        try {
            // Cancel any existing work
            WorkManager.getInstance(context).cancelUniqueWork(WEEKLY_REPORT_WORK_NAME)

            // Calculate next Sunday at 9 AM
            val nextSunday = getNextSundayAt9AM()
            val delay = nextSunday - System.currentTimeMillis()

            val weeklyReportRequest = OneTimeWorkRequestBuilder<WeeklyReportWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WEEKLY_REPORT_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                weeklyReportRequest
            )

            appLogger.i(TAG, "Weekly report notification scheduled for: ${Date(nextSunday)}")
            
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to schedule weekly report notification", e)
        }
    }

    fun schedulePeriodicWeeklyReports() {
        try {
            // Cancel any existing periodic work
            WorkManager.getInstance(context).cancelUniqueWork("${WEEKLY_REPORT_WORK_NAME}_periodic")

            val weeklyReportRequest = PeriodicWorkRequestBuilder<WeeklyReportWorker>(
                7, TimeUnit.DAYS, // Repeat every 7 days
                1, TimeUnit.HOURS  // Flex interval of 1 hour
            )
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "${WEEKLY_REPORT_WORK_NAME}_periodic",
                ExistingPeriodicWorkPolicy.REPLACE,
                weeklyReportRequest
            )

            appLogger.i(TAG, "Periodic weekly report notifications scheduled")
            
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to schedule periodic weekly report notifications", e)
        }
    }

    fun cancelWeeklyReportNotifications() {
        try {
            WorkManager.getInstance(context).cancelUniqueWork(WEEKLY_REPORT_WORK_NAME)
            WorkManager.getInstance(context).cancelUniqueWork("${WEEKLY_REPORT_WORK_NAME}_periodic")
            appLogger.i(TAG, "Weekly report notifications cancelled")
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to cancel weekly report notifications", e)
        }
    }

    private fun getNextSundayAt9AM(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // If it's already past 9 AM on Sunday, schedule for next Sunday
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }
        return calendar.timeInMillis
    }

    private fun calculateInitialDelay(): Long {
        val nextSunday = getNextSundayAt9AM()
        return nextSunday - System.currentTimeMillis()
    }
}

class WeeklyReportWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val weeklyInsightsUseCase: WeeklyInsightsUseCase,
    private val appLogger: AppLogger
) : CoroutineWorker(context, params) {

    @AssistedFactory
    interface Factory {
        fun create(context: Context, params: WorkerParameters): WeeklyReportWorker
    }

    companion object {
        private const val TAG = "WeeklyReportWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            appLogger.i(TAG, "Starting weekly report generation and notification")
            
            // Generate and send the weekly report notification
            weeklyInsightsUseCase.sendWeeklyReportNotification()
            
            appLogger.i(TAG, "Weekly report notification sent successfully")
            Result.success()
            
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to send weekly report notification", e)
            
            // Retry up to 3 times with exponential backoff
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}