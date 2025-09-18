package dev.sadakat.screentimetracker.core.data.repository

import dev.sadakat.screentimetracker.core.domain.model.*
import dev.sadakat.screentimetracker.core.domain.repository.*
import dev.sadakat.screentimetracker.core.data.mapper.UserGoalDataMapper
import dev.sadakat.screentimetracker.data.local.dao.UserGoalDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Implementation of UserGoalRepository that bridges domain interfaces with data layer.
 */
class UserGoalRepositoryImpl @Inject constructor(
    private val userGoalDao: UserGoalDao,
    private val dataMapper: UserGoalDataMapper
) : UserGoalRepository {

    override suspend fun saveGoal(goal: UserGoal) {
        if (dataMapper.validateUserGoal(goal)) {
            val entity = dataMapper.mapToUserGoalEntity(goal)
            userGoalDao.insertGoal(entity)
        }
    }

    override suspend fun updateGoal(goal: UserGoal) {
        if (dataMapper.validateUserGoal(goal)) {
            val entity = dataMapper.mapToUserGoalEntity(goal)
            userGoalDao.updateGoal(entity)
        }
    }

    override suspend fun deleteGoal(goalId: String) {
        // Simplified - use existing getGoalById and delete methods
        val goalIdLong = goalId.toLongOrNull() ?: return
        val entity = userGoalDao.getGoalById(goalIdLong)
        entity?.let { userGoalDao.deleteGoal(it) }
    }

    override suspend fun getGoalById(goalId: String): UserGoal? {
        val goalIdLong = goalId.toLongOrNull() ?: return null
        return userGoalDao.getGoalById(goalIdLong)?.let {
            dataMapper.mapToUserGoal(it)
        }
    }

    override suspend fun getActiveGoals(): List<UserGoal> {
        // Use Flow method and convert to List
        return userGoalDao.getActiveGoals().map { entities ->
            dataMapper.mapToUserGoals(entities)
        }.first()
    }

    override suspend fun getAllGoals(): List<UserGoal> {
        // Simplified - use existing export method
        val entities = userGoalDao.getAllUserGoalsForExport()
        return dataMapper.mapToUserGoals(entities)
    }

    override suspend fun getGoalsByType(type: GoalType): List<UserGoal> {
        val typeString = mapGoalTypeToString(type)
        return userGoalDao.getGoalsByType(typeString).map { entities ->
            dataMapper.mapToUserGoals(entities)
        }.first()
    }

    override suspend fun getCompletedGoals(timeRange: TimeRange): List<UserGoal> {
        // Simplified - filter from all goals
        val allGoals = getAllGoals()
        return allGoals.filter { !it.isActive }
    }

    override suspend fun getOverdueGoals(): List<UserGoal> {
        val currentTime = System.currentTimeMillis()
        val entities = userGoalDao.getExpiredGoals(currentTime)
        return dataMapper.mapToUserGoals(entities)
    }

    override fun observeActiveGoals(): Flow<List<UserGoal>> {
        return userGoalDao.getActiveGoals().map { entities ->
            dataMapper.mapToUserGoals(entities)
        }
    }

    override fun observeGoal(goalId: String): Flow<UserGoal?> {
        // Simplified - not implemented since DAO doesn't support it
        return kotlinx.coroutines.flow.flowOf(null)
    }

    override suspend fun updateGoalProgress(goalId: String, progress: Long) {
        val goalIdLong = goalId.toLongOrNull() ?: return
        userGoalDao.updateGoalProgress(goalIdLong, progress, System.currentTimeMillis())
    }

    override suspend fun completeGoal(goalId: String, completedAt: Long) {
        // Simplified - deactivate the goal
        val goalIdLong = goalId.toLongOrNull() ?: return
        userGoalDao.deactivateGoal(goalIdLong)
    }

    override suspend fun resetGoalProgress(goalId: String) {
        val goalIdLong = goalId.toLongOrNull() ?: return
        userGoalDao.updateGoalProgress(goalIdLong, 0, System.currentTimeMillis())
    }

    override suspend fun getGoalStats(timeRange: TimeRange): GoalStatistics {
        // Simplified implementation
        val entities = userGoalDao.getAllUserGoalsForExport()
        return dataMapper.mapToGoalStatistics(entities, timeRange)
    }

    override suspend fun getGoalCompletionHistory(timeRange: TimeRange): List<GoalCompletion> {
        // Simplified implementation
        val completedGoals = getCompletedGoals(timeRange)
        return completedGoals.map { goal ->
            GoalCompletion(
                goal = goal,
                completedAt = System.currentTimeMillis(),
                timeTaken = 0L,
                progressHistory = emptyList()
            )
        }
    }

    override suspend fun archiveCompletedGoals(beforeDate: Long) {
        // Simplified - not implemented since DAO doesn't support it
    }

    override suspend fun getGoalPerformanceAnalytics(timeRange: TimeRange): GoalPerformanceAnalytics {
        // Simplified implementation for now
        return GoalPerformanceAnalytics(
            consistencyScore = 0.7f,
            improvementTrend = "stable",
            bestPerformingGoalTypes = listOf(GoalType.SCREEN_TIME_LIMIT),
            strugglingGoalTypes = listOf(GoalType.UNLOCK_LIMIT),
            recommendedGoalAdjustments = emptyList()
        )
    }

    // ==================== Helper Methods ====================

    private fun mapGoalTypeToString(type: GoalType): String {
        return when (type) {
            GoalType.SCREEN_TIME_LIMIT -> "screen_time_limit"
            GoalType.UNLOCK_LIMIT -> "unlock_limit"
            GoalType.FOCUS_SESSIONS -> "focus_sessions"
            GoalType.PRODUCTIVE_TIME -> "productive_time"
            GoalType.APP_LIMIT -> "app_limit"
            GoalType.WELLNESS_STREAK -> "wellness_streak"
            GoalType.DIGITAL_DETOX -> "digital_detox"
            GoalType.SLEEP_HYGIENE -> "sleep_hygiene"
        }
    }

    private fun mapStringToGoalType(typeString: String): GoalType {
        return when (typeString) {
            "screen_time_limit" -> GoalType.SCREEN_TIME_LIMIT
            "unlock_limit" -> GoalType.UNLOCK_LIMIT
            "focus_sessions" -> GoalType.FOCUS_SESSIONS
            "productive_time" -> GoalType.PRODUCTIVE_TIME
            "app_limit" -> GoalType.APP_LIMIT
            "wellness_streak" -> GoalType.WELLNESS_STREAK
            "digital_detox" -> GoalType.DIGITAL_DETOX
            "sleep_hygiene" -> GoalType.SLEEP_HYGIENE
            else -> GoalType.SCREEN_TIME_LIMIT // Default fallback
        }
    }
}