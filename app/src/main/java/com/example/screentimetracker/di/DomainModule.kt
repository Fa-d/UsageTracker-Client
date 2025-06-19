package com.example.screentimetracker.di

import com.example.screentimetracker.domain.repository.TrackerRepository
import com.example.screentimetracker.domain.usecases.GetDashboardDataUseCase
import com.example.screentimetracker.domain.usecases.RecordAppUsageEventUseCase // Existing
import com.example.screentimetracker.domain.usecases.RecordScreenUnlockUseCase // Existing
import com.example.screentimetracker.domain.usecases.RecordAppSessionUseCase // New UseCase
import com.example.screentimetracker.domain.usecases.GetHistoricalDataUseCase // New UseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
// import dagger.hilt.android.components.ViewModelComponent // Changed to SingletonComponent
// import dagger.hilt.android.scopes.ViewModelScoped       // Changed to @Singleton
import dagger.hilt.components.SingletonComponent // For Singleton scope
import javax.inject.Singleton // For Singleton scope

@Module
// Consider if this module should be installed in SingletonComponent if UseCases are also used by Services
// For now, keeping ViewModelComponent as per previous setup, but RecordAppSessionUseCase will be used by a Service.
// Let's change to SingletonComponent for UseCases that might be shared or used by long-lived components like Services.
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    // @ViewModelScoped // Changed to @Singleton
    @Singleton
    fun provideGetDashboardDataUseCase(repository: TrackerRepository): GetDashboardDataUseCase {
        return GetDashboardDataUseCase(repository)
    }

    @Provides
    // @ViewModelScoped // Changed to @Singleton
    @Singleton
    fun provideRecordScreenUnlockUseCase(repository: TrackerRepository): RecordScreenUnlockUseCase {
        return RecordScreenUnlockUseCase(repository)
    }

    @Provides
    // @ViewModelScoped // Changed to @Singleton
    @Singleton
    fun provideRecordAppUsageEventUseCase(repository: TrackerRepository): RecordAppUsageEventUseCase {
        // This UseCase might be deprecated later if all "open" events are derived from sessions
        return RecordAppUsageEventUseCase(repository)
    }

    @Provides
    // @ViewModelScoped // Changed to @Singleton for service usage
    @Singleton
    fun provideRecordAppSessionUseCase(repository: TrackerRepository): RecordAppSessionUseCase {
        return RecordAppSessionUseCase(repository)
    }

    @Provides
    @Singleton // Assuming this can be a singleton
    fun provideGetHistoricalDataUseCase(repository: TrackerRepository): GetHistoricalDataUseCase {
        return GetHistoricalDataUseCase(repository)
    }
}
