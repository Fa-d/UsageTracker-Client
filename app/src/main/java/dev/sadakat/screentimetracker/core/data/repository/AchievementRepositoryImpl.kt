package dev.sadakat.screentimetracker.core.data.repository

import dev.sadakat.screentimetracker.core.domain.model.*
import dev.sadakat.screentimetracker.core.domain.repository.*
import dev.sadakat.screentimetracker.core.data.mapper.AchievementDataMapper
import dev.sadakat.screentimetracker.data.local.dao.AchievementDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Implementation of AchievementRepository that bridges domain interfaces with data layer.
 */
class AchievementRepositoryImpl @Inject constructor(
    private val achievementDao: AchievementDao,
    private val dataMapper: AchievementDataMapper
) : AchievementRepository {

    override suspend fun saveAchievement(achievement: Achievement) {
        if (dataMapper.validateAchievement(achievement)) {
            val entity = dataMapper.mapToAchievementEntity(achievement)
            achievementDao.insertAchievement(entity)
        }
    }

    override suspend fun updateAchievement(achievement: Achievement) {
        if (dataMapper.validateAchievement(achievement)) {
            val entity = dataMapper.mapToAchievementEntity(achievement)
            achievementDao.updateAchievement(entity)
        }
    }

    override suspend fun updateAchievementProgress(achievementId: String, progress: Int) {
        achievementDao.updateAchievementProgress(achievementId, progress)
    }

    override suspend fun unlockAchievement(achievementId: String, unlockedAt: Long) {
        achievementDao.unlockAchievement(achievementId, unlockedAt)
    }

    override suspend fun getAchievementById(achievementId: String): Achievement? {
        return achievementDao.getAchievementById(achievementId)?.let {
            dataMapper.mapToAchievement(it)
        }
    }

    override suspend fun getAllAchievements(): List<Achievement> {
        val entities = achievementDao.getAllAchievementsForExport()
        return dataMapper.mapToAchievements(entities)
    }

    override suspend fun getUnlockedAchievements(): List<Achievement> {
        return achievementDao.getUnlockedAchievements().map { entities ->
            dataMapper.mapToAchievements(entities)
        }.first()
    }

    override suspend fun getLockedAchievements(): List<Achievement> {
        // Filter from all achievements since there's no specific DAO method
        val allAchievements = getAllAchievements()
        return allAchievements.filter { !it.isUnlocked }
    }

    override suspend fun getAchievementsByCategory(category: AchievementCategory): List<Achievement> {
        // Filter from all achievements since there's no specific DAO method
        val allAchievements = getAllAchievements()
        return allAchievements.filter { it.category == category }
    }

    override suspend fun getAchievementsByTier(tier: AchievementTier): List<Achievement> {
        // Filter from all achievements since there's no specific DAO method
        val allAchievements = getAllAchievements()
        return allAchievements.filter { it.tier == tier }
    }

    override suspend fun getRecentlyUnlockedAchievements(timeRange: TimeRange): List<Achievement> {
        // Filter from unlocked achievements
        val unlockedAchievements = getUnlockedAchievements()
        return unlockedAchievements.filter { achievement ->
            achievement.unlockedAt != null &&
            timeRange.contains(achievement.unlockedAt)
        }
    }

    override suspend fun getAlmostCompletedAchievements(progressThreshold: Float): List<Achievement> {
        // Filter from all achievements
        val allAchievements = getAllAchievements()
        return allAchievements.filter { achievement ->
            !achievement.isUnlocked &&
            achievement.progressPercentage >= progressThreshold * 100
        }
    }

    override fun observeAchievements(): Flow<List<Achievement>> {
        return achievementDao.getAllAchievements().map { entities ->
            dataMapper.mapToAchievements(entities)
        }
    }

    override fun observeUnlockedAchievements(): Flow<List<Achievement>> {
        return achievementDao.getUnlockedAchievements().map { entities ->
            dataMapper.mapToAchievements(entities)
        }
    }

    override fun observeAchievement(achievementId: String): Flow<Achievement?> {
        // Simplified - not directly supported by DAO
        return kotlinx.coroutines.flow.flowOf(null)
    }

    override suspend fun getAchievementStatistics(): AchievementStatistics {
        val entities = achievementDao.getAllAchievementsForExport()
        return dataMapper.mapToAchievementStatistics(entities)
    }

    override suspend fun getAchievementHistory(timeRange: TimeRange): List<AchievementUnlock> {
        val recentlyUnlocked = getRecentlyUnlockedAchievements(timeRange)
        return recentlyUnlocked.map { achievement ->
            AchievementUnlock(
                achievement = achievement,
                unlockedAt = achievement.unlockedAt ?: 0L,
                progressHistory = emptyList() // Would need separate tracking
            )
        }
    }

    override suspend fun resetAchievementProgress(achievementId: String) {
        // Update progress to 0
        achievementDao.updateAchievementProgress(achievementId, 0)
    }

    override suspend fun deleteAchievement(achievementId: String) {
        // Not supported by current DAO - would need to add method
    }

    override suspend fun updateMultipleAchievements(updates: List<AchievementProgressUpdate>) {
        updates.forEach { update ->
            achievementDao.updateAchievementProgress(update.achievementId, update.newProgress)
        }
    }

    override suspend fun getAchievementAnalytics(timeRange: TimeRange): AchievementAnalytics {
        val entities = achievementDao.getAllAchievementsForExport()
        return dataMapper.mapToAchievementAnalytics(entities, timeRange)
    }

    // ==================== Helper Methods ====================

    private fun mapCategoryToString(category: AchievementCategory): String {
        return when (category) {
            AchievementCategory.STREAK -> "STREAK"
            AchievementCategory.MINDFUL -> "MINDFUL"
            AchievementCategory.FOCUS -> "FOCUS"
            AchievementCategory.DISCIPLINE -> "CLEANER"
            AchievementCategory.BALANCE -> "WARRIOR"
            AchievementCategory.PRODUCTIVITY -> "EARLY_BIRD"
            AchievementCategory.WELLNESS -> "DIGITAL_SUNSET"
        }
    }

    private fun mapTierToString(tier: AchievementTier): String {
        return when (tier) {
            AchievementTier.BRONZE -> "bronze"
            AchievementTier.SILVER -> "silver"
            AchievementTier.GOLD -> "gold"
            AchievementTier.PLATINUM -> "platinum"
        }
    }
}