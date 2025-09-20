package dev.sadakat.screentimetracker.core.domain.service

import dev.sadakat.screentimetracker.core.domain.model.Achievement
import dev.sadakat.screentimetracker.core.domain.model.WellnessScore
import dev.sadakat.screentimetracker.core.domain.model.TimeRange
import java.util.concurrent.TimeUnit

/**
 * Domain service for determining notification strategies and content.
 * Contains pure business logic for when and what notifications should be shown.
 */
interface NotificationStrategyService {

    /**
     * Determines if a limit warning should be shown based on app usage
     */
    fun shouldShowLimitWarning(
        usageDurationMillis: Long,
        limitDurationMillis: Long,
        packageName: String
    ): Boolean

    /**
     * Calculates the warning message for limit notifications
     */
    fun calculateLimitWarningMessage(
        appName: String,
        usageDurationMillis: Long,
        limitDurationMillis: Long
    ): NotificationContent

    /**
     * Determines if a time warning should be shown before limit is reached
     */
    fun shouldShowTimeWarning(
        usageDurationMillis: Long,
        limitDurationMillis: Long,
        lastWarningTime: Long?
    ): Boolean

    /**
     * Calculates time warning message
     */
    fun calculateTimeWarningMessage(
        appName: String,
        minutesLeft: Int,
        limitMinutes: Int
    ): NotificationContent

    /**
     * Determines if a break reminder should be shown
     */
    fun shouldShowBreakReminder(
        continuousUsageMillis: Long,
        lastBreakTime: Long?
    ): Boolean

    /**
     * Determines if an achievement notification should be shown
     */
    fun shouldShowAchievementNotification(achievement: Achievement): Boolean

    /**
     * Calculates achievement notification content
     */
    fun calculateAchievementContent(achievement: Achievement): NotificationContent

    /**
     * Determines if a motivation boost should be shown
     */
    fun shouldShowMotivationBoost(wellnessScore: WellnessScore): Boolean

    /**
     * Calculates motivation message based on wellness score
     */
    fun calculateMotivationMessage(wellnessScore: WellnessScore): NotificationContent

    /**
     * Determines if weekly report should be shown
     */
    fun shouldShowWeeklyReport(dayOfWeek: Int, lastReportTime: Long?): Boolean

    /**
     * Calculates weekly report content
     */
    fun calculateWeeklyReportContent(
        totalScreenTime: Long,
        goalsAchieved: Int,
        totalGoals: Int
    ): NotificationContent

    /**
     * Determines if focus session notification should be shown
     */
    fun shouldShowFocusSessionNotification(sessionState: FocusSessionState): Boolean

    /**
     * Calculates focus session notification content
     */
    fun calculateFocusSessionContent(
        durationMinutes: Int,
        isStart: Boolean,
        success: Boolean? = null
    ): NotificationContent
}

/**
 * Represents the content of a notification with business logic applied
 */
data class NotificationContent(
    val title: String,
    val message: String,
    val priority: NotificationPriority,
    val channelType: NotificationChannelType,
    val actionButtons: List<NotificationAction> = emptyList()
)

/**
 * Priority levels for notifications based on business importance
 */
enum class NotificationPriority {
    LOW,
    DEFAULT,
    HIGH,
    URGENT
}

/**
 * Types of notification channels for different purposes
 */
enum class NotificationChannelType {
    USAGE_WARNING,
    TIME_WARNING,
    BREAK_REMINDER,
    ACHIEVEMENT,
    MOTIVATION,
    WEEKLY_REPORT,
    FOCUS_SESSION,
    MILESTONE,
    CONTENT_BLOCKED,
    GOAL_WARNING
}

/**
 * Action buttons that can be added to notifications
 */
data class NotificationAction(
    val label: String,
    val actionType: NotificationActionType
)

enum class NotificationActionType {
    OPEN_APP,
    EXTEND_LIMIT,
    TAKE_BREAK,
    VIEW_PROGRESS,
    DISMISS
}

/**
 * State of focus session for notification decisions
 */
enum class FocusSessionState {
    STARTING,
    ACTIVE,
    COMPLETED_SUCCESS,
    COMPLETED_INTERRUPTED,
    PAUSED
}