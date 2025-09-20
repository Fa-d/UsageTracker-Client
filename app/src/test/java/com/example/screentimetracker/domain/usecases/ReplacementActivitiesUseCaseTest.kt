package dev.sadakat.screentimetracker.domain.usecases

import dev.sadakat.screentimetracker.core.data.local.*
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
    fun `initializeDefaultActivities should not insert when activities already exist`() = runTest {
        // Given
        val existingActivities = listOf(
            createMockReplacementActivity(1L, "walking", "Take a walk", "physical")
        )
        every { mockReplacementActivityDao.getAllActivities() } returns flowOf(existingActivities)

        // When
        replacementActivitiesUseCase.initializeDefaultActivities()

        // Then
        coVerify(exactly = 0) { mockReplacementActivityDao.insertActivities(any()) }
    }

    // Test data retrieval functionality
    @Test
    fun `getAllActivities should return flow from dao`() = runTest {
        // Given
        val mockActivities = listOf(
            createMockReplacementActivity(1L, "reading", "Read a book", "mental"),
            createMockReplacementActivity(2L, "walking", "Take a walk", "physical")
        )
        every { mockReplacementActivityDao.getAllActivities() } returns flowOf(mockActivities)

        // When
        val result = replacementActivitiesUseCase.getAllActivities().first()

        // Then
        assertEquals(mockActivities, result)
        verify { mockReplacementActivityDao.getAllActivities() }
    }

    @Test
    fun `getActivitiesByCategory should return filtered activities`() = runTest {
        // Given
        val category = "wellness"
        val mockActivities = listOf(
            createMockReplacementActivity(1L, "breathing", "Deep breathing", "wellness")
        )
        every { mockReplacementActivityDao.getActivitiesByCategory(category) } returns flowOf(mockActivities)

        // When
        val result = replacementActivitiesUseCase.getActivitiesByCategory(category).first()

        // Then
        assertEquals(mockActivities, result)
        verify { mockReplacementActivityDao.getActivitiesByCategory(category) }
    }

    // Test smart suggestions functionality
    @Test
    fun `getSmartSuggestions should call dao with correct parameters`() = runTest {
        // Given
        val availableMinutes = 15
        val userEnergyLevel = 2
        val preferredCategory = "physical"
        val mockSuggestions = listOf(
            createMockReplacementActivity(1L, "stretching", "Do stretches", "physical")
        )
        coEvery { 
            mockReplacementActivityDao.getSmartSuggestions(availableMinutes, userEnergyLevel, preferredCategory, 3)
        } returns mockSuggestions

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
    fun `getSuggestionsForBlockedApp should suggest wellness for social media apps`() = runTest {
        // Given
        val blockedApp = "com.instagram.android"
        val mockSuggestions = listOf(
            createMockReplacementActivity(1L, "breathing", "Deep breathing", "wellness")
        )
        coEvery { 
            mockReplacementActivityDao.getSmartSuggestions(10, 2, "wellness", 4)
        } returns mockSuggestions

        // When
        val result = replacementActivitiesUseCase.getSuggestionsForBlockedApp(blockedApp)

        // Then
        assertEquals(mockSuggestions, result)
        coVerify { 
            mockReplacementActivityDao.getSmartSuggestions(
                availableMinutes = 10,
                maxDifficulty = 2,
                preferredCategory = "wellness",
                limit = 4
            )
        }
    }

    @Test
    fun `getSuggestionsForBlockedApp should suggest physical for games`() = runTest {
        // Given
        val blockedApp = "com.king.candycrushsaga"
        val mockSuggestions = listOf(
            createMockReplacementActivity(1L, "walking", "Take a walk", "physical")
        )
        coEvery { 
            mockReplacementActivityDao.getSmartSuggestions(10, 2, "physical", 4)
        } returns mockSuggestions

        // When
        val result = replacementActivitiesUseCase.getSuggestionsForBlockedApp(blockedApp)

        // Then
        coVerify { 
            mockReplacementActivityDao.getSmartSuggestions(
                availableMinutes = 10,
                maxDifficulty = 2,
                preferredCategory = "physical",
                limit = 4
            )
        }
    }

    // Test activity completion functionality
    @Test
    fun `completeActivity should update completion count and rating`() = runTest {
        // Given
        val activityId = 123L
        val actualDurationMinutes = 7
        val userRating = 4
        val notes = "Great activity!"
        val contextTrigger = "timer_expired"
        
        val mockActivity = mockk<ReplacementActivity> {
            every { averageRating } returns 3.0f
            every { timesCompleted } returns 2
        }
        
        coEvery { mockReplacementActivityDao.incrementCompletionCount(any(), any()) } just Runs
        coEvery { mockReplacementActivityDao.getActivityById(activityId) } returns mockActivity
        coEvery { mockReplacementActivityDao.updateAverageRating(any(), any()) } just Runs

        val beforeCall = System.currentTimeMillis()

        // When
        replacementActivitiesUseCase.completeActivity(
            activityId = activityId,
            actualDurationMinutes = actualDurationMinutes,
            userRating = userRating,
            notes = notes,
            contextTrigger = contextTrigger
        )

        val afterCall = System.currentTimeMillis()

        // Then
        coVerify { 
            mockReplacementActivityDao.incrementCompletionCount(
                activityId = activityId,
                timestamp = match { it >= beforeCall && it <= afterCall }
            )
        }
        
        coVerify { mockReplacementActivityDao.getActivityById(activityId) }
        coVerify { mockReplacementActivityDao.updateAverageRating(activityId, match { it > 3.0f }) }
    }

    @Test
    fun `completeActivity should not update rating when rating is zero`() = runTest {
        // Given
        val activityId = 123L
        
        coEvery { mockReplacementActivityDao.incrementCompletionCount(any(), any()) } just Runs

        // When
        replacementActivitiesUseCase.completeActivity(
            activityId = activityId,
            actualDurationMinutes = 5,
            userRating = 0
        )

        // Then
        coVerify { mockReplacementActivityDao.incrementCompletionCount(activityId, any()) }
        coVerify(exactly = 0) { mockReplacementActivityDao.getActivityById(any()) }
        coVerify(exactly = 0) { mockReplacementActivityDao.updateAverageRating(any(), any()) }
    }

    @Test
    fun `completeActivity should handle exceptions gracefully`() = runTest {
        // Given
        val activityId = 123L
        
        coEvery { mockReplacementActivityDao.incrementCompletionCount(any(), any()) } throws RuntimeException("Database error")

        // When & Then - should not throw exception
        replacementActivitiesUseCase.completeActivity(
            activityId = activityId,
            actualDurationMinutes = 5,
            userRating = 4
        )
        
        // Test passes if no exception is thrown
    }

    // Test custom activity creation
    @Test
    fun `createCustomActivity should create activity with correct properties`() = runTest {
        // Given
        val title = "Custom Workout"
        val description = "My custom workout routine"
        val emoji = "💪"
        val estimatedMinutes = 20
        val category = "physical"
        val difficulty = 3
        val expectedId = 456L
        
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

    // Test stats functionality
    @Test
    fun `getWeeklyStats should calculate stats from existing activities`() = runTest {
        // Given
        val mockActivities = listOf(
            createMockReplacementActivity(1L, "walking", "Walk", "physical").copy(
                timesCompleted = 5,
                averageRating = 4.5f,
                estimatedDurationMinutes = 10
            ),
            createMockReplacementActivity(2L, "reading", "Read", "mental").copy(
                timesCompleted = 3,
                averageRating = 4.0f,
                estimatedDurationMinutes = 15
            ),
            createMockReplacementActivity(3L, "breathing", "Breathe", "wellness").copy(
                timesCompleted = 0,
                averageRating = 0f
            )
        )
        every { mockReplacementActivityDao.getAllActivities() } returns flowOf(mockActivities)

        // When
        val result = replacementActivitiesUseCase.getWeeklyStats()

        // Then
        assertEquals(8, result.totalCompletions) // 5 + 3
        assertEquals(95, result.totalDurationMinutes) // (5*10) + (3*15)
        assertEquals(4.25f, result.averageRating, 0.01f) // (4.5 + 4.0) / 2
        assertEquals(2, result.categoryStats.size)
        
        verify { mockReplacementActivityDao.getAllActivities() }
    }

    @Test
    fun `getTodayCompletions should return empty list`() = runTest {
        // Given - actual implementation returns empty list

        // When
        val result = replacementActivitiesUseCase.getTodayCompletions()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getPersonalizedRecommendations should use default category`() = runTest {
        // Given
        val mockRecommendations = listOf(
            createMockReplacementActivity(1L, "breathing", "Deep breathing", "wellness")
        )
        coEvery { 
            mockReplacementActivityDao.getSmartSuggestions(15, 3, "wellness", 5)
        } returns mockRecommendations

        // When
        val result = replacementActivitiesUseCase.getPersonalizedRecommendations()

        // Then
        assertEquals(mockRecommendations, result)
        coVerify { 
            mockReplacementActivityDao.getSmartSuggestions(
                availableMinutes = 15,
                maxDifficulty = 3,
                preferredCategory = "wellness",
                limit = 5
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
}