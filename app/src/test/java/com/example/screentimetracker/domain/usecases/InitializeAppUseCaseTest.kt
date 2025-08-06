package com.example.screentimetracker.domain.usecases

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.screentimetracker.workers.HistoricalDataWorker
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

class InitializeAppUseCaseTest {

    @Mock
    private lateinit var mockApplication: Application

    @Mock
    private lateinit var mockWorkManager: WorkManager

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    private lateinit var mockSharedPreferencesEditor: SharedPreferences.Editor

    private lateinit var initializeAppUseCase: InitializeAppUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        whenever(mockApplication.getSharedPreferences("app_prefs", Context.MODE_PRIVATE))
            .thenReturn(mockSharedPreferences)
        whenever(mockSharedPreferences.edit()).thenReturn(mockSharedPreferencesEditor)
        whenever(mockSharedPreferencesEditor.putBoolean(any(), any())).thenReturn(mockSharedPreferencesEditor)
        
        initializeAppUseCase = InitializeAppUseCase(mockApplication, mockWorkManager)
    }

    @Test
    fun `invoke should enqueue historical data worker on first launch`() = runTest {
        // Given
        whenever(mockSharedPreferences.getBoolean("is_first_launch", true)).thenReturn(true)

        // When
        initializeAppUseCase()

        // Then
        verify(mockWorkManager).enqueue(any<OneTimeWorkRequest>())
        verify(mockSharedPreferencesEditor).putBoolean("is_first_launch", false)
    }

    @Test
    fun `invoke should not enqueue worker if not first launch`() = runTest {
        // Given
        whenever(mockSharedPreferences.getBoolean("is_first_launch", true)).thenReturn(false)

        // When
        initializeAppUseCase()

        // Then
        verify(mockWorkManager, never()).enqueue(any<OneTimeWorkRequest>())
        verify(mockSharedPreferencesEditor, never()).putBoolean("is_first_launch", false)
    }

    @Test
    fun `invoke should access correct shared preferences`() = runTest {
        // Given
        whenever(mockSharedPreferences.getBoolean("is_first_launch", true)).thenReturn(false)

        // When
        initializeAppUseCase()

        // Then
        verify(mockApplication).getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        verify(mockSharedPreferences).getBoolean("is_first_launch", true)
    }
}