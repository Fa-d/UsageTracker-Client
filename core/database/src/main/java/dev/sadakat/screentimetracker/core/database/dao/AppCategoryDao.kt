package dev.sadakat.screentimetracker.core.database.dao

import androidx.room.*
import dev.sadakat.screentimetracker.core.database.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppCategoryDao {
    
    @Query("SELECT * FROM app_categories WHERE packageName = :packageName LIMIT 1")
    suspend fun getCategoryByPackage(packageName: String): AppCategory?
    
    @Query("SELECT * FROM app_categories WHERE packageName = :packageName LIMIT 1")
    fun getCategoryByPackageFlow(packageName: String): Flow<AppCategory?>
    
    @Query("SELECT * FROM app_categories WHERE category = :category")
    suspend fun getAppsByCategory(category: String): List<AppCategory>
    
    @Query("SELECT * FROM app_categories")
    suspend fun getAllCategories(): List<AppCategory>
    
    @Query("SELECT * FROM app_categories")
    fun getAllCategoriesFlow(): Flow<List<AppCategory>>
    
    @Query("SELECT DISTINCT category FROM app_categories ORDER BY category")
    suspend fun getDistinctCategories(): List<String>
    
    @Query("SELECT COUNT(*) FROM app_categories WHERE category = :category")
    suspend fun getAppCountByCategory(category: String): Int
    
    @Query("SELECT * FROM app_categories WHERE source = :source")
    suspend fun getCategoriesBySource(source: String): List<AppCategory>
    
    @Query("SELECT * FROM app_categories WHERE lastUpdated < :timestamp")
    suspend fun getStaleCategories(timestamp: Long): List<AppCategory>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(appCategory: AppCategory)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(appCategories: List<AppCategory>)
    
    @Update
    suspend fun updateCategory(appCategory: AppCategory)
    
    @Delete
    suspend fun deleteCategory(appCategory: AppCategory)
    
    @Query("DELETE FROM app_categories WHERE packageName = :packageName")
    suspend fun deleteCategoryByPackage(packageName: String)
    
    @Query("DELETE FROM app_categories WHERE source = :source")
    suspend fun deleteCategoriesBySource(source: String)
    
    @Query("DELETE FROM app_categories")
    suspend fun deleteAllCategories()
    
    // Update category for manual user override
    @Query("UPDATE app_categories SET category = :category, source = 'manual', confidence = 1.0, lastUpdated = :timestamp WHERE packageName = :packageName")
    suspend fun updateCategoryManually(packageName: String, category: String, timestamp: Long = System.currentTimeMillis())
    
    // Batch operations for better performance
    @Transaction
    suspend fun upsertCategories(appCategories: List<AppCategory>) {
        insertCategories(appCategories)
    }
}