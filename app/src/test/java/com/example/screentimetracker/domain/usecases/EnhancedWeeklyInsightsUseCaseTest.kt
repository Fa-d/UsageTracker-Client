package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.AppSessionDataAggregate
import com.example.screentimetracker.data.local.DailyAppSummary
import com.example.screentimetracker.data.local.Goal
import com.example.screentimetracker.data.local.SessionData
import com.example.screentimetracker.domain.model.WellnessScore
import com.example.screentimetracker.domain.repository.TrackerRepository
import com.example.screentimetracker.utils.logger.AppLogger
import com.example.screentimetracker.utils.ui.AppNotificationManager
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class EnhancedWeeklyInsightsUseCaseTest {

    @MockK
    private lateinit var repository: TrackerRepository

    @MockK
    private lateinit var notificationManager: AppNotificationManager

    @MockK
    private lateinit var appLogger: AppLogger

    private lateinit var useCase: WeeklyInsightsUseCase

    // Test data
    private val currentTime = System.currentTimeMillis()
    private val weekStart = currentTime - TimeUnit.DAYS.toMillis(7)
    
    private val sampleSessionData = listOf(
        AppSessionDataAggregate(
            packageName = "com.instagram.android",
            totalDuration = TimeUnit.HOURS.toMillis(2), // 2 hours
            dayStartMillis = weekStart
        ),
        AppSessionDataAggregate(
            packageName = "com.netflix.mediaclient", 
            totalDuration = TimeUnit.HOURS.toMillis(3), // 3 hours
            dayStartMillis = weekStart + TimeUnit.DAYS.toMillis(1)
        )
    )
    
    private val sampleAppSummaries = listOf(
        DailyAppSummary(
            packageName = "com.instagram.android",
            totalDurationMillis = TimeUnit.HOURS.toMillis(2),
            openCount = 25,
            dayStartMillis = weekStart
        ),
        DailyAppSummary(
            packageName = "com.netflix.mediaclient",
            totalDurationMillis = TimeUnit.HOURS.toMillis(3), 
            openCount = 10,
            dayStartMillis = weekStart
        )
    )
    
    private val sampleSessions = listOf(
        SessionData(
            id = 1,
            packageName = "com.instagram.android",
            startTimeMillis = weekStart + TimeUnit.HOURS.toMillis(9), // 9 AM
            endTimeMillis = weekStart + TimeUnit.HOURS.toMillis(10), // 10 AM
            durationMillis = TimeUnit.HOURS.toMillis(1)
        ),
        SessionData(
            id = 2,
            packageName = "com.netflix.mediaclient", 
            startTimeMillis = weekStart + TimeUnit.HOURS.toMillis(14), // 2 PM
            endTimeMillis = weekStart + TimeUnit.HOURS.toMillis(15), // 3 PM
            durationMillis = TimeUnit.HOURS.toMillis(1)
        ),
        SessionData(
            id = 3,
            packageName = "com.instagram.android",
            startTimeMillis = weekStart + TimeUnit.HOURS.toMillis(22), // 10 PM
            endTimeMillis = weekStart + TimeUnit.HOURS.toMillis(23), // 11 PM
            durationMillis = TimeUnit.HOURS.toMillis(1)
        )
    )
    
    private val sampleWellnessScores = listOf(
        WellnessScore(1, 75, weekStart, weekStart),
        WellnessScore(2, 80, weekStart + TimeUnit.DAYS.toMillis(1), weekStart + TimeUnit.DAYS.toMillis(1))
    )
    
    private val sampleGoals = listOf(
        Goal(1, "Reduce Social Media", 60, 45, System.currentTimeMillis(), true),
        Goal(2, "Increase Focus Time", 120, 150, System.currentTimeMillis(), true)
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        
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
        assertNotNull(report)
        assertEquals(5, report.totalScreenTimeMillis / TimeUnit.HOURS.toMillis(1)) // 5 hours total
        assertEquals(0, report.averageDailyScreenTimeMillis / TimeUnit.HOURS.toMillis(1)) // 0 hours average (5/7 rounded down)
        assertEquals(50, report.totalUnlocks)
        assertEquals(7, report.averageUnlocksPerDay) // 50/7
        assertEquals(77, report.averageWellnessScore) // Average of 75 and 80
        assertEquals(2, report.topApps.size)
        assertTrue(report.insights.isNotEmpty())
        
        // Verify top apps are sorted by usage time
        val topApp = report.topApps.first()
        assertEquals("com.netflix.mediaclient", topApp.packageName)
        assertEquals(3, topApp.totalTimeMillis / TimeUnit.HOURS.toMillis(1)) // 3 hours
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
        assertEquals(24, productivityHours.size)
        
        // Check specific hours have data
        val hour9 = productivityHours.find { it.hour == 9 }
        assertNotNull(hour9)
        assertTrue(hour9!!.usageTimeMillis > 0)
        assertTrue(hour9.productivity > 0)
        
        val hour14 = productivityHours.find { it.hour == 14 }
        assertNotNull(hour14)
        assertTrue(hour14!!.usageTimeMillis > 0)
        
        val hour22 = productivityHours.find { it.hour == 22 }
        assertNotNull(hour22)
        assertTrue(hour22!!.usageTimeMillis > 0)
        assertEquals(0.1f, hour22.productivity, 0.01f) // Late night usage should have low productivity
        
        // Check hours with no data
        val hour3 = productivityHours.find { it.hour == 3 }
        assertNotNull(hour3)
        assertEquals(0, hour3!!.usageTimeMillis)
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
        assertFalse(categoryInsights.isEmpty())
        
        // Find social category (Instagram)
        val socialCategory = categoryInsights.find { it.categoryName == "Social" }
        assertNotNull(socialCategory)
        assertEquals(2, socialCategory!!.totalTimeMillis / TimeUnit.HOURS.toMillis(1)) // Instagram 2 hours
        assertTrue(socialCategory.percentageOfTotal > 0)
        
        // Find entertainment category (Netflix)  
        val entertainmentCategory = categoryInsights.find { it.categoryName == "Entertainment" }
        assertNotNull(entertainmentCategory)
        assertEquals(3, entertainmentCategory!!.totalTimeMillis / TimeUnit.HOURS.toMillis(1)) // Netflix 3 hours
        
        // Categories should be sorted by usage time (Entertainment first with 3h, then Social with 2h)
        assertEquals("Entertainment", categoryInsights.first().categoryName)
        assertEquals("Social", categoryInsights[1].categoryName)
        
        // Verify percentages add up correctly
        val totalPercentage = categoryInsights.sumOf { it.percentageOfTotal.toDouble() }
        assertTrue("Percentages should be reasonable", totalPercentage <= 100.0)
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