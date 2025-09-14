package dev.sadakat.screentimetracker.domain.usecases

import dev.sadakat.screentimetracker.data.local.AppCategories
import dev.sadakat.screentimetracker.data.local.AppCategory
import dev.sadakat.screentimetracker.domain.categorization.AppCategorizer
import dev.sadakat.screentimetracker.domain.repository.AppCategoryRepository
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppCategoryManagementUseCase @Inject constructor(
    private val appCategorizer: AppCategorizer,
    private val appCategoryRepository: AppCategoryRepository,
    private val appLogger: AppLogger
) {
    companion object {
        private const val TAG = "AppCategoryManagement"
    }
    
    suspend fun categorizeApp(packageName: String): String {
        return appCategorizer.categorizeApp(packageName)
    }
    
    suspend fun updateCategoryManually(packageName: String, category: String) {
        try {
            appCategoryRepository.updateCategoryManually(packageName, category)
            appLogger.d(TAG, "Updated category for $packageName to $category")
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to update category for $packageName", e)
            throw e
        }
    }
    
    suspend fun getCategoryForApp(packageName: String): AppCategory? {
        return try {
            appCategoryRepository.getCategoryByPackage(packageName)
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to get category for $packageName", e)
            null
        }
    }
    
    fun getCategoryForAppFlow(packageName: String): Flow<AppCategory?> {
        return appCategoryRepository.getCategoryByPackageFlow(packageName)
    }
    
    suspend fun getAppsByCategory(category: String): List<AppCategory> {
        return try {
            appCategoryRepository.getAppsByCategory(category)
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to get apps by category $category", e)
            emptyList()
        }
    }
    
    suspend fun getAllAvailableCategories(): List<String> {
        return AppCategories.ALL_CATEGORIES
    }
    
    suspend fun getCategoryStats(): Map<String, Int> {
        return try {
            appCategoryRepository.getCategoryStats()
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to get category stats", e)
            emptyMap()
        }
    }
    
    suspend fun getDistinctUsedCategories(): List<String> {
        return try {
            appCategoryRepository.getDistinctCategories()
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to get distinct categories", e)
            emptyList()
        }
    }
    
    suspend fun cleanupStaleCategories() {
        try {
            appCategoryRepository.cleanStaleCache()
            appLogger.d(TAG, "Cleaned up stale categories")
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to clean up stale categories", e)
        }
    }
    
    suspend fun recategorizeApp(packageName: String): String {
        return try {
            // Delete existing category to force re-categorization
            appCategoryRepository.deleteCategoryByPackage(packageName)
            // Re-categorize the app
            appCategorizer.categorizeApp(packageName)
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to recategorize app $packageName", e)
            AppCategories.OTHER
        }
    }
    
    suspend fun bulkCategorizeApps(packageNames: List<String>): Map<String, String> {
        val results = mutableMapOf<String, String>()
        packageNames.forEach { packageName ->
            try {
                results[packageName] = appCategorizer.categorizeApp(packageName)
            } catch (e: Exception) {
                appLogger.e(TAG, "Failed to categorize $packageName", e)
                results[packageName] = AppCategories.OTHER
            }
        }
        return results
    }
    
    data class CategoryStatistic(
        val categoryName: String,
        val appCount: Int,
        val totalUsageTime: Long = 0L
    )
    
    suspend fun getCategoryStatistics(): List<CategoryStatistic> {
        return try {
            val stats = getCategoryStats()
            stats.map { (category, count) ->
                CategoryStatistic(category, count)
            }.sortedByDescending { it.appCount }
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to get category statistics", e)
            emptyList()
        }
    }
}