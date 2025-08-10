package com.example.screentimetracker.performance

import com.example.screentimetracker.data.local.*
import com.example.screentimetracker.domain.model.WellnessScore
import com.example.screentimetracker.domain.repository.TrackerRepository
import com.example.screentimetracker.domain.usecases.WeeklyInsightsUseCase
import com.example.screentimetracker.utils.logger.AppLogger
import com.example.screentimetracker.utils.ui.AppNotificationManager
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

class LargeDatasetPerformanceTest {

    @MockK
    private lateinit var repository: TrackerRepository

    @MockK
    private lateinit var notificationManager: AppNotificationManager

    @MockK
    private lateinit var appLogger: AppLogger

    private lateinit var weeklyInsightsUseCase: WeeklyInsightsUseCase

    // Performance thresholds (in milliseconds)
    private val WEEKLY_REPORT_THRESHOLD = 5000L // 5 seconds
    private val PRODUCTIVITY_HOURS_THRESHOLD = 3000L // 3 seconds
    private val CATEGORY_INSIGHTS_THRESHOLD = 2000L // 2 seconds

    private val currentTime = System.currentTimeMillis()
    private val weekStart = currentTime - TimeUnit.DAYS.toMillis(7)

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        
        every { appLogger.i(any(), any()) } just runs
        every { appLogger.e(any(), any(), any()) } just runs
        every { appLogger.d(any(), any()) } just runs

        weeklyInsightsUseCase = WeeklyInsightsUseCase(repository, notificationManager, appLogger)
    }

    @Test
    fun `generateWeeklyReport should perform well with large dataset`() = runTest {
        // Given - Large dataset (10,000 app sessions over 7 days)
        val largeSessionDataset = generateLargeSessionDataset(10000)
        val largeDailySummaries = generateLargeDailyAppSummaries(1000) // 1000 unique apps
        val largeWellnessScores = generateLargeWellnessScores(7 * 24) // Hourly scores for a week
        val largeGoals = generateLargeGoals(100) // 100 goals
        
        coEvery { repository.getAggregatedSessionDataForDayFlow(any(), any()) } returns flowOf(largeSessionDataset)
        coEvery { repository.getDailyAppSummaries(any(), any()) } returns flowOf(largeDailySummaries)
        coEvery { repository.getUnlockCountForDayFlow(any(), any()) } returns flowOf(500) // High unlock count
        coEvery { repository.getAllWellnessScores() } returns flowOf(largeWellnessScores)
        coEvery { repository.getAllSessionsInRange(any(), any()) } returns flowOf(emptyList())
        coEvery { repository.getActiveGoals() } returns flowOf(largeGoals)

        // When - Measure performance
        val executionTime = measureTimeMillis {
            val report = weeklyInsightsUseCase.generateWeeklyReport()
            
            // Verify report is generated correctly
            assertNotNull("Report should not be null", report)
            assertTrue("Should have processed session data", report.totalScreenTimeMillis > 0)
            assertTrue("Should have top apps", report.topApps.isNotEmpty())
            assertTrue("Should have insights", report.insights.isNotEmpty())
            assertTrue("Top apps should be limited to reasonable number", report.topApps.size <= 10)
        }

        // Then - Verify performance
        assertTrue(
            "Weekly report generation should complete within $WEEKLY_REPORT_THRESHOLD ms, but took $executionTime ms",
            executionTime < WEEKLY_REPORT_THRESHOLD
        )
        
        println("Weekly report generation with 10,000 sessions took: $executionTime ms")
    }

    @Test
    fun `getProductivityHours should perform well with large dataset`() = runTest {
        // Given - Large session dataset with varied hours
        val largeSessions = generateLargeSessionsWithHours(50000) // 50,000 sessions across all hours
        
        coEvery { repository.getAllSessionsInRange(any(), any()) } returns flowOf(largeSessions)

        // When - Measure performance
        val executionTime = measureTimeMillis {
            val productivityHours = weeklyInsightsUseCase.getProductivityHours()
            
            // Verify correct processing
            assertEquals("Should have 24 hours", 24, productivityHours.size)
            productivityHours.forEach { hour ->
                assertTrue("Hour should be valid", hour.hour in 0..23)
                assertTrue("Usage time should be non-negative", hour.usageTimeMillis >= 0)
                assertTrue("Productivity score should be valid", hour.productivity in 0f..1f)
            }
        }

        // Then - Verify performance
        assertTrue(
            "Productivity hours calculation should complete within $PRODUCTIVITY_HOURS_THRESHOLD ms, but took $executionTime ms",
            executionTime < PRODUCTIVITY_HOURS_THRESHOLD
        )
        
        println("Productivity hours calculation with 50,000 sessions took: $executionTime ms")
    }

    @Test
    fun `getAppCategoryInsights should perform well with large dataset`() = runTest {
        // Given - Large dataset with diverse app categories
        val largeDailySummaries = generateDiverseAppCategories(5000) // 5000 apps across categories
        
        coEvery { repository.getDailyAppSummaries(any(), any()) } returns flowOf(largeDailySummaries)

        // When - Measure performance
        val executionTime = measureTimeMillis {
            val categoryInsights = weeklyInsightsUseCase.getAppCategoryInsights()
            
            // Verify correct processing
            assertFalse("Should have category insights", categoryInsights.isEmpty())
            
            val totalPercentage = categoryInsights.sumOf { it.percentageOfTotal.toDouble() }
            assertTrue("Total percentages should be reasonable", totalPercentage <= 100.1) // Allow small floating point error
            
            categoryInsights.forEach { category ->
                assertTrue("Category name should not be empty", category.categoryName.isNotEmpty())
                assertTrue("Total time should be non-negative", category.totalTimeMillis >= 0)
                assertTrue("Percentage should be non-negative", category.percentageOfTotal >= 0f)
            }
        }

        // Then - Verify performance
        assertTrue(
            "Category insights calculation should complete within $CATEGORY_INSIGHTS_THRESHOLD ms, but took $executionTime ms",
            executionTime < CATEGORY_INSIGHTS_THRESHOLD
        )
        
        println("Category insights calculation with 5,000 apps took: $executionTime ms")
    }

    @Test
    fun `memory usage should remain reasonable with large datasets`() = runTest {
        // Given - Very large dataset
        val veryLargeSessionDataset = generateLargeSessionDataset(100000) // 100,000 sessions
        val veryLargeDailySummaries = generateLargeDailyAppSummaries(10000) // 10,000 apps
        
        coEvery { repository.getAggregatedSessionDataForDayFlow(any(), any()) } returns flowOf(veryLargeSessionDataset)
        coEvery { repository.getDailyAppSummaries(any(), any()) } returns flowOf(veryLargeDailySummaries)
        coEvery { repository.getUnlockCountForDayFlow(any(), any()) } returns flowOf(1000)
        coEvery { repository.getAllWellnessScores() } returns flowOf(emptyList())
        coEvery { repository.getAllSessionsInRange(any(), any()) } returns flowOf(emptyList())
        coEvery { repository.getActiveGoals() } returns flowOf(emptyList())

        // When - Process large dataset
        val runtime = Runtime.getRuntime()
        val memoryBefore = runtime.totalMemory() - runtime.freeMemory()
        
        val report = weeklyInsightsUseCase.generateWeeklyReport()
        
        // Force garbage collection to get accurate memory reading
        System.gc()
        Thread.sleep(100)
        val memoryAfter = runtime.totalMemory() - runtime.freeMemory()
        val memoryUsed = memoryAfter - memoryBefore

        // Then - Verify memory usage is reasonable
        val maxMemoryMB = 50 // 50 MB threshold
        val memoryUsedMB = memoryUsed / (1024 * 1024)
        
        assertTrue(
            "Memory usage should be reasonable (under ${maxMemoryMB}MB), but used ${memoryUsedMB}MB",
            memoryUsedMB < maxMemoryMB
        )
        
        // Verify data was processed correctly despite large size
        assertNotNull("Report should be generated", report)
        assertTrue("Should have processed large dataset", report.totalScreenTimeMillis > 0)
        
        println("Memory used for 100,000 sessions: ${memoryUsedMB}MB")
    }

    @Test
    fun `concurrent operations should perform well`() = runTest {
        // Given - Large dataset for concurrent operations
        val sessionDataset = generateLargeSessionDataset(5000)
        val dailySummaries = generateLargeDailyAppSummaries(500)
        val sessions = generateLargeSessionsWithHours(10000)
        
        coEvery { repository.getAggregatedSessionDataForDayFlow(any(), any()) } returns flowOf(sessionDataset)
        coEvery { repository.getDailyAppSummaries(any(), any()) } returns flowOf(dailySummaries)
        coEvery { repository.getUnlockCountForDayFlow(any(), any()) } returns flowOf(200)
        coEvery { repository.getAllWellnessScores() } returns flowOf(emptyList())
        coEvery { repository.getAllSessionsInRange(any(), any()) } returns flowOf(sessions)
        coEvery { repository.getActiveGoals() } returns flowOf(emptyList())

        // When - Run operations concurrently and measure total time
        val totalExecutionTime = measureTimeMillis {
            val report = weeklyInsightsUseCase.generateWeeklyReport()
            val productivityHours = weeklyInsightsUseCase.getProductivityHours()
            val categoryInsights = weeklyInsightsUseCase.getAppCategoryInsights()
            
            // Verify all operations completed successfully
            assertNotNull("Weekly report should be generated", report)
            assertEquals("Productivity hours should have 24 entries", 24, productivityHours.size)
            assertFalse("Category insights should not be empty", categoryInsights.isEmpty())
        }

        // Then - Verify total time is reasonable
        val maxConcurrentTime = 10000L // 10 seconds for all operations
        assertTrue(
            "Concurrent operations should complete within $maxConcurrentTime ms, but took $totalExecutionTime ms",
            totalExecutionTime < maxConcurrentTime
        )
        
        println("Concurrent operations with large dataset took: $totalExecutionTime ms")
    }

    @Test
    fun `edge case - empty large collections should perform well`() = runTest {
        // Given - Large empty collections (simulating database with many empty results)
        coEvery { repository.getAggregatedSessionDataForDayFlow(any(), any()) } returns flowOf(emptyList())
        coEvery { repository.getDailyAppSummaries(any(), any()) } returns flowOf(emptyList())
        coEvery { repository.getUnlockCountForDayFlow(any(), any()) } returns flowOf(0)
        coEvery { repository.getAllWellnessScores() } returns flowOf(emptyList())
        coEvery { repository.getAllSessionsInRange(any(), any()) } returns flowOf(emptyList())
        coEvery { repository.getActiveGoals() } returns flowOf(emptyList())

        // When - Measure performance with empty datasets
        val executionTime = measureTimeMillis {
            val report = weeklyInsightsUseCase.generateWeeklyReport()
            val productivityHours = weeklyInsightsUseCase.getProductivityHours()
            val categoryInsights = weeklyInsightsUseCase.getAppCategoryInsights()
            
            // Verify empty datasets are handled efficiently
            assertEquals("Empty report should be returned quickly", WeeklyInsightsUseCase.WeeklyReport.empty(), report)
            assertTrue("Empty productivity hours should be returned quickly", productivityHours.isEmpty())
            assertTrue("Empty category insights should be returned quickly", categoryInsights.isEmpty())
        }

        // Then - Should be very fast with empty data
        val emptyDataThreshold = 500L // 500ms
        assertTrue(
            "Empty dataset operations should complete within $emptyDataThreshold ms, but took $executionTime ms",
            executionTime < emptyDataThreshold
        )
        
        println("Empty dataset operations took: $executionTime ms")
    }

    // Helper methods to generate test data
    
    private fun generateLargeSessionDataset(count: Int): List<AppSessionDataAggregate> {
        val packages = listOf(
            "com.instagram.android", "com.netflix.mediaclient", "com.spotify.music",
            "com.whatsapp", "com.google.android.youtube", "com.facebook.katana",
            "com.twitter.android", "com.snapchat.android", "com.pinterest",
            "com.linkedin.android"
        )
        
        return (1..count).map { i ->
            AppSessionDataAggregate(
                packageName = packages[i % packages.size],
                totalDuration = TimeUnit.MINUTES.toMillis((1..120).random().toLong()),
                sessionCount = (1..20).random()
            )
        }
    }
    
    private fun generateLargeDailyAppSummaries(count: Int): List<DailyAppSummary> {
        return (1..count).map { i ->
            DailyAppSummary(
                dateMillis = weekStart + TimeUnit.DAYS.toMillis((i % 7).toLong()),
                packageName = "com.test.app$i",
                totalDurationMillis = TimeUnit.MINUTES.toMillis((1..300).random().toLong()),
                openCount = (1..50).random()
            )
        }
    }
    
    private fun generateLargeWellnessScores(count: Int): List<WellnessScore> {
        return (1..count).map { i ->
            WellnessScore(
                date = weekStart + TimeUnit.DAYS.toMillis((i % 7).toLong()),
                totalScore = (0..100).random(),
                timeLimitScore = (0..25).random(),
                focusSessionScore = (0..25).random(),
                breaksScore = (0..25).random(),
                sleepHygieneScore = (0..25).random(),
                level = com.example.screentimetracker.domain.model.WellnessLevel.values().random(),
                calculatedAt = currentTime - TimeUnit.HOURS.toMillis((count - i).toLong())
            )
        }
    }
    
    private fun generateLargeGoals(count: Int): List<UserGoal> {
        return (1..count).map { i ->
            UserGoal(
                id = i.toLong(),
                goalType = "goal_type_$i",
                targetValue = (60..300).random().toLong(),
                currentProgress = (0..350).random().toLong(),
                createdAt = currentTime - TimeUnit.DAYS.toMillis((i % 30).toLong()),
                isActive = (i % 3) != 0 // About 2/3 active
            )
        }
    }
    
    private fun generateLargeSessionsWithHours(count: Int): List<AppSessionEvent> {
        return (1..count).map { i ->
            val startHour = (0..23).random()
            val startTime = weekStart + TimeUnit.DAYS.toMillis((i % 7).toLong()) + TimeUnit.HOURS.toMillis(startHour.toLong())
            val duration = TimeUnit.MINUTES.toMillis((1..60).random().toLong())
            
            AppSessionEvent(
                id = i.toLong(),
                packageName = "com.test.app${i % 100}",
                startTimeMillis = startTime,
                endTimeMillis = startTime + duration,
                durationMillis = duration
            )
        }
    }
    
    private fun generateDiverseAppCategories(count: Int): List<DailyAppSummary> {
        val socialApps = listOf("com.instagram.android", "com.facebook.katana", "com.twitter.android")
        val entertainmentApps = listOf("com.netflix.mediaclient", "com.spotify.music", "com.youtube.android")
        val productivityApps = listOf("com.microsoft.office.word", "com.google.android.apps.docs", "com.slack")
        val gameApps = listOf("com.supercell.clashofclans", "com.king.candycrushsaga", "com.rovio.angrybirdsdream")
        
        val allApps = socialApps + entertainmentApps + productivityApps + gameApps
        
        return (1..count).map { i ->
            DailyAppSummary(
                dateMillis = weekStart + TimeUnit.DAYS.toMillis((i % 7).toLong()),
                packageName = if (i <= allApps.size) allApps[i - 1] else "com.test.app$i",
                totalDurationMillis = TimeUnit.MINUTES.toMillis((1..240).random().toLong()),
                openCount = (1..30).random()
            )
        }
    }
}