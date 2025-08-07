package com.example.screentimetracker.utils.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.screentimetracker.R
import com.example.screentimetracker.domain.model.Achievement
import com.example.screentimetracker.domain.model.LimitedApp
import com.example.screentimetracker.ui.MainActivity
import com.example.screentimetracker.utils.logger.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

interface AppNotificationManager {
    fun showWarningNotification(limitedApp: LimitedApp, continuousDurationMillis: Long)
    fun showTimeWarning(appName: String, minutesLeft: Int, limitMinutes: Int)
    fun showBreakReminder(message: String = "Take a break! You've been active for a while.")
    fun showAchievementUnlocked(achievement: Achievement)
    fun showMotivationBoost(message: String)
    fun showWeeklyReport(totalScreenTime: Long, goalsAchieved: Int, totalGoals: Int)
    fun showFocusSessionStart(durationMinutes: Int)
    fun showFocusSessionComplete(durationMinutes: Int, success: Boolean)
    fun showMilestoneNotification(title: String, description: String)
}

@Singleton
class AppNotificationManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appLogger: AppLogger
) : AppNotificationManager {

    companion object {
        const val WARNING_NOTIFICATION_CHANNEL_ID = "AppUsageWarningChannel"
        const val TIME_WARNING_CHANNEL_ID = "TimeWarningChannel"
        const val BREAK_REMINDER_CHANNEL_ID = "BreakReminderChannel"
        const val ACHIEVEMENT_CHANNEL_ID = "AchievementChannel"
        const val MOTIVATION_CHANNEL_ID = "MotivationChannel"
        const val WEEKLY_REPORT_CHANNEL_ID = "WeeklyReportChannel"
        const val FOCUS_SESSION_CHANNEL_ID = "FocusSessionChannel"
        const val MILESTONE_CHANNEL_ID = "MilestoneChannel"
        
        private const val WARNING_NOTIFICATION_ID_BASE = 1000
        private const val TIME_WARNING_ID = 2000
        private const val BREAK_REMINDER_ID = 3000
        private const val ACHIEVEMENT_ID = 4000
        private const val MOTIVATION_ID = 5000
        private const val WEEKLY_REPORT_ID = 6000
        private const val FOCUS_SESSION_ID = 7000
        private const val MILESTONE_ID = 8000
        private const val TAG = "AppNotificationManager"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val warningChannel = NotificationChannel(
            WARNING_NOTIFICATION_CHANNEL_ID, "App Usage Limit Warnings", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for when app usage limits are reached."
            enableVibration(true)
            lightColor = Color.RED
        }
        
        val timeWarningChannel = NotificationChannel(
            TIME_WARNING_CHANNEL_ID, "Time Warnings", NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for time warnings before limits."
            enableVibration(false)
            lightColor = Color.YELLOW
        }
        
        val breakReminderChannel = NotificationChannel(
            BREAK_REMINDER_CHANNEL_ID, "Break Reminders", NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Gentle reminders to take breaks."
            enableVibration(false)
            lightColor = Color.BLUE
        }
        
        val achievementChannel = NotificationChannel(
            ACHIEVEMENT_CHANNEL_ID, "Achievement Unlocks", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Celebrations for unlocked achievements."
            enableVibration(true)
            lightColor = Color.GREEN
        }
        
        val motivationChannel = NotificationChannel(
            MOTIVATION_CHANNEL_ID, "Motivation Messages", NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Encouraging messages and motivation boosts."
            enableVibration(false)
            lightColor = Color.MAGENTA
        }
        
        val weeklyReportChannel = NotificationChannel(
            WEEKLY_REPORT_CHANNEL_ID, "Weekly Reports", NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Weekly summary reports and insights."
            enableVibration(false)
            lightColor = Color.CYAN
        }
        
        val focusSessionChannel = NotificationChannel(
            FOCUS_SESSION_CHANNEL_ID, "Focus Sessions", NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Focus session start and completion notifications."
            enableVibration(false)
            lightColor = Color.BLUE
        }
        
        val milestoneChannel = NotificationChannel(
            MILESTONE_CHANNEL_ID, "Progressive Milestones", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Milestone achievements for progressive limits."
            enableVibration(true)
            lightColor = Color.MAGENTA
        }
        
        manager.createNotificationChannels(listOf(
            warningChannel, timeWarningChannel, breakReminderChannel, 
            achievementChannel, motivationChannel, weeklyReportChannel, 
            focusSessionChannel, milestoneChannel
        ))
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

    override fun showTimeWarning(appName: String, minutesLeft: Int, limitMinutes: Int) {
        val notificationManager = NotificationManagerCompat.from(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        
        val warningMessage = when {
            minutesLeft <= 1 -> "Only 1 minute left!"
            minutesLeft <= 5 -> "$minutesLeft minutes left"
            else -> "$minutesLeft minutes until limit"
        }
        
        val notification = NotificationCompat.Builder(context, TIME_WARNING_CHANNEL_ID)
            .setContentTitle("⏰ Time Warning - $appName")
            .setContentText(warningMessage)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        try { notificationManager.notify(TIME_WARNING_ID, notification) } 
        catch (e: SecurityException) { appLogger.e(TAG, "Missing POST_NOTIFICATIONS permission for time warning.", e) }
        appLogger.i(TAG, "Time warning notification shown for $appName: $minutesLeft minutes left.")
    }

    override fun showBreakReminder(message: String) {
        val notificationManager = NotificationManagerCompat.from(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        
        val notification = NotificationCompat.Builder(context, BREAK_REMINDER_CHANNEL_ID)
            .setContentTitle("🧘 Take a Break")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        try { notificationManager.notify(BREAK_REMINDER_ID, notification) } 
        catch (e: SecurityException) { appLogger.e(TAG, "Missing POST_NOTIFICATIONS permission for break reminder.", e) }
        appLogger.i(TAG, "Break reminder notification shown.")
    }

    override fun showAchievementUnlocked(achievement: Achievement) {
        val notificationManager = NotificationManagerCompat.from(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        
        val notification = NotificationCompat.Builder(context, ACHIEVEMENT_CHANNEL_ID)
            .setContentTitle("🏆 Achievement Unlocked!")
            .setContentText("${achievement.name} - ${achievement.description}")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 300, 100, 300))
            .build()
        
        try { notificationManager.notify(ACHIEVEMENT_ID, notification) } 
        catch (e: SecurityException) { appLogger.e(TAG, "Missing POST_NOTIFICATIONS permission for achievement.", e) }
        appLogger.i(TAG, "Achievement unlock notification shown for: ${achievement.name}")
    }

    override fun showMotivationBoost(message: String) {
        val notificationManager = NotificationManagerCompat.from(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        
        val notification = NotificationCompat.Builder(context, MOTIVATION_CHANNEL_ID)
            .setContentTitle("💪 Stay Strong!")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        try { notificationManager.notify(MOTIVATION_ID, notification) } 
        catch (e: SecurityException) { appLogger.e(TAG, "Missing POST_NOTIFICATIONS permission for motivation.", e) }
        appLogger.i(TAG, "Motivation boost notification shown.")
    }

    override fun showWeeklyReport(totalScreenTime: Long, goalsAchieved: Int, totalGoals: Int) {
        val notificationManager = NotificationManagerCompat.from(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        
        val screenTimeHours = TimeUnit.MILLISECONDS.toHours(totalScreenTime)
        val goalPercentage = if (totalGoals > 0) (goalsAchieved * 100) / totalGoals else 0
        
        val notification = NotificationCompat.Builder(context, WEEKLY_REPORT_CHANNEL_ID)
            .setContentTitle("📊 Weekly Summary")
            .setContentText("${screenTimeHours}h screen time • $goalPercentage% goals achieved")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        try { notificationManager.notify(WEEKLY_REPORT_ID, notification) } 
        catch (e: SecurityException) { appLogger.e(TAG, "Missing POST_NOTIFICATIONS permission for weekly report.", e) }
        appLogger.i(TAG, "Weekly report notification shown.")
    }

    override fun showFocusSessionStart(durationMinutes: Int) {
        val notificationManager = NotificationManagerCompat.from(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        
        val notification = NotificationCompat.Builder(context, FOCUS_SESSION_CHANNEL_ID)
            .setContentTitle("🎯 Focus Session Started")
            .setContentText("$durationMinutes minute focus session is now active")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
        
        try { notificationManager.notify(FOCUS_SESSION_ID, notification) } 
        catch (e: SecurityException) { appLogger.e(TAG, "Missing POST_NOTIFICATIONS permission for focus session.", e) }
        appLogger.i(TAG, "Focus session start notification shown.")
    }

    override fun showFocusSessionComplete(durationMinutes: Int, success: Boolean) {
        val notificationManager = NotificationManagerCompat.from(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        
        val title = if (success) "🎉 Focus Session Complete!" else "⏰ Focus Session Ended"
        val message = if (success) {
            "Great job! You completed $durationMinutes minutes of focused time."
        } else {
            "Focus session ended after $durationMinutes minutes."
        }
        
        val notification = NotificationCompat.Builder(context, FOCUS_SESSION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .apply { if (success) setVibrate(longArrayOf(0, 200, 100, 200)) }
            .build()
        
        try { notificationManager.notify(FOCUS_SESSION_ID + 1, notification) } 
        catch (e: SecurityException) { appLogger.e(TAG, "Missing POST_NOTIFICATIONS permission for focus session complete.", e) }
        appLogger.i(TAG, "Focus session complete notification shown. Success: $success")
    }

    override fun showMilestoneNotification(title: String, description: String) {
        val notificationManager = NotificationManagerCompat.from(context)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        
        val notification = NotificationCompat.Builder(context, MILESTONE_CHANNEL_ID)
            .setContentTitle("🎯 $title")
            .setContentText(description)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 400, 200, 400, 200, 400))
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .build()
        
        try { notificationManager.notify(MILESTONE_ID, notification) } 
        catch (e: SecurityException) { appLogger.e(TAG, "Missing POST_NOTIFICATIONS permission for milestone.", e) }
        appLogger.i(TAG, "Milestone notification shown: $title")
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