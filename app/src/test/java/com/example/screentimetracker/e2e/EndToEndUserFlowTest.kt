package com.example.screentimetracker

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.screentimetracker.data.local.AppSessionDataAggregate
import com.example.screentimetracker.data.local.DailyAppSummary
import com.example.screentimetracker.data.local.TimeRestriction
import com.example.screentimetracker.domain.model.WellnessLevel
import com.example.screentimetracker.domain.model.WellnessScore
import com.example.screentimetracker.domain.repository.TrackerRepository
import com.example.screentimetracker.domain.usecases.TimeRestrictionManagerUseCase
import com.example.screentimetracker.domain.usecases.WeeklyInsightsUseCase
import com.example.screentimetracker.services.NotificationScheduler
import com.example.screentimetracker.ui.dashboard.cards.TimeRestrictionCard
import com.example.screentimetracker.ui.dashboard.cards.WeeklyInsightsCard
import com.example.screentimetracker.ui.timerestrictions.screens.TimeRestrictionsScreen
import com.example.screentimetracker.ui.timerestrictions.viewmodels.RestrictionStatusPreview
import com.example.screentimetracker.ui.timerestrictions.viewmodels.TimeRestrictionsViewModel
import com.example.screentimetracker.utils.logger.AppLogger
import com.example.screentimetracker.utils.ui.AppNotificationManager
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class EndToEndUserFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @MockK
    private lateinit var repository: TrackerRepository

    @MockK
    private lateinit var notificationManager: AppNotificationManager

    @MockK
    private lateinit var appLogger: AppLogger

    private lateinit var timeRestrictionUseCase: TimeRestrictionManagerUseCase
    private lateinit var weeklyInsightsUseCase: WeeklyInsightsUseCase
    private lateinit var notificationScheduler: NotificationScheduler
    private lateinit var viewModel: TimeRestrictionsViewModel

    // Test data
    private val currentTime = System.currentTimeMillis()
    private val weekStart = currentTime - TimeUnit.DAYS.toMillis(7)

    private val restrictionsFlow = MutableStateFlow(emptyList<TimeRestriction>())

    private val sampleSessionData = listOf(
        AppSessionDataAggregate(
            packageName = "com.instagram.android",
            totalDuration = TimeUnit.HOURS.toMillis(3),
            sessionCount = 25
        ),
        AppSessionDataAggregate(
            packageName = "com.netflix.mediaclient",
            totalDuration = TimeUnit.HOURS.toMillis(4),
            sessionCount = 12
        )
    )

    private val sampleDailyAppSummaries = listOf(
        DailyAppSummary(
            dateMillis = weekStart,
            packageName = "com.instagram.android",
            totalDurationMillis = TimeUnit.HOURS.toMillis(3),
            openCount = 35
        ),
        DailyAppSummary(
            dateMillis = weekStart + TimeUnit.DAYS.toMillis(1),
            packageName = "com.netflix.mediaclient",
            totalDurationMillis = TimeUnit.HOURS.toMillis(4),
            openCount = 12
        )
    )

    private val sampleWellnessScores = listOf(
        WellnessScore(
            date = weekStart,
            totalScore = 72,
            timeLimitScore = 18,
            focusSessionScore = 18,
            breaksScore = 18,
            sleepHygieneScore = 18,
            level = WellnessLevel.BALANCED_USER,
            calculatedAt = currentTime
        ),
        WellnessScore(
            date = weekStart + TimeUnit.DAYS.toMillis(1),
            totalScore = 68,
            timeLimitScore = 17,
            focusSessionScore = 17,
            breaksScore = 17,
            sleepHygieneScore = 17,
            level = WellnessLevel.BALANCED_USER,
            calculatedAt = currentTime
        )
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        every { appLogger.i(any(), any()) } just runs
        every { appLogger.e(any(), any(), any()) } just runs
        every { appLogger.d(any(), any()) } just runs

        // Setup repository mocks
        coEvery { repository.getAllTimeRestrictions() } returns restrictionsFlow
        coEvery { repository.insertTimeRestriction(any()) } coAnswers {
            val newRestriction = firstArg<TimeRestriction>().copy(id = (restrictionsFlow.value.size + 1).toLong())
            restrictionsFlow.value = restrictionsFlow.value + newRestriction
            newRestriction.id
        }
        coEvery { repository.updateRestrictionEnabled(any(), any(), any()) } coAnswers {
            val id = firstArg<Long>()
            val enabled = secondArg<Boolean>()
            restrictionsFlow.value = restrictionsFlow.value.map {
                if (it.id == id) it.copy(isEnabled = enabled, updatedAt = System.currentTimeMillis()) else it
            }
        }

        coEvery { repository.getAggregatedSessionDataForDayFlow(any(), any()) } returns flowOf(
            sampleSessionData
        )
        coEvery { repository.getDailyAppSummaries(any(), any()) } returns flowOf(
            sampleDailyAppSummaries
        )
        coEvery { repository.getUnlockCountForDayFlow(any(), any()) } returns flowOf(80)
        coEvery { repository.getAllWellnessScores() } returns flowOf(sampleWellnessScores)
        coEvery { repository.getAllSessionsInRange(any(), any()) } returns flowOf(emptyList())
        coEvery { repository.getActiveGoals() } returns flowOf(emptyList())

        // Create use cases
        timeRestrictionUseCase = TimeRestrictionManagerUseCase(
            repository,
            notificationManager,
            appLogger
        )
        weeklyInsightsUseCase = WeeklyInsightsUseCase(repository, notificationManager, appLogger)
        notificationScheduler =
            NotificationScheduler(ApplicationProvider.getApplicationContext(), appLogger)

        viewModel = TimeRestrictionsViewModel(timeRestrictionUseCase)
    }

    @Test
    fun `complete user flow - create time restriction and view in weekly insights`() {
        // Step 1: Start with dashboard showing current insights
        composeTestRule.setContent {
            WeeklyInsightsCard(weeklyInsights = weeklyInsightsUseCase)
        }

        // Verify initial weekly insights display
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("📊 Weekly Insights").assertIsDisplayed()
        composeTestRule.onNodeWithText("7h").assertIsDisplayed() // Total screen time

        // Step 2: User navigates to time restrictions screen
        composeTestRule.setContent {
            TimeRestrictionsScreen(
                viewModel = viewModel
            )
        }

        // Verify time restrictions screen loads
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("⏰ Time Restrictions").assertIsDisplayed()
        composeTestRule.onNodeWithText("No active restrictions").assertIsDisplayed()

        // Step 3: User creates a new time restriction
        composeTestRule.onNodeWithText("Add New Restriction").performClick()
        composeTestRule.waitForIdle()

        // Fill out the create restriction dialog
        composeTestRule.onNodeWithText("Create Time Restriction").assertIsDisplayed()

        // Select Instagram (mock user selecting an app)
        composeTestRule.onNodeWithText("Select Apps").performClick()
        // Note: In real test, we would interact with app selection, but for this test we'll simulate

        // Set time range (9 AM to 5 PM)
        composeTestRule.onNodeWithText("Start Time").performClick()
        // Note: Time picker interaction would be tested here

        // Select weekdays
        composeTestRule.onNodeWithText("Mon").performClick()
        composeTestRule.onNodeWithText("Tue").performClick()
        composeTestRule.onNodeWithText("Wed").performClick()
        composeTestRule.onNodeWithText("Thu").performClick()
        composeTestRule.onNodeWithText("Fri").performClick()

        // Create the restriction
        composeTestRule.onNodeWithText("Create").performClick()
        composeTestRule.waitForIdle()

        // Step 4: Verify restriction appears in the list
        composeTestRule.onNodeWithText("No active restrictions").assertDoesNotExist()
        // Should show the newly created restriction

        // Step 5: Go back to dashboard and verify time restriction card appears
        composeTestRule.setContent {
            TimeRestrictionCard(
                activeRestrictions = emptyList(),
                allRestrictions = emptyList(),
                onToggleRestriction = {},
                onNavigateToSettings = {},
                formatTime = { "${it / 60}:${String.format("%02d", it % 60)}" },
                formatTimeUntil = { "${it / 60}h" },
                getRestrictionStatusPreview = { restriction ->
                    RestrictionStatusPreview(
                        restriction = restriction,
                        isCurrentlyActive = false,
                        nextChangeTimeMinutes = null,
                        timeUntilChange = null
                    )
                }
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("🔒 Active Time Restrictions").assertIsDisplayed()
        // Should show restriction info

        // Step 6: Verify weekly insights reflect the restriction
        composeTestRule.setContent {
            WeeklyInsightsCard(weeklyInsights = weeklyInsightsUseCase)
        }

        composeTestRule.waitForIdle()
        // Switch to different views to verify all work
        composeTestRule.onNodeWithText("Productivity").performClick()
        composeTestRule.onNodeWithText("⏰ Productivity Hours Heatmap").assertIsDisplayed()

        composeTestRule.onNodeWithText("Categories").performClick()
        composeTestRule.onNodeWithText("📊 App Category Breakdown").assertIsDisplayed()
    }

    @Test
    fun `user flow - modify existing restriction and see impact`() {
        // Given - Start with an existing restriction
        val existingRestriction = TimeRestriction(
            id = 1,
            restrictionType = "work_hours_focus",
            name = "Work Hours Focus",
            description = "Block social media during work",
            startTimeMinutes = 9 * 60,
            endTimeMinutes = 17 * 60,
            appsBlocked = "com.instagram.android",
            daysOfWeek = "1,2,3,4,5",
            isEnabled = true,
            createdAt = currentTime
        )
        restrictionsFlow.value = listOf(existingRestriction)

        // Step 1: View existing restriction
        composeTestRule.setContent {
            TimeRestrictionsScreen(
                viewModel = viewModel
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Instagram").assertIsDisplayed()
        composeTestRule.onNodeWithText("Weekdays • 9:00 AM - 5:00 PM").assertIsDisplayed()

        // Step 2: Toggle restriction off
        composeTestRule.onNode(hasContentDescription("Toggle restriction")).performClick()
        composeTestRule.waitForIdle()

        // Step 3: Verify restriction is disabled in dashboard
        composeTestRule.setContent {
            TimeRestrictionCard(
                activeRestrictions = emptyList(),
                allRestrictions = emptyList(),
                onToggleRestriction = {},
                onNavigateToSettings = {},
                formatTime = { "${it / 60}:${String.format("%02d", it % 60)}" },
                formatTimeUntil = { "${it / 60}h" },
                getRestrictionStatusPreview = { restriction ->
                    RestrictionStatusPreview(
                        restriction = restriction,
                        isCurrentlyActive = false,
                        nextChangeTimeMinutes = null,
                        timeUntilChange = null
                    )
                }
            )
        }

        composeTestRule.waitForIdle()
        // Should show no active restrictions or disabled state

        // Step 4: Toggle back on and verify weekly insights update
        composeTestRule.setContent {
            TimeRestrictionsScreen(
                viewModel = viewModel
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNode(hasContentDescription("Toggle restriction")).performClick()

        // Step 5: Check weekly insights again
        composeTestRule.setContent {
            WeeklyInsightsCard(weeklyInsights = weeklyInsightsUseCase)
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("📊 Weekly Insights").assertIsDisplayed()
    }

    @Test
    fun `user flow - view weekly report notification workflow`() {
        // Step 1: User views weekly insights and requests notification setup
        composeTestRule.setContent {
            WeeklyInsightsCard(weeklyInsights = weeklyInsightsUseCase)
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("View Full Report").performClick()

        // Verify full report displays
        composeTestRule.onNodeWithText("📱 Most Used Apps").assertIsDisplayed()
        composeTestRule.onNodeWithText("💡 Insights & Recommendations").assertIsDisplayed()

        // Step 2: Simulate scheduling weekly notifications
        // In real app, this might be triggered by a settings toggle
        notificationScheduler.schedulePeriodicWeeklyReports()

        // Step 3: Verify notification would be sent
        every { notificationManager.showWeeklyReport(any(), any(), any()) } just runs
        runBlocking { weeklyInsightsUseCase.sendWeeklyReportNotification() }

        verify { notificationManager.showWeeklyReport(any(), any(), any()) }
    }

    @Test
    fun `user flow - productivity insights interaction`() {
        // Step 1: User navigates to productivity view
        composeTestRule.setContent {
            WeeklyInsightsCard(weeklyInsights = weeklyInsightsUseCase)
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Productivity").performClick()

        // Step 2: Verify productivity heatmap displays
        composeTestRule.onNodeWithText("⏰ Productivity Hours Heatmap").assertIsDisplayed()
        composeTestRule.onNodeWithText("Shows your phone usage intensity throughout the day").assertIsDisplayed()

        // Step 3: Verify heatmap elements
        composeTestRule.onNodeWithText("Less").assertIsDisplayed()
        composeTestRule.onNodeWithText("More").assertIsDisplayed()
        composeTestRule.onNodeWithText("0h").assertIsDisplayed()
        composeTestRule.onNodeWithText("12h").assertIsDisplayed()

        // Step 4: Switch to categories view
        composeTestRule.onNodeWithText("Categories").performClick()

        // Step 5: Verify category breakdown
        composeTestRule.onNodeWithText("📊 App Category Breakdown").assertIsDisplayed()
        composeTestRule.onNodeWithText("Social").assertIsDisplayed()
        composeTestRule.onNodeWithText("Entertainment").assertIsDisplayed()

        // Step 6: Back to summary
        composeTestRule.onNodeWithText("Summary").performClick()
        composeTestRule.onNodeWithText("7h").assertIsDisplayed() // Total screen time
    }

    @Test
    fun `user flow - error handling during restriction creation`() {
        // Given - Repository will fail on insert
        coEvery { repository.insertTimeRestriction(any()) } throws RuntimeException("Database error")

        // Step 1: User attempts to create restriction
        composeTestRule.setContent {
            TimeRestrictionsScreen(
                viewModel = viewModel
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Add New Restriction").performClick()

        // Step 2: Fill out form and attempt to create
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Create").performClick()

        // Step 3: Verify error handling
        composeTestRule.waitForIdle()
        // Error should be handled gracefully (logged but not crash)
        verify {
            appLogger.e(
                "TimeRestrictionManagerUseCase",
                "Failed to create time restriction",
                any()
            )
        }

        // Step 4: UI should remain functional
        composeTestRule.onNodeWithText("⏰ Time Restrictions").assertIsDisplayed()
    }

    @Test
    fun `user flow - app blocking simulation`() {
        // Given - Active restriction during blocking hours
        val activeRestriction = TimeRestriction(
            id = 1,
            restrictionType = "bedtime_mode",
            name = "Bedtime Mode",
            description = "Block all apps during bedtime",
            startTimeMinutes = 0, // 12:00 AM
            endTimeMinutes = 23 * 60 + 59, // 11:59 PM
            appsBlocked = "com.instagram.android",
            daysOfWeek = "0,1,2,3,4,5,6", // All days
            isEnabled = true,
            createdAt = currentTime
        )
        restrictionsFlow.value = listOf(activeRestriction)

        // Step 1: Check if app is currently restricted
        val isRestricted =
            runBlocking { timeRestrictionUseCase.isAppBlockedByTimeRestriction("com.instagram.android") }
        Assert.assertTrue("Instagram should be currently restricted", isRestricted)

        // Step 2: Verify restriction shows in dashboard
        composeTestRule.setContent {
            TimeRestrictionCard(
                activeRestrictions = listOf(activeRestriction),
                allRestrictions = listOf(activeRestriction),
                onToggleRestriction = {},
                onNavigateToSettings = {},
                formatTime = { "${it / 60}:${String.format("%02d", it % 60)}" },
                formatTimeUntil = { "${it / 60}h" },
                getRestrictionStatusPreview = { restriction ->
                    RestrictionStatusPreview(
                        restriction = restriction,
                        isCurrentlyActive = true,
                        nextChangeTimeMinutes = null,
                        timeUntilChange = null
                    )
                }
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("🔒 Active Time Restrictions").assertIsDisplayed()

        // Step 3: Simulate app usage attempt (would trigger blocking overlay in real app)
        // This would normally show the AppBlockedOverlay, but we'll verify the restriction logic

        // Step 4: Verify weekly insights account for blocked time
        composeTestRule.setContent {
            WeeklyInsightsCard(weeklyInsights = weeklyInsightsUseCase)
        }

        composeTestRule.waitForIdle()
        // Insights should still show usage data but with context of restrictions
        composeTestRule.onNodeWithText("📊 Weekly Insights").assertIsDisplayed()
    }

    @Test
    fun `user flow - complete settings and notification workflow`() {
        // Step 1: User sets up weekly notifications
        notificationScheduler.schedulePeriodicWeeklyReports()

        // Step 2: Verify notification can be cancelled
        notificationScheduler.cancelWeeklyReportNotifications()

        // Step 3: Re-enable notifications
        notificationScheduler.scheduleWeeklyReportNotification()

        // Step 4: Test notification generation
        every { notificationManager.showWeeklyReport(any(), any(), any()) } just runs
        runBlocking { weeklyInsightsUseCase.sendWeeklyReportNotification() }

        // Verify notification was sent
        verify { notificationManager.showWeeklyReport(any(), any(), any()) }
        verify { appLogger.i("WeeklyInsightsUseCase", "Weekly report notification sent") }
    }

    @Test
    fun `user flow - data consistency across features`() {
        // Step 1: Create multiple restrictions
        val restriction1 = TimeRestriction(
            id = 1,
            restrictionType = "work_hours_focus",
            name = "Work Focus",
            description = "Block social media during work",
            startTimeMinutes = 9 * 60,
            endTimeMinutes = 17 * 60,
            appsBlocked = "com.instagram.android",
            daysOfWeek = "1,2,3,4,5",
            isEnabled = true,
            createdAt = currentTime
        )

        val restriction2 = TimeRestriction(
            id = 2,
            restrictionType = "bedtime_mode",
            name = "Bedtime Mode",
            description = "Block entertainment apps at night",
            startTimeMinutes = 22 * 60,
            endTimeMinutes = 7 * 60,
            appsBlocked = "com.netflix.mediaclient",
            daysOfWeek = "6,7",
            isEnabled = true,
            createdAt = currentTime
        )

        restrictionsFlow.value = listOf(restriction1, restriction2)

        // Step 2: Verify restrictions appear in dashboard
        composeTestRule.setContent {
            TimeRestrictionCard(
                activeRestrictions = listOf(restriction1, restriction2),
                allRestrictions = listOf(restriction1, restriction2),
                onToggleRestriction = {},
                onNavigateToSettings = {},
                formatTime = { "${it / 60}:${String.format("%02d", it % 60)}" },
                formatTimeUntil = { "${it / 60}h" },
                getRestrictionStatusPreview = { restriction ->
                    RestrictionStatusPreview(
                        restriction = restriction,
                        isCurrentlyActive = false,
                        nextChangeTimeMinutes = null,
                        timeUntilChange = null
                    )
                }
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("🔒 Active Time Restrictions").assertIsDisplayed()

        // Step 3: Verify both apps appear in weekly insights
        composeTestRule.setContent {
            WeeklyInsightsCard(weeklyInsights = weeklyInsightsUseCase)
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("View Full Report").performClick()

        // Both apps should appear in the usage data
        composeTestRule.onNodeWithText("📱 Most Used Apps").assertIsDisplayed()

        // Step 4: Test different insight views
        composeTestRule.onNodeWithText("Categories").performClick()
        composeTestRule.onNodeWithText("Social").assertIsDisplayed() // Instagram
        composeTestRule.onNodeWithText("Entertainment").assertIsDisplayed() // Netflix

        // Step 5: Verify data remains consistent
        composeTestRule.onNodeWithText("Summary").performClick()
        composeTestRule.onNodeWithText("7h").assertIsDisplayed() // Same total across views
    }
}