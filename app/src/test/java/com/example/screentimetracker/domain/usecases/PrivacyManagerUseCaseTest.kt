package dev.sadakat.screentimetracker.domain.usecases

import dev.sadakat.screentimetracker.data.local.entities.PrivacySettings
import dev.sadakat.screentimetracker.data.local.dao.PrivacySettingsDao
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.security.MessageDigest

class PrivacyManagerUseCaseTest {

    private val mockPrivacySettingsDao = mockk<PrivacySettingsDao>()
    private lateinit var privacyManagerUseCase: PrivacyManagerUseCase

    @Before
    fun setup() {
        privacyManagerUseCase = PrivacyManagerUseCase(mockPrivacySettingsDao)
        MockKAnnotations.init(this)
    }

    // Test getPrivacySettings function
    @Test
    fun `getPrivacySettings should return default settings when dao returns null`() = runTest {
        // Given
        every { mockPrivacySettingsDao.getPrivacySettings() } returns flowOf(null)

        // When
        val result = privacyManagerUseCase.getPrivacySettings().first()

        // Then
        assertEquals(PrivacySettings(), result)
        verify { mockPrivacySettingsDao.getPrivacySettings() }
    }

    @Test
    fun `getPrivacySettings should return existing settings when dao returns data`() = runTest {
        // Given
        val existingSettings = PrivacySettings(
            isStealthModeEnabled = true,
            stealthModePassword = "hashed_password",
            hiddenAppsPackages = listOf("com.app1", "com.app2")
        )
        every { mockPrivacySettingsDao.getPrivacySettings() } returns flowOf(existingSettings)

        // When
        val result = privacyManagerUseCase.getPrivacySettings().first()

        // Then
        assertEquals(existingSettings, result)
        verify { mockPrivacySettingsDao.getPrivacySettings() }
    }

    // Test stealth mode functionality
    @Test
    fun `enableStealthMode should hash password and enable stealth mode`() = runTest {
        // Given
        val password = "mySecretPassword"
        val currentSettings = PrivacySettings()
        coEvery { mockPrivacySettingsDao.getPrivacySettingsSync() } returns currentSettings
        coEvery { mockPrivacySettingsDao.insertPrivacySettings(any()) } just Runs

        // When
        privacyManagerUseCase.enableStealthMode(password)

        // Then
        coVerify { mockPrivacySettingsDao.getPrivacySettingsSync() }
        coVerify { 
            mockPrivacySettingsDao.insertPrivacySettings(
                match { settings ->
                    settings.isStealthModeEnabled == true &&
                    settings.stealthModePassword.isNotEmpty() &&
                    settings.stealthModePassword != password // Should be hashed
                }
            )
        }
    }

    @Test
    fun `enableStealthMode should preserve existing settings while updating stealth mode`() = runTest {
        // Given
        val password = "mySecretPassword"
        val currentSettings = PrivacySettings(
            hiddenAppsPackages = listOf("com.app1"),
            excludedAppsFromTracking = listOf("com.app2"),
            dataExportEnabled = false
        )
        coEvery { mockPrivacySettingsDao.getPrivacySettingsSync() } returns currentSettings
        coEvery { mockPrivacySettingsDao.insertPrivacySettings(any()) } just Runs

        // When
        privacyManagerUseCase.enableStealthMode(password)

        // Then
        coVerify { 
            mockPrivacySettingsDao.insertPrivacySettings(
                match { settings ->
                    settings.isStealthModeEnabled == true &&
                    settings.hiddenAppsPackages == listOf("com.app1") &&
                    settings.excludedAppsFromTracking == listOf("com.app2") &&
                    settings.dataExportEnabled == false
                }
            )
        }
    }

    @Test
    fun `disableStealthMode should call dao to disable stealth mode`() = runTest {
        // Given
        coEvery { mockPrivacySettingsDao.setStealthModeEnabled(any()) } just Runs

        // When
        privacyManagerUseCase.disableStealthMode()

        // Then
        coVerify { mockPrivacySettingsDao.setStealthModeEnabled(false) }
    }

    @Test
    fun `verifyStealthModePassword should return true for correct password`() = runTest {
        // Given
        val originalPassword = "mySecretPassword"
        val hashedPassword = hashPassword(originalPassword)
        val settings = PrivacySettings(
            isStealthModeEnabled = true,
            stealthModePassword = hashedPassword
        )
        coEvery { mockPrivacySettingsDao.getPrivacySettingsSync() } returns settings

        // When
        val result = privacyManagerUseCase.verifyStealthModePassword(originalPassword)

        // Then
        assertTrue(result)
        coVerify { mockPrivacySettingsDao.getPrivacySettingsSync() }
    }

    @Test
    fun `verifyStealthModePassword should return false for incorrect password`() = runTest {
        // Given
        val originalPassword = "mySecretPassword"
        val wrongPassword = "wrongPassword"
        val hashedPassword = hashPassword(originalPassword)
        val settings = PrivacySettings(
            isStealthModeEnabled = true,
            stealthModePassword = hashedPassword
        )
        coEvery { mockPrivacySettingsDao.getPrivacySettingsSync() } returns settings

        // When
        val result = privacyManagerUseCase.verifyStealthModePassword(wrongPassword)

        // Then
        assertFalse(result)
        coVerify { mockPrivacySettingsDao.getPrivacySettingsSync() }
    }

    @Test
    fun `verifyStealthModePassword should return true when stealth mode is disabled`() = runTest {
        // Given
        val settings = PrivacySettings(isStealthModeEnabled = false)
        coEvery { mockPrivacySettingsDao.getPrivacySettingsSync() } returns settings

        // When
        val result = privacyManagerUseCase.verifyStealthModePassword("anyPassword")

        // Then
        assertTrue(result)
        coVerify { mockPrivacySettingsDao.getPrivacySettingsSync() }
    }

    @Test
    fun `verifyStealthModePassword should return true when settings are null`() = runTest {
        // Given
        coEvery { mockPrivacySettingsDao.getPrivacySettingsSync() } returns null

        // When
        val result = privacyManagerUseCase.verifyStealthModePassword("anyPassword")

        // Then
        assertTrue(result)
        coVerify { mockPrivacySettingsDao.getPrivacySettingsSync() }
    }

    // Test guest mode functionality
    @Test
    fun `enableGuestMode should set guest mode with correct timestamps`() = runTest {
        // Given
        val durationMinutes = 30
        coEvery { mockPrivacySettingsDao.setGuestMode(any(), any(), any()) } just Runs

        val beforeCall = System.currentTimeMillis()

        // When
        privacyManagerUseCase.enableGuestMode(durationMinutes)

        val afterCall = System.currentTimeMillis()

        // Then
        coVerify { 
            mockPrivacySettingsDao.setGuestMode(
                enabled = true,
                startTime = match { it >= beforeCall && it <= afterCall },
                endTime = match { 
                    val expectedEndTime = beforeCall + (durationMinutes * 60 * 1000L)
                    it >= expectedEndTime && it <= afterCall + (durationMinutes * 60 * 1000L)
                }
            )
        }
    }

    @Test
    fun `disableGuestMode should disable guest mode and reset timestamps`() = runTest {
        // Given
        coEvery { mockPrivacySettingsDao.setGuestMode(any(), any(), any()) } just Runs

        // When
        privacyManagerUseCase.disableGuestMode()

        // Then
        coVerify { mockPrivacySettingsDao.setGuestMode(false, 0L, 0L) }
    }

    @Test
    fun `isGuestModeActive should return true when guest mode is enabled and not expired`() = runTest {
        // Given
        val futureEndTime = System.currentTimeMillis() + 60000L // 1 minute from now
        val settings = PrivacySettings(
            isGuestModeEnabled = true,
            guestModeEndTime = futureEndTime
        )
        coEvery { mockPrivacySettingsDao.getPrivacySettingsSync() } returns settings

        // When
        val result = privacyManagerUseCase.isGuestModeActive()

        // Then
        assertTrue(result)
        coVerify { mockPrivacySettingsDao.getPrivacySettingsSync() }
    }

    @Test
    fun `isGuestModeActive should return false when guest mode is enabled but expired`() = runTest {
        // Given
        val pastEndTime = System.currentTimeMillis() - 60000L // 1 minute ago
        val settings = PrivacySettings(
            isGuestModeEnabled = true,
            guestModeEndTime = pastEndTime
        )
        coEvery { mockPrivacySettingsDao.getPrivacySettingsSync() } returns settings

        // When
        val result = privacyManagerUseCase.isGuestModeActive()

        // Then
        assertFalse(result)
        coVerify { mockPrivacySettingsDao.getPrivacySettingsSync() }
    }

    @Test
    fun `isGuestModeActive should return false when guest mode is disabled`() = runTest {
        // Given
        val settings = PrivacySettings(isGuestModeEnabled = false)
        coEvery { mockPrivacySettingsDao.getPrivacySettingsSync() } returns settings

        // When
        val result = privacyManagerUseCase.isGuestModeActive()

        // Then
        assertFalse(result)
        coVerify { mockPrivacySettingsDao.getPrivacySettingsSync() }
    }

    // Test hidden apps functionality
    @Test
    fun `addHiddenApp should add new package to hidden apps list`() = runTest {
        // Given
        val existingPackages = listOf("com.app1", "com.app2")
        val newPackage = "com.app3"
        val settings = PrivacySettings(hiddenAppsPackages = existingPackages)
        coEvery { mockPrivacySettingsDao.getPrivacySettingsSync() } returns settings
        coEvery { mockPrivacySettingsDao.updateHiddenApps(any()) } just Runs

        // When
        privacyManagerUseCase.addHiddenApp(newPackage)

        // Then
        coVerify { 
            mockPrivacySettingsDao.updateHiddenApps(
                listOf("com.app1", "com.app2", "com.app3")
            )
        }
    }

    @Test
    fun `addHiddenApp should not add duplicate package to hidden apps list`() = runTest {
        // Given
        val existingPackages = listOf("com.app1", "com.app2")
        val duplicatePackage = "com.app1"
        val settings = PrivacySettings(hiddenAppsPackages = existingPackages)
        coEvery { mockPrivacySettingsDao.getPrivacySettingsSync() } returns settings
        coEvery { mockPrivacySettingsDao.updateHiddenApps(any()) } just Runs

        // When
        privacyManagerUseCase.addHiddenApp(duplicatePackage)

        // Then
        coVerify(exactly = 0) { mockPrivacySettingsDao.updateHiddenApps(any()) }
    }

    @Test
    fun `addHiddenApp should handle null settings gracefully`() = runTest {
        // Given
        val newPackage = "com.app1"
        coEvery { mockPrivacySettingsDao.getPrivacySettingsSync() } returns null
        coEvery { mockPrivacySettingsDao.updateHiddenApps(any()) } just Runs

        // When
        privacyManagerUseCase.addHiddenApp(newPackage)

        // Then
        coVerify { mockPrivacySettingsDao.updateHiddenApps(listOf(newPackage)) }
    }

    @Test
    fun `removeHiddenApp should remove package from hidden apps list`() = runTest {
        // Given
        val existingPackages = listOf("com.app1", "com.app2", "com.app3")
        val packageToRemove = "com.app2"
        val settings = PrivacySettings(hiddenAppsPackages = existingPackages)
        coEvery { mockPrivacySettingsDao.getPrivacySettingsSync() } returns settings
        coEvery { mockPrivacySettingsDao.updateHiddenApps(any()) } just Runs

        // When
        privacyManagerUseCase.removeHiddenApp(packageToRemove)

        // Then
        coVerify { 
            mockPrivacySettingsDao.updateHiddenApps(
                listOf("com.app1", "com.app3")
            )
        }
    }

    // Test excluded apps functionality
    @Test
    fun `addExcludedApp should add new package to excluded apps list`() = runTest {
        // Given
        val existingPackages = listOf("com.system1")
        val newPackage = "com.system2"
        val settings = PrivacySettings(excludedAppsFromTracking = existingPackages)
        coEvery { mockPrivacySettingsDao.getPrivacySettingsSync() } returns settings
        coEvery { mockPrivacySettingsDao.updateExcludedApps(any()) } just Runs

        // When
        privacyManagerUseCase.addExcludedApp(newPackage)

        // Then
        coVerify { 
            mockPrivacySettingsDao.updateExcludedApps(
                listOf("com.system1", "com.system2")
            )
        }
    }

    @Test
    fun `addExcludedApp should not add duplicate package to excluded apps list`() = runTest {
        // Given
        val existingPackages = listOf("com.system1", "com.system2")
        val duplicatePackage = "com.system1"
        val settings = PrivacySettings(excludedAppsFromTracking = existingPackages)
        coEvery { mockPrivacySettingsDao.getPrivacySettingsSync() } returns settings
        coEvery { mockPrivacySettingsDao.updateExcludedApps(any()) } just Runs

        // When
        privacyManagerUseCase.addExcludedApp(duplicatePackage)

        // Then
        coVerify(exactly = 0) { mockPrivacySettingsDao.updateExcludedApps(any()) }
    }

    @Test
    fun `removeExcludedApp should remove package from excluded apps list`() = runTest {
        // Given
        val existingPackages = listOf("com.system1", "com.system2", "com.system3")
        val packageToRemove = "com.system2"
        val settings = PrivacySettings(excludedAppsFromTracking = existingPackages)
        coEvery { mockPrivacySettingsDao.getPrivacySettingsSync() } returns settings
        coEvery { mockPrivacySettingsDao.updateExcludedApps(any()) } just Runs

        // When
        privacyManagerUseCase.removeExcludedApp(packageToRemove)

        // Then
        coVerify { 
            mockPrivacySettingsDao.updateExcludedApps(
                listOf("com.system1", "com.system3")
            )
        }
    }

    // Test app checking functionality
    @Test
    fun `isAppHidden should return true for hidden app`() = runTest {
        // Given
        val hiddenApps = listOf("com.app1", "com.app2")
        val settings = PrivacySettings(hiddenAppsPackages = hiddenApps)
        coEvery { mockPrivacySettingsDao.getPrivacySettingsSync() } returns settings

        // When
        val result = privacyManagerUseCase.isAppHidden("com.app1")

        // Then
        assertTrue(result)
        coVerify { mockPrivacySettingsDao.getPrivacySettingsSync() }
    }

    @Test
    fun `isAppHidden should return false for non-hidden app`() = runTest {
        // Given
        val hiddenApps = listOf("com.app1", "com.app2")
        val settings = PrivacySettings(hiddenAppsPackages = hiddenApps)
        coEvery { mockPrivacySettingsDao.getPrivacySettingsSync() } returns settings

        // When
        val result = privacyManagerUseCase.isAppHidden("com.app3")

        // Then
        assertFalse(result)
        coVerify { mockPrivacySettingsDao.getPrivacySettingsSync() }
    }

    @Test
    fun `isAppHidden should return false when settings are null`() = runTest {
        // Given
        coEvery { mockPrivacySettingsDao.getPrivacySettingsSync() } returns null

        // When
        val result = privacyManagerUseCase.isAppHidden("com.app1")

        // Then
        assertFalse(result)
        coVerify { mockPrivacySettingsDao.getPrivacySettingsSync() }
    }

    @Test
    fun `isAppExcludedFromTracking should return true for excluded app`() = runTest {
        // Given
        val excludedApps = listOf("com.system1", "com.system2")
        val settings = PrivacySettings(excludedAppsFromTracking = excludedApps)
        coEvery { mockPrivacySettingsDao.getPrivacySettingsSync() } returns settings

        // When
        val result = privacyManagerUseCase.isAppExcludedFromTracking("com.system1")

        // Then
        assertTrue(result)
        coVerify { mockPrivacySettingsDao.getPrivacySettingsSync() }
    }

    @Test
    fun `isAppExcludedFromTracking should return false for non-excluded app`() = runTest {
        // Given
        val excludedApps = listOf("com.system1", "com.system2")
        val settings = PrivacySettings(excludedAppsFromTracking = excludedApps)
        coEvery { mockPrivacySettingsDao.getPrivacySettingsSync() } returns settings

        // When
        val result = privacyManagerUseCase.isAppExcludedFromTracking("com.app1")

        // Then
        assertFalse(result)
        coVerify { mockPrivacySettingsDao.getPrivacySettingsSync() }
    }

    // Test data export functionality
    @Test
    fun `updateLastExportTime should update export timestamp`() = runTest {
        // Given
        coEvery { mockPrivacySettingsDao.updateLastExportTime(any()) } just Runs
        val beforeCall = System.currentTimeMillis()

        // When
        privacyManagerUseCase.updateLastExportTime()

        val afterCall = System.currentTimeMillis()

        // Then
        coVerify { 
            mockPrivacySettingsDao.updateLastExportTime(
                match { it >= beforeCall && it <= afterCall }
            )
        }
    }

    // Test edge cases
    @Test
    fun `enableStealthMode should handle empty password`() = runTest {
        // Given
        val emptyPassword = ""
        val currentSettings = PrivacySettings()
        coEvery { mockPrivacySettingsDao.getPrivacySettingsSync() } returns currentSettings
        coEvery { mockPrivacySettingsDao.insertPrivacySettings(any()) } just Runs

        // When
        privacyManagerUseCase.enableStealthMode(emptyPassword)

        // Then
        coVerify { 
            mockPrivacySettingsDao.insertPrivacySettings(
                match { settings ->
                    settings.isStealthModeEnabled == true &&
                    settings.stealthModePassword.isNotEmpty() // Should still be hashed
                }
            )
        }
    }

    @Test
    fun `enableGuestMode should handle zero duration`() = runTest {
        // Given
        val zeroDuration = 0
        coEvery { mockPrivacySettingsDao.setGuestMode(any(), any(), any()) } just Runs

        // When
        privacyManagerUseCase.enableGuestMode(zeroDuration)

        // Then
        coVerify { 
            mockPrivacySettingsDao.setGuestMode(
                enabled = true,
                startTime = any(),
                endTime = any() // Should still set end time even if duration is 0
            )
        }
    }

    @Test
    fun `enableGuestMode should handle negative duration`() = runTest {
        // Given
        val negativeDuration = -10
        coEvery { mockPrivacySettingsDao.setGuestMode(any(), any(), any()) } just Runs

        // When
        privacyManagerUseCase.enableGuestMode(negativeDuration)

        // Then
        coVerify { 
            mockPrivacySettingsDao.setGuestMode(
                enabled = true,
                startTime = any(),
                endTime = any() // Should still set end time
            )
        }
    }

    // Helper function to match the internal hashing logic
    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(bytes)
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }
}