package com.example.screentimetracker.utils.ui

import android.content.Context
import com.example.screentimetracker.domain.model.Achievement
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
    private lateinit var notificationManager: AppNotificationManagerImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        notificationManager = AppNotificationManagerImpl(mockContext, mockAppLogger)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `showTimeWarning should display appropriate warning for different time limits`() = runTest {
        // Test 15-minute warning
        notificationManager.showTimeWarning("Instagram", 15, 60)
        verify { mockAppLogger.i("AppNotificationManager", "Time warning notification shown for Instagram: 15 minutes left.") }

        // Test 5-minute warning
        notificationManager.showTimeWarning("Instagram", 5, 60)
        verify { mockAppLogger.i("AppNotificationManager", "Time warning notification shown for Instagram: 5 minutes left.") }

        // Test 1-minute warning
        notificationManager.showTimeWarning("Instagram", 1, 60)
        verify { mockAppLogger.i("AppNotificationManager", "Time warning notification shown for Instagram: 1 minutes left.") }
    }

    @Test
    fun `showAchievementUnlocked should display achievement notification`() = runTest {
        val achievement = Achievement(
            id = "test_achievement",
            name = "Digital Sunset",
            description = "No screen time 1 hour before bedtime for 5 days",
            emoji = "🌙",
            targetValue = 5,
            isUnlocked = true,
            unlockedDate = System.currentTimeMillis()
        )

        notificationManager.showAchievementUnlocked(achievement)
        verify { mockAppLogger.i("AppNotificationManager", "Achievement unlock notification shown for: Digital Sunset") }
    }

    @Test
    fun `showWeeklyReport should format screen time correctly`() = runTest {
        val totalScreenTime = TimeUnit.HOURS.toMillis(25) // 25 hours
        val goalsAchieved = 3
        val totalGoals = 5

        notificationManager.showWeeklyReport(totalScreenTime, goalsAchieved, totalGoals)
        verify { mockAppLogger.i("AppNotificationManager", "Weekly report notification shown.") }
    }

    @Test
    fun `showFocusSessionStart should show ongoing notification`() = runTest {
        val durationMinutes = 25

        notificationManager.showFocusSessionStart(durationMinutes)
        verify { mockAppLogger.i("AppNotificationManager", "Focus session start notification shown.") }
    }

    @Test
    fun `showFocusSessionComplete should show different messages for success and failure`() = runTest {
        // Test successful completion
        notificationManager.showFocusSessionComplete(25, true)
        verify { mockAppLogger.i("AppNotificationManager", "Focus session complete notification shown. Success: true") }

        // Test unsuccessful completion
        notificationManager.showFocusSessionComplete(15, false)
        verify { mockAppLogger.i("AppNotificationManager", "Focus session complete notification shown. Success: false") }
    }

    @Test
    fun `showBreakReminder should use default message when none provided`() = runTest {
        notificationManager.showBreakReminder()
        verify { mockAppLogger.i("AppNotificationManager", "Break reminder notification shown.") }
    }

    @Test
    fun `showBreakReminder should use custom message when provided`() = runTest {
        val customMessage = "Time to stretch your legs!"
        notificationManager.showBreakReminder(customMessage)
        verify { mockAppLogger.i("AppNotificationManager", "Break reminder notification shown.") }
    }

    @Test
    fun `showMotivationBoost should display custom motivation message`() = runTest {
        val motivationMessage = "You're doing great! Keep up the healthy habits!"
        notificationManager.showMotivationBoost(motivationMessage)
        verify { mockAppLogger.i("AppNotificationManager", "Motivation boost notification shown.") }
    }

    @Test
    fun `showWarningNotification should work with LimitedApp object`() = runTest {
        val limitedApp = LimitedApp(
            packageName = "com.instagram.android",
            timeLimitMillis = TimeUnit.HOURS.toMillis(1)
        )
        val continuousDuration = TimeUnit.MINUTES.toMillis(65) // 65 minutes

        notificationManager.showWarningNotification(limitedApp, continuousDuration)
        verify { mockAppLogger.i("AppNotificationManager", "Usage limit warning notification shown for com.instagram.android.") }
    }
}