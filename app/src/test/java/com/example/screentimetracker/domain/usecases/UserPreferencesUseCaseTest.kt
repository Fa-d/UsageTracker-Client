package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.UserPreferences
import com.example.screentimetracker.data.local.UserPreferencesDao
import com.example.screentimetracker.data.local.ThemeMode
import com.example.screentimetracker.data.local.ColorScheme
import com.example.screentimetracker.data.local.PersonalityMode
import com.example.screentimetracker.data.local.DashboardLayout
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UserPreferencesUseCaseTest {

    private lateinit var userPreferencesDao: UserPreferencesDao
    private lateinit var userPreferencesUseCase: UserPreferencesUseCase

    private val defaultPreferences = UserPreferences()
    private val testPreferences = UserPreferences(
        themeMode = "DARK",
        colorScheme = "COLORFUL",
        personalityMode = "MOTIVATIONAL_BUDDY",
        dashboardLayout = "DETAILED",
        motivationalMessagesEnabled = false,
        achievementCelebrationsEnabled = false,
        breakRemindersEnabled = false,
        wellnessCoachingEnabled = false
    )

    @Before
    fun setup() {
        userPreferencesDao = mockk()
        userPreferencesUseCase = UserPreferencesUseCase(userPreferencesDao)
    }

    @Test
    fun `getUserPreferences returns default preferences when none exist`() = runTest {
        // Given
        every { userPreferencesDao.getUserPreferences() } returns flowOf(null)

        // When
        val result = userPreferencesUseCase.getUserPreferences().first()

        // Then
        assertEquals(defaultPreferences, result)
        verify { userPreferencesDao.getUserPreferences() }
    }

    @Test
    fun `getUserPreferences returns existing preferences`() = runTest {
        // Given
        every { userPreferencesDao.getUserPreferences() } returns flowOf(testPreferences)

        // When
        val result = userPreferencesUseCase.getUserPreferences().first()

        // Then
        assertEquals(testPreferences, result)
        verify { userPreferencesDao.getUserPreferences() }
    }

    @Test
    fun `getUserPreferencesOnce returns default when none exist`() = runTest {
        // Given
        coEvery { userPreferencesDao.getUserPreferencesOnce() } returns null

        // When
        val result = userPreferencesUseCase.getUserPreferencesOnce()

        // Then
        assertEquals(defaultPreferences, result)
        coVerify { userPreferencesDao.getUserPreferencesOnce() }
    }

    @Test
    fun `updateThemeMode creates preferences if none exist and updates theme`() = runTest {
        // Given
        coEvery { userPreferencesDao.getUserPreferencesOnce() } returns null
        coEvery { userPreferencesDao.insertOrUpdatePreferences(any()) } just Runs
        coEvery { userPreferencesDao.updateThemeMode(any(), any()) } just Runs

        // When
        userPreferencesUseCase.updateThemeMode(ThemeMode.DARK)

        // Then
        coVerify { userPreferencesDao.insertOrUpdatePreferences(defaultPreferences) }
        coVerify { userPreferencesDao.updateThemeMode("DARK", any()) }
    }

    @Test
    fun `updateThemeMode updates existing preferences`() = runTest {
        // Given
        coEvery { userPreferencesDao.getUserPreferencesOnce() } returns testPreferences
        coEvery { userPreferencesDao.updateThemeMode(any(), any()) } just Runs

        // When
        userPreferencesUseCase.updateThemeMode(ThemeMode.LIGHT)

        // Then
        coVerify(exactly = 0) { userPreferencesDao.insertOrUpdatePreferences(any()) }
        coVerify { userPreferencesDao.updateThemeMode("LIGHT", any()) }
    }

    @Test
    fun `updateColorScheme updates color scheme correctly`() = runTest {
        // Given
        coEvery { userPreferencesDao.getUserPreferencesOnce() } returns testPreferences
        coEvery { userPreferencesDao.updateColorScheme(any(), any()) } just Runs

        // When
        userPreferencesUseCase.updateColorScheme(ColorScheme.MINIMAL)

        // Then
        coVerify { userPreferencesDao.updateColorScheme("MINIMAL", any()) }
    }

    @Test
    fun `updatePersonalityMode updates personality mode correctly`() = runTest {
        // Given
        coEvery { userPreferencesDao.getUserPreferencesOnce() } returns testPreferences
        coEvery { userPreferencesDao.updatePersonalityMode(any(), any()) } just Runs

        // When
        userPreferencesUseCase.updatePersonalityMode(PersonalityMode.STRICT_COACH)

        // Then
        coVerify { userPreferencesDao.updatePersonalityMode("STRICT_COACH", any()) }
    }

    @Test
    fun `updateDashboardLayout updates dashboard layout correctly`() = runTest {
        // Given
        coEvery { userPreferencesDao.getUserPreferencesOnce() } returns testPreferences
        coEvery { userPreferencesDao.updateDashboardLayout(any(), any()) } just Runs

        // When
        userPreferencesUseCase.updateDashboardLayout(DashboardLayout.COMPACT)

        // Then
        coVerify { userPreferencesDao.updateDashboardLayout("COMPACT", any()) }
    }

    @Test
    fun `updateMotivationalMessages updates flag correctly`() = runTest {
        // Given
        coEvery { userPreferencesDao.getUserPreferencesOnce() } returns testPreferences
        coEvery { userPreferencesDao.updateMotivationalMessages(any(), any()) } just Runs

        // When
        userPreferencesUseCase.updateMotivationalMessages(true)

        // Then
        coVerify { userPreferencesDao.updateMotivationalMessages(true, any()) }
    }

    @Test
    fun `getThemeMode returns correct parsed enum`() = runTest {
        // Given
        coEvery { userPreferencesDao.getUserPreferencesOnce() } returns testPreferences

        // When
        val result = userPreferencesUseCase.getThemeMode()

        // Then
        assertEquals(ThemeMode.DARK, result)
    }

    @Test
    fun `getThemeMode returns default for invalid value`() = runTest {
        // Given
        val invalidPreferences = testPreferences.copy(themeMode = "INVALID")
        coEvery { userPreferencesDao.getUserPreferencesOnce() } returns invalidPreferences

        // When
        val result = userPreferencesUseCase.getThemeMode()

        // Then
        assertEquals(ThemeMode.SYSTEM, result)
    }

    @Test
    fun `getColorScheme returns correct parsed enum`() = runTest {
        // Given
        coEvery { userPreferencesDao.getUserPreferencesOnce() } returns testPreferences

        // When
        val result = userPreferencesUseCase.getColorScheme()

        // Then
        assertEquals(ColorScheme.COLORFUL, result)
    }

    @Test
    fun `getPersonalityMode returns correct parsed enum`() = runTest {
        // Given
        coEvery { userPreferencesDao.getUserPreferencesOnce() } returns testPreferences

        // When
        val result = userPreferencesUseCase.getPersonalityMode()

        // Then
        assertEquals(PersonalityMode.MOTIVATIONAL_BUDDY, result)
    }

    @Test
    fun `getDashboardLayout returns correct parsed enum`() = runTest {
        // Given
        coEvery { userPreferencesDao.getUserPreferencesOnce() } returns testPreferences

        // When
        val result = userPreferencesUseCase.getDashboardLayout()

        // Then
        assertEquals(DashboardLayout.DETAILED, result)
    }

    @Test
    fun `getMotivationalMessage returns strict coach message`() = runTest {
        // Given
        val strictCoachPreferences = testPreferences.copy(personalityMode = "STRICT_COACH")
        coEvery { userPreferencesDao.getUserPreferencesOnce() } returns strictCoachPreferences

        // When
        val result = userPreferencesUseCase.getMotivationalMessage(MotivationContext.TIME_WARNING)

        // Then
        assertTrue(result.contains("Time's up!"))
        assertTrue(result.contains("need to stop"))
    }

    @Test
    fun `getMotivationalMessage returns gentle guide message`() = runTest {
        // Given
        val gentleGuidePreferences = testPreferences.copy(personalityMode = "GENTLE_GUIDE")
        coEvery { userPreferencesDao.getUserPreferencesOnce() } returns gentleGuidePreferences

        // When
        val result = userPreferencesUseCase.getMotivationalMessage(MotivationContext.BREAK_REMINDER)

        // Then
        assertTrue(result.contains("How about"))
        assertTrue(result.contains("when you're ready") || result.contains("will thank you"))
    }

    @Test
    fun `getMotivationalMessage returns motivational buddy message`() = runTest {
        // Given
        coEvery { userPreferencesDao.getUserPreferencesOnce() } returns testPreferences // Uses MOTIVATIONAL_BUDDY

        // When
        val result = userPreferencesUseCase.getMotivationalMessage(MotivationContext.GOAL_ACHIEVED)

        // Then
        assertTrue(result.contains("YES!") || result.contains("crushed"))
        assertTrue(result.contains("🎉") || result.contains("proud"))
    }

    @Test
    fun `updateNotificationSound updates sound correctly`() = runTest {
        // Given
        coEvery { userPreferencesDao.getUserPreferencesOnce() } returns testPreferences
        coEvery { userPreferencesDao.insertOrUpdatePreferences(any()) } just Runs

        // When
        userPreferencesUseCase.updateNotificationSound("custom_sound.mp3")

        // Then
        // Verify the saved preferences have the correct sound
        coVerify { 
            userPreferencesDao.insertOrUpdatePreferences(
                match { it.notificationSound == "custom_sound.mp3" }
            ) 
        }
    }

    @Test
    fun `saveAllPreferences updates timestamp and saves`() = runTest {
        // Given
        val originalTimestamp = 12345L
        val preferencesToSave = testPreferences.copy(updatedAt = originalTimestamp)
        coEvery { userPreferencesDao.insertOrUpdatePreferences(any()) } just Runs

        // When
        userPreferencesUseCase.saveAllPreferences(preferencesToSave)

        // Then
        coVerify { 
            userPreferencesDao.insertOrUpdatePreferences(
                match { it.updatedAt > originalTimestamp }
            ) 
        }
    }
}