package com.example.screentimetracker.examples

import com.example.screentimetracker.domain.categorization.AppCategorizer
import com.example.screentimetracker.domain.usecases.AppCategoryManagementUseCase
import com.example.screentimetracker.data.local.AppCategories
import javax.inject.Inject

/**
 * Example demonstrating the new robust app categorization system
 * 
 * Features:
 * 1. Hybrid categorization approach (system + patterns + known apps + fallback)
 * 2. Database caching with confidence levels and sources
 * 3. Manual user overrides
 * 4. Automatic stale cache cleanup
 * 5. Scalable to handle any installed app
 */
class AppCategorizationExample @Inject constructor(
    private val appCategorizer: AppCategorizer,
    private val categoryManagementUseCase: AppCategoryManagementUseCase
) {
    
    /**
     * Demonstrates basic categorization
     */
    suspend fun basicCategorizationExample() {
        // Examples of different categorization sources:
        
        // 1. Known apps (highest confidence)
        val instagramCategory = appCategorizer.categorizeApp("com.instagram.android")
        println("Instagram: $instagramCategory") // Output: "Social"
        
        // 2. System category (medium-high confidence) 
        val gameCategory = appCategorizer.categorizeApp("com.supercell.boombeach")
        println("Boom Beach: $gameCategory") // Output: "Games" (from Android system category)
        
        // 3. Pattern matching (medium confidence)
        val bankAppCategory = appCategorizer.categorizeApp("com.example.mybank.app")
        println("Bank App: $bankAppCategory") // Output: "Finance" (pattern: contains "bank")
        
        // 4. Default fallback (lowest confidence)
        val unknownAppCategory = appCategorizer.categorizeApp("com.random.unknown.app")
        println("Unknown App: $unknownAppCategory") // Output: "Other"
    }
    
    /**
     * Demonstrates manual category overrides
     */
    suspend fun manualOverrideExample() {
        val packageName = "com.spotify.music"
        
        // Get current category
        val currentCategory = categoryManagementUseCase.categorizeApp(packageName)
        println("Spotify current category: $currentCategory") // "Entertainment"
        
        // User wants to categorize Spotify as Productivity (work music)
        categoryManagementUseCase.updateCategoryManually(packageName, AppCategories.PRODUCTIVITY)
        
        // Verify the change
        val newCategory = categoryManagementUseCase.categorizeApp(packageName)
        println("Spotify new category: $newCategory") // "Productivity"
        
        // The manual override has the highest confidence and will persist
    }
    
    /**
     * Demonstrates category statistics and management
     */
    suspend fun categoryStatsExample() {
        // Get statistics for all categories
        val stats = categoryManagementUseCase.getCategoryStats()
        println("Category Statistics:")
        stats.forEach { (category, count) ->
            println("  $category: $count apps")
        }
        
        // Get apps in a specific category
        val socialApps = categoryManagementUseCase.getAppsByCategory(AppCategories.SOCIAL)
        println("Social apps:")
        socialApps.forEach { appCategory ->
            println("  ${appCategory.packageName} (${appCategory.appName}) - confidence: ${appCategory.confidence}")
        }
        
        // Get all available categories
        val allCategories = categoryManagementUseCase.getAllAvailableCategories()
        println("Available categories: ${allCategories.joinToString(", ")}")
    }
    
    /**
     * Demonstrates bulk categorization for efficiency
     */
    suspend fun bulkCategorizationExample() {
        val packageNames = listOf(
            "com.whatsapp",
            "com.netflix.mediaclient", 
            "com.microsoft.office.word",
            "com.king.candycrushsaga",
            "com.chase.sig.android",
            "com.myfitnesspal.android",
            "com.unknown.random.app"
        )
        
        // Categorize all apps at once
        val results = categoryManagementUseCase.bulkCategorizeApps(packageNames)
        println("Bulk categorization results:")
        results.forEach { (packageName, category) ->
            println("  $packageName -> $category")
        }
    }
    
    /**
     * Demonstrates cache management and cleanup
     */
    suspend fun cacheManagementExample() {
        val packageName = "com.example.testapp"
        
        // First categorization (will be cached)
        val category1 = appCategorizer.categorizeApp(packageName)
        println("First categorization: $category1")
        
        // Second categorization (will use cache - faster)
        val category2 = appCategorizer.categorizeApp(packageName)
        println("Second categorization: $category2")
        
        // Force re-categorization (ignores cache)
        val freshCategory = categoryManagementUseCase.recategorizeApp(packageName)
        println("Fresh categorization: $freshCategory")
        
        // Clean up stale cache entries (older than 30 days)
        categoryManagementUseCase.cleanupStaleCategories()
        println("Stale cache cleaned up")
    }
    
    /**
     * Demonstrates the advantage over the old hardcoded approach
     */
    suspend fun comparisonWithOldApproach() {
        println("=== Comparison: Old vs New Approach ===")
        
        // Old approach: Only worked for hardcoded apps
        val oldKnownApps = mapOf(
            "com.instagram.android" to "Social",
            "com.facebook.katana" to "Social",
            "com.netflix.mediaclient" to "Entertainment"
            // Only ~15 apps supported
        )
        
        // New approach: Works with any app
        val testApps = listOf(
            "com.instagram.android",      // Known app
            "com.tiktok.android",         // Pattern match (social)  
            "com.spotify.music",          // Pattern match (music -> entertainment)
            "com.chase.mobile",           // Pattern match (bank -> finance)
            "com.myfitnesspal.android",   // Pattern match (fitness -> health)
            "com.random.unknown.app123"   // Default fallback
        )
        
        println("Old approach results:")
        testApps.forEach { packageName ->
            val oldResult = oldKnownApps[packageName] ?: "Unknown/Uncategorized"
            println("  $packageName -> $oldResult")
        }
        
        println("\nNew approach results:")
        testApps.forEach { packageName ->
            val newResult = appCategorizer.categorizeApp(packageName)
            println("  $packageName -> $newResult")
        }
        
        println("\nAdvantages of new approach:")
        println("✅ Handles ANY installed app")
        println("✅ Uses multiple categorization sources")
        println("✅ Caches results for performance")
        println("✅ Allows user customization") 
        println("✅ Self-maintaining (cache cleanup)")
        println("✅ Confidence levels for reliability")
        println("✅ No maintenance burden for new apps")
    }
}