package dev.sadakat.screentimetracker.ui.dashboard.cards

import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.sadakat.screentimetracker.domain.usecases.WeeklyInsightsUseCase
import dev.sadakat.screentimetracker.ui.base.BaseComposeInstrumentedTest
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class EnhancedWeeklyInsightsCardTest : BaseComposeInstrumentedTest() {

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
            "💪 Good wellness score. Small improvements can make it even better."
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
        )
    )

    @Before
    override fun setUp() {
        super.setUp()
        MockKAnnotations.init(this)
        
        coEvery { weeklyInsightsUseCase.generateWeeklyReport() } returns sampleReport
        coEvery { weeklyInsightsUseCase.getProductivityHours() } returns sampleProductivityHours
        coEvery { weeklyInsightsUseCase.getAppCategoryInsights() } returns sampleCategoryInsights
    }

    @Test
    fun weeklyInsightsCard_displaysWithMockData() {
        composeTestRule.setContent {
            WeeklyInsightsCard(weeklyInsights = weeklyInsightsUseCase)
        }

        // Wait for loading to complete
        composeTestRule.waitForIdle()

        // Verify header
        composeTestRule.onNodeWithText("📊 Weekly Insights").assertIsDisplayed()
    }

    @Test
    fun weeklyInsightsCard_handlesEmptyData() {
        // Mock empty response
        coEvery { weeklyInsightsUseCase.generateWeeklyReport() } returns WeeklyInsightsUseCase.WeeklyReport.empty()
        
        composeTestRule.setContent {
            WeeklyInsightsCard(weeklyInsights = weeklyInsightsUseCase)
        }

        composeTestRule.waitForIdle()
        
        // Should display header even with empty data
        composeTestRule.onNodeWithText("📊 Weekly Insights").assertIsDisplayed()
    }
}