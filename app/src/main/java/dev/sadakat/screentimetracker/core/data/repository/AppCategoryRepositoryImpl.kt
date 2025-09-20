package dev.sadakat.screentimetracker.core.data.repository

import dev.sadakat.screentimetracker.core.data.local.entities.AppCategory
import dev.sadakat.screentimetracker.core.data.local.dao.AppCategoryDao
import dev.sadakat.screentimetracker.domain.repository.AppCategoryRepository
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppCategoryRepositoryImpl @Inject constructor(
    private val appCategoryDao: AppCategoryDao,
    private val appLogger: AppLogger
) : AppCategoryRepository {
    
    companion object {
        private const val TAG = "AppCategoryRepository"
    }
    
    override suspend fun getCategoryByPackage(packageName: String): AppCategory? {
        return try {
            appCategoryDao.getCategoryByPackage(packageName)
        } catch (e: Exception) {
            appLogger.e(TAG, "Error getting category for package $packageName", e)
            null
        }
    }
    
    override fun getCategoryByPackageFlow(packageName: String): Flow<AppCategory?> {
        return appCategoryDao.getCategoryByPackageFlow(packageName)
    }
    
    override suspend fun getAppsByCategory(category: String): List<AppCategory> {
        return try {
            appCategoryDao.getAppsByCategory(category)
        } catch (e: Exception) {
            appLogger.e(TAG, "Error getting apps by category $category", e)
            emptyList()
        }
    }
    
    override suspend fun getAllCategories(): List<AppCategory> {
        return try {
            appCategoryDao.getAllCategories()
        } catch (e: Exception) {
            appLogger.e(TAG, "Error getting all categories", e)
            emptyList()
        }
    }
    
    override fun getAllCategoriesFlow(): Flow<List<AppCategory>> {
        return appCategoryDao.getAllCategoriesFlow()
    }
    
    override suspend fun getDistinctCategories(): List<String> {
        return try {
            appCategoryDao.getDistinctCategories()
        } catch (e: Exception) {
            appLogger.e(TAG, "Error getting distinct categories", e)
            emptyList()
        }
    }
    
    override suspend fun getAppCountByCategory(category: String): Int {
        return try {
            appCategoryDao.getAppCountByCategory(category)
        } catch (e: Exception) {
            appLogger.e(TAG, "Error getting app count for category $category", e)
            0
        }
    }
    
    override suspend fun getCategoriesBySource(source: String): List<AppCategory> {
        return try {
            appCategoryDao.getCategoriesBySource(source)
        } catch (e: Exception) {
            appLogger.e(TAG, "Error getting categories by source $source", e)
            emptyList()
        }
    }
    
    override suspend fun getStaleCategories(timestamp: Long): List<AppCategory> {
        return try {
            appCategoryDao.getStaleCategories(timestamp)
        } catch (e: Exception) {
            appLogger.e(TAG, "Error getting stale categories", e)
            emptyList()
        }
    }
    
    override suspend fun insertCategory(appCategory: AppCategory) {
        try {
            appCategoryDao.insertCategory(appCategory)
        } catch (e: Exception) {
            appLogger.e(TAG, "Error inserting category for ${appCategory.packageName}", e)
        }
    }
    
    override suspend fun insertCategories(appCategories: List<AppCategory>) {
        try {
            appCategoryDao.insertCategories(appCategories)
        } catch (e: Exception) {
            appLogger.e(TAG, "Error inserting ${appCategories.size} categories", e)
        }
    }
    
    override suspend fun updateCategory(appCategory: AppCategory) {
        try {
            appCategoryDao.updateCategory(appCategory)
        } catch (e: Exception) {
            appLogger.e(TAG, "Error updating category for ${appCategory.packageName}", e)
        }
    }
    
    override suspend fun deleteCategory(appCategory: AppCategory) {
        try {
            appCategoryDao.deleteCategory(appCategory)
        } catch (e: Exception) {
            appLogger.e(TAG, "Error deleting category for ${appCategory.packageName}", e)
        }
    }
    
    override suspend fun deleteCategoryByPackage(packageName: String) {
        try {
            appCategoryDao.deleteCategoryByPackage(packageName)
        } catch (e: Exception) {
            appLogger.e(TAG, "Error deleting category by package $packageName", e)
        }
    }
    
    override suspend fun deleteCategoriesBySource(source: String) {
        try {
            appCategoryDao.deleteCategoriesBySource(source)
        } catch (e: Exception) {
            appLogger.e(TAG, "Error deleting categories by source $source", e)
        }
    }
    
    override suspend fun deleteAllCategories() {
        try {
            appCategoryDao.deleteAllCategories()
        } catch (e: Exception) {
            appLogger.e(TAG, "Error deleting all categories", e)
        }
    }
    
    override suspend fun updateCategoryManually(packageName: String, category: String, timestamp: Long) {
        try {
            appCategoryDao.updateCategoryManually(packageName, category, timestamp)
        } catch (e: Exception) {
            appLogger.e(TAG, "Error manually updating category for $packageName", e)
        }
    }
    
    override suspend fun upsertCategories(appCategories: List<AppCategory>) {
        try {
            appCategoryDao.upsertCategories(appCategories)
        } catch (e: Exception) {
            appLogger.e(TAG, "Error upserting ${appCategories.size} categories", e)
        }
    }
}