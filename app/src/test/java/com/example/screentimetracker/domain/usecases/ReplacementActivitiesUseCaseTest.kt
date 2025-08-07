package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.*
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class ReplacementActivitiesUseCaseTest {

    private val mockReplacementActivityDao = mockk<ReplacementActivityDao>()
    private lateinit var replacementActivitiesUseCase: ReplacementActivitiesUseCase

    @Before
    fun setup() {
        replacementActivitiesUseCase = ReplacementActivitiesUseCase(mockReplacementActivityDao)
        MockKAnnotations.init(this)
    }

    // Test initialization functionality
    @Test
    fun `initializeDefaultActivities should insert default activities when database is empty`() = runTest {
        // Given
        every { mockReplacementActivityDao.getAllActivities() } returns flowOf(emptyList())
        coEvery { mockReplacementActivityDao.insertActivities(any()) } just Runs

        // When
        replacementActivitiesUseCase.initializeDefaultActivities()

        // Then
        coVerify { 
            mockReplacementActivityDao.insertActivities(
                match { activities -> 
                    activities.isNotEmpty() && 
                    activities.any { it.activityType == "walking" } &&
                    activities.any { it.activityType == "breathing" } &&
                    activities.any { it.activityType == "reading" } &&
                    activities.any { it.category == "physical" } &&
                    activities.any { it.category == "mental" } &&
                    activities.any { it.category == "wellness" } &&
                    activities.any { it.category == "productivity" }
                }
            )
        }
    }

    @Test
    fun `initializeDefaultActivities should not insert activities when database has existing activities`() = runTest {
        // Given
        val existingActivities = listOf(
            createMockReplacementActivity(1L, "walking", "Take a walk", "physical"),
            createMockReplacementActivity(2L, "reading", "Read a book", "mental")
        )
        every { mockReplacementActivityDao.getAllActivities() } returns flowOf(existingActivities)

        // When
        replacementActivitiesUseCase.initializeDefaultActivities()

        // Then
        coVerify(exactly = 0) { mockReplacementActivityDao.insertActivities(any()) }
    }

    @Test
    fun `initializeDefaultActivities should create activities with correct default properties`() = runTest {
        // Given
        every { mockReplacementActivityDao.getAllActivities() } returns flowOf(emptyList())
        coEvery { mockReplacementActivityDao.insertActivities(any()) } just Runs

        // When
        replacementActivitiesUseCase.initializeDefaultActivities()

        // Then
        coVerify { 
            mockReplacementActivityDao.insertActivities(
                match { activities ->
                    activities.all { activity ->
                        activity.title.isNotEmpty() &&
                        activity.description.isNotEmpty() &&
                        activity.emoji.isNotEmpty() &&
                        activity.estimatedDurationMinutes > 0 &&
                        activity.category.isNotEmpty() &&
                        activity.difficultyLevel in 1..3 &&
                        !activity.isCustom // Should be false for default activities
                    }
                }
            )
        }
    }

    // Test activity retrieval functionality
    @Test
    fun `getAllActivities should return flow from dao`() = runTest {
        // Given
        val mockActivities = listOf(
            createMockReplacementActivity(1L, "walking", "Take a walk", "physical"),
            createMockReplacementActivity(2L, "meditation", "Meditate", "wellness")
        )
        every { mockReplacementActivityDao.getAllActivities() } returns flowOf(mockActivities)

        // When
        val result = replacementActivitiesUseCase.getAllActivities().first()

        // Then
        assertEquals(mockActivities, result)
        verify { mockReplacementActivityDao.getAllActivities() }
    }

    @Test
    fun `getActivitiesByCategory should return activities filtered by category`() = runTest {
        // Given
        val category = "wellness"
        val mockActivities = listOf(
            createMockReplacementActivity(1L, "breathing", "Deep breathing", "wellness"),
            createMockReplacementActivity(2L, "meditation", "Meditate", "wellness")
        )
        every { mockReplacementActivityDao.getActivitiesByCategory(any()) } returns flowOf(mockActivities)

        // When
        val result = replacementActivitiesUseCase.getActivitiesByCategory(category).first()

        // Then
        assertEquals(mockActivities, result)
        verify { mockReplacementActivityDao.getActivitiesByCategory(category) }
    }

    // Test smart suggestions functionality
    @Test
    fun `getSmartSuggestions should return suggestions based on parameters`() = runTest {
        // Given
        val availableMinutes = 10
        val userEnergyLevel = 2
        val preferredCategory = "wellness"
        val mockSuggestions = listOf(
            createMockReplacementActivity(1L, "breathing", "Deep breathing", "wellness", 5, 1),
            createMockReplacementActivity(2L, "water", "Drink water", "wellness", 2, 1)
        )
        
        coEvery { mockReplacementActivityDao.getSmartSuggestions(any(), any(), any(), any()) } returns mockSuggestions

        // When
        val result = replacementActivitiesUseCase.getSmartSuggestions(
            availableMinutes = availableMinutes,
            userEnergyLevel = userEnergyLevel,
            preferredCategory = preferredCategory
        )

        // Then
        assertEquals(mockSuggestions, result)
        coVerify { 
            mockReplacementActivityDao.getSmartSuggestions(
                availableMinutes = availableMinutes,
                maxDifficulty = userEnergyLevel,
                preferredCategory = preferredCategory,
                limit = 3
            )
        }
    }

    @Test
    fun `getSmartSuggestions should use default parameters when not specified`() = runTest {
        // Given
        val mockSuggestions = listOf(
            createMockReplacementActivity(1L, "breathing", "Deep breathing", "wellness")
        )
        
        coEvery { mockReplacementActivityDao.getSmartSuggestions(any(), any(), any(), any()) } returns mockSuggestions

        // When
        val result = replacementActivitiesUseCase.getSmartSuggestions()

        // Then
        coVerify { 
            mockReplacementActivityDao.getSmartSuggestions(
                availableMinutes = any(), // Should use reasonable default
                maxDifficulty = 2, // Default energy level
                preferredCategory = "wellness", // Default category
                limit = 3
            )
        }
    }

    // Test app-specific suggestions functionality
    @Test
    fun `getSuggestionsForBlockedApp should return wellness suggestions for social media apps`() = runTest {
        // Given
        val blockedApp = "com.instagram.android"
        val mockSuggestions = listOf(
            createMockReplacementActivity(1L, "breathing", "Deep breathing", "wellness")
        )
        
        coEvery { mockReplacementActivityDao.getSmartSuggestions(any(), any(), any(), any()) } returns mockSuggestions

        // When
        val result = replacementActivitiesUseCase.getSuggestionsForBlockedApp(blockedApp)

        // Then
        assertEquals(mockSuggestions, result)
        coVerify { 
            mockReplacementActivityDao.getSmartSuggestions(
                availableMinutes = 10,
                maxDifficulty = 2,
                preferredCategory = "wellness", // Social media should suggest wellness
                limit = 4
            )
        }
    }

    @Test
    fun `getSuggestionsForBlockedApp should return physical suggestions for games`() = runTest {
        // Given
        val blockedApp = "com.game.example"
        val mockSuggestions = listOf(
            createMockReplacementActivity(1L, "walking", "Take a walk", "physical")
        )
        
        coEvery { mockReplacementActivityDao.getSmartSuggestions(any(), any(), any(), any()) } returns mockSuggestions

        // When
        val result = replacementActivitiesUseCase.getSuggestionsForBlockedApp(blockedApp)

        // Then
        coVerify { 
            mockReplacementActivityDao.getSmartSuggestions(
                availableMinutes = 10,
                maxDifficulty = 2,
                preferredCategory = "physical", // Games should suggest physical activity
                limit = 4
            )
        }
    }

    @Test
    fun `getSuggestionsForBlockedApp should return mental suggestions for video apps`() = runTest {
        // Given
        val blockedApp = "com.youtube.example"
        val mockSuggestions = listOf(
            createMockReplacementActivity(1L, "reading", "Read a book", "mental")
        )
        
        coEvery { mockReplacementActivityDao.getSmartSuggestions(any(), any(), any(), any()) } returns mockSuggestions

        // When
        val result = replacementActivitiesUseCase.getSuggestionsForBlockedApp(blockedApp)

        // Then
        coVerify { 
            mockReplacementActivityDao.getSmartSuggestions(
                availableMinutes = 10,
                maxDifficulty = 2,
                preferredCategory = "mental", // Video apps should suggest mental stimulation
                limit = 4
            )
        }
    }

    @Test
    fun `getSuggestionsForBlockedApp should return wellness suggestions for unknown apps`() = runTest {
        // Given
        val blockedApp = "com.unknown.app"
        val mockSuggestions = listOf(
            createMockReplacementActivity(1L, "breathing", "Deep breathing", "wellness")
        )
        
        coEvery { mockReplacementActivityDao.getSmartSuggestions(any(), any(), any(), any()) } returns mockSuggestions

        // When
        val result = replacementActivitiesUseCase.getSuggestionsForBlockedApp(blockedApp)

        // Then
        coVerify { 
            mockReplacementActivityDao.getSmartSuggestions(
                availableMinutes = 10,
                maxDifficulty = 2,
                preferredCategory = "wellness", // Default to wellness for unknown apps
                limit = 4
            )
        }
    }

    // Test activity completion functionality
    @Test
    fun `completeActivity should record completion and update activity stats`() = runTest {
        // Given
        val activityId = 123L
        val actualDurationMinutes = 7
        val userRating = 4
        val notes = "Great activity!"
        val contextTrigger = "timer_expired"
        val expectedCompletionId = 456L

        coEvery { mockReplacementActivityDao.insertCompletion(any()) } returns expectedCompletionId
        coEvery { mockReplacementActivityDao.incrementCompletionCount(any(), any()) } just Runs
        every { mockReplacementActivityDao.getCompletionsForActivity(any()) } returns flowOf(listOf(
            createMockActivityCompletion(1L, activityId, userRating = 4),
            createMockActivityCompletion(2L, activityId, userRating = 5)
        ))
        coEvery { mockReplacementActivityDao.updateAverageRating(any(), any()) } just Runs

        val beforeCall = System.currentTimeMillis()

        // When
        val result = replacementActivitiesUseCase.completeActivity(
            activityId = activityId,
            actualDurationMinutes = actualDurationMinutes,
            userRating = userRating,
            notes = notes,
            contextTrigger = contextTrigger
        )

        val afterCall = System.currentTimeMillis()

        // Then
        assertEquals(expectedCompletionId, result)
        
        coVerify { 
            mockReplacementActivityDao.insertCompletion(
                match { completion ->
                    completion.activityId == activityId &&
                    completion.actualDurationMinutes == actualDurationMinutes &&
                    completion.userRating == userRating &&
                    completion.notes == notes &&
                    completion.contextTrigger == contextTrigger &&
                    completion.completedAt >= beforeCall &&
                    completion.completedAt <= afterCall
                }
            )
        }
        
        coVerify { 
            mockReplacementActivityDao.incrementCompletionCount(
                activityId = activityId,
                timestamp = match { it >= beforeCall && it <= afterCall }
            )
        }
        
        coVerify { mockReplacementActivityDao.updateAverageRating(activityId, 4.5f) } // Average of 4 and 5
    }

    @Test
    fun `completeActivity should handle empty notes and context trigger`() = runTest {
        // Given
        val activityId = 123L
        val actualDurationMinutes = 5
        val userRating = 3

        coEvery { mockReplacementActivityDao.insertCompletion(any()) } returns 1L
        coEvery { mockReplacementActivityDao.incrementCompletionCount(any(), any()) } just Runs
        every { mockReplacementActivityDao.getCompletionsForActivity(any()) } returns flowOf(listOf(
            createMockActivityCompletion(1L, activityId, userRating = 3)
        ))
        coEvery { mockReplacementActivityDao.updateAverageRating(any(), any()) } just Runs

        // When
        replacementActivitiesUseCase.completeActivity(
            activityId = activityId,
            actualDurationMinutes = actualDurationMinutes,
            userRating = userRating
        )

        // Then
        coVerify { 
            mockReplacementActivityDao.insertCompletion(
                match { completion ->
                    completion.notes == "" &&
                    completion.contextTrigger == ""
                }
            )
        }
    }

    @Test
    fun `completeActivity should not update average rating when no ratings exist`() = runTest {
        // Given
        val activityId = 123L
        
        coEvery { mockReplacementActivityDao.insertCompletion(any()) } returns 1L
        coEvery { mockReplacementActivityDao.incrementCompletionCount(any(), any()) } just Runs
        every { mockReplacementActivityDao.getCompletionsForActivity(any()) } returns flowOf(emptyList())

        // When
        replacementActivitiesUseCase.completeActivity(
            activityId = activityId,
            actualDurationMinutes = 5,
            userRating = 4
        )

        // Then
        coVerify(exactly = 0) { mockReplacementActivityDao.updateAverageRating(any(), any()) }
    }

    @Test
    fun `completeActivity should calculate average rating correctly excluding zero ratings`() = runTest {
        // Given
        val activityId = 123L
        
        coEvery { mockReplacementActivityDao.insertCompletion(any()) } returns 1L
        coEvery { mockReplacementActivityDao.incrementCompletionCount(any(), any()) } just Runs
        every { mockReplacementActivityDao.getCompletionsForActivity(any()) } returns flowOf(listOf(
            createMockActivityCompletion(1L, activityId, userRating = 4),
            createMockActivityCompletion(2L, activityId, userRating = 0), // Should be excluded
            createMockActivityCompletion(3L, activityId, userRating = 5)
        ))
        coEvery { mockReplacementActivityDao.updateAverageRating(any(), any()) } just Runs

        // When
        replacementActivitiesUseCase.completeActivity(
            activityId = activityId,
            actualDurationMinutes = 5,
            userRating = 4
        )

        // Then
        coVerify { mockReplacementActivityDao.updateAverageRating(activityId, 4.5f) } // (4 + 5) / 2, excluding 0
    }

    // Test custom activity creation functionality
    @Test
    fun `createCustomActivity should insert custom activity with correct properties`() = runTest {
        // Given
        val title = "Custom Workout"
        val description = "My personal workout routine"
        val emoji = "💪"
        val estimatedMinutes = 30
        val category = "physical"
        val difficulty = 3
        val expectedId = 789L

        coEvery { mockReplacementActivityDao.insertActivity(any()) } returns expectedId

        // When
        val result = replacementActivitiesUseCase.createCustomActivity(
            title = title,
            description = description,
            emoji = emoji,
            estimatedMinutes = estimatedMinutes,
            category = category,
            difficulty = difficulty
        )

        // Then
        assertEquals(expectedId, result)
        coVerify { 
            mockReplacementActivityDao.insertActivity(
                match { activity ->
                    activity.activityType == "custom" &&
                    activity.title == title &&
                    activity.description == description &&
                    activity.emoji == emoji &&
                    activity.estimatedDurationMinutes == estimatedMinutes &&
                    activity.category == category &&
                    activity.difficultyLevel == difficulty &&
                    activity.isCustom == true
                }
            )
        }
    }

    // Test completion statistics functionality
    @Test
    fun `getTodayCompletions should return completions since start of today`() = runTest {
        // Given
        val mockCompletions = listOf(
            createMockActivityCompletion(1L, 123L),
            createMockActivityCompletion(2L, 456L)
        )
        
        every { mockReplacementActivityDao.getCompletionsSince(any()) } returns flowOf(mockCompletions)

        // When
        val result = replacementActivitiesUseCase.getTodayCompletions()

        // Then
        assertEquals(mockCompletions, result)
        verify { 
            mockReplacementActivityDao.getCompletionsSince(
                match { timestamp -> 
                    // Should be around start of today
                    val now = System.currentTimeMillis()
                    val dayStart = timestamp
                    val dayEnd = dayStart + (24 * 60 * 60 * 1000L)
                    now >= dayStart && now <= dayEnd
                }
            )
        }
    }

    @Test
    fun `getWeeklyStats should return comprehensive weekly statistics`() = runTest {
        // Given
        val expectedTotalCompletions = 15
        val expectedTotalDuration = 180 // minutes
        val expectedAverageRating = 4.2f
        val expectedCategoryStats = listOf(
            CategoryStat("wellness", 8),
            CategoryStat("physical", 4),
            CategoryStat("mental", 3)
        )

        coEvery { mockReplacementActivityDao.getCompletionCountSince(any()) } returns expectedTotalCompletions
        coEvery { mockReplacementActivityDao.getTotalDurationSince(any()) } returns expectedTotalDuration
        coEvery { mockReplacementActivityDao.getAverageRatingSince(any()) } returns expectedAverageRating
        coEvery { mockReplacementActivityDao.getCategoryStatsSince(any()) } returns expectedCategoryStats

        // When
        val result = replacementActivitiesUseCase.getWeeklyStats()

        // Then
        assertEquals(expectedTotalCompletions, result.totalCompletions)
        assertEquals(expectedTotalDuration, result.totalDurationMinutes)
        assertEquals(expectedAverageRating, result.averageRating, 0.001f)
        assertEquals(expectedCategoryStats, result.categoryStats)
        
        // Verify all DAO methods were called with correct time range (7 days ago)
        coVerify { 
            mockReplacementActivityDao.getCompletionCountSince(
                match { timestamp -> 
                    val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
                    Math.abs(timestamp - weekAgo) < 1000L // Allow 1 second tolerance
                }
            )
        }
    }

    @Test
    fun `getWeeklyStats should handle null values from dao gracefully`() = runTest {
        // Given
        coEvery { mockReplacementActivityDao.getCompletionCountSince(any()) } returns 10
        coEvery { mockReplacementActivityDao.getTotalDurationSince(any()) } returns null
        coEvery { mockReplacementActivityDao.getAverageRatingSince(any()) } returns null
        coEvery { mockReplacementActivityDao.getCategoryStatsSince(any()) } returns emptyList()

        // When
        val result = replacementActivitiesUseCase.getWeeklyStats()

        // Then
        assertEquals(10, result.totalCompletions)
        assertEquals(0, result.totalDurationMinutes) // Should default to 0 when null
        assertEquals(0f, result.averageRating, 0.001f) // Should default to 0 when null
        assertEquals(emptyList<CategoryStat>(), result.categoryStats)
    }

    // Test personalized recommendations functionality
    @Test
    fun `getPersonalizedRecommendations should return suggestions based on user history`() = runTest {
        // Given
        val todayCompletions = listOf(
            createMockActivityCompletion(1L, 100L), // Activity 100
            createMockActivityCompletion(2L, 200L), // Activity 200
            createMockActivityCompletion(3L, 100L)  // Activity 100 again
        )
        
        every { mockReplacementActivityDao.getCompletionsSince(any()) } returns flowOf(todayCompletions)
        coEvery { mockReplacementActivityDao.getActivityById(100L) } returns 
            createMockReplacementActivity(100L, "breathing", "Deep breathing", "wellness")
        coEvery { mockReplacementActivityDao.getActivityById(200L) } returns 
            createMockReplacementActivity(200L, "walking", "Take a walk", "physical")
        
        val mockRecommendations = listOf(
            createMockReplacementActivity(1L, "meditation", "Meditate", "wellness")
        )
        coEvery { mockReplacementActivityDao.getSmartSuggestions(any(), any(), any(), any()) } returns mockRecommendations

        // When
        val result = replacementActivitiesUseCase.getPersonalizedRecommendations()

        // Then
        assertEquals(mockRecommendations, result)
        
        // Should prefer wellness category since it appears twice in today's completions
        coVerify { 
            mockReplacementActivityDao.getSmartSuggestions(
                availableMinutes = 15,
                maxDifficulty = 3,
                preferredCategory = "wellness", // Most completed category today
                limit = 5
            )
        }
    }

    @Test
    fun `getPersonalizedRecommendations should use default category when no history exists`() = runTest {
        // Given
        every { mockReplacementActivityDao.getCompletionsSince(any()) } returns flowOf(emptyList())
        
        val mockRecommendations = listOf(
            createMockReplacementActivity(1L, "breathing", "Deep breathing", "wellness")
        )
        coEvery { mockReplacementActivityDao.getSmartSuggestions(any(), any(), any(), any()) } returns mockRecommendations

        // When
        val result = replacementActivitiesUseCase.getPersonalizedRecommendations()

        // Then
        coVerify { 
            mockReplacementActivityDao.getSmartSuggestions(
                availableMinutes = 15,
                maxDifficulty = 3,
                preferredCategory = "wellness", // Default category
                limit = 5
            )
        }
    }

    @Test
    fun `getPersonalizedRecommendations should handle activities not found gracefully`() = runTest {
        // Given
        val todayCompletions = listOf(
            createMockActivityCompletion(1L, 999L) // Non-existent activity
        )
        
        every { mockReplacementActivityDao.getCompletionsSince(any()) } returns flowOf(todayCompletions)
        coEvery { mockReplacementActivityDao.getActivityById(999L) } returns null
        
        val mockRecommendations = listOf(
            createMockReplacementActivity(1L, "breathing", "Deep breathing", "wellness")
        )
        coEvery { mockReplacementActivityDao.getSmartSuggestions(any(), any(), any(), any()) } returns mockRecommendations

        // When
        val result = replacementActivitiesUseCase.getPersonalizedRecommendations()

        // Then
        coVerify { 
            mockReplacementActivityDao.getSmartSuggestions(
                availableMinutes = 15,
                maxDifficulty = 3,
                preferredCategory = "wellness", // Should default to wellness when activity not found
                limit = 5
            )
        }
    }

    // Test edge cases
    @Test
    fun `completeActivity should handle zero duration`() = runTest {
        // Given
        val activityId = 123L
        val actualDurationMinutes = 0
        val userRating = 3

        coEvery { mockReplacementActivityDao.insertCompletion(any()) } returns 1L
        coEvery { mockReplacementActivityDao.incrementCompletionCount(any(), any()) } just Runs
        every { mockReplacementActivityDao.getCompletionsForActivity(any()) } returns flowOf(emptyList())

        // When
        val result = replacementActivitiesUseCase.completeActivity(
            activityId = activityId,
            actualDurationMinutes = actualDurationMinutes,
            userRating = userRating
        )

        // Then
        assertEquals(1L, result)
        coVerify { 
            mockReplacementActivityDao.insertCompletion(
                match { completion -> completion.actualDurationMinutes == 0 }
            )
        }
    }

    @Test
    fun `completeActivity should handle negative duration`() = runTest {
        // Given
        val activityId = 123L
        val actualDurationMinutes = -5
        val userRating = 2

        coEvery { mockReplacementActivityDao.insertCompletion(any()) } returns 1L
        coEvery { mockReplacementActivityDao.incrementCompletionCount(any(), any()) } just Runs
        every { mockReplacementActivityDao.getCompletionsForActivity(any()) } returns flowOf(emptyList())

        // When
        val result = replacementActivitiesUseCase.completeActivity(
            activityId = activityId,
            actualDurationMinutes = actualDurationMinutes,
            userRating = userRating
        )

        // Then
        assertEquals(1L, result)
        coVerify { 
            mockReplacementActivityDao.insertCompletion(
                match { completion -> completion.actualDurationMinutes == -5 }
            )
        }
    }

    @Test
    fun `createCustomActivity should handle empty title and description`() = runTest {
        // Given
        coEvery { mockReplacementActivityDao.insertActivity(any()) } returns 1L

        // When
        val result = replacementActivitiesUseCase.createCustomActivity(
            title = "",
            description = "",
            emoji = "😊",
            estimatedMinutes = 5,
            category = "wellness",
            difficulty = 1
        )

        // Then
        assertEquals(1L, result)
        coVerify { 
            mockReplacementActivityDao.insertActivity(
                match { activity -> 
                    activity.title == "" && 
                    activity.description == ""
                }
            )
        }
    }

    @Test
    fun `getSuggestionsForBlockedApp should handle case insensitive matching`() = runTest {
        // Given
        val blockedApp = "COM.INSTAGRAM.ANDROID" // Uppercase
        val mockSuggestions = listOf(
            createMockReplacementActivity(1L, "breathing", "Deep breathing", "wellness")
        )
        
        coEvery { mockReplacementActivityDao.getSmartSuggestions(any(), any(), any(), any()) } returns mockSuggestions

        // When
        val result = replacementActivitiesUseCase.getSuggestionsForBlockedApp(blockedApp)

        // Then
        coVerify { 
            mockReplacementActivityDao.getSmartSuggestions(
                availableMinutes = 10,
                maxDifficulty = 2,
                preferredCategory = "wellness", // Should still match instagram and suggest wellness
                limit = 4
            )
        }
    }

    // Helper functions for creating mock objects
    private fun createMockReplacementActivity(
        id: Long,
        activityType: String,
        title: String,
        category: String,
        estimatedDurationMinutes: Int = 5,
        difficultyLevel: Int = 1,
        isCustom: Boolean = false,
        emoji: String = "🌟"
    ) = ReplacementActivity(
        id = id,
        activityType = activityType,
        title = title,
        description = "Description for $title",
        emoji = emoji,
        estimatedDurationMinutes = estimatedDurationMinutes,
        category = category,
        difficultyLevel = difficultyLevel,
        isCustom = isCustom
    )

    private fun createMockActivityCompletion(
        id: Long,
        activityId: Long,
        completedAt: Long = System.currentTimeMillis(),
        actualDurationMinutes: Int = 5,
        userRating: Int = 4,
        notes: String = "",
        contextTrigger: String = ""
    ) = ActivityCompletion(
        id = id,
        activityId = activityId,
        completedAt = completedAt,
        actualDurationMinutes = actualDurationMinutes,
        userRating = userRating,
        notes = notes,
        contextTrigger = contextTrigger
    )
}