package dev.sadakat.screentimetracker.core.domain.service

import dev.sadakat.screentimetracker.core.domain.model.TimeRange
import java.util.concurrent.TimeUnit

/**
 * Domain service for enforcing usage limits and managing app restrictions.
 * Contains pure business logic for limit calculations and enforcement decisions.
 */
interface LimitEnforcementService {

    /**
     * Determines if an app should be blocked based on current usage and limits
     */
    fun shouldBlockApp(
        packageName: String,
        currentUsage: AppUsageContext,
        limits: List<UsageLimit>
    ): LimitEnforcementResult

    /**
     * Calculates remaining time before an app hits its limit
     */
    fun calculateRemainingTime(
        packageName: String,
        currentUsage: AppUsageContext,
        limits: List<UsageLimit>
    ): RemainingTimeInfo

    /**
     * Determines what type of warning should be shown for approaching limits
     */
    fun calculateWarningLevel(
        packageName: String,
        currentUsage: AppUsageContext,
        limits: List<UsageLimit>
    ): WarningLevel

    /**
     * Calculates progressive limit adjustments based on usage patterns
     */
    fun calculateProgressiveLimit(
        packageName: String,
        historicalUsage: List<DailyUsageRecord>,
        currentLimits: List<UsageLimit>,
        progressionStrategy: ProgressionStrategy
    ): ProgressiveLimitRecommendation

    /**
     * Determines if a temporary limit extension should be allowed
     */
    fun shouldAllowExtension(
        packageName: String,
        requestedExtensionMinutes: Int,
        currentUsage: AppUsageContext,
        extensionHistory: List<ExtensionRecord>
    ): ExtensionDecision

    /**
     * Validates if a proposed limit is reasonable and achievable
     */
    fun validateLimitProposal(
        packageName: String,
        proposedLimit: UsageLimit,
        historicalUsage: List<DailyUsageRecord>
    ): LimitValidationResult

    /**
     * Calculates cooldown periods after limit violations
     */
    fun calculateCooldownPeriod(
        packageName: String,
        violationSeverity: ViolationSeverity,
        violationHistory: List<ViolationRecord>
    ): CooldownPeriod

    /**
     * Determines enforcement strategy based on user behavior patterns
     */
    fun determineEnforcementStrategy(
        userProfile: UserBehaviorProfile,
        appCategory: AppCategory,
        timeContext: TimeContext
    ): EnforcementStrategy
}

/**
 * Context information about current app usage
 */
data class AppUsageContext(
    val packageName: String,
    val todayUsageMillis: Long,
    val currentSessionStartTime: Long,
    val currentSessionDurationMillis: Long,
    val unlocksSinceLastUse: Int,
    val timeOfDay: Int, // Hour of day (0-23)
    val dayOfWeek: Int, // 1-7 (Monday = 1)
    val isWeekend: Boolean
)

/**
 * Usage limit definition
 */
data class UsageLimit(
    val id: String,
    val packageName: String,
    val limitType: LimitType,
    val durationMillis: Long,
    val timeRange: TimeRange? = null, // For time-specific limits
    val daysOfWeek: Set<Int> = emptySet(), // Empty means all days
    val priority: LimitPriority = LimitPriority.NORMAL,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

enum class LimitType {
    DAILY_TOTAL,        // Total daily usage limit
    SESSION_DURATION,   // Single session limit
    HOURLY,            // Usage per hour limit
    TIME_WINDOW,       // Usage within specific time window
    BREAK_ENFORCEMENT, // Mandatory break after usage
    BEDTIME_BLOCK     // Complete block during sleep hours
}

enum class LimitPriority {
    LOW,
    NORMAL,
    HIGH,
    STRICT // Cannot be overridden or extended
}

/**
 * Result of limit enforcement check
 */
data class LimitEnforcementResult(
    val shouldBlock: Boolean,
    val reason: String,
    val violatedLimits: List<UsageLimit>,
    val suggestedAction: EnforcementAction,
    val allowedExtensions: List<ExtensionOption> = emptyList(),
    val nextCheckTimeMillis: Long? = null
)

enum class EnforcementAction {
    BLOCK_IMMEDIATELY,
    SHOW_WARNING,
    SUGGEST_BREAK,
    ALLOW_WITH_REMINDER,
    TRACK_ONLY
}

/**
 * Information about remaining time before limits
 */
data class RemainingTimeInfo(
    val packageName: String,
    val remainingMillis: Long,
    val limitType: LimitType,
    val confidence: Float, // How certain we are about this calculation
    val nextMilestone: TimeMilestone? = null
)

data class TimeMilestone(
    val description: String,
    val timeMillis: Long,
    val severity: MilestoneSeverity
)

enum class MilestoneSeverity {
    INFO,
    WARNING,
    CRITICAL
}

/**
 * Warning levels for approaching limits
 */
enum class WarningLevel {
    NONE,
    GENTLE_REMINDER,    // 75% of limit reached
    STRONG_WARNING,     // 90% of limit reached
    FINAL_WARNING,      // 95% of limit reached
    LIMIT_EXCEEDED
}

/**
 * Progressive limit recommendation
 */
data class ProgressiveLimitRecommendation(
    val currentLimitMillis: Long,
    val recommendedLimitMillis: Long,
    val adjustmentType: AdjustmentType,
    val reasoning: String,
    val confidence: Float,
    val implementationTimeline: String
)

enum class AdjustmentType {
    REDUCE_AGGRESSIVE,  // > 25% reduction
    REDUCE_MODERATE,    // 10-25% reduction
    REDUCE_GENTLE,      // < 10% reduction
    MAINTAIN,           // No change
    INCREASE_GENTLE,    // < 10% increase (for too strict limits)
    RESET_TO_BASELINE   // User struggling too much
}

enum class ProgressionStrategy {
    AGGRESSIVE,         // Fast reduction for motivated users
    MODERATE,          // Steady, sustainable reduction
    GENTLE,            // Slow, gradual changes
    ADAPTIVE           // Adjusts based on success rate
}

/**
 * Daily usage record for historical analysis
 */
data class DailyUsageRecord(
    val date: Long,
    val packageName: String,
    val totalUsageMillis: Long,
    val sessionCount: Int,
    val limitViolations: Int,
    val extensionsUsed: Int
)

/**
 * Extension decision result
 */
data class ExtensionDecision(
    val allowed: Boolean,
    val reason: String,
    val maxExtensionMinutes: Int,
    val conditions: List<ExtensionCondition> = emptyList(),
    val costToUser: ExtensionCost? = null
)

data class ExtensionOption(
    val durationMinutes: Int,
    val label: String,
    val cost: ExtensionCost? = null
)

data class ExtensionCondition(
    val type: ConditionType,
    val description: String,
    val required: Boolean
)

enum class ConditionType {
    TAKE_BREAK_FIRST,
    ANSWER_REFLECTION_QUESTION,
    SET_INTENTION,
    LIMIT_FUTURE_SESSIONS,
    COMPLETE_WELLNESS_TASK
}

data class ExtensionCost(
    val type: CostType,
    val description: String,
    val value: Long
)

enum class CostType {
    REDUCED_TOMORROW_LIMIT,
    WELLNESS_SCORE_PENALTY,
    EXTRA_BREAK_TIME,
    REFLECTION_REQUIREMENT
}

/**
 * Extension history record
 */
data class ExtensionRecord(
    val timestamp: Long,
    val packageName: String,
    val extensionMinutes: Int,
    val reason: String,
    val wasCompleted: Boolean
)

/**
 * Limit validation result
 */
data class LimitValidationResult(
    val isValid: Boolean,
    val issues: List<ValidationIssue>,
    val recommendations: List<String>,
    val adjustedLimit: UsageLimit? = null
)

data class ValidationIssue(
    val severity: IssueSeverity,
    val message: String,
    val suggestedFix: String? = null
)

enum class IssueSeverity {
    INFO,
    WARNING,
    ERROR
}

/**
 * Violation records for tracking limit breaches
 */
data class ViolationRecord(
    val timestamp: Long,
    val packageName: String,
    val violationType: ViolationType,
    val exceedsBy: Long,
    val userResponse: UserResponse
)

enum class ViolationType {
    DAILY_LIMIT_EXCEEDED,
    SESSION_TOO_LONG,
    BREAK_SKIPPED,
    BEDTIME_VIOLATION,
    HOURLY_LIMIT_EXCEEDED
}

enum class UserResponse {
    STOPPED_IMMEDIATELY,
    TOOK_EXTENSION,
    IGNORED_WARNING,
    FORCE_CLOSED_APP
}

enum class ViolationSeverity {
    MINOR,      // Slightly over limit
    MODERATE,   // Significantly over limit
    MAJOR,      // Grossly over limit
    SEVERE      // Repeated major violations
}

/**
 * Cooldown period after violations
 */
data class CooldownPeriod(
    val durationMillis: Long,
    val startTime: Long,
    val severity: ViolationSeverity,
    val allowedActions: Set<CooldownAction>
)

enum class CooldownAction {
    VIEW_STATS,
    SET_GOALS,
    TAKE_WELLNESS_QUIZ,
    NOTHING // Complete block
}

/**
 * User behavior profile for enforcement decisions
 */
data class UserBehaviorProfile(
    val motivationLevel: MotivationLevel,
    val selfControlScore: Float, // 0.0 to 1.0
    val preferredEnforcementStyle: EnforcementStyle,
    val responsesToWarnings: Float, // Compliance rate 0.0 to 1.0
    val streakDays: Int,
    val recentViolations: Int
)

enum class MotivationLevel {
    LOW,
    MODERATE,
    HIGH,
    VERY_HIGH
}

enum class EnforcementStyle {
    GENTLE_NUDGES,
    FIRM_BOUNDARIES,
    STRICT_BLOCKING,
    GAMIFIED_CHALLENGES
}

/**
 * App category for context-aware enforcement
 */
enum class AppCategory {
    SOCIAL_MEDIA,
    ENTERTAINMENT,
    PRODUCTIVITY,
    COMMUNICATION,
    GAMES,
    NEWS,
    SHOPPING,
    EDUCATION,
    HEALTH_FITNESS,
    UTILITIES
}

/**
 * Time context for enforcement decisions
 */
data class TimeContext(
    val timeOfDay: TimeOfDay,
    val dayType: DayType,
    val userActivity: UserActivity,
    val environmentContext: EnvironmentContext
)

enum class TimeOfDay {
    EARLY_MORNING,   // 5-8 AM
    MORNING,         // 8-12 PM
    AFTERNOON,       // 12-6 PM
    EVENING,         // 6-10 PM
    LATE_NIGHT      // 10 PM-5 AM
}

enum class DayType {
    WEEKDAY,
    WEEKEND,
    HOLIDAY,
    VACATION
}

enum class UserActivity {
    WORKING,
    COMMUTING,
    EXERCISING,
    EATING,
    RELAXING,
    SLEEPING,
    SOCIALIZING,
    UNKNOWN
}

enum class EnvironmentContext {
    HOME,
    WORK,
    COMMUTE,
    SOCIAL_SETTING,
    GYM,
    OUTDOORS,
    UNKNOWN
}

/**
 * Enforcement strategy result
 */
data class EnforcementStrategy(
    val primaryAction: EnforcementAction,
    val warningStyle: WarningStyle,
    val allowedExtensions: Boolean,
    val cooldownBehavior: CooldownBehavior,
    val escalationPath: List<EnforcementAction>
)

enum class WarningStyle {
    MINIMAL,          // Just a small notification
    INFORMATIVE,      // Show stats and progress
    MOTIVATIONAL,     // Encourage better habits
    FIRM,            // Clear consequences
    HUMOROUS         // Light-hearted approach
}

enum class CooldownBehavior {
    IMMEDIATE_BLOCK,
    GRADUATED_WARNINGS,
    REFLECTION_REQUIRED,
    ALTERNATIVE_ACTIVITIES
}