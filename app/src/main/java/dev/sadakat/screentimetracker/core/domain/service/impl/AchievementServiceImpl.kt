package dev.sadakat.screentimetracker.core.domain.service.impl

import dev.sadakat.screentimetracker.core.domain.model.Achievement
import dev.sadakat.screentimetracker.core.domain.model.AchievementCategory
import dev.sadakat.screentimetracker.core.domain.model.AchievementTier
import dev.sadakat.screentimetracker.core.domain.model.ScreenTimeMetrics
import dev.sadakat.screentimetracker.core.domain.model.UserGoal
import dev.sadakat.screentimetracker.core.domain.model.WellnessScore
import dev.sadakat.screentimetracker.core.domain.model.TimeRange
import dev.sadakat.screentimetracker.core.domain.model.GoalType
import dev.sadakat.screentimetracker.core.domain.service.AchievementService
import dev.sadakat.screentimetracker.core.domain.service.AchievementPreferences
import dev.sadakat.screentimetracker.core.domain.service.DifficultyLevel
import dev.sadakat.screentimetracker.core.domain.service.MotivationStyle
import dev.sadakat.screentimetracker.core.domain.service.AchievementStats
import dev.sadakat.screentimetracker.core.domain.service.AchievementValidation
import dev.sadakat.screentimetracker.core.domain.service.UserProgressData
import java.util.UUID

class AchievementServiceImpl : AchievementService {

    companion object {
        private const val HOURS_TO_MILLISECONDS = 3600000L
        private const val ACHIEVEMENT_PROGRESS_THRESHOLD = 0.8f
    }

    override fun updateAchievementProgress(
        achievements: List<Achievement>,
        metrics: ScreenTimeMetrics,
        goals: List<UserGoal>,
        wellnessScore: WellnessScore
    ): List<Achievement> {
        return achievements.map { achievement ->
            val newProgress = calculateAchievementProgress(achievement, metrics, goals, wellnessScore)
            achievement.addProgress(newProgress - achievement.currentProgress)
        }
    }

    override fun checkUnlockedAchievements(
        achievements: List<Achievement>
    ): List<Achievement> {
        return achievements.filter { it.canUnlock }.map { it.unlock() }
    }

    override fun generateDefaultAchievements(): List<Achievement> {
        return listOf(
            // Streak achievements
            Achievement.createStreakAchievement(3, AchievementTier.BRONZE),
            Achievement.createStreakAchievement(7, AchievementTier.SILVER),
            Achievement.createStreakAchievement(14, AchievementTier.GOLD),
            Achievement.createStreakAchievement(30, AchievementTier.PLATINUM),

            // Limit achievements
            Achievement.createLimitAchievement(3),
            Achievement.createLimitAchievement(7),
            Achievement.createLimitAchievement(14),

            // Screen time achievements
            createScreenTimeAchievement("under_4h", "4-Hour Champion", "Keep screen time under 4 hours", 4),
            createScreenTimeAchievement("under_3h", "Digital Minimalist", "Keep screen time under 3 hours", 3),
            createScreenTimeAchievement("under_2h", "Screen Time Master", "Keep screen time under 2 hours", 2),

            // Unlock achievements
            createUnlockAchievement("under_50", "Focus Keeper", "Keep unlocks under 50 per day", 50),
            createUnlockAchievement("under_30", "Mindful User", "Keep unlocks under 30 per day", 30),
            createUnlockAchievement("under_20", "Zen Master", "Keep unlocks under 20 per day", 20),

            // Wellness achievements
            createWellnessAchievement("score_70", "Wellness Warrior", "Achieve wellness score of 70+", 70),
            createWellnessAchievement("score_80", "Balance Seeker", "Achieve wellness score of 80+", 80),
            createWellnessAchievement("score_90", "Digital Guru", "Achieve wellness score of 90+", 90),

            // Productivity achievements
            createProductivityAchievement("productive_50", "Productivity Starter", "50% productive app usage", 50),
            createProductivityAchievement("productive_70", "Efficiency Expert", "70% productive app usage", 70),
            createProductivityAchievement("productive_85", "Productivity Master", "85% productive app usage", 85)
        )
    }

    override fun calculateAchievementProgress(
        achievement: Achievement,
        metrics: ScreenTimeMetrics,
        goals: List<UserGoal>,
        wellnessScore: WellnessScore
    ): Int {
        return when (achievement.category) {
            AchievementCategory.STREAK -> {
                // For streak achievements, this would be calculated based on consecutive good days
                // For now, returning current progress (would need streak tracking)
                achievement.currentProgress
            }
            AchievementCategory.DISCIPLINE -> {
                // Check if user stayed within limits
                val goalsMet = goals.filter { it.type in listOf(GoalType.SCREEN_TIME_LIMIT, GoalType.UNLOCK_LIMIT) }
                    .count { isGoalMet(it, metrics) }
                if (goalsMet > 0) achievement.currentProgress + 1 else achievement.currentProgress
            }
            AchievementCategory.WELLNESS -> {
                // Based on wellness score thresholds
                if (wellnessScore.overall >= achievement.targetValue) achievement.targetValue else achievement.currentProgress
            }
            AchievementCategory.PRODUCTIVITY -> {
                val productivityPercent = if (metrics.totalScreenTimeMillis > 0) {
                    (metrics.getProductiveTimeMillis().toFloat() / metrics.totalScreenTimeMillis * 100).toInt()
                } else 0
                if (productivityPercent >= achievement.targetValue) achievement.targetValue else achievement.currentProgress
            }
            AchievementCategory.MINDFUL -> {
                // Based on mindful usage patterns
                calculateMindfulnessProgress(achievement, metrics)
            }
            AchievementCategory.FOCUS -> {
                // Based on focus sessions and deep work
                calculateFocusProgress(achievement, metrics)
            }
            AchievementCategory.BALANCE -> {
                // Based on overall balance metrics
                calculateBalanceProgress(achievement, metrics, wellnessScore)
            }
        }
    }

    override fun getAlmostUnlockedAchievements(
        achievements: List<Achievement>,
        threshold: Float
    ): List<Achievement> {
        return achievements.filter {
            !it.isUnlocked && it.progressPercentage >= (threshold * 100)
        }
    }

    override fun suggestPersonalizedAchievements(
        currentAchievements: List<Achievement>,
        historicalMetrics: List<ScreenTimeMetrics>,
        userPreferences: AchievementPreferences
    ): List<Achievement> {
        val suggestions = mutableListOf<Achievement>()

        if (historicalMetrics.isNotEmpty()) {
            val averageMetrics = calculateAverageMetrics(historicalMetrics)

            // Suggest achievements based on user's current performance
            userPreferences.preferredCategories.forEach { category ->
                val suggestion = createPersonalizedAchievement(category, averageMetrics, userPreferences.difficultyLevel)
                if (suggestion != null && !currentAchievements.any { it.id == suggestion.id }) {
                    suggestions.add(suggestion)
                }
            }
        }

        return suggestions.take(5) // Limit to 5 suggestions
    }

    override fun calculateAchievementStats(
        achievements: List<Achievement>
    ): AchievementStats {
        val unlockedAchievements = achievements.count { it.isUnlocked }
        val completionRate = if (achievements.isNotEmpty()) {
            unlockedAchievements.toFloat() / achievements.size
        } else 0f

        val favoriteCategory = achievements
            .filter { it.isUnlocked }
            .groupBy { it.category }
            .maxByOrNull { it.value.size }?.key

        // Calculate streak (simplified - would need temporal data)
        val currentStreak = 0 // TODO: Implement streak calculation
        val longestStreak = 0 // TODO: Implement longest streak calculation

        return AchievementStats(
            totalAchievements = achievements.size,
            unlockedAchievements = unlockedAchievements,
            completionRate = completionRate,
            averageProgressTime = 0L, // TODO: Calculate from unlock timestamps
            favoriteCategory = favoriteCategory,
            currentStreak = currentStreak,
            longestStreak = longestStreak
        )
    }

    override fun validateAchievementDifficulty(
        achievement: Achievement,
        userLevel: Int,
        historicalMetrics: List<ScreenTimeMetrics>
    ): AchievementValidation {
        val difficulty = when {
            achievement.targetValue <= 30 && achievement.tier == AchievementTier.BRONZE -> DifficultyLevel.BEGINNER
            achievement.targetValue <= 60 && achievement.tier == AchievementTier.SILVER -> DifficultyLevel.INTERMEDIATE
            achievement.targetValue <= 90 && achievement.tier == AchievementTier.GOLD -> DifficultyLevel.ADVANCED
            else -> DifficultyLevel.EXPERT
        }

        val isAppropriate = when (difficulty) {
            DifficultyLevel.BEGINNER -> userLevel <= 10
            DifficultyLevel.INTERMEDIATE -> userLevel in 5..25
            DifficultyLevel.ADVANCED -> userLevel in 15..50
            DifficultyLevel.EXPERT -> userLevel >= 30
        }

        val estimatedTime = estimateTimeToComplete(achievement, historicalMetrics)
        val recommendations = generateAchievementRecommendations(achievement, isAppropriate, difficulty)

        return AchievementValidation(
            isAppropriate = isAppropriate,
            difficulty = difficulty,
            estimatedTimeToComplete = estimatedTime,
            recommendations = recommendations
        )
    }

    override fun createMilestoneAchievements(
        category: AchievementCategory,
        userProgress: UserProgressData
    ): List<Achievement> {
        return when (category) {
            AchievementCategory.WELLNESS -> createWellnessMilestones(userProgress)
            AchievementCategory.PRODUCTIVITY -> createProductivityMilestones(userProgress)
            AchievementCategory.STREAK -> createStreakMilestones(userProgress)
            AchievementCategory.FOCUS -> createFocusMilestones(userProgress)
            else -> emptyList()
        }
    }

    private fun isGoalMet(goal: UserGoal, metrics: ScreenTimeMetrics): Boolean {
        return when (goal.type) {
            GoalType.SCREEN_TIME_LIMIT -> metrics.totalScreenTimeMillis <= goal.targetValue
            GoalType.UNLOCK_LIMIT -> metrics.unlockCount <= goal.targetValue
            GoalType.PRODUCTIVE_TIME -> metrics.getProductiveTimeMillis() >= goal.targetValue
            else -> false
        }
    }

    private fun calculateMindfulnessProgress(achievement: Achievement, metrics: ScreenTimeMetrics): Int {
        // Mindfulness could be measured by session length, breaks taken, etc.
        val averageSessionTime = metrics.getAverageSessionDuration()
        val optimalSessionTime = 25 * 60 * 1000L // 25 minutes

        return if (averageSessionTime <= optimalSessionTime) {
            achievement.currentProgress + 1
        } else {
            achievement.currentProgress
        }
    }

    private fun calculateFocusProgress(achievement: Achievement, metrics: ScreenTimeMetrics): Int {
        // Focus could be measured by productive app usage, long focused sessions, etc.
        val productiveTime = metrics.getProductiveTimeMillis()
        val totalTime = metrics.totalScreenTimeMillis

        val focusRatio = if (totalTime > 0) productiveTime.toFloat() / totalTime else 0f

        return if (focusRatio >= 0.7f) { // 70% productive usage
            achievement.currentProgress + 1
        } else {
            achievement.currentProgress
        }
    }

    private fun calculateBalanceProgress(achievement: Achievement, metrics: ScreenTimeMetrics, wellnessScore: WellnessScore): Int {
        // Balance considers multiple factors
        val isBalanced = wellnessScore.overall >= 70 &&
                        metrics.totalScreenTimeMillis <= 4 * HOURS_TO_MILLISECONDS &&
                        metrics.unlockCount <= 50

        return if (isBalanced) {
            achievement.currentProgress + 1
        } else {
            achievement.currentProgress
        }
    }

    private fun calculateAverageMetrics(historicalMetrics: List<ScreenTimeMetrics>): ScreenTimeMetrics {
        // Create average metrics from historical data
        val avgScreenTime = historicalMetrics.map { it.totalScreenTimeMillis }.average().toLong()
        val avgUnlocks = historicalMetrics.map { it.unlockCount }.average().toInt()
        val avgProductiveTime = historicalMetrics.map { it.getProductiveTimeMillis() }.average().toLong()

        return ScreenTimeMetrics(
            totalScreenTimeMillis = avgScreenTime,
            unlockCount = avgUnlocks,
            appSessions = emptyList(),
            wellnessScore = WellnessScore.default()
        )
    }

    private fun createPersonalizedAchievement(
        category: AchievementCategory,
        averageMetrics: ScreenTimeMetrics,
        difficultyLevel: DifficultyLevel
    ): Achievement? {
        val id = UUID.randomUUID().toString()

        return when (category) {
            AchievementCategory.WELLNESS -> {
                val target = when (difficultyLevel) {
                    DifficultyLevel.BEGINNER -> 60
                    DifficultyLevel.INTERMEDIATE -> 75
                    DifficultyLevel.ADVANCED -> 85
                    DifficultyLevel.EXPERT -> 95
                }
                createWellnessAchievement(id, "Personal Wellness Goal", "Achieve wellness score of $target+", target)
            }
            AchievementCategory.PRODUCTIVITY -> {
                val target = when (difficultyLevel) {
                    DifficultyLevel.BEGINNER -> 40
                    DifficultyLevel.INTERMEDIATE -> 60
                    DifficultyLevel.ADVANCED -> 75
                    DifficultyLevel.EXPERT -> 90
                }
                createProductivityAchievement(id, "Personal Productivity Goal", "$target% productive app usage", target)
            }
            else -> null
        }
    }

    private fun createScreenTimeAchievement(id: String, name: String, description: String, hours: Int): Achievement {
        return Achievement(
            id = id,
            name = name,
            description = description,
            emoji = "⏰",
            category = AchievementCategory.DISCIPLINE,
            targetValue = 1, // One day of meeting the goal
            tier = when (hours) {
                4 -> AchievementTier.BRONZE
                3 -> AchievementTier.SILVER
                2 -> AchievementTier.GOLD
                else -> AchievementTier.PLATINUM
            }
        )
    }

    private fun createUnlockAchievement(id: String, name: String, description: String, maxUnlocks: Int): Achievement {
        return Achievement(
            id = id,
            name = name,
            description = description,
            emoji = "🔒",
            category = AchievementCategory.MINDFUL,
            targetValue = 1,
            tier = when (maxUnlocks) {
                50 -> AchievementTier.BRONZE
                30 -> AchievementTier.SILVER
                20 -> AchievementTier.GOLD
                else -> AchievementTier.PLATINUM
            }
        )
    }

    private fun createWellnessAchievement(id: String, name: String, description: String, score: Int): Achievement {
        return Achievement(
            id = id,
            name = name,
            description = description,
            emoji = "🌟",
            category = AchievementCategory.WELLNESS,
            targetValue = score,
            tier = when (score) {
                70 -> AchievementTier.BRONZE
                80 -> AchievementTier.SILVER
                90 -> AchievementTier.GOLD
                else -> AchievementTier.PLATINUM
            }
        )
    }

    private fun createProductivityAchievement(id: String, name: String, description: String, percentage: Int): Achievement {
        return Achievement(
            id = id,
            name = name,
            description = description,
            emoji = "💼",
            category = AchievementCategory.PRODUCTIVITY,
            targetValue = percentage,
            tier = when (percentage) {
                50 -> AchievementTier.BRONZE
                70 -> AchievementTier.SILVER
                85 -> AchievementTier.GOLD
                else -> AchievementTier.PLATINUM
            }
        )
    }

    private fun estimateTimeToComplete(achievement: Achievement, historicalMetrics: List<ScreenTimeMetrics>): Long {
        // Simplified estimation - in reality would be more sophisticated
        return when (achievement.category) {
            AchievementCategory.STREAK -> achievement.targetValue * 24 * 60 * 60 * 1000L // Days in milliseconds
            else -> 7 * 24 * 60 * 60 * 1000L // Default to 7 days
        }
    }

    private fun generateAchievementRecommendations(
        achievement: Achievement,
        isAppropriate: Boolean,
        difficulty: DifficultyLevel
    ): List<String> {
        val recommendations = mutableListOf<String>()

        if (!isAppropriate) {
            recommendations.add("This achievement may be too ${if (difficulty.ordinal > 2) "difficult" else "easy"} for your current level")
        }

        when (achievement.category) {
            AchievementCategory.STREAK -> recommendations.add("Focus on consistency rather than perfection")
            AchievementCategory.WELLNESS -> recommendations.add("Track your wellness score daily")
            AchievementCategory.PRODUCTIVITY -> recommendations.add("Identify your most productive apps")
            else -> recommendations.add("Set daily reminders to track progress")
        }

        return recommendations
    }

    private fun createWellnessMilestones(userProgress: UserProgressData): List<Achievement> {
        val currentScore = userProgress.averageWellnessScore.toInt()
        val nextMilestone = ((currentScore / 10) + 1) * 10

        return if (nextMilestone <= 100) {
            listOf(createWellnessAchievement(
                "wellness_milestone_$nextMilestone",
                "Wellness Milestone $nextMilestone",
                "Reach wellness score of $nextMilestone",
                nextMilestone
            ))
        } else emptyList()
    }

    private fun createProductivityMilestones(userProgress: UserProgressData): List<Achievement> {
        // Create productivity milestones based on user's strong areas
        return userProgress.strongestAreas.take(2).map { area ->
            createProductivityAchievement(
                "productivity_${area.lowercase()}_milestone",
                "$area Productivity Master",
                "Excel in $area productivity",
                80
            )
        }
    }

    private fun createStreakMilestones(userProgress: UserProgressData): List<Achievement> {
        val nextStreakTarget = when {
            userProgress.consistencyRating >= 0.9f -> 30
            userProgress.consistencyRating >= 0.7f -> 14
            userProgress.consistencyRating >= 0.5f -> 7
            else -> 3
        }

        return listOf(Achievement.createStreakAchievement(nextStreakTarget))
    }

    private fun createFocusMilestones(userProgress: UserProgressData): List<Achievement> {
        return listOf(
            Achievement(
                id = "focus_milestone_${userProgress.currentLevel}",
                name = "Focus Champion",
                description = "Complete focused work sessions consistently",
                emoji = "🎯",
                category = AchievementCategory.FOCUS,
                targetValue = 7, // 7 days of good focus
                tier = AchievementTier.SILVER
            )
        )
    }
}