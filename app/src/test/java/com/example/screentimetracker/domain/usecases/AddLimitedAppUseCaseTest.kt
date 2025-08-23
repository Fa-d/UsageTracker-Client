package dev.sadakat.screentimetracker.domain.usecases

import dev.sadakat.screentimetracker.data.local.LimitedApp
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import io.mockk.*
import java.util.concurrent.TimeUnit

class AddLimitedAppUseCaseTest {

    private val mockRepository = mockk<TrackerRepository>()
    private lateinit var addLimitedAppUseCase: AddLimitedAppUseCase

    @Before
    fun setup() {
        addLimitedAppUseCase = AddLimitedAppUseCase(mockRepository)
    }

    @Test
    fun `invoke should add limited app with valid time limit`() = runTest {
        // Given
        val limitedApp = LimitedApp(
            packageName = "com.instagram.android",
            timeLimitMillis = TimeUnit.HOURS.toMillis(1) // 1 hour
        )
        coEvery { mockRepository.insertLimitedApp(limitedApp) } just Runs

        // When
        addLimitedAppUseCase(limitedApp)

        // Then
        coVerify { mockRepository.insertLimitedApp(limitedApp) }
    }

    @Test
    fun `invoke should throw exception for zero time limit`() = runTest {
        // Given
        val limitedApp = LimitedApp(
            packageName = "com.instagram.android",
            timeLimitMillis = 0L
        )

        // When & Then
        try {
            addLimitedAppUseCase(limitedApp)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Time limit must be positive.", e.message)
        }
        
        coVerify(exactly = 0) { mockRepository.insertLimitedApp(any()) }
    }

    @Test
    fun `invoke should throw exception for negative time limit`() = runTest {
        // Given
        val limitedApp = LimitedApp(
            packageName = "com.instagram.android",
            timeLimitMillis = -1000L
        )

        // When & Then
        try {
            addLimitedAppUseCase(limitedApp)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Time limit must be positive.", e.message)
        }
        
        coVerify(exactly = 0) { mockRepository.insertLimitedApp(any()) }
    }

    @Test
    fun `invoke should handle different package names correctly`() = runTest {
        // Given
        val limitedApps = listOf(
            LimitedApp("com.facebook.katana", TimeUnit.MINUTES.toMillis(30)),
            LimitedApp("com.twitter.android", TimeUnit.MINUTES.toMillis(45)),
            LimitedApp("com.snapchat.android", TimeUnit.HOURS.toMillis(2))
        )
        limitedApps.forEach { 
            coEvery { mockRepository.insertLimitedApp(it) } just Runs
        }

        // When
        limitedApps.forEach { addLimitedAppUseCase(it) }

        // Then
        limitedApps.forEach { 
            coVerify { mockRepository.insertLimitedApp(it) }
        }
    }

    @Test
    fun `invoke should handle minimum valid time limit`() = runTest {
        // Given
        val limitedApp = LimitedApp(
            packageName = "com.app.test",
            timeLimitMillis = 1L // Minimum valid value
        )
        coEvery { mockRepository.insertLimitedApp(limitedApp) } just Runs

        // When
        addLimitedAppUseCase(limitedApp)

        // Then
        coVerify { mockRepository.insertLimitedApp(limitedApp) }
    }
}