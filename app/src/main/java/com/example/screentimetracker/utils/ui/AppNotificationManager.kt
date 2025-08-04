package com.example.screentimetracker.utils.ui

import android.app.Notification
import android.app.NotificationChannel // Added import
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.screentimetracker.R
import com.example.screentimetracker.domain.model.LimitedApp
import com.example.screentimetracker.ui.MainActivity
import com.example.screentimetracker.utils.logger.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

interface AppNotificationManager {
    fun showWarningNotification(limitedApp: LimitedApp, continuousDurationMillis: Long)
}

@Singleton
class AppNotificationManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appLogger: AppLogger
) : AppNotificationManager {

    companion object {
        const val WARNING_NOTIFICATION_CHANNEL_ID = "AppUsageWarningChannel"
        private const val WARNING_NOTIFICATION_ID_BASE = 1000
        private const val TAG = "AppNotificationManager"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val warningChannel = NotificationChannel(
            WARNING_NOTIFICATION_CHANNEL_ID, "App Usage Limit Warnings", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for when app usage limits are reached."
            enableVibration(true)
            lightColor = Color.RED
        }
        manager.createNotificationChannel(warningChannel)
    }

    override fun showWarningNotification(limitedApp: LimitedApp, continuousDurationMillis: Long) {
        val notificationManager = NotificationManagerCompat.from(context)
        val appName = getAppName(limitedApp.packageName)
        val durationMinutes = TimeUnit.MILLISECONDS.toMinutes(continuousDurationMillis)
        val limitMinutes = TimeUnit.MILLISECONDS.toMinutes(limitedApp.timeLimitMillis)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getActivity(context, limitedApp.packageName.hashCode(), intent, pendingIntentFlags)
        val notificationIcon = R.drawable.ic_launcher_foreground
        val notification = NotificationCompat.Builder(context, WARNING_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Usage Limit Reached")
            .setContentText("$appName used for $durationMinutes minutes (Limit: $limitMinutes min). Consider a break.")
            .setSmallIcon(notificationIcon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        val notificationId = WARNING_NOTIFICATION_ID_BASE + limitedApp.packageName.hashCode()
        try { notificationManager.notify(notificationId, notification) } catch (e: SecurityException) { appLogger.e(TAG, "Missing POST_NOTIFICATIONS permission for warning.", e) }
        appLogger.i(TAG, "Usage limit warning notification shown for ${limitedApp.packageName}.")
    }

    private fun getAppName(packageName: String): String {
        return try {
            context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(packageName, 0)).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            appLogger.w(TAG, "App name not found for $packageName", e)
            packageName
        } catch (e: Exception) {
            appLogger.e(TAG, "Error getting app name for $packageName", e)
            packageName
        }
    }
}