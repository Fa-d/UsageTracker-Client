package dev.sadakat.screentimetracker.domain.goals.usecases

import dev.sadakat.screentimetracker.domain.goals.repository.GoalsRepository
import android.util.Log
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateAchievementProgressUseCase @Inject constructor(
    private val repository: GoalsRepository
) {
    companion object {
        private const val TAG = "UpdateAchievementProgress"

        // Achievement IDs from InitializeAchievementsUseCase
        const val DAILY_STREAK_3 = "daily_streak_3"
        const val MINDFUL_MOMENTS_5 = "mindful_moments_5"
        const val FOCUS_CHAMPION_3 = "focus_champion_3"
        const val APP_CLEANER_5 = "app_cleaner_5"
        const val WEEKEND_WARRIOR_2 = "weekend_warrior_2"
        const val EARLY_BIRD_7 = "early_bird_7"
        const val DIGITAL_SUNSET_5 = "digital_sunset_5"
    }

    suspend operator fun invoke() {
        try {
            val achievements = repository.getAllAchievements().first()

            achievements.forEach { achievement ->
                when (achievement.achievementId) {
                    DAILY_STREAK_3 -> updateProgress(achievement.achievementId, calculateDailyStreak())
                    MINDFUL_MOMENTS_5 -> updateProgress(achievement.achievementId, calculateMindfulMoments())
                    FOCUS_CHAMPION_3 -> updateProgress(achievement.achievementId, calculateFocusChampion())
                    APP_CLEANER_5 -> updateProgress(achievement.achievementId, calculateAppCleaner())
                    WEEKEND_WARRIOR_2 -> updateProgress(achievement.achievementId, calculateWeekendWarrior())
                    EARLY_BIRD_7 -> updateProgress(achievement.achievementId, calculateEarlyBird())
                    DIGITAL_SUNSET_5 -> updateProgress(achievement.achievementId, calculateDigitalSunset())
                    else -> {
                        Log.w(TAG, "Unknown achievement ID: ${achievement.achievementId}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update achievement progress", e)
        }
    }

    private suspend fun updateProgress(achievementId: String, progress: Int) {
        repository.updateAchievementProgress(achievementId, progress)

        val achievement = repository.getAchievementById(achievementId)
        if (achievement != null && !achievement.isUnlocked && progress >= achievement.targetValue) {
            repository.unlockAchievement(achievementId, System.currentTimeMillis())
            Log.i(TAG, "Achievement unlocked: $achievementId")
        }
    }

    // Placeholder calculation methods - would need actual data access
    private fun calculateDailyStreak(): Int = 0
    private fun calculateMindfulMoments(): Int = 0
    private fun calculateFocusChampion(): Int = 0
    private fun calculateAppCleaner(): Int = 0
    private fun calculateWeekendWarrior(): Int = 0
    private fun calculateEarlyBird(): Int = 0
    private fun calculateDigitalSunset(): Int = 0
}