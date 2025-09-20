package dev.sadakat.screentimetracker.core.data.repository

import dev.sadakat.screentimetracker.core.domain.repository.UsageLimitRepository
import dev.sadakat.screentimetracker.core.domain.repository.*
import dev.sadakat.screentimetracker.core.domain.service.*
import dev.sadakat.screentimetracker.data.local.dao.ProgressiveLimitDao
import dev.sadakat.screentimetracker.data.local.entities.ProgressiveLimit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of UsageLimitRepository using ProgressiveLimitDao and in-memory storage.
 * Uses existing ProgressiveLimit entities for basic limit functionality,
 * with in-memory storage for complex domain features not yet in database.
 */
@Singleton
class UsageLimitRepositoryImpl @Inject constructor(
    private val progressiveLimitDao: ProgressiveLimitDao
) : UsageLimitRepository {

    // In-memory storage for complex domain objects not yet in database
    private val violationRecords = ConcurrentHashMap<String, MutableList<ViolationRecord>>()
    private val extensionRecords = ConcurrentHashMap<String, MutableList<ExtensionRecord>>()
    private val dailyUsageRecords = ConcurrentHashMap<String, MutableList<DailyUsageRecord>>()
    private val cooldowns = ConcurrentHashMap<String, CooldownPeriod>()
    private val validationResults = ConcurrentHashMap<String, MutableList<TimestampedValidationResult>>()
    private val enforcementStrategies = ConcurrentHashMap<String, MutableList<TimestampedEnforcementStrategy>>()

    private val mutex = Mutex()

    override suspend fun createLimit(limit: UsageLimit): String {
        val progressiveLimit = mapToProgressiveLimit(limit)
        val id = progressiveLimitDao.insertLimit(progressiveLimit)
        return id.toString()
    }

    override suspend fun updateLimit(limit: UsageLimit) {
        // Find existing limit by package name and update
        val existing = progressiveLimitDao.getActiveLimitForApp(limit.packageName)
        if (existing != null) {
            val updated = existing.copy(
                currentLimitMillis = limit.durationMillis,
                targetLimitMillis = limit.durationMillis
            )
            progressiveLimitDao.updateLimit(updated)
        }
    }

    override suspend fun deleteLimit(limitId: String) {
        limitId.toLongOrNull()?.let { id ->
            progressiveLimitDao.deleteLimit(id)
        }
    }

    override suspend fun getLimitsForPackage(packageName: String): List<UsageLimit> {
        val progressiveLimit = progressiveLimitDao.getActiveLimitForApp(packageName)
        return if (progressiveLimit != null) {
            listOf(mapToUsageLimit(progressiveLimit))
        } else {
            emptyList()
        }
    }

    override suspend fun getAllActiveLimits(): List<UsageLimit> {
        return progressiveLimitDao.getAllActiveLimits().first().map { mapToUsageLimit(it) }
    }

    override fun observeLimitsForPackage(packageName: String): Flow<List<UsageLimit>> {
        return progressiveLimitDao.getAllActiveLimits().map { progressiveLimits ->
            progressiveLimits
                .filter { it.appPackageName == packageName }
                .map { mapToUsageLimit(it) }
        }
    }

    override fun observeAllActiveLimits(): Flow<List<UsageLimit>> {
        return progressiveLimitDao.getAllActiveLimits().map { progressiveLimits ->
            progressiveLimits.map { mapToUsageLimit(it) }
        }
    }

    override suspend fun getViolatedLimits(): List<LimitViolationInfo> {
        // Simplified implementation - would need actual usage tracking
        return emptyList()
    }

    override suspend fun recordViolation(violation: ViolationRecord) {
        mutex.withLock {
            val packageViolations = violationRecords.getOrPut(violation.packageName) { mutableListOf() }
            packageViolations.add(violation)
        }
    }

    override suspend fun getViolationHistory(packageName: String, days: Int): List<ViolationRecord> {
        val cutoff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return violationRecords[packageName]?.filter { it.timestamp >= cutoff } ?: emptyList()
    }

    override suspend fun getAllViolationHistory(days: Int): List<ViolationRecord> {
        val cutoff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return violationRecords.values.flatten().filter { it.timestamp >= cutoff }
    }

    override suspend fun recordExtension(extension: ExtensionRecord) {
        mutex.withLock {
            val packageExtensions = extensionRecords.getOrPut(extension.packageName) { mutableListOf() }
            packageExtensions.add(extension)
        }
    }

    override suspend fun getExtensionHistory(packageName: String, days: Int): List<ExtensionRecord> {
        val cutoff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return extensionRecords[packageName]?.filter { it.timestamp >= cutoff } ?: emptyList()
    }

    override suspend fun getDailyUsageRecords(packageName: String, days: Int): List<DailyUsageRecord> {
        val cutoff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return dailyUsageRecords[packageName]?.filter { it.date >= cutoff } ?: emptyList()
    }

    override suspend fun saveDailyUsageRecord(record: DailyUsageRecord) {
        mutex.withLock {
            val packageRecords = dailyUsageRecords.getOrPut(record.packageName) { mutableListOf() }
            packageRecords.add(record)
        }
    }

    override suspend fun getActiveCooldowns(): List<PackageCooldown> {
        val currentTime = System.currentTimeMillis()
        return cooldowns.mapNotNull { (packageName, cooldown) ->
            if (cooldown.startTime + cooldown.durationMillis > currentTime) {
                PackageCooldown(packageName, cooldown, "Limit violation")
            } else null
        }
    }

    override suspend fun setCooldown(packageName: String, cooldown: CooldownPeriod) {
        cooldowns[packageName] = cooldown
    }

    override suspend fun clearCooldown(packageName: String) {
        cooldowns.remove(packageName)
    }

    override suspend fun getProgressiveLimitRecommendations(packageName: String): List<ProgressiveLimitRecommendation> {
        // Simplified - would calculate based on historical data
        return listOf(
            ProgressiveLimitRecommendation(
                currentLimitMillis = 60 * 60 * 1000L, // 1 hour
                recommendedLimitMillis = 45 * 60 * 1000L, // 45 minutes
                adjustmentType = AdjustmentType.REDUCE_GENTLE,
                reasoning = "Gradual reduction based on usage patterns",
                confidence = 0.8f,
                implementationTimeline = "Next week"
            )
        )
    }

    override suspend fun saveProgressiveLimitRecommendation(
        packageName: String,
        recommendation: ProgressiveLimitRecommendation
    ) {
        // In a real implementation, we'd store this data
    }

    override suspend fun getLimitValidationHistory(
        packageName: String,
        days: Int
    ): List<TimestampedValidationResult> {
        val cutoff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return validationResults[packageName]?.filter { it.validatedAt >= cutoff } ?: emptyList()
    }

    override suspend fun saveLimitValidationResult(
        packageName: String,
        result: LimitValidationResult
    ) {
        mutex.withLock {
            val packageResults = validationResults.getOrPut(packageName) { mutableListOf() }
            packageResults.add(
                TimestampedValidationResult(
                    result = result,
                    validatedAt = System.currentTimeMillis(),
                    proposedLimit = UsageLimit(
                        id = "",
                        packageName = packageName,
                        limitType = LimitType.DAILY_TOTAL,
                        durationMillis = 60 * 60 * 1000L
                    )
                )
            )
        }
    }

    override suspend fun getEnforcementStrategyHistory(
        packageName: String,
        days: Int
    ): List<TimestampedEnforcementStrategy> {
        val cutoff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return enforcementStrategies[packageName]?.filter { it.appliedAt >= cutoff } ?: emptyList()
    }

    override suspend fun saveEnforcementStrategy(
        packageName: String,
        strategy: EnforcementStrategy,
        context: TimeContext
    ) {
        mutex.withLock {
            val packageStrategies = enforcementStrategies.getOrPut(packageName) { mutableListOf() }
            packageStrategies.add(
                TimestampedEnforcementStrategy(
                    strategy = strategy,
                    appliedAt = System.currentTimeMillis(),
                    context = context
                )
            )
        }
    }

    override suspend fun getLimitEffectivenessMetrics(): LimitEffectivenessMetrics {
        return LimitEffectivenessMetrics(
            totalLimitsCreated = violationRecords.size,
            activeLimits = cooldowns.size,
            averageComplianceRate = 0.75f,
            mostEffectiveLimitType = LimitType.DAILY_TOTAL,
            leastEffectiveLimitType = LimitType.SESSION_DURATION,
            averageLimitDuration = 60 * 60 * 1000L,
            userSatisfactionScore = 4.2f,
            limitAdjustmentFrequency = 0.3f
        )
    }

    override suspend fun updateLimitEffectiveness(limitId: String, effectiveness: LimitEffectiveness) {
        // In a real implementation, we'd store effectiveness data
    }

    override suspend fun getSmartLimitSuggestions(
        packageName: String,
        historicalUsage: List<DailyUsageRecord>
    ): List<SmartLimitSuggestion> {
        // Simplified implementation
        return listOf(
            SmartLimitSuggestion(
                suggestedLimit = UsageLimit(
                    id = "",
                    packageName = packageName,
                    limitType = LimitType.DAILY_TOTAL,
                    durationMillis = 45 * 60 * 1000L
                ),
                reasoning = "Based on your recent usage patterns",
                confidence = 0.8f,
                basedOnPatterns = listOf("Declining usage trend", "High compliance rate"),
                expectedEffectiveness = 0.85f
            )
        )
    }

    override suspend fun createLimitsFromTemplate(template: LimitTemplate): List<String> {
        val createdIds = mutableListOf<String>()
        template.limits.forEach { limitTemplate ->
            val limit = UsageLimit(
                id = "",
                packageName = "", // Would be set based on context
                limitType = limitTemplate.limitType,
                durationMillis = limitTemplate.durationMillis,
                timeRange = limitTemplate.timeRange,
                daysOfWeek = limitTemplate.daysOfWeek,
                priority = limitTemplate.priority
            )
            val id = createLimit(limit)
            createdIds.add(id)
        }
        return createdIds
    }

    override suspend fun getLimitTemplates(): List<LimitTemplate> {
        return listOf(
            LimitTemplate(
                id = "social_media_detox",
                name = "Social Media Detox",
                description = "Gradual reduction of social media usage",
                category = LimitTemplateCategory.DIGITAL_DETOX,
                limits = listOf(
                    UsageLimitTemplate(
                        limitType = LimitType.DAILY_TOTAL,
                        durationMillis = 60 * 60 * 1000L,
                        priority = LimitPriority.HIGH,
                        applicableCategories = setOf(AppCategory.SOCIAL_MEDIA)
                    )
                ),
                targetUserType = TargetUserType.BEGINNER,
                difficulty = RecommendationDifficulty.MEDIUM
            )
        )
    }

    override suspend fun archiveOldData(retentionDays: Int) {
        val cutoff = System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000L)
        mutex.withLock {
            violationRecords.values.forEach { violations ->
                violations.removeAll { it.timestamp < cutoff }
            }
            extensionRecords.values.forEach { extensions ->
                extensions.removeAll { it.timestamp < cutoff }
            }
            dailyUsageRecords.values.forEach { records ->
                records.removeAll { it.date < cutoff }
            }
        }
    }

    // Helper methods to map between domain and data models
    private fun mapToProgressiveLimit(usageLimit: UsageLimit): ProgressiveLimit {
        return ProgressiveLimit(
            appPackageName = usageLimit.packageName,
            originalLimitMillis = usageLimit.durationMillis,
            targetLimitMillis = usageLimit.durationMillis,
            currentLimitMillis = usageLimit.durationMillis,
            startDate = java.time.LocalDate.now().toString(),
            nextReductionDate = java.time.LocalDate.now().plusWeeks(1).toString()
        )
    }

    private fun mapToUsageLimit(progressiveLimit: ProgressiveLimit): UsageLimit {
        return UsageLimit(
            id = progressiveLimit.id.toString(),
            packageName = progressiveLimit.appPackageName,
            limitType = LimitType.DAILY_TOTAL,
            durationMillis = progressiveLimit.currentLimitMillis,
            priority = LimitPriority.NORMAL,
            isActive = progressiveLimit.isActive,
            createdAt = progressiveLimit.createdAt
        )
    }
}