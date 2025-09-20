package dev.sadakat.screentimetracker.core.domain.usecases

import dev.sadakat.screentimetracker.core.data.local.entities.AppCategory
import dev.sadakat.screentimetracker.core.domain.categorization.AppCategorizer
import dev.sadakat.screentimetracker.core.domain.categorization.DomainAppCategories
import dev.sadakat.screentimetracker.core.domain.error.DomainResult
import dev.sadakat.screentimetracker.core.domain.repository.AppCategoryRepository
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import kotlinx.coroutines.flow.Flow

class AppCategoryManagementUseCase(
    private val appCategorizer: AppCategorizer,
    private val appCategoryRepository: AppCategoryRepository,
    private val appLogger: AppLogger
) {
    companion object {
        private const val TAG = "AppCategoryManagement"
    }
    
    suspend fun categorizeApp(packageName: String): String {
        return when (val result = appCategorizer.categorizeApp(packageName)) {
            is DomainResult.Success -> result.data
            is DomainResult.Failure -> {
                appLogger.e(TAG, "Failed to categorize app $packageName", result.error)
                DomainAppCategories.OTHER
            }
        }
    }
    
    suspend fun updateCategoryManually(packageName: String, category: String) {
        when (val result = appCategorizer.updateCategoryManually(packageName, category)) {
            is DomainResult.Success -> {
                appLogger.d(TAG, "Updated category for $packageName to $category")
            }
            is DomainResult.Failure -> {
                appLogger.e(TAG, "Failed to update category for $packageName", result.error)
                throw result.error
            }
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
        return DomainAppCategories.ALL_CATEGORIES
    }
    
    suspend fun getCategoryStats(): Map<String, Int> {
        return when (val result = appCategorizer.getCategoryStats()) {
            is DomainResult.Success -> result.data
            is DomainResult.Failure -> {
                appLogger.e(TAG, "Failed to get category stats", result.error)
                emptyMap()
            }
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
        when (val result = appCategorizer.cleanStaleCache()) {
            is DomainResult.Success -> {
                appLogger.d(TAG, "Cleaned up stale categories")
            }
            is DomainResult.Failure -> {
                appLogger.e(TAG, "Failed to clean up stale categories", result.error)
            }
        }
    }
    
    suspend fun recategorizeApp(packageName: String): String {
        return try {
            // Delete existing category to force re-categorization
            appCategoryRepository.deleteCategoryByPackage(packageName)
            // Re-categorize the app
            when (val result = appCategorizer.categorizeApp(packageName)) {
                is DomainResult.Success -> result.data
                is DomainResult.Failure -> {
                    appLogger.e(TAG, "Failed to recategorize app $packageName", result.error)
                    DomainAppCategories.OTHER
                }
            }
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to recategorize app $packageName", e)
            DomainAppCategories.OTHER
        }
    }
    
    suspend fun bulkCategorizeApps(packageNames: List<String>): Map<String, String> {
        val results = mutableMapOf<String, String>()
        packageNames.forEach { packageName ->
            when (val result = appCategorizer.categorizeApp(packageName)) {
                is DomainResult.Success -> {
                    results[packageName] = result.data
                }
                is DomainResult.Failure -> {
                    appLogger.e(TAG, "Failed to categorize $packageName", result.error)
                    results[packageName] = DomainAppCategories.OTHER
                }
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