package dev.sadakat.screentimetracker.domain.usecases

import dev.sadakat.screentimetracker.data.local.entities.UserPreferences
import dev.sadakat.screentimetracker.data.local.dao.UserPreferencesDao
import dev.sadakat.screentimetracker.data.local.entities.ThemeMode
import dev.sadakat.screentimetracker.data.local.entities.ColorScheme
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
        coEvery { userPreferencesDao.insertOrUpdateUserPreferences(any()) } just Runs
        coEvery { userPreferencesDao.updateThemeMode(any(), any()) } just Runs

        // When
        userPreferencesUseCase.updateThemeMode(ThemeMode.DARK)

        // Then
        coVerify { userPreferencesDao.insertOrUpdateUserPreferences(defaultPreferences) }
        coVerify { userPreferencesDao.updateThemeMode("DARK", any()) }
    }

    @Test
    fun `updateThemeMode updates existing preferences`() = runTest {
        // Given
        coEvery { userPreferencesDao.getUserPreferencesSync() } returns testPreferences
        coEvery { userPreferencesDao.updateThemeMode(any(), any()) } just Runs

        // When
        userPreferencesUseCase.updateThemeMode(ThemeMode.LIGHT)

        // Then
        coVerify(exactly = 0) { userPreferencesDao.insertOrUpdateUserPreferences(any()) }
        coVerify { userPreferencesDao.updateThemeMode("LIGHT", any()) }
    }

    @Test
    fun `updateColorScheme updates color scheme correctly`() = runTest {
        // Given
        coEvery { userPreferencesDao.getUserPreferencesSync() } returns testPreferences
        coEvery { userPreferencesDao.updateColorScheme(any(), any()) } just Runs

        // When
        userPreferencesUseCase.updateColorScheme(ColorScheme.MINIMAL)

        // Then
        coVerify { userPreferencesDao.updateColorScheme("MINIMAL", any()) }
    }


    @Test
    fun `updateMotivationalMessagesEnabled updates flag correctly`() = runTest {
        // Given
        coEvery { userPreferencesDao.getUserPreferencesSync() } returns testPreferences
        coEvery { userPreferencesDao.updateMotivationalMessagesEnabled(any(), any()) } just Runs

        // When
        userPreferencesUseCase.updateMotivationalMessagesEnabled(true)

        // Then
        coVerify { userPreferencesDao.updateMotivationalMessagesEnabled(true, any()) }
    }

    @Test
    fun `getThemeMode returns correct parsed enum`() = runTest {
        // Given
        coEvery { userPreferencesDao.getUserPreferencesSync() } returns testPreferences

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
        coEvery { userPreferencesDao.getUserPreferencesSync() } returns testPreferences

        // When
        val result = userPreferencesUseCase.getColorScheme()

        // Then
        assertEquals(ColorScheme.COLORFUL, result)
    }



    @Test
    fun `updateNotificationSound updates sound correctly`() = runTest {
        // Given
        coEvery { userPreferencesDao.getUserPreferencesSync() } returns testPreferences
        coEvery { userPreferencesDao.insertOrUpdateUserPreferences(any()) } just Runs

        // When
        userPreferencesUseCase.updateNotificationSound("custom_sound.mp3")

        // Then
        // Verify the saved preferences have the correct sound
        coVerify { 
            userPreferencesDao.insertOrUpdateUserPreferences(
                match { it.notificationSound == "custom_sound.mp3" }
            ) 
        }
    }

    @Test
    fun `saveAllPreferences updates timestamp and saves`() = runTest {
        // Given
        val originalTimestamp = 12345L
        val preferencesToSave = testPreferences.copy(updatedAt = originalTimestamp)
        coEvery { userPreferencesDao.insertOrUpdateUserPreferences(any()) } just Runs

        // When
        userPreferencesUseCase.saveAllPreferences(preferencesToSave)

        // Then
        coVerify { 
            userPreferencesDao.insertOrUpdateUserPreferences(
                match { it.updatedAt > originalTimestamp }
            ) 
        }
    }
}