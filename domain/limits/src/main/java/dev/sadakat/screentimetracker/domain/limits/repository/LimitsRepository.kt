package dev.sadakat.screentimetracker.domain.limits.repository

import dev.sadakat.screentimetracker.core.database.entities.UserGoal
import kotlinx.coroutines.flow.Flow

interface LimitsRepository {
    suspend fun insertAppLimit(limit: AppLimit)
    suspend fun updateAppLimit(limit: AppLimit)
    suspend fun deleteAppLimit(packageName: String)
    suspend fun getAppLimit(packageName: String): AppLimit?
    fun getAllAppLimits(): Flow<List<AppLimit>>
    fun getActiveAppLimits(): Flow<List<AppLimit>>

    suspend fun insertCategoryLimit(limit: CategoryLimit)
    suspend fun updateCategoryLimit(limit: CategoryLimit)
    suspend fun deleteCategoryLimit(categoryId: String)
    suspend fun getCategoryLimit(categoryId: String): CategoryLimit?
    fun getAllCategoryLimits(): Flow<List<CategoryLimit>>
    fun getActiveCategoryLimits(): Flow<List<CategoryLimit>>

    suspend fun insertScreenTimeGoal(goal: UserGoal)
    suspend fun updateScreenTimeGoal(goal: UserGoal)
    suspend fun getActiveScreenTimeGoal(): UserGoal?
    fun getAllScreenTimeGoals(): Flow<List<UserGoal>>

    suspend fun logLimitViolation(violation: LimitViolation)
    fun getLimitViolationsForDate(date: Long): Flow<List<LimitViolation>>
    fun getLimitViolationsInRange(startDate: Long, endDate: Long): Flow<List<LimitViolation>>

    suspend fun isAppBlocked(packageName: String): Boolean
    suspend fun isCategoryBlocked(categoryId: String): Boolean
    suspend fun getBlockedApps(): List<String>
    suspend fun getBlockedCategories(): List<String>
}

data class AppLimit(
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val dailyLimitMinutes: Int,
    val isActive: Boolean = true,
    val blockWhenExceeded: Boolean = false,
    val allowedBreaksPerDay: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis()
)

data class CategoryLimit(
    val id: Long = 0,
    val categoryId: String,
    val categoryName: String,
    val dailyLimitMinutes: Int,
    val isActive: Boolean = true,
    val blockWhenExceeded: Boolean = false,
    val allowedBreaksPerDay: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis()
)

data class LimitViolation(
    val id: Long = 0,
    val limitType: LimitType,
    val limitId: String, // packageName for app limits, categoryId for category limits, "screen_time" for screen time goals
    val limitName: String,
    val violationDate: Long,
    val exceededByMinutes: Int,
    val wasBlocked: Boolean,
    val breaksUsed: Int = 0,
    val recordedAt: Long = System.currentTimeMillis()
)

enum class LimitType {
    APP_LIMIT,
    CATEGORY_LIMIT,
    SCREEN_TIME_GOAL
}