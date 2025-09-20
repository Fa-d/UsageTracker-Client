package dev.sadakat.screentimetracker.core.domain.categorization

import dev.sadakat.screentimetracker.core.domain.service.AppCategorizationService
import dev.sadakat.screentimetracker.core.domain.error.DomainResult

/**
 * Domain interface for app categorization.
 * This is now just an alias to AppCategorizationService for backward compatibility.
 * Use AppCategorizationService directly in new code.
 *
 * @deprecated Use AppCategorizationService instead
 */
@Deprecated(
    message = "Use AppCategorizationService instead",
    replaceWith = ReplaceWith(
        "AppCategorizationService",
        "dev.sadakat.screentimetracker.core.domain.service.AppCategorizationService"
    )
)
interface AppCategorizer : AppCategorizationService

/**
 * Domain constants for app categories.
 * These replace the data layer AppCategories constants.
 */
object DomainAppCategories {
    const val SOCIAL = "Social"
    const val ENTERTAINMENT = "Entertainment"
    const val PRODUCTIVITY = "Productivity"
    const val COMMUNICATION = "Communication"
    const val GAMES = "Games"
    const val FINANCE = "Finance"
    const val HEALTH = "Health"
    const val PHOTOGRAPHY = "Photography"
    const val SHOPPING = "Shopping"
    const val NAVIGATION = "Navigation"
    const val NEWS = "News"
    const val EDUCATION = "Education"
    const val UTILITIES = "Utilities"
    const val OTHER = "Other"

    val ALL_CATEGORIES = listOf(
        SOCIAL, ENTERTAINMENT, PRODUCTIVITY, COMMUNICATION,
        GAMES, FINANCE, HEALTH, PHOTOGRAPHY, SHOPPING,
        NAVIGATION, NEWS, EDUCATION, UTILITIES, OTHER
    )

    /**
     * Categories that are considered productive
     */
    val PRODUCTIVE_CATEGORIES = setOf(
        PRODUCTIVITY, EDUCATION, HEALTH, UTILITIES
    )

    /**
     * Categories that are typically distracting
     */
    val DISTRACTING_CATEGORIES = setOf(
        SOCIAL, ENTERTAINMENT, GAMES, SHOPPING
    )

    /**
     * Categories that are neutral/communication focused
     */
    val NEUTRAL_CATEGORIES = setOf(
        COMMUNICATION, FINANCE, PHOTOGRAPHY, NAVIGATION, NEWS, OTHER
    )

    /**
     * Gets the default time limit for a category in minutes
     */
    fun getDefaultLimitMinutes(category: String): Int = when (category) {
        SOCIAL -> 60
        ENTERTAINMENT -> 120
        GAMES -> 90
        SHOPPING -> 30
        NEWS -> 45
        else -> 0 // No limit for productive categories
    }

    /**
     * Checks if a category is considered productive
     */
    fun isProductive(category: String): Boolean = category in PRODUCTIVE_CATEGORIES

    /**
     * Checks if a category is considered distracting
     */
    fun isDistracting(category: String): Boolean = category in DISTRACTING_CATEGORIES

    /**
     * Gets the color associated with a category
     */
    fun getCategoryColor(category: String): String = when (category) {
        SOCIAL -> "#EF4444"
        ENTERTAINMENT -> "#F59E0B"
        PRODUCTIVITY -> "#10B981"
        COMMUNICATION -> "#8B5CF6"
        GAMES -> "#F97316"
        FINANCE -> "#059669"
        HEALTH -> "#06B6D4"
        PHOTOGRAPHY -> "#EC4899"
        SHOPPING -> "#84CC16"
        NAVIGATION -> "#3B82F6"
        NEWS -> "#6B7280"
        EDUCATION -> "#3B82F6"
        UTILITIES -> "#84CC16"
        else -> "#6B7280"
    }
}