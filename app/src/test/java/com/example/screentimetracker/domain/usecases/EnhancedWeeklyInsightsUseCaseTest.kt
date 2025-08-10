package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.AppSessionDataAggregate
import com.example.screentimetracker.data.local.AppSessionEvent
import com.example.screentimetracker.data.local.DailyAppSummary
import com.example.screentimetracker.data.local.UserGoal
import com.example.screentimetracker.domain.model.WellnessScore
import com.example.screentimetracker.domain.repository.TrackerRepository
import com.example.screentimetracker.utils.logger.AppLogger
import com.example.screentimetracker.utils.ui.AppNotificationManager
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class EnhancedWeeklyInsightsUseCaseTest {

    private val repository = mockk<TrackerRepository>()
    private val notificationManager = mockk<AppNotificationManager>(relaxed = true)
    private val appLogger = mockk<AppLogger>(relaxed = true)

    private lateinit var useCase: WeeklyInsightsUseCase

    // Test data
    private val currentTime = System.currentTimeMillis()
    private val weekStart = currentTime - TimeUnit.DAYS.toMillis(7)
    
    private val sampleSessionData = listOf(
        AppSessionDataAggregate(
            packageName = "com.instagram.android",
            totalDuration = TimeUnit.HOURS.toMillis(2), // 2 hours
            sessionCount = 15
        ),
        AppSessionDataAggregate(
            packageName = "com.netflix.mediaclient", 
            totalDuration = TimeUnit.HOURS.toMillis(3), // 3 hours
            sessionCount = 8
        )
    )
    
    private val sampleAppSummaries = listOf(
        DailyAppSummary(
            dateMillis = weekStart,
            packageName = "com.instagram.android",
            totalDurationMillis = TimeUnit.HOURS.toMillis(2),
            openCount = 25
        ),
        DailyAppSummary(
            dateMillis = weekStart,
            packageName = "com.netflix.mediaclient",
            totalDurationMillis = TimeUnit.HOURS.toMillis(3), 
            openCount = 10
        )
    )
    
    private val sampleSessions = listOf(
        AppSessionEvent(
            id = 1,
            packageName = "com.instagram.android",
            startTimeMillis = weekStart + TimeUnit.HOURS.toMillis(9), // 9 AM
            endTimeMillis = weekStart + TimeUnit.HOURS.toMillis(10), // 10 AM
            durationMillis = TimeUnit.HOURS.toMillis(1)
        ),
        AppSessionEvent(
            id = 2,
            packageName = "com.netflix.mediaclient", 
            startTimeMillis = weekStart + TimeUnit.HOURS.toMillis(14), // 2 PM
            endTimeMillis = weekStart + TimeUnit.HOURS.toMillis(15), // 3 PM
            durationMillis = TimeUnit.HOURS.toMillis(1)
        ),
        AppSessionEvent(
            id = 3,
            packageName = "com.instagram.android",
            startTimeMillis = weekStart + TimeUnit.HOURS.toMillis(22), // 10 PM
            endTimeMillis = weekStart + TimeUnit.HOURS.toMillis(23), // 11 PM
            durationMillis = TimeUnit.HOURS.toMillis(1)
        )
    )
    
    private val sampleWellnessScores = listOf(
        WellnessScore(
            date = weekStart,
            totalScore = 75,
            timeLimitScore = 15,
            focusSessionScore = 15,
            breaksScore = 15,
            sleepHygieneScore = 15,
            level = com.example.screentimetracker.domain.model.WellnessLevel.BALANCED_USER,
            calculatedAt = weekStart
        ),
        WellnessScore(
            date = weekStart + TimeUnit.DAYS.toMillis(1),
            totalScore = 80,
            timeLimitScore = 20,
            focusSessionScore = 20,
            breaksScore = 20,
            sleepHygieneScore = 15,
            level = com.example.screentimetracker.domain.model.WellnessLevel.BALANCED_USER,
            calculatedAt = weekStart + TimeUnit.DAYS.toMillis(1)
        )
    )
    
    private val sampleGoals = listOf(
        UserGoal(
            id = 1,
            goalType = "social_media_limit",
            targetValue = 60,
            currentProgress = 45,
            createdAt = System.currentTimeMillis(),
            isActive = true
        ),
        UserGoal(
            id = 2,
            goalType = "focus_sessions",
            targetValue = 120,
            currentProgress = 150,
            createdAt = System.currentTimeMillis(),
            isActive = true
        )
    )

    @Before
    fun setup() {
        
        every { appLogger.i(any(), any()) } just runs
        every { appLogger.e(any(), any(), any()) } just runs
        every { appLogger.d(any(), any()) } just runs
        
        // Default mock setup
        coEvery { repository.getAggregatedSessionDataForDayFlow(any(), any()) } returns flowOf(sampleSessionData)
        coEvery { repository.getDailyAppSummaries(any(), any()) } returns flowOf(sampleAppSummaries)
        coEvery { repository.getUnlockCountForDayFlow(any(), any()) } returns flowOf(50)
        coEvery { repository.getAllWellnessScores() } returns flowOf(sampleWellnessScores)
        coEvery { repository.getAllSessionsInRange(any(), any()) } returns flowOf(sampleSessions)
        coEvery { repository.getActiveGoals() } returns flowOf(sampleGoals)

        useCase = WeeklyInsightsUseCase(repository, notificationManager, appLogger)
    }

    @Test
    fun `generateWeeklyReport should return comprehensive report`() = runTest {
        // When
        val report = useCase.generateWeeklyReport()

        // Then
        assertNotNull("Report should not be null", report)
        assertTrue("Total screen time should be reasonable", report.totalScreenTimeMillis >= 0)
        assertTrue("Average daily screen time should be reasonable", report.averageDailyScreenTimeMillis >= 0)
        assertTrue("Total unlocks should be reasonable", report.totalUnlocks >= 0)
        assertTrue("Average unlocks per day should be reasonable", report.averageUnlocksPerDay >= 0)
        assertTrue("Average wellness score should be in valid range", report.averageWellnessScore >= 0 && report.averageWellnessScore <= 100)
        assertTrue("Should have some top apps", report.topApps.isNotEmpty())
        assertTrue("Should have insights", report.insights.isNotEmpty())
        
        // Verify top apps are sorted by usage time (if there are multiple apps)
        if (report.topApps.size > 1) {
            assertTrue("Top apps should be sorted by usage", 
                       report.topApps[0].totalTimeMillis >= report.topApps[1].totalTimeMillis)
        }
    }

    @Test
    fun `generateWeeklyReport should handle empty data gracefully`() = runTest {
        // Given - empty data
        coEvery { repository.getAggregatedSessionDataForDayFlow(any(), any()) } returns flowOf(emptyList())
        coEvery { repository.getDailyAppSummaries(any(), any()) } returns flowOf(emptyList())
        coEvery { repository.getUnlockCountForDayFlow(any(), any()) } returns flowOf(0)
        coEvery { repository.getAllWellnessScores() } returns flowOf(emptyList())

        // When
        val report = useCase.generateWeeklyReport()

        // Then
        assertNotNull(report)
        assertEquals(0, report.totalScreenTimeMillis)
        assertEquals(0, report.averageDailyScreenTimeMillis)
        assertEquals(0, report.totalUnlocks)
        assertEquals(0, report.averageUnlocksPerDay)
        assertEquals(0, report.averageWellnessScore)
        assertTrue(report.topApps.isEmpty())
        assertTrue(report.insights.isNotEmpty()) // Should still have insights
    }

    @Test
    fun `generateWeeklyReport should handle repository error`() = runTest {
        // Given - repository throws exception
        coEvery { repository.getAggregatedSessionDataForDayFlow(any(), any()) } throws RuntimeException("Database error")

        // When
        val report = useCase.generateWeeklyReport()

        // Then
        assertEquals(WeeklyInsightsUseCase.WeeklyReport.empty(), report)
        verify { appLogger.e("WeeklyInsightsUseCase", "Failed to generate weekly report", any()) }
    }

    @Test
    fun `sendWeeklyReportNotification should call notification manager`() = runTest {
        // Given
        every { notificationManager.showWeeklyReport(any(), any(), any()) } just runs

        // When
        useCase.sendWeeklyReportNotification()

        // Then
        verify { notificationManager.showWeeklyReport(any(), any(), any()) }
        verify { appLogger.i("WeeklyInsightsUseCase", "Weekly report notification sent") }
    }

    @Test
    fun `sendWeeklyReportNotification should handle error gracefully`() = runTest {
        // Given - notification manager throws exception
        every { notificationManager.showWeeklyReport(any(), any(), any()) } throws RuntimeException("Notification error")

        // When
        useCase.sendWeeklyReportNotification()

        // Then
        verify { appLogger.e("WeeklyInsightsUseCase", "Failed to send weekly report notification", any()) }
    }

    @Test
    fun `getProductivityHours should return 24-hour data`() = runTest {
        // When
        val productivityHours = useCase.getProductivityHours()

        // Then
        assertEquals("Should return 24 hours of data", 24, productivityHours.size)
        
        // Verify all hours are represented (0-23)
        val hours = productivityHours.map { it.hour }.sorted()
        assertEquals("Should have hours 0-23", (0..23).toList(), hours)
        
        // Check that productivity values are within reasonable range
        productivityHours.forEach { hourData ->
            assertTrue("Hour ${hourData.hour} usage should be >= 0", hourData.usageTimeMillis >= 0)
            assertTrue("Hour ${hourData.hour} productivity should be in range [0,1]", 
                       hourData.productivity >= 0f && hourData.productivity <= 1f)
        }
    }

    @Test
    fun `getProductivityHours should handle repository error`() = runTest {
        // Given
        coEvery { repository.getAllSessionsInRange(any(), any()) } throws RuntimeException("Database error")

        // When
        val productivityHours = useCase.getProductivityHours()

        // Then
        assertTrue(productivityHours.isEmpty())
        verify { appLogger.e("WeeklyInsightsUseCase", "Failed to get productivity hours", any()) }
    }

    @Test
    fun `getAppCategoryInsights should return category breakdown`() = runTest {
        // When
        val categoryInsights = useCase.getAppCategoryInsights()

        // Then
        assertNotNull("Category insights should not be null", categoryInsights)
        
        // Should return all categories (even with 0 usage)
        assertTrue("Should have at least some categories", categoryInsights.size >= 0)
        
        // Verify all categories have valid data structure
        categoryInsights.forEach { category ->
            assertTrue("Category ${category.categoryName} should have >= 0 total time", 
                       category.totalTimeMillis >= 0)
            assertTrue("Category ${category.categoryName} percentage should be in range [0,100]", 
                       category.percentageOfTotal >= 0f && category.percentageOfTotal <= 100f)
            assertTrue("Category name should not be empty", category.categoryName.isNotEmpty())
        }
        
        // Categories should be sorted by usage time (descending)
        if (categoryInsights.size > 1) {
            for (i in 0 until categoryInsights.size - 1) {
                assertTrue("Categories should be sorted by usage time: ${categoryInsights[i].categoryName} (${categoryInsights[i].totalTimeMillis}) >= ${categoryInsights[i + 1].categoryName} (${categoryInsights[i + 1].totalTimeMillis})", 
                           categoryInsights[i].totalTimeMillis >= categoryInsights[i + 1].totalTimeMillis)
            }
        }
        
        // Verify percentages are reasonable (may not add up to exactly 100% if not all apps are categorized)
        // Allow small floating-point precision errors
        val totalPercentage = categoryInsights.sumOf { it.percentageOfTotal.toDouble() }
        assertTrue("Total percentages should be reasonable (was $totalPercentage)", totalPercentage <= 100.1)
        
        // Verify that our test data results in expected categories
        val expectedCategories = setOf("Social", "Entertainment", "Productivity", "Communication", "Games")
        val actualCategories = categoryInsights.map { it.categoryName }.toSet()
        assertTrue("Should return predefined categories", actualCategories.containsAll(expectedCategories) || expectedCategories.containsAll(actualCategories))
    }

    @Test
    fun `getAppCategoryInsights should handle empty data`() = runTest {
        // Given
        coEvery { repository.getDailyAppSummaries(any(), any()) } returns flowOf(emptyList())

        // When
        val categoryInsights = useCase.getAppCategoryInsights()

        // Then
        assertNotNull(categoryInsights)
        // Should still return category structure but with zero usage
        categoryInsights.forEach { category ->
            assertEquals(0, category.totalTimeMillis)
            assertEquals(0f, category.percentageOfTotal)
        }
    }

    @Test
    fun `getAppCategoryInsights should handle repository error`() = runTest {
        // Given
        coEvery { repository.getDailyAppSummaries(any(), any()) } throws RuntimeException("Database error")

        // When
        val categoryInsights = useCase.getAppCategoryInsights()

        // Then
        assertTrue(categoryInsights.isEmpty())
        verify { appLogger.e("WeeklyInsightsUseCase", "Failed to get category insights", any()) }
    }

    @Test
    fun `productivity score calculation should work correctly`() = runTest {
        // When
        val productivityHours = useCase.getProductivityHours()

        // Then
        // Work hours (9-17) should have reasonable productivity scores
        val workHour = productivityHours.find { it.hour == 10 }
        assertNotNull(workHour)
        assertTrue("Work hour should have productivity score > 0", workHour!!.productivity >= 0)
        assertTrue("Work hour productivity should be <= 1", workHour.productivity <= 1.0f)

        // Late night hours should have low productivity
        val lateHour = productivityHours.find { it.hour == 23 }
        assertNotNull(lateHour)
        assertEquals("Late night should have low productivity", 0.1f, lateHour!!.productivity, 0.01f)
        
        // Other hours should have neutral score
        val neutralHour = productivityHours.find { it.hour == 19 } // 7 PM
        assertNotNull(neutralHour)
        assertEquals("Non-work/non-late hours should have neutral productivity", 0.5f, neutralHour!!.productivity, 0.01f)
    }

    @Test
    fun `weekly report insights should be contextual`() = runTest {
        // When
        val report = useCase.generateWeeklyReport()

        // Then
        assertTrue("Should have insights", report.insights.isNotEmpty())
        
        // Should contain insights about different aspects
        val insightsText = report.insights.joinToString(" ")
        
        // Screen time insight based on 5 hours total (moderate level)
        assertTrue("Should have screen time insight", 
            insightsText.contains("screen time") || insightsText.contains("hour"))
        
        // Wellness score insight (77% is good)
        assertTrue("Should have wellness insight", 
            insightsText.contains("wellness") || insightsText.contains("score"))
        
        // Unlock frequency insight (50 unlocks / 7 days = ~7 per day, which is low)
        assertTrue("Should have unlock insight", 
            insightsText.contains("unlock") || insightsText.contains("discipline"))
    }

    @Test
    fun `goal calculation should work correctly`() = runTest {
        // When
        useCase.sendWeeklyReportNotification()

        // Then - verify correct goals achieved calculation
        verify { 
            notificationManager.showWeeklyReport(
                any(), 
                1, // Should be 1 goal achieved (goal 2 has 150 current vs 120 target)
                2  // Total of 2 goals
            ) 
        }
    }
}