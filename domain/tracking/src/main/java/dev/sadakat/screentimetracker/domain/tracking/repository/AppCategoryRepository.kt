package dev.sadakat.screentimetracker.domain.tracking.repository

import dev.sadakat.screentimetracker.core.database.entities.AppCategory
import kotlinx.coroutines.flow.Flow

interface AppCategoryRepository {
    suspend fun insertCategory(category: AppCategory)
    suspend fun updateCategory(category: AppCategory)
    suspend fun deleteCategory(category: AppCategory)
    suspend fun getCategoryById(categoryId: String): AppCategory?
    fun getAllCategories(): Flow<List<AppCategory>>
    fun getCategoriesByType(type: String): Flow<List<AppCategory>>
    suspend fun getCategoryByPackageName(packageName: String): AppCategory?
}