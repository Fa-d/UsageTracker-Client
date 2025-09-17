package dev.sadakat.screentimetracker.domain.repository

import dev.sadakat.screentimetracker.data.local.AppCategory
import kotlinx.coroutines.flow.Flow

interface AppCategoryRepository {
    suspend fun getCategoryByPackage(packageName: String): AppCategory?
    fun getCategoryByPackageFlow(packageName: String): Flow<AppCategory?>
    suspend fun getAppsByCategory(category: String): List<AppCategory>
    suspend fun getAllCategories(): List<AppCategory>
    fun getAllCategoriesFlow(): Flow<List<AppCategory>>
    suspend fun getDistinctCategories(): List<String>
    suspend fun getAppCountByCategory(category: String): Int
    suspend fun getCategoriesBySource(source: String): List<AppCategory>
    suspend fun getStaleCategories(timestamp: Long): List<AppCategory>
    suspend fun insertCategory(appCategory: AppCategory)
    suspend fun insertCategories(appCategories: List<AppCategory>)
    suspend fun updateCategory(appCategory: AppCategory)
    suspend fun deleteCategory(appCategory: AppCategory)
    suspend fun deleteCategoryByPackage(packageName: String)
    suspend fun deleteCategoriesBySource(source: String)
    suspend fun deleteAllCategories()
    suspend fun updateCategoryManually(packageName: String, category: String, timestamp: Long = System.currentTimeMillis())
    suspend fun upsertCategories(appCategories: List<AppCategory>)
}