package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.LimitedApp
import com.example.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import io.mockk.*
import io.mockk.impl.annotations.MockK
import java.util.concurrent.TimeUnit

class RemoveLimitedAppUseCaseTest {

    @MockK
    private lateinit var mockRepository: TrackerRepository

    private lateinit var removeLimitedAppUseCase: RemoveLimitedAppUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        removeLimitedAppUseCase = RemoveLimitedAppUseCase(mockRepository)
    }

    @Test
    fun `invoke should remove limited app from repository`() = runTest {
        // Given
        val limitedApp = LimitedApp(
            packageName = "com.instagram.android",
            timeLimitMillis = TimeUnit.HOURS.toMillis(1)
        )

        // When
        removeLimitedAppUseCase(limitedApp)

        // Then
        coVerify { mockRepository.deleteLimitedApp(limitedApp) }
    }

    @Test
    fun `invoke should handle multiple app removals`() = runTest {
        // Given
        val limitedApps = listOf(
            LimitedApp("com.facebook.katana", TimeUnit.MINUTES.toMillis(30)),
            LimitedApp("com.twitter.android", TimeUnit.MINUTES.toMillis(45)),
            LimitedApp("com.snapchat.android", TimeUnit.HOURS.toMillis(2))
        )

        // When
        limitedApps.forEach { removeLimitedAppUseCase(it) }

        // Then
        limitedApps.forEach { 
            coVerify { mockRepository.deleteLimitedApp(it) }
        }
    }

    @Test
    fun `invoke should work with different package names`() = runTest {
        // Given
        val testApps = listOf(
            "com.whatsapp",
            "com.spotify.music",
            "com.netflix.mediaclient",
            "com.google.android.youtube"
        )

        // When & Then
        testApps.forEach { packageName ->
            val limitedApp = LimitedApp(packageName, TimeUnit.MINUTES.toMillis(30))
            removeLimitedAppUseCase(limitedApp)
            coVerify { mockRepository.deleteLimitedApp(limitedApp) }
        }
    }

    @Test
    fun `invoke should handle app with zero time limit`() = runTest {
        // Given - even with invalid time limit, removal should work
        val limitedApp = LimitedApp(
            packageName = "com.example.app",
            timeLimitMillis = 0L
        )

        // When
        removeLimitedAppUseCase(limitedApp)

        // Then
        coVerify { mockRepository.deleteLimitedApp(limitedApp) }
    }
}