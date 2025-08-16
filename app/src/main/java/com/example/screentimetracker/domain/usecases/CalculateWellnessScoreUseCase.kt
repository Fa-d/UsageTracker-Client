package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.domain.model.WellnessLevel
import com.example.screentimetracker.domain.model.WellnessScore
import com.example.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CalculateWellnessScoreUseCase @Inject constructor(
    private val repository: TrackerRepository
) {
    suspend operator fun invoke(date: Long, forceRecalculate: Boolean = false): WellnessScore {
        val startOfDay = getStartOfDay(date)
        val existingScore = repository.getAllWellnessScores().first().find { it.date == startOfDay }
        
        // Check if we should recalculate based on time or force flag
        val shouldRecalculate = forceRecalculate || shouldRecalculateScore(existingScore)
        
        if (existingScore != null && !shouldRecalculate) {
            return existingScore
        } else {
            // Calculate real wellness score based on actual data
            val endOfDay = startOfDay + (24 * 60 * 60 * 1000L) - 1
            
            val timeLimitScore = calculateTimeLimitScore(startOfDay, endOfDay)
            val focusSessionScore = calculateFocusSessionScore(startOfDay, endOfDay)
            val breaksScore = calculateBreaksScore(startOfDay, endOfDay)
            val sleepHygieneScore = calculateSleepHygieneScore(startOfDay, endOfDay)
            
            // Weighted average: Time Limits (30%), Focus (25%), Breaks (20%), Sleep (25%)
            val totalScore = (
                timeLimitScore * 0.30 +
                focusSessionScore * 0.25 +
                breaksScore * 0.20 +
                sleepHygieneScore * 0.25
            ).toInt().coerceIn(0, 100)
            
            val level = WellnessLevel.fromScore(totalScore)
            
            val wellnessScore = WellnessScore(
                date = startOfDay,
                totalScore = totalScore,
                timeLimitScore = timeLimitScore,
                focusSessionScore = focusSessionScore,
                breaksScore = breaksScore,
                sleepHygieneScore = sleepHygieneScore,
                level = level,
                calculatedAt = System.currentTimeMillis()
            )
            
            // Save calculated score to database (insert or update)
            repository.insertWellnessScore(wellnessScore)
            return wellnessScore
        }
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    private fun shouldRecalculateScore(existingScore: WellnessScore?): Boolean {
        if (existingScore == null) return true
        
        val currentTime = System.currentTimeMillis()
        val timeSinceCalculation = currentTime - existingScore.calculatedAt
        
        // Recalculate every 30 minutes for dynamic updates during the day
        val recalculationInterval = 30 * 60 * 1000L // 30 minutes
        
        return timeSinceCalculation > recalculationInterval
    }
    
    private suspend fun calculateTimeLimitScore(startOfDay: Long, endOfDay: Long): Int {
        return try {
            // Get user's active goals for the day
            val activeGoals = repository.getActiveGoals().first()
            if (activeGoals.isEmpty()) return 50 // Neutral score if no goals set
            
            // Get actual usage for the day
            val sessionData = repository.getAggregatedSessionDataForDayFlow(startOfDay, endOfDay).first()
            val totalUsageMillis = sessionData.sumOf { it.totalDuration }
            
            // Check against daily screen time goals
            val dailyGoals = activeGoals.filter { it.goalType == "daily_screen_time" }
            if (dailyGoals.isEmpty()) return 50
            
            val targetMillis = dailyGoals.minOfOrNull { it.targetValue } ?: return 50
            
            // Calculate score based on how well user stayed within limits
            when {
                totalUsageMillis <= targetMillis -> 100 // Perfect adherence
                totalUsageMillis <= targetMillis * 1.1 -> 90 // Within 10% of goal
                totalUsageMillis <= targetMillis * 1.25 -> 70 // Within 25% of goal  
                totalUsageMillis <= targetMillis * 1.5 -> 50 // Within 50% of goal
                totalUsageMillis <= targetMillis * 2.0 -> 30 // Within 100% of goal
                else -> 10 // Significantly over goal
            }
        } catch (e: Exception) {
            50 // Default neutral score on error
        }
    }
    
    private suspend fun calculateFocusSessionScore(startOfDay: Long, endOfDay: Long): Int {
        return try {
            val focusSessions = repository.getFocusSessionsForDate(startOfDay)
            val daysSessions = focusSessions.filter { 
                it.startTime >= startOfDay && it.startTime < endOfDay 
            }
            
            val completedSessions = daysSessions.count { it.wasSuccessful }
            val totalSessions = daysSessions.size
            
            // Score based on focus session completion
            when {
                totalSessions == 0 -> 30 // No focus sessions attempted
                completedSessions >= 3 -> 100 // Excellent focus
                completedSessions == 2 -> 85 // Great focus
                completedSessions == 1 -> 70 // Good focus
                totalSessions >= 3 && completedSessions == 0 -> 10 // Many attempts, no success
                else -> 40 // Some attempts made
            }
        } catch (e: Exception) {
            30
        }
    }
    
    private suspend fun calculateBreaksScore(startOfDay: Long, endOfDay: Long): Int {
        return try {
            val sessionData = repository.getAggregatedSessionDataForDayFlow(startOfDay, endOfDay).first()
            val unlockCount = repository.getUnlockCountForDayFlow(startOfDay, endOfDay).first()
            
            if (sessionData.isEmpty()) return 50
            
            val totalSessions = sessionData.sumOf { it.sessionCount }
            val totalUsageHours = sessionData.sumOf { it.totalDuration } / (1000.0 * 60 * 60)
            
            // Calculate average session length and unlock frequency
            val avgSessionMinutes = if (totalSessions > 0) {
                (totalUsageHours * 60) / totalSessions
            } else 0.0
            
            val unlocksPerHour = if (totalUsageHours > 0) unlockCount / totalUsageHours else 0.0
            
            // Score based on healthy break patterns
            val sessionLengthScore = when {
                avgSessionMinutes <= 15 -> 100 // Short healthy sessions
                avgSessionMinutes <= 30 -> 80 // Moderate sessions  
                avgSessionMinutes <= 60 -> 60 // Longer sessions
                else -> 20 // Very long sessions without breaks
            }
            
            val unlockFrequencyScore = when {
                unlocksPerHour <= 5 -> 100 // Focused usage
                unlocksPerHour <= 10 -> 80 // Moderate checking
                unlocksPerHour <= 20 -> 60 // Frequent checking
                else -> 20 // Compulsive checking
            }
            
            // Combined score weighted equally
            ((sessionLengthScore + unlockFrequencyScore) / 2).toInt()
        } catch (e: Exception) {
            50
        }
    }
    
    private suspend fun calculateSleepHygieneScore(startOfDay: Long, endOfDay: Long): Int {
        return try {
            val calendar = java.util.Calendar.getInstance()
            
            // Define evening period (9 PM - 11 PM) and bedtime period (11 PM - 6 AM)
            calendar.timeInMillis = startOfDay
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 21) // 9 PM
            val eveningStart = calendar.timeInMillis
            
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23) // 11 PM  
            val bedtimeStart = calendar.timeInMillis
            
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 6) // 6 AM next day
            val bedtimeEnd = calendar.timeInMillis
            
            // Get usage during evening and bedtime hours
            val allSessions = repository.getAllSessionsInRange(startOfDay, endOfDay).first()
            
            val eveningUsage = allSessions.filter { session ->
                session.startTimeMillis >= eveningStart && session.startTimeMillis < bedtimeStart
            }.sumOf { it.durationMillis }
            
            val bedtimeUsage = allSessions.filter { session ->
                (session.startTimeMillis >= bedtimeStart && session.startTimeMillis < endOfDay) ||
                (session.startTimeMillis >= startOfDay && session.startTimeMillis < bedtimeEnd - (24 * 60 * 60 * 1000L))
            }.sumOf { it.durationMillis }
            
            // Score based on digital sunset principles
            val eveningScore = when {
                eveningUsage == 0L -> 100 // No evening usage - excellent
                eveningUsage <= 30 * 60 * 1000L -> 80 // Light evening usage
                eveningUsage <= 60 * 60 * 1000L -> 60 // Moderate evening usage
                eveningUsage <= 2 * 60 * 60 * 1000L -> 40 // Heavy evening usage
                else -> 20 // Very heavy evening usage
            }
            
            val bedtimeScore = when {
                bedtimeUsage == 0L -> 100 // No bedtime usage - perfect
                bedtimeUsage <= 15 * 60 * 1000L -> 70 // Brief bedtime usage
                bedtimeUsage <= 30 * 60 * 1000L -> 50 // Moderate bedtime usage
                bedtimeUsage <= 60 * 60 * 1000L -> 30 // Heavy bedtime usage
                else -> 10 // Very heavy bedtime usage
            }
            
            // Weight bedtime usage more heavily than evening usage
            ((eveningScore * 0.3 + bedtimeScore * 0.7).toInt())
        } catch (e: Exception) {
            50
        }
    }
}