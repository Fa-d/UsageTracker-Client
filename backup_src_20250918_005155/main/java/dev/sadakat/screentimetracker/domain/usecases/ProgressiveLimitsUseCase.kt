package dev.sadakat.screentimetracker.domain.usecases

import dev.sadakat.screentimetracker.data.local.ProgressiveLimit
import dev.sadakat.screentimetracker.data.local.ProgressiveLimitDao
import dev.sadakat.screentimetracker.data.local.ProgressiveMilestone
import dev.sadakat.screentimetracker.data.local.ProgressiveMilestoneDao
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class ProgressiveLimitsUseCase @Inject constructor(
    private val progressiveLimitDao: ProgressiveLimitDao,
    private val progressiveMilestoneDao: ProgressiveMilestoneDao,
    private val trackerRepository: TrackerRepository
) {
    
    suspend fun createProgressiveLimit(
        appPackageName: String,
        targetLimitMillis: Long,
        reductionPercentage: Int = 10
    ): Long {
        // Get current usage for the app (last 7 days average)
        val currentUsage = getAverageAppUsageLast7Days(appPackageName)
        val originalLimit = (currentUsage * 1.1).toLong() // Add 10% buffer
        
        val progressiveLimit = ProgressiveLimit(
            appPackageName = appPackageName,
            originalLimitMillis = originalLimit,
            targetLimitMillis = targetLimitMillis,
            currentLimitMillis = originalLimit,
            reductionPercentage = reductionPercentage,
            startDate = LocalDate.now().toString(),
            nextReductionDate = LocalDate.now().plusWeeks(1).toString(),
            isActive = true,
            progressPercentage = 0f
        )
        
        val limitId = progressiveLimitDao.insertLimit(progressiveLimit)
        
        // Create milestones
        createMilestonesForLimit(limitId, appPackageName)
        
        return limitId
    }
    
    private suspend fun createMilestonesForLimit(limitId: Long, appPackageName: String) {
        val milestones = listOf(
            ProgressiveMilestone(
                limitId = limitId,
                milestonePercentage = 25,
                rewardTitle = "Quarter Way There! 🎯",
                rewardDescription = "You've reduced your $appPackageName usage by 25%! Keep it up!"
            ),
            ProgressiveMilestone(
                limitId = limitId,
                milestonePercentage = 50,
                rewardTitle = "Halfway Champion! 🏆", 
                rewardDescription = "Amazing! You've cut your $appPackageName time in half!"
            ),
            ProgressiveMilestone(
                limitId = limitId,
                milestonePercentage = 75,
                rewardTitle = "Digital Warrior! ⚡",
                rewardDescription = "Incredible progress! 75% reduction achieved!"
            ),
            ProgressiveMilestone(
                limitId = limitId,
                milestonePercentage = 100,
                rewardTitle = "Limit Master! 🌟",
                rewardDescription = "You've reached your target! Digital wellness achieved!"
            )
        )
        
        progressiveMilestoneDao.insertMilestones(milestones)
    }
    
    suspend fun processWeeklyReductions() {
        val currentDate = LocalDate.now().toString()
        val limitsToReduce = progressiveLimitDao.getLimitsReadyForReduction(currentDate)
        
        limitsToReduce.forEach { limit ->
            val reductionAmount = (limit.currentLimitMillis * limit.reductionPercentage / 100.0).toLong()
            val newLimit = maxOf(
                limit.currentLimitMillis - reductionAmount,
                limit.targetLimitMillis
            )
            
            val totalReduction = limit.originalLimitMillis - newLimit
            val totalPossibleReduction = limit.originalLimitMillis - limit.targetLimitMillis
            val progressPercentage = (totalReduction.toFloat() / totalPossibleReduction.toFloat() * 100f).coerceIn(0f, 100f)
            
            val updatedLimit = limit.copy(
                currentLimitMillis = newLimit,
                nextReductionDate = LocalDate.now().plusWeeks(1).toString(),
                progressPercentage = progressPercentage,
                isActive = newLimit > limit.targetLimitMillis
            )
            
            progressiveLimitDao.updateLimit(updatedLimit)
            
            // Check and update milestones
            updateMilestoneProgress(limit.id, progressPercentage)
        }
    }
    
    private suspend fun updateMilestoneProgress(limitId: Long, progressPercentage: Float) {
        val milestones = progressiveMilestoneDao.getMilestonesForLimit(limitId)
        val currentDate = LocalDate.now().toString()
        
        milestones.forEach { milestone ->
            if (!milestone.isAchieved && progressPercentage >= milestone.milestonePercentage) {
                progressiveMilestoneDao.markMilestoneAchieved(
                    limitId, 
                    milestone.milestonePercentage,
                    currentDate
                )
            }
        }
    }
    
    fun getAllActiveLimits(): Flow<List<ProgressiveLimit>> {
        return progressiveLimitDao.getAllActiveLimits()
    }
    
    suspend fun getActiveLimitForApp(packageName: String): ProgressiveLimit? {
        return progressiveLimitDao.getActiveLimitForApp(packageName)
    }
    
    suspend fun cancelProgressiveLimit(packageName: String) {
        progressiveLimitDao.deactivateLimitForApp(packageName)
    }
    
    suspend fun getUncelebratedMilestones(): List<ProgressiveMilestone> {
        return progressiveMilestoneDao.getUncelebratedMilestones()
    }
    
    suspend fun markCelebrationShown(milestoneId: Long) {
        progressiveMilestoneDao.markCelebrationShown(milestoneId)
    }
    
    private suspend fun getAverageAppUsageLast7Days(packageName: String): Long {
        return trackerRepository.getAverageAppUsageLast7Days(packageName)
    }
}