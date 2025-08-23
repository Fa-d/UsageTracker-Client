package dev.sadakat.screentimetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_categories")
data class AppCategory(
    @PrimaryKey val packageName: String,
    val category: String,
    val confidence: Float, // 0.0 - 1.0 confidence level
    val source: String, // "system", "manual", "pattern", "known", "default"
    val lastUpdated: Long,
    val appName: String // Store app display name for debugging
)

enum class CategorySource(val value: String) {
    SYSTEM("system"),           // From Android ApplicationInfo.category
    MANUAL("manual"),           // User-defined category
    PATTERN("pattern"),         // Pattern matching on package name
    KNOWN("known"),            // From hardcoded known apps list
    DEFAULT("default")          // Default fallback category
}

object AppCategories {
    const val SOCIAL = "Social"
    const val ENTERTAINMENT = "Entertainment" 
    const val PRODUCTIVITY = "Productivity"
    const val COMMUNICATION = "Communication"
    const val GAMES = "Games"
    const val FINANCE = "Finance"
    const val HEALTH = "Health"
    const val PHOTOGRAPHY = "Photography"
    const val NEWS = "News"
    const val NAVIGATION = "Navigation"
    const val SHOPPING = "Shopping"
    const val EDUCATION = "Education"
    const val UTILITIES = "Utilities"
    const val OTHER = "Other"
    
    val ALL_CATEGORIES = listOf(
        SOCIAL, ENTERTAINMENT, PRODUCTIVITY, COMMUNICATION, GAMES,
        FINANCE, HEALTH, PHOTOGRAPHY, NEWS, NAVIGATION, SHOPPING,
        EDUCATION, UTILITIES, OTHER
    )
}