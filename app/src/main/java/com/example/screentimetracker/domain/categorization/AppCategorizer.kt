package com.example.screentimetracker.domain.categorization

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.screentimetracker.data.local.AppCategory
import com.example.screentimetracker.data.local.AppCategoryDao
import com.example.screentimetracker.data.local.AppCategories
import com.example.screentimetracker.data.local.CategorySource
import com.example.screentimetracker.utils.logger.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppCategorizer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appCategoryDao: AppCategoryDao,
    private val appLogger: AppLogger
) {
    companion object {
        private const val TAG = "AppCategorizer"
        private const val CACHE_VALIDITY_DAYS = 30
        private const val CACHE_VALIDITY_MILLIS = CACHE_VALIDITY_DAYS * 24 * 60 * 60 * 1000L
    }
    
    // Known apps mapping (your current hardcoded list as fallback)
    private val knownApps = mapOf(
        // Social
        "com.instagram.android" to AppCategories.SOCIAL,
        "com.facebook.katana" to AppCategories.SOCIAL,
        "com.twitter.android" to AppCategories.SOCIAL,
        "com.snapchat.android" to AppCategories.SOCIAL,
        "com.linkedin.android" to AppCategories.SOCIAL,
        "com.reddit.frontpage" to AppCategories.SOCIAL,
        
        // Entertainment
        "com.netflix.mediaclient" to AppCategories.ENTERTAINMENT,
        "com.youtube.android" to AppCategories.ENTERTAINMENT,
        "com.spotify.music" to AppCategories.ENTERTAINMENT,
        "com.disney.disneyplus" to AppCategories.ENTERTAINMENT,
        "com.hulu.plus" to AppCategories.ENTERTAINMENT,
        "com.amazon.avod.thirdpartyclient" to AppCategories.ENTERTAINMENT,
        
        // Productivity
        "com.microsoft.office.word" to AppCategories.PRODUCTIVITY,
        "com.google.android.apps.docs.editors.docs" to AppCategories.PRODUCTIVITY,
        "com.slack" to AppCategories.PRODUCTIVITY,
        "com.microsoft.teams" to AppCategories.PRODUCTIVITY,
        "notion.id" to AppCategories.PRODUCTIVITY,
        "com.trello" to AppCategories.PRODUCTIVITY,
        
        // Communication
        "com.whatsapp" to AppCategories.COMMUNICATION,
        "com.android.mms" to AppCategories.COMMUNICATION,
        "com.google.android.apps.messaging" to AppCategories.COMMUNICATION,
        "com.discord" to AppCategories.COMMUNICATION,
        "com.skype.raider" to AppCategories.COMMUNICATION,
        "com.viber.voip" to AppCategories.COMMUNICATION,
        
        // Games
        "com.android.games" to AppCategories.GAMES,
        "com.supercell.clashofclans" to AppCategories.GAMES,
        "com.king.candycrushsaga" to AppCategories.GAMES,
        "com.mojang.minecraftpe" to AppCategories.GAMES,
        
        // Finance
        "com.paypal.android.p2pmobile" to AppCategories.FINANCE,
        "com.venmo" to AppCategories.FINANCE,
        "com.chase.sig.android" to AppCategories.FINANCE,
        "com.bankofamerica.digitalwallet" to AppCategories.FINANCE,
        
        // Health
        "com.myfitnesspal.android" to AppCategories.HEALTH,
        "com.fitbit.FitbitMobile" to AppCategories.HEALTH,
        "com.google.android.apps.fitness" to AppCategories.HEALTH,
        "com.nike.plusone" to AppCategories.HEALTH
    )
    
    suspend fun categorizeApp(packageName: String): String {
        return withContext(Dispatchers.IO) {
            try {
                appLogger.d(TAG, "Categorizing app: $packageName")
                
                // 1. Check cache first
                val cachedCategory = getCachedCategory(packageName)
                if (cachedCategory != null && !isCacheStale(cachedCategory)) {
                    appLogger.d(TAG, "Using cached category for $packageName: ${cachedCategory.category}")
                    return@withContext cachedCategory.category
                }
                
                // 2. Use known mappings (high confidence)
                knownApps[packageName]?.let { category ->
                    appLogger.d(TAG, "Using known mapping for $packageName: $category")
                    cacheCategory(packageName, category, CategorySource.KNOWN.value, 1.0f)
                    return@withContext category
                }
                
                // 3. Use Android system category
                getSystemCategory(packageName)?.let { category ->
                    appLogger.d(TAG, "Using system category for $packageName: $category")
                    cacheCategory(packageName, category, CategorySource.SYSTEM.value, 0.8f)
                    return@withContext category
                }
                
                // 4. Pattern matching on package name
                categorizeByPattern(packageName)?.let { category ->
                    appLogger.d(TAG, "Using pattern matching for $packageName: $category")
                    cacheCategory(packageName, category, CategorySource.PATTERN.value, 0.6f)
                    return@withContext category
                }
                
                // 5. Default category
                val defaultCategory = AppCategories.OTHER
                appLogger.d(TAG, "Using default category for $packageName: $defaultCategory")
                cacheCategory(packageName, defaultCategory, CategorySource.DEFAULT.value, 0.3f)
                return@withContext defaultCategory
                
            } catch (e: Exception) {
                appLogger.e(TAG, "Error categorizing app $packageName", e)
                AppCategories.OTHER
            }
        }
    }
    
    private suspend fun getCachedCategory(packageName: String): AppCategory? {
        return try {
            appCategoryDao.getCategoryByPackage(packageName)
        } catch (e: Exception) {
            appLogger.e(TAG, "Error getting cached category for $packageName", e)
            null
        }
    }
    
    private fun isCacheStale(appCategory: AppCategory): Boolean {
        val now = System.currentTimeMillis()
        return (now - appCategory.lastUpdated) > CACHE_VALIDITY_MILLIS
    }
    
    private suspend fun cacheCategory(
        packageName: String, 
        category: String, 
        source: String, 
        confidence: Float
    ) {
        try {
            val appName = getAppName(packageName)
            val appCategory = AppCategory(
                packageName = packageName,
                category = category,
                confidence = confidence,
                source = source,
                lastUpdated = System.currentTimeMillis(),
                appName = appName
            )
            appCategoryDao.insertCategory(appCategory)
        } catch (e: Exception) {
            appLogger.e(TAG, "Error caching category for $packageName", e)
        }
    }
    
    private fun getSystemCategory(packageName: String): String? {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            mapSystemCategoryToOurs(appInfo.category)
        } catch (e: Exception) {
            appLogger.e(TAG, "Error getting system category for $packageName", e)
            null
        }
    }
    
    private fun mapSystemCategoryToOurs(systemCategory: Int): String? {
        return when (systemCategory) {
            ApplicationInfo.CATEGORY_GAME -> AppCategories.GAMES
            ApplicationInfo.CATEGORY_AUDIO -> AppCategories.ENTERTAINMENT
            ApplicationInfo.CATEGORY_VIDEO -> AppCategories.ENTERTAINMENT
            ApplicationInfo.CATEGORY_IMAGE -> AppCategories.PHOTOGRAPHY
            ApplicationInfo.CATEGORY_SOCIAL -> AppCategories.SOCIAL
            ApplicationInfo.CATEGORY_NEWS -> AppCategories.NEWS
            ApplicationInfo.CATEGORY_MAPS -> AppCategories.NAVIGATION
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> AppCategories.PRODUCTIVITY
            else -> null
        }
    }
    
    private fun categorizeByPattern(packageName: String): String? {
        val lowercasePackage = packageName.lowercase()
        
        return when {
            // Games patterns
            lowercasePackage.contains("game") || 
            lowercasePackage.contains("play") ||
            lowercasePackage.contains("puzzle") ||
            lowercasePackage.contains("casino") -> AppCategories.GAMES
            
            // Social patterns
            lowercasePackage.contains("social") || 
            lowercasePackage.contains("chat") ||
            lowercasePackage.contains("dating") ||
            lowercasePackage.contains("meet") -> AppCategories.SOCIAL
            
            // Entertainment patterns
            lowercasePackage.contains("music") || 
            lowercasePackage.contains("video") ||
            lowercasePackage.contains("stream") ||
            lowercasePackage.contains("media") ||
            lowercasePackage.contains("radio") -> AppCategories.ENTERTAINMENT
            
            // Finance patterns
            lowercasePackage.contains("bank") || 
            lowercasePackage.contains("pay") ||
            lowercasePackage.contains("wallet") ||
            lowercasePackage.contains("finance") ||
            lowercasePackage.contains("invest") -> AppCategories.FINANCE
            
            // Health patterns
            lowercasePackage.contains("health") || 
            lowercasePackage.contains("fitness") ||
            lowercasePackage.contains("medical") ||
            lowercasePackage.contains("workout") -> AppCategories.HEALTH
            
            // Communication patterns
            lowercasePackage.contains("message") ||
            lowercasePackage.contains("mail") ||
            lowercasePackage.contains("sms") ||
            lowercasePackage.contains("call") -> AppCategories.COMMUNICATION
            
            // Photography patterns
            lowercasePackage.contains("camera") ||
            lowercasePackage.contains("photo") ||
            lowercasePackage.contains("image") ||
            lowercasePackage.contains("gallery") -> AppCategories.PHOTOGRAPHY
            
            // Shopping patterns
            lowercasePackage.contains("shop") ||
            lowercasePackage.contains("store") ||
            lowercasePackage.contains("buy") ||
            lowercasePackage.contains("market") -> AppCategories.SHOPPING
            
            // Navigation patterns
            lowercasePackage.contains("map") ||
            lowercasePackage.contains("navigation") ||
            lowercasePackage.contains("gps") -> AppCategories.NAVIGATION
            
            // News patterns
            lowercasePackage.contains("news") ||
            lowercasePackage.contains("newspaper") -> AppCategories.NEWS
            
            // Education patterns
            lowercasePackage.contains("learn") ||
            lowercasePackage.contains("edu") ||
            lowercasePackage.contains("school") ||
            lowercasePackage.contains("study") -> AppCategories.EDUCATION
            
            else -> null
        }
    }
    
    private fun getAppName(packageName: String): String {
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }
    
    suspend fun updateCategoryManually(packageName: String, category: String) {
        try {
            appCategoryDao.updateCategoryManually(packageName, category)
            appLogger.d(TAG, "Manually updated category for $packageName to $category")
        } catch (e: Exception) {
            appLogger.e(TAG, "Error manually updating category for $packageName", e)
        }
    }
    
    suspend fun getCategoryStats(): Map<String, Int> {
        return try {
            AppCategories.ALL_CATEGORIES.associateWith { category ->
                appCategoryDao.getAppCountByCategory(category)
            }
        } catch (e: Exception) {
            appLogger.e(TAG, "Error getting category stats", e)
            emptyMap()
        }
    }
    
    suspend fun cleanStaleCache() {
        try {
            val staleTimestamp = System.currentTimeMillis() - CACHE_VALIDITY_MILLIS
            val staleCategories = appCategoryDao.getStaleCategories(staleTimestamp)
            staleCategories.forEach { category ->
                // Re-categorize stale entries
                categorizeApp(category.packageName)
            }
            appLogger.d(TAG, "Cleaned ${staleCategories.size} stale cache entries")
        } catch (e: Exception) {
            appLogger.e(TAG, "Error cleaning stale cache", e)
        }
    }
}