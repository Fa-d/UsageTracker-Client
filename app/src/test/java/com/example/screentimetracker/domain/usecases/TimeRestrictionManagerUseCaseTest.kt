package dev.sadakat.screentimetracker.domain.usecases

import dev.sadakat.screentimetracker.data.local.entities.TimeRestriction
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import dev.sadakat.screentimetracker.utils.ui.AppNotificationManager
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class TimeRestrictionManagerUseCaseTest {

    @MockK
    private lateinit var repository: TrackerRepository

    @MockK
    private lateinit var notificationManager: AppNotificationManager

    @MockK
    private lateinit var appLogger: AppLogger

    private lateinit var useCase: TimeRestrictionManagerUseCase

    private val sampleRestriction = TimeRestriction(
        id = 1L,
        restrictionType = TimeRestrictionManagerUseCase.BEDTIME_MODE,
        name = "Digital Sunset",
        description = "Block distracting apps before bedtime",
        startTimeMinutes = 22 * 60, // 10 PM
        endTimeMinutes = 8 * 60, // 8 AM
        appsBlocked = "com.instagram.android,com.twitter.android",
        daysOfWeek = "0,1,2,3,4,5,6", // All days
        allowEmergencyApps = true,
        showNotifications = true,
        isEnabled = true
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        
        // Setup default mock behavior
        every { appLogger.i(any(), any()) } just runs
        every { appLogger.d(any(), any()) } just runs
        every { appLogger.e(any(), any(), any()) } just runs
        
        useCase = TimeRestrictionManagerUseCase(repository, notificationManager, appLogger)
    }

    @Test
    fun `createDefaultTimeRestrictions should insert four default restrictions`() = runTest {
        // Given
        coEvery { repository.insertTimeRestriction(any()) } returns 1L

        // When
        useCase.createDefaultTimeRestrictions()

        // Then
        coVerify(exactly = 4) { repository.insertTimeRestriction(any()) }
        verify { appLogger.i(any(), "Default time restrictions created successfully") }
    }

    @Test
    fun `createDefaultTimeRestrictions should handle database error`() = runTest {
        // Given
        val error = RuntimeException("Database error")
        coEvery { repository.insertTimeRestriction(any()) } throws error

        // When
        useCase.createDefaultTimeRestrictions()

        // Then
        verify { appLogger.e(any(), "Failed to create default time restrictions", error) }
    }

    @Test
    fun `getAllTimeRestrictions should return repository flow`() {
        // Given
        val restrictions = listOf(sampleRestriction)
        every { repository.getAllTimeRestrictions() } returns flowOf(restrictions)

        // When
        val result = useCase.getAllTimeRestrictions()

        // Then
        assertEquals(flowOf(restrictions), result)
        verify { repository.getAllTimeRestrictions() }
    }

    @Test
    fun `getActiveTimeRestrictions should return repository flow`() {
        // Given
        val activeRestrictions = listOf(sampleRestriction)
        every { repository.getActiveTimeRestrictions() } returns flowOf(activeRestrictions)

        // When
        val result = useCase.getActiveTimeRestrictions()

        // Then
        assertEquals(flowOf(activeRestrictions), result)
        verify { repository.getActiveTimeRestrictions() }
    }

    @Test
    fun `isAppBlockedByTimeRestriction should return false for emergency apps when allowed`() = runTest {
        // Given
        val emergencyApp = "com.android.dialer"
        val restrictionWithEmergencyAllowed = sampleRestriction.copy(
            allowEmergencyApps = true,
            appsBlocked = "" // Block all apps
        )
        coEvery { repository.getActiveRestrictionsForTime(any(), any()) } returns listOf(restrictionWithEmergencyAllowed)

        // When
        val result = useCase.isAppBlockedByTimeRestriction(emergencyApp)

        // Then
        assertFalse(result)
    }

    @Test
    fun `isAppBlockedByTimeRestriction should return true for emergency apps when not allowed`() = runTest {
        // Given
        val emergencyApp = "com.android.dialer"
        val restrictionWithoutEmergency = sampleRestriction.copy(
            allowEmergencyApps = false,
            appsBlocked = "" // Block all apps
        )
        coEvery { repository.getActiveRestrictionsForTime(any(), any()) } returns listOf(restrictionWithoutEmergency)

        // When
        val result = useCase.isAppBlockedByTimeRestriction(emergencyApp)

        // Then
        assertTrue(result)
    }

    @Test
    fun `isAppBlockedByTimeRestriction should return true for apps in blocked list`() = runTest {
        // Given
        val blockedApp = "com.instagram.android"
        coEvery { repository.getActiveRestrictionsForTime(any(), any()) } returns listOf(sampleRestriction)

        // When
        val result = useCase.isAppBlockedByTimeRestriction(blockedApp)

        // Then
        assertTrue(result)
        verify { appLogger.d(any(), "App $blockedApp blocked by ${sampleRestriction.name}") }
    }

    @Test
    fun `isAppBlockedByTimeRestriction should return false for apps not in blocked list`() = runTest {
        // Given
        val nonBlockedApp = "com.spotify.music"
        coEvery { repository.getActiveRestrictionsForTime(any(), any()) } returns listOf(sampleRestriction)

        // When
        val result = useCase.isAppBlockedByTimeRestriction(nonBlockedApp)

        // Then
        assertFalse(result)
    }

    @Test
    fun `isAppBlockedByTimeRestriction should return true when appsBlocked is empty (block all)`() = runTest {
        // Given
        val anyApp = "com.spotify.music"
        val restrictionBlockingAll = sampleRestriction.copy(appsBlocked = "")
        coEvery { repository.getActiveRestrictionsForTime(any(), any()) } returns listOf(restrictionBlockingAll)

        // When
        val result = useCase.isAppBlockedByTimeRestriction(anyApp)

        // Then
        assertTrue(result)
        verify { appLogger.d(any(), "App $anyApp blocked by ${restrictionBlockingAll.name} (blocks all apps)") }
    }

    @Test
    fun `isAppBlockedByTimeRestriction should handle repository error gracefully`() = runTest {
        // Given
        val testApp = "com.test.app"
        val error = RuntimeException("Database error")
        coEvery { repository.getActiveRestrictionsForTime(any(), any()) } throws error

        // When
        val result = useCase.isAppBlockedByTimeRestriction(testApp)

        // Then
        assertFalse(result) // Should default to false on error
        verify { appLogger.e(any(), "Error checking time restrictions for $testApp", error) }
    }

    @Test
    fun `updateRestrictionEnabled should call repository with correct parameters`() = runTest {
        // Given
        val restrictionId = 1L
        val enabled = false
        coEvery { repository.updateRestrictionEnabled(any(), any(), any()) } just runs

        // When
        useCase.updateRestrictionEnabled(restrictionId, enabled)

        // Then
        coVerify { repository.updateRestrictionEnabled(restrictionId, enabled, any()) }
        verify { appLogger.i(any(), "Time restriction $restrictionId disabled") }
    }

    @Test
    fun `updateRestrictionEnabled should handle repository error`() = runTest {
        // Given
        val restrictionId = 1L
        val error = RuntimeException("Update failed")
        coEvery { repository.updateRestrictionEnabled(any(), any(), any()) } throws error

        // When
        useCase.updateRestrictionEnabled(restrictionId, true)

        // Then
        verify { appLogger.e(any(), "Failed to update restriction $restrictionId", error) }
    }

    @Test
    fun `createCustomRestriction should create restriction with correct parameters`() = runTest {
        // Given
        val expectedId = 42L
        val name = "Custom Work Hours"
        val description = "Block social media during work"
        val startTimeMinutes = 9 * 60
        val endTimeMinutes = 17 * 60
        val blockedApps = listOf("com.instagram.android", "com.twitter.android")
        val daysOfWeek = listOf(1, 2, 3, 4, 5)
        
        coEvery { repository.insertTimeRestriction(any()) } returns expectedId

        // When
        val result = useCase.createCustomRestriction(
            name, description, startTimeMinutes, endTimeMinutes,
            blockedApps, daysOfWeek
        )

        // Then
        assertEquals(expectedId, result)
        coVerify { 
            repository.insertTimeRestriction(
                match { restriction ->
                    restriction.name == name &&
                    restriction.description == description &&
                    restriction.startTimeMinutes == startTimeMinutes &&
                    restriction.endTimeMinutes == endTimeMinutes &&
                    restriction.appsBlocked == blockedApps.joinToString(",") &&
                    restriction.daysOfWeek == daysOfWeek.joinToString(",") &&
                    restriction.restrictionType == "custom"
                }
            )
        }
        verify { appLogger.i(any(), "Custom time restriction created: $name") }
    }

    @Test
    fun `createCustomRestriction should handle repository error`() = runTest {
        // Given
        val error = RuntimeException("Insert failed")
        coEvery { repository.insertTimeRestriction(any()) } throws error

        // When & Then
        try {
            useCase.createCustomRestriction(
                "Test", "Desc", 0, 0, emptyList(), emptyList()
            )
            fail("Expected exception to be thrown")
        } catch (e: RuntimeException) {
            assertEquals(error, e)
            verify { appLogger.e(any(), "Failed to create custom restriction: Test", error) }
        }
    }

    @Test
    fun `getCurrentActiveRestrictions should call repository with current time`() = runTest {
        // Given
        val activeRestrictions = listOf(sampleRestriction)
        coEvery { repository.getActiveRestrictionsForTime(any(), any()) } returns activeRestrictions

        // When
        val result = useCase.getCurrentActiveRestrictions()

        // Then
        assertEquals(activeRestrictions, result)
        coVerify { repository.getActiveRestrictionsForTime(any(), any()) }
    }

    @Test
    fun `getCurrentActiveRestrictions should handle repository error`() = runTest {
        // Given
        val error = RuntimeException("Query failed")
        coEvery { repository.getActiveRestrictionsForTime(any(), any()) } throws error

        // When
        val result = useCase.getCurrentActiveRestrictions()

        // Then
        assertTrue(result.isEmpty())
        verify { appLogger.e(any(), "Failed to get current active restrictions", error) }
    }

    @Test
    fun `checkAndNotifyRestrictionChanges should show notification when restrictions active`() = runTest {
        // Given
        val activeRestrictions = listOf(
            sampleRestriction.copy(name = "Work Focus"),
            sampleRestriction.copy(name = "Digital Sunset")
        )
        coEvery { repository.getActiveRestrictionsForTime(any(), any()) } returns activeRestrictions
        every { notificationManager.showMotivationBoost(any()) } just runs

        // When
        useCase.checkAndNotifyRestrictionChanges()

        // Then
        verify { 
            notificationManager.showMotivationBoost(
                "⏰ Time restriction active: Work Focus, Digital Sunset. Stay focused!"
            )
        }
    }

    @Test
    fun `checkAndNotifyRestrictionChanges should not show notification when no restrictions active`() = runTest {
        // Given
        coEvery { repository.getActiveRestrictionsForTime(any(), any()) } returns emptyList()

        // When
        useCase.checkAndNotifyRestrictionChanges()

        // Then
        verify(exactly = 0) { notificationManager.showMotivationBoost(any()) }
    }

    @Test
    fun `checkAndNotifyRestrictionChanges should handle error gracefully`() = runTest {
        // Given
        val error = RuntimeException("Notification error")
        coEvery { repository.getActiveRestrictionsForTime(any(), any()) } throws error

        // When
        useCase.checkAndNotifyRestrictionChanges()

        // Then
        verify { appLogger.e(any(), "Failed to check and notify restriction changes", error) }
    }

    @Test
    fun `emergency apps constant should contain essential communication apps`() {
        // Test that essential apps are included
        assertTrue(TimeRestrictionManagerUseCase.EMERGENCY_APPS.contains("com.android.dialer"))
        assertTrue(TimeRestrictionManagerUseCase.EMERGENCY_APPS.contains("com.google.android.dialer"))
        assertTrue(TimeRestrictionManagerUseCase.EMERGENCY_APPS.contains("com.android.mms"))
        assertTrue(TimeRestrictionManagerUseCase.EMERGENCY_APPS.contains("com.google.android.apps.messaging"))
        assertTrue(TimeRestrictionManagerUseCase.EMERGENCY_APPS.contains("com.android.emergency"))
        assertTrue(TimeRestrictionManagerUseCase.EMERGENCY_APPS.contains("com.google.android.contacts"))
    }

    @Test
    fun `restriction type constants should be correctly defined`() {
        assertEquals("bedtime_mode", TimeRestrictionManagerUseCase.BEDTIME_MODE)
        assertEquals("work_hours_focus", TimeRestrictionManagerUseCase.WORK_HOURS_FOCUS)
        assertEquals("meal_time_protection", TimeRestrictionManagerUseCase.MEAL_TIME_PROTECTION)
        assertEquals("morning_routine", TimeRestrictionManagerUseCase.MORNING_ROUTINE)
    }

    @Test
    fun `default restrictions should have correct configuration`() = runTest {
        // This test verifies the configuration of default restrictions
        // We'll capture the restrictions being inserted
        val insertedRestrictions = mutableListOf<TimeRestriction>()
        coEvery { repository.insertTimeRestriction(capture(insertedRestrictions)) } returns 1L

        // When
        useCase.createDefaultTimeRestrictions()

        // Then
        assertEquals(4, insertedRestrictions.size)

        // Verify bedtime restriction
        val bedtimeRestriction = insertedRestrictions.find { it.restrictionType == TimeRestrictionManagerUseCase.BEDTIME_MODE }
        assertNotNull(bedtimeRestriction)
        bedtimeRestriction?.let {
            assertEquals("Digital Sunset", it.name)
            assertEquals(22 * 60, it.startTimeMinutes)
            assertEquals(8 * 60, it.endTimeMinutes)
            assertTrue(it.allowEmergencyApps)
            assertTrue(it.showNotifications)
            assertTrue(it.isEnabled)
        }

        // Verify work hours restriction
        val workRestriction = insertedRestrictions.find { it.restrictionType == TimeRestrictionManagerUseCase.WORK_HOURS_FOCUS }
        assertNotNull(workRestriction)
        workRestriction?.let {
            assertEquals("Work Focus Mode", it.name)
            assertEquals(9 * 60, it.startTimeMinutes)
            assertEquals(17 * 60, it.endTimeMinutes)
            assertFalse(it.isEnabled) // Should be disabled by default
        }

        // Verify morning routine restriction
        val morningRestriction = insertedRestrictions.find { it.restrictionType == TimeRestrictionManagerUseCase.MORNING_ROUTINE }
        assertNotNull(morningRestriction)
        morningRestriction?.let {
            assertEquals("Morning Routine", it.name)
            assertFalse(it.isEnabled) // Should be disabled by default
        }

        // Verify meal time restriction
        val mealRestriction = insertedRestrictions.find { it.restrictionType == TimeRestrictionManagerUseCase.MEAL_TIME_PROTECTION }
        assertNotNull(mealRestriction)
        mealRestriction?.let {
            assertEquals("Mindful Meals", it.name)
            assertFalse(it.isEnabled) // Should be disabled by default
        }
    }
}