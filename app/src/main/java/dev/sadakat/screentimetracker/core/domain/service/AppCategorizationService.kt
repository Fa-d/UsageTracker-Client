package dev.sadakat.screentimetracker.core.domain.service

import dev.sadakat.screentimetracker.core.domain.error.DomainResult

/**
 * Domain abstraction for app categorization services.
 * This interface removes Android dependencies from the domain layer.
 */
interface AppCategorizationService {

    /**
     * Categorizes an app by its package name
     */
    suspend fun categorizeApp(packageName: String): DomainResult<String>

    /**
     * Manually updates an app's category
     */
    suspend fun updateCategoryManually(packageName: String, category: String): DomainResult<Unit>

    /**
     * Gets statistics about app categories
     */
    suspend fun getCategoryStats(): DomainResult<Map<String, Int>>

    /**
     * Cleans stale cache entries
     */
    suspend fun cleanStaleCache(): DomainResult<Unit>

    /**
     * Gets app information including category
     */
    suspend fun getAppInfo(packageName: String): DomainResult<AppInfo>

    /**
     * Batch categorizes multiple apps
     */
    suspend fun categorizeApps(packageNames: List<String>): DomainResult<Map<String, String>>
}

/**
 * Domain representation of app information
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val category: String,
    val confidence: Float,
    val source: CategorySource
)

/**
 * Sources for app categorization
 */
enum class CategorySource(val displayName: String) {
    KNOWN("Known Mapping"),
    SYSTEM("System Category"),
    PATTERN("Pattern Matching"),
    MANUAL("Manual Override"),
    DEFAULT("Default Category"),
    AI("AI Classification")
}

/**
 * Confidence levels for categorization
 */
enum class CategorizationConfidence(val threshold: Float) {
    HIGH(0.9f),
    MEDIUM(0.7f),
    LOW(0.5f),
    UNKNOWN(0.0f);

    companion object {
        fun fromValue(confidence: Float): CategorizationConfidence {
            return when {
                confidence >= HIGH.threshold -> HIGH
                confidence >= MEDIUM.threshold -> MEDIUM
                confidence >= LOW.threshold -> LOW
                else -> UNKNOWN
            }
        }
    }
}

/**
 * Result of app categorization with metadata
 */
data class CategorizationResult(
    val packageName: String,
    val category: String,
    val confidence: CategorizationConfidence,
    val source: CategorySource,
    val reasoning: String? = null,
    val alternativeCategories: List<String> = emptyList()
)