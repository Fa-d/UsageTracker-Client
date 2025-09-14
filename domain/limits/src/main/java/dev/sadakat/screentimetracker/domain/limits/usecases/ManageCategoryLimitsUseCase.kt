package dev.sadakat.screentimetracker.domain.limits.usecases

import dev.sadakat.screentimetracker.domain.limits.repository.CategoryLimit
import dev.sadakat.screentimetracker.domain.limits.repository.LimitsRepository
import android.util.Log
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageCategoryLimitsUseCase @Inject constructor(
    private val repository: LimitsRepository
) {
    companion object {
        private const val TAG = "ManageCategoryLimitsUseCase"
    }

    suspend fun createCategoryLimit(
        categoryId: String,
        categoryName: String,
        dailyLimitMinutes: Int,
        blockWhenExceeded: Boolean = false,
        allowedBreaksPerDay: Int = 0
    ): Result<CategoryLimit> {
        return try {
            val existingLimit = repository.getCategoryLimit(categoryId)
            if (existingLimit != null) {
                Log.w(TAG, "Category limit already exists for $categoryId")
                return Result.failure(Exception("Category limit already exists for this category"))
            }

            val categoryLimit = CategoryLimit(
                categoryId = categoryId,
                categoryName = categoryName,
                dailyLimitMinutes = dailyLimitMinutes,
                blockWhenExceeded = blockWhenExceeded,
                allowedBreaksPerDay = allowedBreaksPerDay
            )

            repository.insertCategoryLimit(categoryLimit)
            Log.i(TAG, "Created category limit for $categoryName: ${dailyLimitMinutes}min")
            Result.success(categoryLimit)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to create category limit for $categoryId", e)
            Result.failure(e)
        }
    }

    suspend fun updateCategoryLimit(
        categoryId: String,
        dailyLimitMinutes: Int? = null,
        blockWhenExceeded: Boolean? = null,
        allowedBreaksPerDay: Int? = null,
        isActive: Boolean? = null
    ): Result<CategoryLimit> {
        return try {
            val existingLimit = repository.getCategoryLimit(categoryId)
                ?: return Result.failure(Exception("Category limit not found for $categoryId"))

            val updatedLimit = existingLimit.copy(
                dailyLimitMinutes = dailyLimitMinutes ?: existingLimit.dailyLimitMinutes,
                blockWhenExceeded = blockWhenExceeded ?: existingLimit.blockWhenExceeded,
                allowedBreaksPerDay = allowedBreaksPerDay ?: existingLimit.allowedBreaksPerDay,
                isActive = isActive ?: existingLimit.isActive,
                modifiedAt = System.currentTimeMillis()
            )

            repository.updateCategoryLimit(updatedLimit)
            Log.i(TAG, "Updated category limit for ${existingLimit.categoryName}")
            Result.success(updatedLimit)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to update category limit for $categoryId", e)
            Result.failure(e)
        }
    }

    suspend fun deleteCategoryLimit(categoryId: String): Result<Unit> {
        return try {
            val existingLimit = repository.getCategoryLimit(categoryId)
            if (existingLimit == null) {
                Log.w(TAG, "Category limit not found for $categoryId")
                return Result.failure(Exception("Category limit not found"))
            }

            repository.deleteCategoryLimit(categoryId)
            Log.i(TAG, "Deleted category limit for ${existingLimit.categoryName}")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete category limit for $categoryId", e)
            Result.failure(e)
        }
    }

    suspend fun getCategoryLimit(categoryId: String): CategoryLimit? {
        return try {
            repository.getCategoryLimit(categoryId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get category limit for $categoryId", e)
            null
        }
    }

    fun getAllCategoryLimits(): Flow<List<CategoryLimit>> {
        return repository.getAllCategoryLimits()
    }

    fun getActiveCategoryLimits(): Flow<List<CategoryLimit>> {
        return repository.getActiveCategoryLimits()
    }

    suspend fun toggleCategoryLimitStatus(categoryId: String): Result<CategoryLimit> {
        return try {
            val existingLimit = repository.getCategoryLimit(categoryId)
                ?: return Result.failure(Exception("Category limit not found for $categoryId"))

            val updatedLimit = existingLimit.copy(
                isActive = !existingLimit.isActive,
                modifiedAt = System.currentTimeMillis()
            )

            repository.updateCategoryLimit(updatedLimit)
            val status = if (updatedLimit.isActive) "enabled" else "disabled"
            Log.i(TAG, "Category limit $status for ${existingLimit.categoryName}")
            Result.success(updatedLimit)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle category limit status for $categoryId", e)
            Result.failure(e)
        }
    }

    fun getCommonCategoryLimits(): List<Pair<String, String>> {
        return listOf(
            "social_media" to "Social Media",
            "games" to "Games",
            "entertainment" to "Entertainment",
            "news" to "News",
            "shopping" to "Shopping",
            "productivity" to "Productivity",
            "communication" to "Communication",
            "education" to "Education",
            "tools" to "Tools & Utilities",
            "health" to "Health & Fitness"
        )
    }
}