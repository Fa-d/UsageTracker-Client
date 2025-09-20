package dev.sadakat.screentimetracker.domain.usecases

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import io.mockk.*

class InitializeAppUseCaseTest {

    private val mockApplication = mockk<Application>()
    private val mockWorkManager = mockk<WorkManager>()
    private val mockSharedPreferences = mockk<SharedPreferences>()
    private val mockSharedPreferencesEditor = mockk<SharedPreferences.Editor>(relaxed = true)

    private lateinit var initializeAppUseCase: InitializeAppUseCase

    @Before
    fun setup() {
        every { mockApplication.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) } returns mockSharedPreferences
        every { mockSharedPreferences.edit() } returns mockSharedPreferencesEditor
        every { mockSharedPreferencesEditor.putBoolean(any(), any()) } returns mockSharedPreferencesEditor
        every { mockWorkManager.enqueue(any<OneTimeWorkRequest>()) } returns mockk()
        
        initializeAppUseCase = InitializeAppUseCase(mockApplication, mockWorkManager)
    }

    @Test
    fun `invoke should enqueue historical data worker on first launch`() = runTest {
        // Given
        every { mockSharedPreferences.getBoolean("is_first_launch", true) } returns true

        // When
        initializeAppUseCase()

        // Then
        verify { mockWorkManager.enqueue(any<OneTimeWorkRequest>()) }
        verify { mockSharedPreferencesEditor.putBoolean("is_first_launch", false) }
    }

    @Test
    fun `invoke should not enqueue worker if not first launch`() = runTest {
        // Given
        every { mockSharedPreferences.getBoolean("is_first_launch", true) } returns false

        // When
        initializeAppUseCase()

        // Then
        verify(exactly = 0) { mockWorkManager.enqueue(any<OneTimeWorkRequest>()) }
        verify(exactly = 0) { mockSharedPreferencesEditor.putBoolean("is_first_launch", false) }
    }

    @Test
    fun `invoke should access correct shared preferences`() = runTest {
        // Given
        every { mockSharedPreferences.getBoolean("is_first_launch", true) } returns false

        // When
        initializeAppUseCase()

        // Then
        verify { mockApplication.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
        verify { mockSharedPreferences.getBoolean("is_first_launch", true) }
    }
}