package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.LimitedApp
import com.example.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import java.util.concurrent.TimeUnit

class AddLimitedAppUseCaseTest {

    @Mock
    private lateinit var mockRepository: TrackerRepository

    private lateinit var addLimitedAppUseCase: AddLimitedAppUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        addLimitedAppUseCase = AddLimitedAppUseCase(mockRepository)
    }

    @Test
    fun `invoke should add limited app with valid time limit`() = runTest {
        // Given
        val limitedApp = LimitedApp(
            packageName = "com.instagram.android",
            timeLimitMillis = TimeUnit.HOURS.toMillis(1) // 1 hour
        )

        // When
        addLimitedAppUseCase(limitedApp)

        // Then
        verify(mockRepository).insertLimitedApp(limitedApp)
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
        
        verify(mockRepository, never()).insertLimitedApp(any())
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
        
        verify(mockRepository, never()).insertLimitedApp(any())
    }

    @Test
    fun `invoke should handle different package names correctly`() = runTest {
        // Given
        val limitedApps = listOf(
            LimitedApp("com.facebook.katana", TimeUnit.MINUTES.toMillis(30)),
            LimitedApp("com.twitter.android", TimeUnit.MINUTES.toMillis(45)),
            LimitedApp("com.snapchat.android", TimeUnit.HOURS.toMillis(2))
        )

        // When
        limitedApps.forEach { addLimitedAppUseCase(it) }

        // Then
        limitedApps.forEach { 
            verify(mockRepository).insertLimitedApp(it)
        }
    }

    @Test
    fun `invoke should handle minimum valid time limit`() = runTest {
        // Given
        val limitedApp = LimitedApp(
            packageName = "com.app.test",
            timeLimitMillis = 1L // Minimum valid value
        )

        // When
        addLimitedAppUseCase(limitedApp)

        // Then
        verify(mockRepository).insertLimitedApp(limitedApp)
    }
}