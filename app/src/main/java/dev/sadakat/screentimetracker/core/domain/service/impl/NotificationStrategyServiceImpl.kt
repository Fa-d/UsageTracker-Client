package dev.sadakat.screentimetracker.core.domain.service.impl

import dev.sadakat.screentimetracker.core.domain.model.Achievement
import dev.sadakat.screentimetracker.core.domain.model.WellnessScore
import dev.sadakat.screentimetracker.core.domain.service.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Implementation of notification strategy with pure business logic.
 * No Android framework dependencies - only domain business rules.
 */
class NotificationStrategyServiceImpl : NotificationStrategyService {

    companion object {
        private const val BREAK_REMINDER_INTERVAL_MILLIS = 60 * 60 * 1000L // 1 hour
        private const val TIME_WARNING_THRESHOLD_MINUTES = 5 // Show warning 5 minutes before limit
        private const val MIN_TIME_BETWEEN_WARNINGS_MILLIS = 15 * 60 * 1000L // 15 minutes
        private const val WEEKLY_REPORT_DAY = Calendar.SUNDAY
        private const val MIN_TIME_BETWEEN_REPORTS_MILLIS = 6 * 24 * 60 * 60 * 1000L // 6 days
        private const val MOTIVATION_BOOST_THRESHOLD = 50 // Show motivation when score < 50
    }

    override fun shouldShowLimitWarning(
        usageDurationMillis: Long,
        limitDurationMillis: Long,
        packageName: String
    ): Boolean {
        return usageDurationMillis >= limitDurationMillis
    }

    override fun calculateLimitWarningMessage(
        appName: String,
        usageDurationMillis: Long,
        limitDurationMillis: Long
    ): NotificationContent {
        val usageMinutes = TimeUnit.MILLISECONDS.toMinutes(usageDurationMillis)
        val limitMinutes = TimeUnit.MILLISECONDS.toMinutes(limitDurationMillis)
        val excessMinutes = usageMinutes - limitMinutes

        return NotificationContent(
            title = "Usage Limit Reached",
            message = "$appName used for $usageMinutes minutes (Limit: $limitMinutes min). Consider a break.",
            priority = NotificationPriority.HIGH,
            channelType = NotificationChannelType.USAGE_WARNING,
            actionButtons = listOf(
                NotificationAction("Take Break", NotificationActionType.TAKE_BREAK),
                NotificationAction("Extend (+15 min)", NotificationActionType.EXTEND_LIMIT)
            )
        )
    }

    override fun shouldShowTimeWarning(
        usageDurationMillis: Long,
        limitDurationMillis: Long,
        lastWarningTime: Long?
    ): Boolean {
        val usageMinutes = TimeUnit.MILLISECONDS.toMinutes(usageDurationMillis)
        val limitMinutes = TimeUnit.MILLISECONDS.toMinutes(limitDurationMillis)
        val minutesLeft = limitMinutes - usageMinutes

        // Show warning when approaching limit
        if (minutesLeft > TIME_WARNING_THRESHOLD_MINUTES) {
            return false
        }

        // Don't show if we've shown a warning recently
        lastWarningTime?.let { lastTime ->
            val timeSinceLastWarning = System.currentTimeMillis() - lastTime
            if (timeSinceLastWarning < MIN_TIME_BETWEEN_WARNINGS_MILLIS) {
                return false
            }
        }

        return minutesLeft > 0 && minutesLeft <= TIME_WARNING_THRESHOLD_MINUTES
    }

    override fun calculateTimeWarningMessage(
        appName: String,
        minutesLeft: Int,
        limitMinutes: Int
    ): NotificationContent {
        return NotificationContent(
            title = "Approaching Limit",
            message = "$appName: $minutesLeft minutes remaining of $limitMinutes minute limit.",
            priority = NotificationPriority.DEFAULT,
            channelType = NotificationChannelType.TIME_WARNING,
            actionButtons = listOf(
                NotificationAction("View Progress", NotificationActionType.VIEW_PROGRESS)
            )
        )
    }

    override fun shouldShowBreakReminder(
        continuousUsageMillis: Long,
        lastBreakTime: Long?
    ): Boolean {
        // Show break reminder after 1 hour of continuous usage
        if (continuousUsageMillis < BREAK_REMINDER_INTERVAL_MILLIS) {
            return false
        }

        // Don't show if user took a break recently
        lastBreakTime?.let { lastBreak ->
            val timeSinceBreak = System.currentTimeMillis() - lastBreak
            if (timeSinceBreak < BREAK_REMINDER_INTERVAL_MILLIS) {
                return false
            }
        }

        return true
    }

    override fun shouldShowAchievementNotification(achievement: Achievement): Boolean {
        return achievement.isUnlocked && achievement.unlockedAt != null
    }

    override fun calculateAchievementContent(achievement: Achievement): NotificationContent {
        return NotificationContent(
            title = "🏆 Achievement Unlocked!",
            message = "${achievement.emoji} ${achievement.name}: ${achievement.description}",
            priority = NotificationPriority.HIGH,
            channelType = NotificationChannelType.ACHIEVEMENT,
            actionButtons = listOf(
                NotificationAction("View All", NotificationActionType.VIEW_PROGRESS)
            )
        )
    }

    override fun shouldShowMotivationBoost(wellnessScore: WellnessScore): Boolean {
        return wellnessScore.overall < MOTIVATION_BOOST_THRESHOLD
    }

    override fun calculateMotivationMessage(wellnessScore: WellnessScore): NotificationContent {
        val message = when {
            wellnessScore.overall < 30 -> "Your digital wellness needs attention. Small steps lead to big changes! 🌱"
            wellnessScore.overall < 50 -> "Keep going! Your wellness score is improving. Every mindful choice counts! 💪"
            else -> "Great job maintaining healthy digital habits! You're on the right track! ⭐"
        }

        return NotificationContent(
            title = "Wellness Check-in",
            message = message,
            priority = NotificationPriority.LOW,
            channelType = NotificationChannelType.MOTIVATION,
            actionButtons = listOf(
                NotificationAction("View Tips", NotificationActionType.VIEW_PROGRESS)
            )
        )
    }

    override fun shouldShowWeeklyReport(dayOfWeek: Int, lastReportTime: Long?): Boolean {
        if (dayOfWeek != WEEKLY_REPORT_DAY) {
            return false
        }

        lastReportTime?.let { lastTime ->
            val timeSinceLastReport = System.currentTimeMillis() - lastTime
            if (timeSinceLastReport < MIN_TIME_BETWEEN_REPORTS_MILLIS) {
                return false
            }
        }

        return true
    }

    override fun calculateWeeklyReportContent(
        totalScreenTime: Long,
        goalsAchieved: Int,
        totalGoals: Int
    ): NotificationContent {
        val screenTimeHours = TimeUnit.MILLISECONDS.toHours(totalScreenTime)
        val achievementRate = if (totalGoals > 0) (goalsAchieved * 100) / totalGoals else 0

        val title = "📊 Weekly Report Ready"
        val message = when {
            achievementRate >= 80 -> "Excellent week! $screenTimeHours hours screen time, $goalsAchieved/$totalGoals goals achieved! 🎉"
            achievementRate >= 60 -> "Good progress! $screenTimeHours hours screen time, $goalsAchieved/$totalGoals goals achieved. 👍"
            achievementRate >= 40 -> "Making progress! $screenTimeHours hours screen time, $goalsAchieved/$totalGoals goals achieved. Keep it up! 📈"
            else -> "This week: $screenTimeHours hours screen time, $goalsAchieved/$totalGoals goals achieved. Let's improve next week! 💪"
        }

        return NotificationContent(
            title = title,
            message = message,
            priority = NotificationPriority.DEFAULT,
            channelType = NotificationChannelType.WEEKLY_REPORT,
            actionButtons = listOf(
                NotificationAction("View Report", NotificationActionType.VIEW_PROGRESS)
            )
        )
    }

    override fun shouldShowFocusSessionNotification(sessionState: FocusSessionState): Boolean {
        return when (sessionState) {
            FocusSessionState.STARTING -> true
            FocusSessionState.COMPLETED_SUCCESS -> true
            FocusSessionState.COMPLETED_INTERRUPTED -> true
            FocusSessionState.ACTIVE -> false // Don't interrupt active sessions
            FocusSessionState.PAUSED -> false
        }
    }

    override fun calculateFocusSessionContent(
        durationMinutes: Int,
        isStart: Boolean,
        success: Boolean?
    ): NotificationContent {
        return when {
            isStart -> NotificationContent(
                title = "🎯 Focus Session Started",
                message = "Focus mode active for $durationMinutes minutes. Stay concentrated!",
                priority = NotificationPriority.DEFAULT,
                channelType = NotificationChannelType.FOCUS_SESSION
            )
            success == true -> NotificationContent(
                title = "✅ Focus Session Complete",
                message = "Great job! You completed your $durationMinutes minute focus session.",
                priority = NotificationPriority.HIGH,
                channelType = NotificationChannelType.FOCUS_SESSION,
                actionButtons = listOf(
                    NotificationAction("View Progress", NotificationActionType.VIEW_PROGRESS)
                )
            )
            success == false -> NotificationContent(
                title = "⏸️ Focus Session Interrupted",
                message = "Session ended early. Don't worry, every attempt builds focus strength!",
                priority = NotificationPriority.DEFAULT,
                channelType = NotificationChannelType.FOCUS_SESSION,
                actionButtons = listOf(
                    NotificationAction("Try Again", NotificationActionType.VIEW_PROGRESS)
                )
            )
            else -> NotificationContent(
                title = "Focus Session",
                message = "Session update",
                priority = NotificationPriority.DEFAULT,
                channelType = NotificationChannelType.FOCUS_SESSION
            )
        }
    }
}