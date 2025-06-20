package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.LimitedApp
import com.example.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.times

class AddLimitedAppUseCaseTest {
    private lateinit var mockRepository: TrackerRepository
    private lateinit var addLimitedAppUseCase: AddLimitedAppUseCase

    @Before
    fun setup() {
        mockRepository = mock(TrackerRepository::class.java)
        addLimitedAppUseCase = AddLimitedAppUseCase(mockRepository)
    }

    @Test
    fun invoke_positiveTimeLimit_insertsApp() = runBlocking {
        val limitedApp = LimitedApp("com.example.app", 300000L)
        addLimitedAppUseCase(limitedApp)
        verify(mockRepository, times(1)).insertLimitedApp(limitedApp)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invoke_zeroTimeLimit_throwsException() = runBlocking {
        val limitedApp = LimitedApp("com.example.app", 0L)
        addLimitedAppUseCase(limitedApp)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invoke_negativeTimeLimit_throwsException() = runBlocking {
        val limitedApp = LimitedApp("com.example.app", -100L)
        addLimitedAppUseCase(limitedApp)
    }
}
