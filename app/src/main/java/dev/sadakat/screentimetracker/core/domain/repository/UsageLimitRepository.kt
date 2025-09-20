package dev.sadakat.screentimetracker.core.domain.repository

import dev.sadakat.screentimetracker.core.domain.service.*
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository for managing usage limits and enforcement.
 * Contains pure domain operations for limit management without framework dependencies.
 */
interface UsageLimitRepository {

    /**
     * Creates a new usage limit
     */
    suspend fun createLimit(limit: UsageLimit): String

    /**
     * Updates an existing usage limit
     */
    suspend fun updateLimit(limit: UsageLimit)

    /**
     * Deletes a usage limit
     */
    suspend fun deleteLimit(limitId: String)

    /**
     * Gets all active limits for a package
     */
    suspend fun getLimitsForPackage(packageName: String): List<UsageLimit>

    /**
     * Gets all active limits
     */
    suspend fun getAllActiveLimits(): List<UsageLimit>

    /**
     * Observes limits for a specific package
     */
    fun observeLimitsForPackage(packageName: String): Flow<List<UsageLimit>>

    /**
     * Observes all active limits
     */
    fun observeAllActiveLimits(): Flow<List<UsageLimit>>

    /**
     * Gets limits that are currently being violated
     */
    suspend fun getViolatedLimits(): List<LimitViolationInfo>

    /**
     * Records a limit violation
     */
    suspend fun recordViolation(violation: ViolationRecord)

    /**
     * Gets violation history for a package
     */
    suspend fun getViolationHistory(
        packageName: String,
        days: Int = 30
    ): List<ViolationRecord>

    /**
     * Gets all violation history
     */
    suspend fun getAllViolationHistory(days: Int = 30): List<ViolationRecord>

    /**
     * Records a limit extension request
     */
    suspend fun recordExtension(extension: ExtensionRecord)

    /**
     * Gets extension history for analysis
     */
    suspend fun getExtensionHistory(
        packageName: String,
        days: Int = 30
    ): List<ExtensionRecord>

    /**
     * Gets daily usage records for limit analysis
     */
    suspend fun getDailyUsageRecords(
        packageName: String,
        days: Int = 30
    ): List<DailyUsageRecord>

    /**
     * Saves daily usage record
     */
    suspend fun saveDailyUsageRecord(record: DailyUsageRecord)

    /**
     * Gets cooldown periods for packages
     */
    suspend fun getActiveCooldowns(): List<PackageCooldown>

    /**
     * Sets a cooldown period for a package
     */
    suspend fun setCooldown(packageName: String, cooldown: CooldownPeriod)

    /**
     * Clears cooldown for a package
     */
    suspend fun clearCooldown(packageName: String)

    /**
     * Gets progressive limit recommendations
     */
    suspend fun getProgressiveLimitRecommendations(
        packageName: String
    ): List<ProgressiveLimitRecommendation>

    /**
     * Saves progressive limit recommendation
     */
    suspend fun saveProgressiveLimitRecommendation(
        packageName: String,
        recommendation: ProgressiveLimitRecommendation
    )

    /**
     * Gets limit validation results for analysis
     */
    suspend fun getLimitValidationHistory(
        packageName: String,
        days: Int = 30
    ): List<TimestampedValidationResult>

    /**
     * Saves limit validation result
     */
    suspend fun saveLimitValidationResult(
        packageName: String,
        result: LimitValidationResult
    )

    /**
     * Gets enforcement strategy history
     */
    suspend fun getEnforcementStrategyHistory(
        packageName: String,
        days: Int = 30
    ): List<TimestampedEnforcementStrategy>

    /**
     * Saves enforcement strategy decision
     */
    suspend fun saveEnforcementStrategy(
        packageName: String,
        strategy: EnforcementStrategy,
        context: TimeContext
    )

    /**
     * Gets limit effectiveness metrics
     */
    suspend fun getLimitEffectivenessMetrics(): LimitEffectivenessMetrics

    /**
     * Updates limit effectiveness based on user behavior
     */
    suspend fun updateLimitEffectiveness(
        limitId: String,
        effectiveness: LimitEffectiveness
    )

    /**
     * Gets smart limit suggestions based on usage patterns
     */
    suspend fun getSmartLimitSuggestions(
        packageName: String,
        historicalUsage: List<DailyUsageRecord>
    ): List<SmartLimitSuggestion>

    /**
     * Bulk creates limits from templates
     */
    suspend fun createLimitsFromTemplate(template: LimitTemplate): List<String>

    /**
     * Gets limit templates for quick setup
     */
    suspend fun getLimitTemplates(): List<LimitTemplate>

    /**
     * Archives old limits and related data
     */
    suspend fun archiveOldData(retentionDays: Int = 365)
}

/**
 * Information about a currently violated limit
 */
data class LimitViolationInfo(
    val limit: UsageLimit,
    val currentUsage: Long,
    val violationSeverity: ViolationSeverity,
    val violationStartTime: Long,
    val suggestedAction: EnforcementAction
)

/**
 * Package with active cooldown period
 */
data class PackageCooldown(
    val packageName: String,
    val cooldown: CooldownPeriod,
    val reason: String
)

/**
 * Timestamped validation result for historical analysis
 */
data class TimestampedValidationResult(
    val result: LimitValidationResult,
    val validatedAt: Long,
    val proposedLimit: UsageLimit
)

/**
 * Timestamped enforcement strategy for pattern analysis
 */
data class TimestampedEnforcementStrategy(
    val strategy: EnforcementStrategy,
    val appliedAt: Long,
    val context: TimeContext,
    val effectiveness: Float? = null // Set later based on user response
)

/**
 * Metrics about limit effectiveness across the app
 */
data class LimitEffectivenessMetrics(
    val totalLimitsCreated: Int,
    val activeLimits: Int,
    val averageComplianceRate: Float,
    val mostEffectiveLimitType: LimitType?,
    val leastEffectiveLimitType: LimitType?,
    val averageLimitDuration: Long,
    val userSatisfactionScore: Float,
    val limitAdjustmentFrequency: Float
)

/**
 * Effectiveness tracking for individual limits
 */
data class LimitEffectiveness(
    val limitId: String,
    val complianceRate: Float, // 0.0 to 1.0
    val userSatisfaction: Float, // 1.0 to 5.0
    val adjustmentCount: Int,
    val violationCount: Int,
    val lastViolationTime: Long?,
    val effectivenessScore: Float, // Computed metric
    val notes: String? = null
)

/**
 * AI-generated smart limit suggestions
 */
data class SmartLimitSuggestion(
    val suggestedLimit: UsageLimit,
    val reasoning: String,
    val confidence: Float,
    val basedOnPatterns: List<String>,
    val expectedEffectiveness: Float,
    val alternativeOptions: List<UsageLimit> = emptyList()
)

/**
 * Template for creating multiple related limits
 */
data class LimitTemplate(
    val id: String,
    val name: String,
    val description: String,
    val category: LimitTemplateCategory,
    val limits: List<UsageLimitTemplate>,
    val targetUserType: TargetUserType,
    val difficulty: RecommendationDifficulty
)

/**
 * Template for creating individual limits
 */
data class UsageLimitTemplate(
    val limitType: LimitType,
    val durationMillis: Long,
    val priority: LimitPriority,
    val applicableCategories: Set<AppCategory>,
    val timeRange: dev.sadakat.screentimetracker.core.domain.model.TimeRange? = null,
    val daysOfWeek: Set<Int> = emptySet()
)

enum class LimitTemplateCategory {
    BEGINNER_FRIENDLY,
    WORK_FOCUS,
    STUDY_MODE,
    DIGITAL_DETOX,
    FAMILY_TIME,
    SLEEP_HYGIENE,
    WEEKEND_BALANCE,
    PRODUCTIVITY_BOOST
}

enum class TargetUserType {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    PARENT,
    STUDENT,
    PROFESSIONAL
}