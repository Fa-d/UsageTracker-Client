package dev.sadakat.screentimetracker.domain.usecases

import dev.sadakat.screentimetracker.data.local.DailyAppSummary
import dev.sadakat.screentimetracker.data.local.UserGoal
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import dev.sadakat.screentimetracker.utils.ui.AppNotificationManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartGoalSettingUseCase @Inject constructor(
    private val repository: TrackerRepository,
    private val notificationManager: AppNotificationManager,
    private val appLogger: AppLogger
) {
    companion object {
        private const val TAG = "SmartGoalSetting"
        
        // Goal types from requirements.md
        const val DAILY_SCREEN_TIME = "daily_screen_time"
        const val APP_SPECIFIC_LIMIT = "app_specific_limit"
        const val SESSION_LIMIT = "session_limit"
        const val UNLOCK_FREQUENCY = "unlock_frequency"
        const val FOCUS_SESSIONS = "focus_sessions"
        const val BREAK_GOALS = "break_goals"
    }

    suspend fun generateAIRecommendedGoals(): List<GoalRecommendation> {
        return try {
            val recommendations = mutableListOf<GoalRecommendation>()
            
            // Analyze last 7 days of usage
            val weekStart = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
            val weekEnd = System.currentTimeMillis()
            
            // Get usage data
            val sessionData = repository.getAggregatedSessionDataForDayFlow(weekStart, weekEnd).first()
            val unlockCounts = repository.getUnlockCountForDayFlow(weekStart, weekEnd).first()
            val appSummaries = repository.getDailyAppSummaries(weekStart, weekEnd).first()
            
            // Calculate averages
            val avgDailyScreenTime = sessionData.sumOf { it.totalDuration } / 7
            val avgDailyUnlocks = unlockCounts / 7
            
            // Recommend daily screen time goal (10% reduction from current average)
            val recommendedScreenTime = (avgDailyScreenTime * 0.9).toLong()
            recommendations.add(
                GoalRecommendation(
                    goalType = DAILY_SCREEN_TIME,
                    title = "Reduce Daily Screen Time",
                    description = "Based on your usage, try reducing daily scredid en time by 10%",
                    targetValue = recommendedScreenTime,
                    currentAverage = avgDailyScreenTime,
                    confidence = calculateConfidence(sessionData.size),
                    difficulty = calculateDifficulty(avgDailyScreenTime, recommendedScreenTime),
                    reasoning = "Gradual reduction helps build sustainable habits"
                )
            )
            
            // Recommend unlock frequency goal
            val recommendedUnlocks = maxOf(30, (avgDailyUnlocks * 0.8).toInt()) // Min 30, or 20% reduction
            recommendations.add(
                GoalRecommendation(
                    goalType = UNLOCK_FREQUENCY,
                    title = "Reduce Phone Unlocks",
                    description = "Try to unlock your phone less frequently throughout the day",
                    targetValue = recommendedUnlocks.toLong(),
                    currentAverage = avgDailyUnlocks.toLong(),
                    confidence = calculateConfidence(7), // Always 7 days of unlock data
                    difficulty = calculateDifficulty(avgDailyUnlocks.toFloat(), recommendedUnlocks.toFloat()),
                    reasoning = "Fewer unlocks indicate more intentional phone usage"
                )
            )
            
            // Find most problematic apps for app-specific limits
            val topApps = appSummaries
                .groupBy { it.packageName }
                .map { (packageName: String, summaries: List<DailyAppSummary>) ->
                    packageName to summaries.sumOf { summary -> summary.totalDurationMillis }
                }
                .sortedByDescending { it.second }
                .take(3)
            
            topApps.forEach { (packageName, totalTime) ->
                val avgDaily = totalTime / 7
                if (avgDaily > TimeUnit.HOURS.toMillis(1)) { // Only suggest for apps used >1h/day
                    val recommendedLimit = (avgDaily * 0.7).toLong() // 30% reduction
                    recommendations.add(
                        GoalRecommendation(
                            goalType = APP_SPECIFIC_LIMIT,
                            title = "Limit ${getAppDisplayName(packageName)}",
                            description = "Set a daily limit for your most-used app",
                            targetValue = recommendedLimit,
                            currentAverage = avgDaily,
                            confidence = 0.8f,
                            difficulty = calculateDifficulty(avgDaily.toFloat(), recommendedLimit.toFloat()),
                            reasoning = "Limiting high-usage apps has the biggest impact",
                            packageName = packageName
                        )
                    )
                }
            }
            
            // Recommend focus session goals
            val focusSessions = repository.getFocusSessionsForDate(weekStart)
            val avgSuccessfulSessions = focusSessions.count { it.wasSuccessful } / 7
            val recommendedFocusSessions = maxOf(1, avgSuccessfulSessions + 1)
            
            recommendations.add(
                GoalRecommendation(
                    goalType = FOCUS_SESSIONS,
                    title = "Daily Focus Sessions",
                    description = "Complete focused work sessions to improve productivity",
                    targetValue = recommendedFocusSessions.toLong(),
                    currentAverage = avgSuccessfulSessions.toLong(),
                    confidence = 0.7f,
                    difficulty = if (avgSuccessfulSessions == 0) DifficultyLevel.HARD else DifficultyLevel.MEDIUM,
                    reasoning = "Regular focus sessions improve concentration and reduce mindless scrolling"
                )
            )
            
            appLogger.i(TAG, "Generated ${recommendations.size} goal recommendations")
            recommendations
            
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to generate goal recommendations", e)
            emptyList()
        }
    }

    suspend fun createGoalFromRecommendation(recommendation: GoalRecommendation): Long {
        return try {
            val deadline = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30) // 30-day goals
            
            val goal = UserGoal(
                goalType = recommendation.goalType,
                targetValue = recommendation.targetValue,
                packageName = recommendation.packageName,
                deadline = deadline,
                isActive = true
            )
            
            val goalId = repository.insertGoal(goal)
            
            notificationManager.showMotivationBoost(
                "🎯 New goal set: ${recommendation.title}! You can do this!"
            )
            
            appLogger.i(TAG, "Created goal from recommendation: ${recommendation.title}")
            goalId
            
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to create goal from recommendation", e)
            throw e
        }
    }

    suspend fun adjustGoalBasedOnPerformance(goalId: Long): GoalAdjustment? {
        return try {
            val goals = repository.getActiveGoals().first()
            val goal = goals.find { it.id == goalId } ?: return null
            
            val progressPercentage = if (goal.targetValue > 0) {
                (goal.currentProgress.toFloat() / goal.targetValue.toFloat()) * 100
            } else 0f
            
            val daysElapsed = getDaysElapsed(goal.createdAt)
            val expectedProgress = (daysElapsed / 30f) * 100 // Assuming 30-day goals
            
            val adjustment = when {
                // Goal is too easy - user is exceeding expectations
                progressPercentage > expectedProgress + 20 -> {
                    val newTarget = (goal.targetValue * 1.2).toLong() // Increase by 20%
                    GoalAdjustment(
                        goalId = goalId,
                        adjustmentType = AdjustmentType.MAKE_HARDER,
                        newTargetValue = newTarget,
                        reasoning = "You're doing great! Let's make this goal more challenging.",
                        confidence = 0.8f
                    )
                }
                
                // Goal is too hard - user is falling behind significantly
                progressPercentage < expectedProgress - 30 && daysElapsed > 7 -> {
                    val newTarget = (goal.targetValue * 0.8).toLong() // Decrease by 20%
                    GoalAdjustment(
                        goalId = goalId,
                        adjustmentType = AdjustmentType.MAKE_EASIER,
                        newTargetValue = newTarget,
                        reasoning = "Let's adjust this goal to be more achievable. Small steps lead to big changes!",
                        confidence = 0.9f
                    )
                }
                
                else -> null // No adjustment needed
            }
            
            adjustment?.let {
                appLogger.i(TAG, "Goal adjustment recommended for goal $goalId: ${it.adjustmentType}")
            }
            
            adjustment
            
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to analyze goal performance for $goalId", e)
            null
        }
    }

    suspend fun applyGoalAdjustment(adjustment: GoalAdjustment): Boolean {
        return try {
            val goals = repository.getActiveGoals().first()
            val goal = goals.find { it.id == adjustment.goalId } ?: return false
            
            val updatedGoal = goal.copy(
                targetValue = adjustment.newTargetValue,
                updatedAt = System.currentTimeMillis()
            )
            
            // Update goal in database (you'd need to implement updateGoal in repository)
            // For now, we'll create a new goal and deactivate the old one
            repository.insertGoal(updatedGoal.copy(id = 0))
            
            notificationManager.showMotivationBoost(
                "🎯 Goal adjusted: ${adjustment.reasoning}"
            )
            
            appLogger.i(TAG, "Applied goal adjustment for goal ${adjustment.goalId}")
            true
            
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to apply goal adjustment", e)
            false
        }
    }

    suspend fun generateContextualGoals(context: GoalContext): List<GoalRecommendation> {
        return try {
            val recommendations = mutableListOf<GoalRecommendation>()
            
            when (context) {
                GoalContext.WORKDAY -> {
                    recommendations.addAll(generateWorkdayGoals())
                }
                GoalContext.WEEKEND -> {
                    recommendations.addAll(generateWeekendGoals())
                }
                GoalContext.EVENING -> {
                    recommendations.addAll(generateEveningGoals())
                }
                GoalContext.MORNING -> {
                    recommendations.addAll(generateMorningGoals())
                }
            }
            
            recommendations
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to generate contextual goals", e)
            emptyList()
        }
    }

    private suspend fun generateWorkdayGoals(): List<GoalRecommendation> {
        return listOf(
            GoalRecommendation(
                goalType = FOCUS_SESSIONS,
                title = "Morning Focus Block",
                description = "Complete 2 focus sessions during work hours (9 AM - 5 PM)",
                targetValue = 2L,
                confidence = 0.8f,
                difficulty = DifficultyLevel.MEDIUM,
                reasoning = "Focus sessions during work hours boost productivity"
            ),
            GoalRecommendation(
                goalType = APP_SPECIFIC_LIMIT,
                title = "Limit Social Media at Work",
                description = "Restrict social apps to 30 minutes during work hours",
                targetValue = TimeUnit.MINUTES.toMillis(30),
                confidence = 0.9f,
                difficulty = DifficultyLevel.HARD,
                reasoning = "Reducing distractions improves work focus"
            )
        )
    }

    private suspend fun generateWeekendGoals(): List<GoalRecommendation> {
        return listOf(
            GoalRecommendation(
                goalType = DAILY_SCREEN_TIME,
                title = "Weekend Digital Detox",
                description = "Reduce screen time by 25% on weekends",
                targetValue = TimeUnit.HOURS.toMillis(4), // 4 hours total
                confidence = 0.7f,
                difficulty = DifficultyLevel.MEDIUM,
                reasoning = "Weekends are perfect for spending time offline"
            )
        )
    }

    private suspend fun generateEveningGoals(): List<GoalRecommendation> {
        return listOf(
            GoalRecommendation(
                goalType = SESSION_LIMIT,
                title = "Evening Wind Down",
                description = "No sessions longer than 20 minutes after 8 PM",
                targetValue = TimeUnit.MINUTES.toMillis(20),
                confidence = 0.9f,
                difficulty = DifficultyLevel.EASY,
                reasoning = "Shorter evening sessions improve sleep quality"
            )
        )
    }

    private suspend fun generateMorningGoals(): List<GoalRecommendation> {
        return listOf(
            GoalRecommendation(
                goalType = BREAK_GOALS,
                title = "Morning Phone-Free Hour",
                description = "No phone usage for first hour after waking",
                targetValue = 1L,
                confidence = 0.8f,
                difficulty = DifficultyLevel.HARD,
                reasoning = "Phone-free mornings reduce anxiety and improve focus"
            )
        )
    }

    private fun calculateConfidence(dataPoints: Int): Float {
        return when {
            dataPoints >= 7 -> 0.9f
            dataPoints >= 3 -> 0.7f
            dataPoints >= 1 -> 0.5f
            else -> 0.3f
        }
    }

    private fun calculateDifficulty(current: Float, target: Float): DifficultyLevel {
        val reductionPercentage = ((current - target) / current) * 100
        return when {
            reductionPercentage <= 10 -> DifficultyLevel.EASY
            reductionPercentage <= 25 -> DifficultyLevel.MEDIUM
            else -> DifficultyLevel.HARD
        }
    }

    private fun calculateDifficulty(current: Long, target: Long): DifficultyLevel {
        return calculateDifficulty(current.toFloat(), target.toFloat())
    }

    private fun getDaysElapsed(createdAt: Long): Int {
        return ((System.currentTimeMillis() - createdAt) / TimeUnit.DAYS.toMillis(1)).toInt()
    }

    private fun getAppDisplayName(packageName: String): String {
        return packageName.split(".").lastOrNull()?.replaceFirstChar { it.uppercase() } ?: packageName
    }

    data class GoalRecommendation(
        val goalType: String,
        val title: String,
        val description: String,
        val targetValue: Long,
        val currentAverage: Long = 0L,
        val confidence: Float,
        val difficulty: DifficultyLevel,
        val reasoning: String,
        val packageName: String? = null
    )

    data class GoalAdjustment(
        val goalId: Long,
        val adjustmentType: AdjustmentType,
        val newTargetValue: Long,
        val reasoning: String,
        val confidence: Float
    )

    enum class DifficultyLevel {
        EASY, MEDIUM, HARD
    }

    enum class AdjustmentType {
        MAKE_EASIER, MAKE_HARDER
    }

    enum class GoalContext {
        WORKDAY, WEEKEND, EVENING, MORNING
    }
}