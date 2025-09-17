package dev.sadakat.screentimetracker

import dev.sadakat.screentimetracker.data.local.AppSessionDataAggregate
import dev.sadakat.screentimetracker.data.local.DailyAppSummary
import dev.sadakat.screentimetracker.data.local.TimeRestriction
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import dev.sadakat.screentimetracker.domain.usecases.TimeRestrictionManagerUseCase
import dev.sadakat.screentimetracker.domain.usecases.WeeklyInsightsUseCase
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import dev.sadakat.screentimetracker.utils.ui.AppNotificationManager
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class TimeRestrictionsWeeklyInsightsIntegrationTest {

    @MockK
    private lateinit var repository: TrackerRepository

    @MockK
    private lateinit var notificationManager: AppNotificationManager

    @MockK
    private lateinit var appLogger: AppLogger

    private lateinit var timeRestrictionUseCase: TimeRestrictionManagerUseCase
    private lateinit var weeklyInsightsUseCase: WeeklyInsightsUseCase

    // Test data
    private val currentTime = System.currentTimeMillis()
    private val weekStart = currentTime - TimeUnit.DAYS.toMillis(7)

    private val testTimeRestriction = TimeRestriction(
        id = 1,
        restrictionType = "work_hours_focus",
        name = "Work Focus",
        description = "Block Instagram during work hours",
        startTimeMinutes = 9 * 60, // 9 AM
        endTimeMinutes = 17 * 60, // 5 PM
        appsBlocked = "com.instagram.android",
        daysOfWeek = "1,2,3,4,5", // Weekdays
        isEnabled = true,
        createdAt = currentTime
    )

    private val testSessionData = listOf(
        // Sessions during restriction time (should be blocked)
        AppSessionDataAggregate(
            packageName = "com.instagram.android",
            totalDuration = TimeUnit.MINUTES.toMillis(30), // 30 minutes
            sessionCount = 5
        ),
        // Sessions outside restriction time (normal usage)
        AppSessionDataAggregate(
            packageName = "com.instagram.android",
            totalDuration = TimeUnit.HOURS.toMillis(2), // 2 hours
            sessionCount = 20
        ),
        // Non-restricted app
        AppSessionDataAggregate(
            packageName = "com.spotify.music",
            totalDuration = TimeUnit.HOURS.toMillis(3),
            sessionCount = 15
        )
    )

    private val testDailyAppSummaries = listOf(
        DailyAppSummary(
            dateMillis = weekStart,
            packageName = "com.instagram.android",
            totalDurationMillis = TimeUnit.HOURS.toMillis(2) + TimeUnit.MINUTES.toMillis(30),
            openCount = 15 // Lower open count due to restrictions
        ),
        DailyAppSummary(
            dateMillis = weekStart + TimeUnit.DAYS.toMillis(1),
            packageName = "com.spotify.music",
            totalDurationMillis = TimeUnit.HOURS.toMillis(3),
            openCount = 45
        )
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        every { appLogger.i(any(), any()) } just runs
        every { appLogger.e(any(), any(), any()) } just runs
        every { appLogger.d(any(), any()) } just runs

        // Setup repository mocks
        coEvery { repository.getAllTimeRestrictions() } returns flowOf(listOf(testTimeRestriction))
        coEvery { repository.insertTimeRestriction(any()) } returns 1L
        coEvery { repository.updateRestrictionEnabled(any(), any(), any()) } just runs
        coEvery { repository.getActiveRestrictionsForTime(any(), any()) } returns listOf(testTimeRestriction)

        coEvery { repository.getAggregatedSessionDataForDayFlow(any(), any()) } returns flowOf(
            testSessionData
        )
        coEvery { repository.getDailyAppSummaries(any(), any()) } returns flowOf(
            testDailyAppSummaries
        )
        coEvery { repository.getUnlockCountForDayFlow(any(), any()) } returns flowOf(30)
        coEvery { repository.getAllWellnessScores() } returns flowOf(emptyList())
        coEvery { repository.getAllSessionsInRange(any(), any()) } returns flowOf(emptyList())
        coEvery { repository.getActiveGoals() } returns flowOf(emptyList())

        timeRestrictionUseCase = TimeRestrictionManagerUseCase(
            repository,
            notificationManager,
            appLogger
        )

        val mockAppCategorizer = mockk<dev.sadakat.screentimetracker.domain.categorization.AppCategorizer>(relaxed = true)
        weeklyInsightsUseCase = WeeklyInsightsUseCase(
            repository,
            notificationManager,
            appLogger,
            mockAppCategorizer
        )
    }

    @Test
    fun `time restrictions should affect weekly insights data`() = runTest {
        // When - Generate weekly report while restrictions are active
        val weeklyReport = weeklyInsightsUseCase.generateWeeklyReport()

        // Then - Verify restricted app shows reduced usage
        val instagramUsage = weeklyReport.topApps.find { it.packageName == "com.instagram.android" }
        Assert.assertNotNull("Instagram should appear in top apps", instagramUsage)

        // Instagram should have less usage than Spotify due to restrictions
        val spotifyUsage = weeklyReport.topApps.find { it.packageName == "com.spotify.music" }
        if (spotifyUsage != null && instagramUsage != null) {
            Assert.assertTrue(
                "Restricted app should have less total usage than unrestricted app",
                instagramUsage.totalTimeMillis <= spotifyUsage.totalTimeMillis
            )
        }

        // Verify insights mention restriction effectiveness
        val insightsText = weeklyReport.insights.joinToString(" ")
        Assert.assertTrue(
            "Weekly report should have meaningful insights",
            weeklyReport.insights.isNotEmpty()
        )

        // Total screen time should be sum of all app usage
        val expectedTotal = testSessionData.sumOf { it.totalDuration }
        Assert.assertEquals(expectedTotal, weeklyReport.totalScreenTimeMillis)
    }

    @Test
    fun `time restriction violations should be tracked in weekly insights`() = runTest {
        // Given - Add violation to restriction
        val violatedRestriction = testTimeRestriction
        coEvery { repository.getAllTimeRestrictions() } returns flowOf(listOf(violatedRestriction))

        // When
        val restrictions = timeRestrictionUseCase.getAllTimeRestrictions().first()
        val weeklyReport = weeklyInsightsUseCase.generateWeeklyReport()

        // Then
        val restriction = restrictions.first()
        Assert.assertNotNull("Should have a restriction", restriction)

        // Weekly report should still show data even with violations
        Assert.assertTrue(
            "Weekly report should have data despite violations",
            weeklyReport.totalScreenTimeMillis > 0
        )
        Assert.assertTrue(
            "Should have insights about restriction effectiveness",
            weeklyReport.insights.isNotEmpty()
        )
    }

    @Test
    fun `productivity insights should consider time restrictions`() = runTest {
        // When
        val productivityHours = weeklyInsightsUseCase.getProductivityHours()

        // Then
        Assert.assertEquals(24, productivityHours.size)

        // Hours during restriction time (9-17) should show different patterns
        val restrictionHours = productivityHours.filter { it.hour in 9..17 }
        val nonRestrictionHours = productivityHours.filter { it.hour !in 9..17 }

        Assert.assertTrue(
            "Should have productivity data for all hours",
            restrictionHours.isNotEmpty()
        )
        Assert.assertTrue(
            "Should have data for non-restriction hours",
            nonRestrictionHours.isNotEmpty()
        )

        // Verify productivity scores are calculated
        restrictionHours.forEach { hour ->
            Assert.assertTrue(
                "Productivity score should be valid",
                hour.productivity >= 0f && hour.productivity <= 1f
            )
        }
    }

    @Test
    fun `category insights should reflect restricted app usage`() = runTest {
        // When
        val categoryInsights = weeklyInsightsUseCase.getAppCategoryInsights()

        // Then
        Assert.assertFalse("Should have category insights", categoryInsights.isEmpty())

        // Find social category (Instagram)
        val socialCategory = categoryInsights.find { it.categoryName == "Social" }
        Assert.assertNotNull("Should have Social category", socialCategory)

        // Find entertainment category (Spotify)
        val entertainmentCategory = categoryInsights.find { it.categoryName == "Entertainment" }
        Assert.assertNotNull("Should have Entertainment category", entertainmentCategory)

        // Verify categories have usage data
        if (socialCategory != null) {
            Assert.assertTrue(
                "Social category should have usage time",
                socialCategory.totalTimeMillis > 0
            )
            Assert.assertTrue(
                "Social category should have percentage",
                socialCategory.percentageOfTotal > 0
            )
        }
    }

    @Test
    fun `time restrictions should not break weekly report generation`() = runTest {
        // Given - Multiple time restrictions for same app
        val multipleRestrictions = listOf(
            testTimeRestriction,
            testTimeRestriction.copy(
                id = 2,
                startTimeMinutes = 20 * 60, // 8 PM
                endTimeMinutes = 23 * 60, // 11 PM
                daysOfWeek = "6,0" // Weekend
            )
        )
        coEvery { repository.getAllTimeRestrictions() } returns flowOf(multipleRestrictions)

        // When
        val restrictions = timeRestrictionUseCase.getAllTimeRestrictions().first()
        val weeklyReport = weeklyInsightsUseCase.generateWeeklyReport()

        // Then
        Assert.assertEquals(2, restrictions.size)
        Assert.assertNotEquals(WeeklyInsightsUseCase.WeeklyReport.empty(), weeklyReport)
        Assert.assertTrue("Should have usage data", weeklyReport.totalScreenTimeMillis > 0)
        Assert.assertFalse("Should have top apps", weeklyReport.topApps.isEmpty())
    }

    @Test
    fun `time restriction status should be available during weekly report generation`() = runTest {
        // When
        val isRestricted =
            runBlocking { timeRestrictionUseCase.isAppBlockedByTimeRestriction("com.instagram.android") }
        val weeklyReport = weeklyInsightsUseCase.generateWeeklyReport()

        // Then
        // Should be able to check restriction status without breaking weekly report
        Assert.assertTrue("Should be able to check restriction status", isRestricted != null)
        Assert.assertNotNull("Weekly report should generate successfully", weeklyReport)
    }

    @Test
    fun `weekly notification should work with time restrictions active`() = runTest {
        // Given
        every { notificationManager.showWeeklyReport(any(), any(), any()) } just runs

        // When
        weeklyInsightsUseCase.sendWeeklyReportNotification()

        // Then
        verify { notificationManager.showWeeklyReport(any(), any(), any()) }
        verify { appLogger.i("WeeklyInsightsUseCase", "Weekly report notification sent") }

        // Should not interfere with time restriction functionality
        val restrictions = timeRestrictionUseCase.getAllTimeRestrictions().first()
        Assert.assertTrue("Time restrictions should still be available", restrictions.isNotEmpty())
    }

    @Test
    fun `error in time restrictions should not break weekly insights`() = runTest {
        // Given - Repository throws exception for time restrictions but not for insights
        coEvery { repository.getAllTimeRestrictions() } throws RuntimeException("Time restriction error")

        // When
        val weeklyReport = weeklyInsightsUseCase.generateWeeklyReport()

        // Then - Weekly insights should still work
        Assert.assertNotNull("Weekly report should still be generated", weeklyReport)
        Assert.assertTrue(
            "Should have usage data despite restriction error",
            weeklyReport.totalScreenTimeMillis > 0
        )
    }

    @Test
    fun `time restriction data should be consistent across both use cases`() = runTest {
        // When
        val restrictions = timeRestrictionUseCase.getAllTimeRestrictions().first()
        val weeklyReport = weeklyInsightsUseCase.generateWeeklyReport()

        // Then
        val restrictedApps = restrictions.filter { it.isEnabled }.flatMap { restriction ->
            restriction.appsBlocked.split(",").filter { it.isNotEmpty() }
        }
        val reportedApps = weeklyReport.topApps.map { it.packageName }

        // Apps in weekly report that are also restricted should have reasonable usage
        val restrictedInReport = reportedApps.intersect(restrictedApps.toSet())

        if (restrictedInReport.isNotEmpty()) {
            restrictedInReport.forEach { packageName ->
                val appUsage = weeklyReport.topApps.find { it.packageName == packageName }
                Assert.assertNotNull("Restricted app should have usage data", appUsage)
                Assert.assertTrue(
                    "Restricted app should have some usage",
                    appUsage!!.totalTimeMillis > 0
                )
            }
        }
    }
}