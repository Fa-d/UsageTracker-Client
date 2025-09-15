package dev.sadakat.screentimetracker.domain.tracking.repository

import dev.sadakat.screentimetracker.core.database.entities.AppCategory
import dev.sadakat.screentimetracker.core.database.dao.AppCategoryDao
import dev.sadakat.screentimetracker.domain.tracking.repository.AppCategoryRepository
import dev.sadakat.screentimetracker.core.common.util.logger.AppLogger
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

    override suspend fun insertCategory(category: AppCategory) {
        try {
            appCategoryDao.insertCategory(category)
        } catch (e: Exception) {
            appLogger.e(TAG, "Error inserting category", e)
        }
    }

    override suspend fun updateCategory(category: AppCategory) {
        try {
            appCategoryDao.updateCategory(category)
        } catch (e: Exception) {
            appLogger.e(TAG, "Error updating category", e)
        }
    }

    override suspend fun deleteCategory(category: AppCategory) {
        try {
            appCategoryDao.deleteCategory(category)
        } catch (e: Exception) {
            appLogger.e(TAG, "Error deleting category", e)
        }
    }

    override suspend fun getCategoryById(categoryId: String): AppCategory? {
        return try {
            appCategoryDao.getCategoryById(categoryId)
        } catch (e: Exception) {
            appLogger.e(TAG, "Error getting category by id $categoryId", e)
            null
        }
    }

    override fun getAllCategories(): Flow<List<AppCategory>> {
        return appCategoryDao.getAllCategories()
    }

    override fun getCategoriesByType(type: String): Flow<List<AppCategory>> {
        return appCategoryDao.getCategoriesByType(type)
    }

    override suspend fun getCategoryByPackageName(packageName: String): AppCategory? {
        return try {
            appCategoryDao.getCategoryByPackageName(packageName)
        } catch (e: Exception) {
            appLogger.e(TAG, "Error getting category for package $packageName", e)
            null
        }
    }
}