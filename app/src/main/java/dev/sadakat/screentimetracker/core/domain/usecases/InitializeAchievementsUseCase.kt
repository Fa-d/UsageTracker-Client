package dev.sadakat.screentimetracker.core.domain.usecases

import dev.sadakat.screentimetracker.core.domain.model.Achievement
import dev.sadakat.screentimetracker.core.domain.model.AchievementCategory
import dev.sadakat.screentimetracker.core.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.first

class InitializeAchievementsUseCase(
    private val repository: TrackerRepository
) {
    suspend operator fun invoke() {
        val defaultAchievements = listOf(
            Achievement(
                id = "daily_streak_3",
                name = "Daily Streaks",
                description = "Stay under your screen time limit for 3 consecutive days",
                emoji = "🔥",
                category = AchievementCategory.STREAK,
                targetValue = 3
            ),
            Achievement(
                id = "mindful_moments_5",
                name = "Mindful Moments",
                description = "Take breaks between app sessions 5 times in a day",
                emoji = "🧘",
                category = AchievementCategory.MINDFUL,
                targetValue = 5
            ),
            Achievement(
                id = "focus_champion_3",
                name = "Focus Champion",
                description = "Complete 3 focus mode sessions in a day",
                emoji = "🎯",
                category = AchievementCategory.FOCUS,
                targetValue = 3
            ),
            Achievement(
                id = "app_cleaner_5",
                name = "App Cleaner",
                description = "Limit or remove 5 distracting apps",
                emoji = "🧹",
                category = AchievementCategory.DISCIPLINE,
                targetValue = 5
            ),
            Achievement(
                id = "weekend_warrior_2",
                name = "Weekend Warrior",
                description = "Maintain healthy habits for 2 consecutive weekends",
                emoji = "🌟",
                category = AchievementCategory.STREAK,
                targetValue = 2
            ),
            Achievement(
                id = "early_bird_7",
                name = "Early Bird",
                description = "First app usage after 8 AM for 7 consecutive days",
                emoji = "🌅",
                category = AchievementCategory.DISCIPLINE,
                targetValue = 7
            ),
            Achievement(
                id = "digital_sunset_5",
                name = "Digital Sunset",
                description = "No screen time 1 hour before bedtime for 5 consecutive days",
                emoji = "🌇",
                category = AchievementCategory.WELLNESS,
                targetValue = 5
            )
        )

        // Check if achievements already exist to avoid duplicates
        val existingAchievements = repository.getAllAchievements().first()
        if (existingAchievements.isEmpty()) {
            repository.insertAchievements(defaultAchievements)
        }
    }
}