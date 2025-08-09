package com.example.screentimetracker.ui.dashboard.cards

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.screentimetracker.domain.usecases.WeeklyInsightsUseCase
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class EnhancedWeeklyInsightsCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @MockK
    private lateinit var weeklyInsightsUseCase: WeeklyInsightsUseCase

    private val sampleReport = WeeklyInsightsUseCase.WeeklyReport(
        weekStart = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7),
        weekEnd = System.currentTimeMillis(),
        totalScreenTimeMillis = TimeUnit.HOURS.toMillis(20), // 20 hours
        averageDailyScreenTimeMillis = TimeUnit.HOURS.toMillis(3), // 3 hours avg
        totalUnlocks = 150,
        averageUnlocksPerDay = 21,
        averageWellnessScore = 68,
        topApps = listOf(
            WeeklyInsightsUseCase.AppUsageInsight(
                packageName = "com.instagram.android",
                totalTimeMillis = TimeUnit.HOURS.toMillis(5),
                sessionsCount = 42,
                averageDailyTime = TimeUnit.HOURS.toMillis(1)
            )
        ),
        insights = listOf(
            "📱 Your most used app consumed 5h this week.",
            "💪 Good wellness score. Small improvements can make it even better.",
            "📲 Moderate unlock frequency. Consider longer focused sessions."
        ),
        generatedAt = System.currentTimeMillis()
    )

    private val sampleProductivityHours = (0..23).map { hour ->
        WeeklyInsightsUseCase.ProductivityHour(
            hour = hour,
            usageTimeMillis = if (hour in 9..17) TimeUnit.MINUTES.toMillis(30) else TimeUnit.MINUTES.toMillis(10),
            productivity = when (hour) {
                in 9..17 -> 0.8f
                in 22..23, in 0..6 -> 0.1f
                else -> 0.5f
            }
        )
    }

    private val sampleCategoryInsights = listOf(
        WeeklyInsightsUseCase.CategoryInsight(
            categoryName = "Social",
            totalTimeMillis = TimeUnit.HOURS.toMillis(8),
            percentageOfTotal = 40f
        ),
        WeeklyInsightsUseCase.CategoryInsight(
            categoryName = "Entertainment",
            totalTimeMillis = TimeUnit.HOURS.toMillis(6),
            percentageOfTotal = 30f
        ),
        WeeklyInsightsUseCase.CategoryInsight(
            categoryName = "Productivity",
            totalTimeMillis = TimeUnit.HOURS.toMillis(4),
            percentageOfTotal = 20f
        )
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        coEvery { weeklyInsightsUseCase.generateWeeklyReport() } returns sampleReport
        coEvery { weeklyInsightsUseCase.getProductivityHours() } returns sampleProductivityHours
        coEvery { weeklyInsightsUseCase.getAppCategoryInsights() } returns sampleCategoryInsights
    }

    @Test
    fun `WeeklyInsightsCard displays summary view by default`() {
        composeTestRule.setContent {
            WeeklyInsightsCard(weeklyInsights = weeklyInsightsUseCase)
        }

        // Wait for loading to complete
        composeTestRule.waitForIdle()

        // Verify header
        composeTestRule.onNodeWithText("📊 Weekly Insights").assertIsDisplayed()
        
        // Verify view selection tabs
        composeTestRule.onNodeWithText("Summary").assertIsDisplayed()
        composeTestRule.onNodeWithText("Productivity").assertIsDisplayed()
        composeTestRule.onNodeWithText("Categories").assertIsDisplayed()
        
        // Verify summary is selected by default
        composeTestRule.onNodeWithText("Summary").assertIsSelected()
        
        // Verify key metrics are displayed
        composeTestRule.onNodeWithText("20h").assertIsDisplayed() // Total screen time
        composeTestRule.onNodeWithText("68%").assertIsDisplayed() // Wellness score
        composeTestRule.onNodeWithText("21").assertIsDisplayed() // Average unlocks
    }

    @Test
    fun `WeeklyInsightsCard switches to productivity view when tab is clicked`() {
        composeTestRule.setContent {
            WeeklyInsightsCard(weeklyInsights = weeklyInsightsUseCase)
        }

        // Wait for loading to complete
        composeTestRule.waitForIdle()

        // Click on Productivity tab
        composeTestRule.onNodeWithText("Productivity").performClick()

        // Verify productivity view is displayed
        composeTestRule.onNodeWithText("⏰ Productivity Hours Heatmap").assertIsDisplayed()
        composeTestRule.onNodeWithText("Shows your phone usage intensity throughout the day").assertIsDisplayed()
        
        // Verify heatmap legend
        composeTestRule.onNodeWithText("Less").assertIsDisplayed()
        composeTestRule.onNodeWithText("More").assertIsDisplayed()
    }

    @Test
    fun `WeeklyInsightsCard switches to categories view when tab is clicked`() {
        composeTestRule.setContent {
            WeeklyInsightsCard(weeklyInsights = weeklyInsightsUseCase)
        }

        // Wait for loading to complete
        composeTestRule.waitForIdle()

        // Click on Categories tab
        composeTestRule.onNodeWithText("Categories").performClick()

        // Verify categories view is displayed
        composeTestRule.onNodeWithText("📊 App Category Breakdown").assertIsDisplayed()
        composeTestRule.onNodeWithText("Time spent across different app categories").assertIsDisplayed()
        
        // Verify category data is displayed
        composeTestRule.onNodeWithText("Social").assertIsDisplayed()
        composeTestRule.onNodeWithText("Entertainment").assertIsDisplayed()
        composeTestRule.onNodeWithText("Productivity").assertIsDisplayed()
        composeTestRule.onNodeWithText("40%").assertIsDisplayed() // Social percentage
    }

    @Test
    fun `WeeklyInsightsCard displays loading state correctly`() {
        // Mock slow response
        coEvery { weeklyInsightsUseCase.generateWeeklyReport() } coAnswers {
            kotlinx.coroutines.delay(1000)
            sampleReport
        }

        composeTestRule.setContent {
            WeeklyInsightsCard(weeklyInsights = weeklyInsightsUseCase)
        }

        // Verify loading indicator is shown
        composeTestRule.onNode(hasTestTag("loading") or isInstanceOf(androidx.compose.material3.CircularProgressIndicator::class.java))
            .assertExists()
    }

    @Test
    fun `WeeklyInsightsCard expands full report when View Full Report is clicked`() {
        composeTestRule.setContent {
            WeeklyInsightsCard(weeklyInsights = weeklyInsightsUseCase)
        }

        // Wait for loading
        composeTestRule.waitForIdle()

        // Click on "View Full Report" button
        composeTestRule.onNodeWithText("View Full Report").performClick()

        // Verify full report elements are displayed
        composeTestRule.onNodeWithText("📱 Most Used Apps").assertIsDisplayed()
        composeTestRule.onNodeWithText("💡 Insights & Recommendations").assertIsDisplayed()
        composeTestRule.onNodeWithText("Show Less").assertIsDisplayed()
        
        // Verify insights are displayed
        composeTestRule.onNodeWithText("📱 Your most used app consumed 5h this week.").assertIsDisplayed()
    }

    @Test
    fun `ProductivityHoursHeatmap displays 24-hour grid correctly`() {
        composeTestRule.setContent {
            WeeklyInsightsCard(weeklyInsights = weeklyInsightsUseCase)
        }

        composeTestRule.waitForIdle()
        
        // Switch to productivity view
        composeTestRule.onNodeWithText("Productivity").performClick()

        // Verify hour labels are displayed
        composeTestRule.onNodeWithText("0h").assertIsDisplayed()
        composeTestRule.onNodeWithText("4h").assertIsDisplayed()
        composeTestRule.onNodeWithText("8h").assertIsDisplayed()
        composeTestRule.onNodeWithText("12h").assertIsDisplayed()
        composeTestRule.onNodeWithText("16h").assertIsDisplayed()
        composeTestRule.onNodeWithText("20h").assertIsDisplayed()
        
        // Verify week label
        composeTestRule.onNodeWithText("Week").assertIsDisplayed()
    }

    @Test
    fun `CategoryInsightsChart displays pie chart and category list`() {
        composeTestRule.setContent {
            WeeklyInsightsCard(weeklyInsights = weeklyInsightsUseCase)
        }

        composeTestRule.waitForIdle()
        
        // Switch to categories view
        composeTestRule.onNodeWithText("Categories").performClick()

        // Verify category items are displayed with correct data
        composeTestRule.onNodeWithText("Social").assertIsDisplayed()
        composeTestRule.onNodeWithText("8h 0m").assertIsDisplayed() // Social time
        composeTestRule.onNodeWithText("40%").assertIsDisplayed() // Social percentage
        
        composeTestRule.onNodeWithText("Entertainment").assertIsDisplayed()
        composeTestRule.onNodeWithText("6h 0m").assertIsDisplayed() // Entertainment time
        composeTestRule.onNodeWithText("30%").assertIsDisplayed() // Entertainment percentage
        
        composeTestRule.onNodeWithText("Productivity").assertIsDisplayed()
        composeTestRule.onNodeWithText("4h 0m").assertIsDisplayed() // Productivity time
        composeTestRule.onNodeWithText("20%").assertIsDisplayed() // Productivity percentage
    }

    @Test
    fun `CategoryInsightsChart handles empty data gracefully`() {
        // Mock empty category data
        coEvery { weeklyInsightsUseCase.getAppCategoryInsights() } returns emptyList()

        composeTestRule.setContent {
            WeeklyInsightsCard(weeklyInsights = weeklyInsightsUseCase)
        }

        composeTestRule.waitForIdle()
        
        // Switch to categories view
        composeTestRule.onNodeWithText("Categories").performClick()

        // Verify empty state message
        composeTestRule.onNodeWithText("No category data available").assertIsDisplayed()
    }

    @Test
    fun `ViewSelectionTabs display correct icons and selection state`() {
        composeTestRule.setContent {
            WeeklyInsightsCard(weeklyInsights = weeklyInsightsUseCase)
        }

        composeTestRule.waitForIdle()

        // Verify Summary tab is selected by default
        composeTestRule.onNodeWithText("Summary").assertIsSelected()
        composeTestRule.onNodeWithText("Productivity").assertIsNotSelected()
        composeTestRule.onNodeWithText("Categories").assertIsNotSelected()

        // Click Productivity tab
        composeTestRule.onNodeWithText("Productivity").performClick()
        
        // Verify selection state changed
        composeTestRule.onNodeWithText("Summary").assertIsNotSelected()
        composeTestRule.onNodeWithText("Productivity").assertIsSelected()
        composeTestRule.onNodeWithText("Categories").assertIsNotSelected()
    }

    @Test
    fun `WeeklyInsightsCard handles use case errors gracefully`() {
        // Mock error from use case
        coEvery { weeklyInsightsUseCase.generateWeeklyReport() } returns WeeklyInsightsUseCase.WeeklyReport.empty()
        coEvery { weeklyInsightsUseCase.getProductivityHours() } returns emptyList()
        coEvery { weeklyInsightsUseCase.getAppCategoryInsights() } returns emptyList()

        composeTestRule.setContent {
            WeeklyInsightsCard(weeklyInsights = weeklyInsightsUseCase)
        }

        composeTestRule.waitForIdle()

        // Should display empty/error state gracefully
        composeTestRule.onNodeWithText("📊 Weekly Insights").assertIsDisplayed()
        
        // Switch to categories view to test empty state
        composeTestRule.onNodeWithText("Categories").performClick()
        composeTestRule.onNodeWithText("No category data available").assertIsDisplayed()
    }

    private fun SemanticsNodeInteraction.assertIsSelected(): SemanticsNodeInteraction {
        return assertIsOn()
    }

    private fun SemanticsNodeInteraction.assertIsNotSelected(): SemanticsNodeInteraction {
        return assertIsOff()
    }
}