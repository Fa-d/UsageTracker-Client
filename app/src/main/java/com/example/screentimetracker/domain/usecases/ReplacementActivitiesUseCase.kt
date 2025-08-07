package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class ReplacementActivitiesUseCase @Inject constructor(
    private val replacementActivityDao: ReplacementActivityDao
) {
    
    suspend fun initializeDefaultActivities() {
        val existingActivities = replacementActivityDao.getAllActivities().first()
        if (existingActivities.isEmpty()) {
            val defaultActivities = createDefaultActivities()
            replacementActivityDao.insertActivities(defaultActivities)
        }
    }
    
    private fun createDefaultActivities(): List<ReplacementActivity> {
        return listOf(
            // Physical Activities
            ReplacementActivity(
                activityType = "walking",
                title = "Take a 5-minute walk",
                description = "Step outside and walk around the block or pace indoors",
                emoji = "🚶",
                estimatedDurationMinutes = 5,
                category = "physical",
                difficultyLevel = 1
            ),
            ReplacementActivity(
                activityType = "stretching",
                title = "Do simple stretches",
                description = "Stretch your neck, shoulders, and back",
                emoji = "🤸",
                estimatedDurationMinutes = 3,
                category = "physical",
                difficultyLevel = 1
            ),
            ReplacementActivity(
                activityType = "exercise",
                title = "Quick workout",
                description = "Do 10 push-ups, squats, or jumping jacks",
                emoji = "💪",
                estimatedDurationMinutes = 5,
                category = "physical",
                difficultyLevel = 2
            ),
            
            // Mental Activities
            ReplacementActivity(
                activityType = "reading",
                title = "Read for 10 minutes",
                description = "Pick up a book, article, or news story",
                emoji = "📚",
                estimatedDurationMinutes = 10,
                category = "mental",
                difficultyLevel = 1
            ),
            ReplacementActivity(
                activityType = "puzzle",
                title = "Solve a puzzle",
                description = "Try a crossword, sudoku, or brain teaser",
                emoji = "🧩",
                estimatedDurationMinutes = 8,
                category = "mental",
                difficultyLevel = 2
            ),
            ReplacementActivity(
                activityType = "learning",
                title = "Learn something new",
                description = "Watch an educational video or read about a topic you're curious about",
                emoji = "🧠",
                estimatedDurationMinutes = 15,
                category = "mental",
                difficultyLevel = 2
            ),
            
            // Wellness Activities
            ReplacementActivity(
                activityType = "water",
                title = "Drink a glass of water",
                description = "Hydrate yourself mindfully",
                emoji = "💧",
                estimatedDurationMinutes = 2,
                category = "wellness",
                difficultyLevel = 1
            ),
            ReplacementActivity(
                activityType = "breathing",
                title = "Take deep breaths",
                description = "Practice 4-7-8 breathing for 2 minutes",
                emoji = "🧘",
                estimatedDurationMinutes = 2,
                category = "wellness",
                difficultyLevel = 1
            ),
            ReplacementActivity(
                activityType = "meditation",
                title = "Mini meditation",
                description = "Sit quietly and focus on your breath",
                emoji = "🕯️",
                estimatedDurationMinutes = 5,
                category = "wellness",
                difficultyLevel = 2
            ),
            
            // Productivity Activities
            ReplacementActivity(
                activityType = "journaling",
                title = "Write in journal",
                description = "Write down your thoughts or three things you're grateful for",
                emoji = "✍️",
                estimatedDurationMinutes = 5,
                category = "productivity",
                difficultyLevel = 1
            ),
            ReplacementActivity(
                activityType = "cleaning",
                title = "Tidy up your space",
                description = "Organize your desk or clean up a small area",
                emoji = "🧹",
                estimatedDurationMinutes = 7,
                category = "productivity",
                difficultyLevel = 1
            ),
            ReplacementActivity(
                activityType = "planning",
                title = "Plan your day",
                description = "Review your schedule or write tomorrow's to-do list",
                emoji = "📅",
                estimatedDurationMinutes = 5,
                category = "productivity",
                difficultyLevel = 1
            )
        )
    }
    
    fun getAllActivities(): Flow<List<ReplacementActivity>> {
        return replacementActivityDao.getAllActivities()
    }
    
    fun getActivitiesByCategory(category: String): Flow<List<ReplacementActivity>> {
        return replacementActivityDao.getActivitiesByCategory(category)
    }
    
    suspend fun getSmartSuggestions(
        availableMinutes: Int,
        userEnergyLevel: Int = 2, // 1-3
        preferredCategory: String = "wellness"
    ): List<ReplacementActivity> {
        return replacementActivityDao.getSmartSuggestions(
            availableMinutes = availableMinutes,
            maxDifficulty = userEnergyLevel,
            preferredCategory = preferredCategory,
            limit = 3
        )
    }
    
    suspend fun getSuggestionsForBlockedApp(blockedAppPackage: String): List<ReplacementActivity> {
        // Smart suggestions based on the type of app that was blocked
        val category = when {
            blockedAppPackage.contains("instagram", ignoreCase = true) ||
            blockedAppPackage.contains("facebook", ignoreCase = true) ||
            blockedAppPackage.contains("twitter", ignoreCase = true) ||
            blockedAppPackage.contains("tiktok", ignoreCase = true) -> "wellness" // Social media -> wellness
            
            blockedAppPackage.contains("game", ignoreCase = true) ||
            blockedAppPackage.contains("candy", ignoreCase = true) -> "physical" // Games -> physical activity
            
            blockedAppPackage.contains("youtube", ignoreCase = true) ||
            blockedAppPackage.contains("netflix", ignoreCase = true) -> "mental" // Video -> mental stimulation
            
            else -> "wellness"
        }
        
        return replacementActivityDao.getSmartSuggestions(
            availableMinutes = 10, // Assume 10 minutes available when app is blocked
            maxDifficulty = 2, // Medium difficulty when user is looking for distraction
            preferredCategory = category,
            limit = 4
        )
    }
    
    suspend fun completeActivity(
        activityId: Long,
        actualDurationMinutes: Int,
        userRating: Int,
        notes: String = "",
        contextTrigger: String = ""
    ) {
        val timestamp = System.currentTimeMillis()
        
        // Update activity stats
        replacementActivityDao.incrementCompletionCount(activityId, timestamp)
        
        // Update average rating with the new rating
        if (userRating > 0) {
            replacementActivityDao.updateAverageRating(activityId, userRating.toFloat())
        }
    }
    
    suspend fun createCustomActivity(
        title: String,
        description: String,
        emoji: String,
        estimatedMinutes: Int,
        category: String,
        difficulty: Int
    ): Long {
        val activity = ReplacementActivity(
            activityType = "custom",
            title = title,
            description = description,
            emoji = emoji,
            estimatedDurationMinutes = estimatedMinutes,
            category = category,
            difficultyLevel = difficulty,
            isCustom = true
        )
        
        return replacementActivityDao.insertActivity(activity)
    }
    
    suspend fun getTodayCompletions(): List<ActivityCompletion> {
        // Disabled completion tracking for now
        return emptyList()
    }
    
    suspend fun getWeeklyStats(): ActivityStats {
        // Return dummy stats for now since completion tracking is disabled
        return ActivityStats(
            totalCompletions = 0,
            totalDurationMinutes = 0,
            averageRating = 0f,
            categoryStats = emptyList()
        )
    }
    
    suspend fun getPersonalizedRecommendations(): List<ReplacementActivity> {
        // Get user's completion history to personalize
        val recentCompletions = getTodayCompletions()
        val preferredCategories = recentCompletions
            .groupBy { completion -> 
                replacementActivityDao.getActivityById(completion.activityId)?.category ?: "wellness"
            }
            .entries
            .sortedByDescending { it.value.size }
            .map { it.key }
        
        val preferredCategory = preferredCategories.firstOrNull() ?: "wellness"
        
        return replacementActivityDao.getSmartSuggestions(
            availableMinutes = 15,
            maxDifficulty = 3,
            preferredCategory = preferredCategory,
            limit = 5
        )
    }
}

data class ActivityStats(
    val totalCompletions: Int,
    val totalDurationMinutes: Int,
    val averageRating: Float,
    val categoryStats: List<CategoryStat>
)