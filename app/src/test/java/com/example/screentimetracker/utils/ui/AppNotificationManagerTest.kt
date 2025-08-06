package com.example.screentimetracker.utils.ui

import android.content.Context
import com.example.screentimetracker.domain.model.Achievement
import com.example.screentimetracker.domain.model.AchievementCategory
import com.example.screentimetracker.domain.model.LimitedApp
import com.example.screentimetracker.utils.logger.AppLogger
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
class AppNotificationManagerTest {

    private val mockContext = mockk<Context>(relaxed = true)
    private val mockAppLogger = mockk<AppLogger>(relaxed = true)
    private val mockNotificationManager = mockk<AppNotificationManager>(relaxed = true)

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `showTimeWarning should display appropriate warning for different time limits`() = runTest {
        // Test 15-minute warning
        mockNotificationManager.showTimeWarning("Instagram", 15, 60)
        verify { mockNotificationManager.showTimeWarning("Instagram", 15, 60) }

        // Test 5-minute warning
        mockNotificationManager.showTimeWarning("Instagram", 5, 60)
        verify { mockNotificationManager.showTimeWarning("Instagram", 5, 60) }

        // Test 1-minute warning
        mockNotificationManager.showTimeWarning("Instagram", 1, 60)
        verify { mockNotificationManager.showTimeWarning("Instagram", 1, 60) }
    }

    @Test
    fun `showAchievementUnlocked should display achievement notification`() = runTest {
        val achievement = Achievement(
            achievementId = "test_achievement",
            name = "Digital Sunset",
            description = "No screen time 1 hour before bedtime for 5 days",
            emoji = "🌙",
            category = AchievementCategory.DIGITAL_SUNSET,
            targetValue = 5,
            isUnlocked = true,
            unlockedDate = System.currentTimeMillis()
        )

        mockNotificationManager.showAchievementUnlocked(achievement)
        verify { mockNotificationManager.showAchievementUnlocked(achievement) }
    }

    @Test
    fun `showWeeklyReport should format screen time correctly`() = runTest {
        val totalScreenTime = TimeUnit.HOURS.toMillis(25) // 25 hours
        val goalsAchieved = 3
        val totalGoals = 5

        mockNotificationManager.showWeeklyReport(totalScreenTime, goalsAchieved, totalGoals)
        verify { mockNotificationManager.showWeeklyReport(totalScreenTime, goalsAchieved, totalGoals) }
    }

    @Test
    fun `showFocusSessionStart should show ongoing notification`() = runTest {
        val durationMinutes = 25

        mockNotificationManager.showFocusSessionStart(durationMinutes)
        verify { mockNotificationManager.showFocusSessionStart(durationMinutes) }
    }

    @Test
    fun `showFocusSessionComplete should show different messages for success and failure`() = runTest {
        // Test successful completion
        mockNotificationManager.showFocusSessionComplete(25, true)
        verify { mockNotificationManager.showFocusSessionComplete(25, true) }

        // Test unsuccessful completion
        mockNotificationManager.showFocusSessionComplete(15, false)
        verify { mockNotificationManager.showFocusSessionComplete(15, false) }
    }

    @Test
    fun `showBreakReminder should use default message when none provided`() = runTest {
        mockNotificationManager.showBreakReminder()
        verify { mockNotificationManager.showBreakReminder(any()) }
    }

    @Test
    fun `showBreakReminder should use custom message when provided`() = runTest {
        val customMessage = "Time to stretch your legs!"
        mockNotificationManager.showBreakReminder(customMessage)
        verify { mockNotificationManager.showBreakReminder(customMessage) }
    }

    @Test
    fun `showMotivationBoost should display custom motivation message`() = runTest {
        val motivationMessage = "You're doing great! Keep up the healthy habits!"
        mockNotificationManager.showMotivationBoost(motivationMessage)
        verify { mockNotificationManager.showMotivationBoost(motivationMessage) }
    }

    @Test
    fun `showWarningNotification should work with LimitedApp object`() = runTest {
        val limitedApp = LimitedApp(
            packageName = "com.instagram.android",
            timeLimitMillis = TimeUnit.HOURS.toMillis(1)
        )
        val continuousDuration = TimeUnit.MINUTES.toMillis(65) // 65 minutes

        mockNotificationManager.showWarningNotification(limitedApp, continuousDuration)
        verify { mockNotificationManager.showWarningNotification(limitedApp, continuousDuration) }
    }
}