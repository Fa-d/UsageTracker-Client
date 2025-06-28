package com.example.screentimetracker.di

import android.app.Application // Needed for GetInstalledAppsUseCase
import com.example.screentimetracker.domain.repository.TrackerRepository
import com.example.screentimetracker.domain.usecases.AddLimitedAppUseCase
import com.example.screentimetracker.domain.usecases.GetAllLimitedAppsUseCase
import com.example.screentimetracker.domain.usecases.GetDashboardDataUseCase
import com.example.screentimetracker.domain.usecases.GetHistoricalDataUseCase
import com.example.screentimetracker.domain.usecases.GetInstalledAppsUseCase
import com.example.screentimetracker.domain.usecases.GetLimitedAppUseCase
import com.example.screentimetracker.domain.usecases.RecordAppSessionUseCase
import com.example.screentimetracker.domain.usecases.RecordAppUsageEventUseCase
import com.example.screentimetracker.domain.usecases.RecordScreenUnlockUseCase
import com.example.screentimetracker.domain.usecases.RemoveLimitedAppUseCase
import com.example.screentimetracker.domain.usecases.GetAppSessionEventsUseCase
import com.example.screentimetracker.domain.usecases.UpdateLimitedAppUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // All these are stateless or app-scoped
object DomainModule {

    @Provides
    @Singleton
    fun provideGetDashboardDataUseCase(repository: TrackerRepository): GetDashboardDataUseCase {
        return GetDashboardDataUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideRecordScreenUnlockUseCase(repository: TrackerRepository): RecordScreenUnlockUseCase {
        return RecordScreenUnlockUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideRecordAppUsageEventUseCase(repository: TrackerRepository): RecordAppUsageEventUseCase {
        // This UseCase might be deprecated later if all "open" events are derived from sessions
        return RecordAppUsageEventUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideRecordAppSessionUseCase(repository: TrackerRepository): RecordAppSessionUseCase {
        return RecordAppSessionUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetHistoricalDataUseCase(repository: TrackerRepository): GetHistoricalDataUseCase {
        return GetHistoricalDataUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideAddLimitedAppUseCase(repository: TrackerRepository): AddLimitedAppUseCase {
        return AddLimitedAppUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideRemoveLimitedAppUseCase(repository: TrackerRepository): RemoveLimitedAppUseCase {
        return RemoveLimitedAppUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetLimitedAppUseCase(repository: TrackerRepository): GetLimitedAppUseCase {
        return GetLimitedAppUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetAllLimitedAppsUseCase(repository: TrackerRepository): GetAllLimitedAppsUseCase {
        return GetAllLimitedAppsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetInstalledAppsUseCase(application: Application): GetInstalledAppsUseCase {
        return GetInstalledAppsUseCase(application)
    }

    @Provides
    @Singleton
    fun provideGetAppSessionEventsUseCase(repository: TrackerRepository): GetAppSessionEventsUseCase {
        return GetAppSessionEventsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateLimitedAppUseCase(repository: TrackerRepository): UpdateLimitedAppUseCase {
        return UpdateLimitedAppUseCase(repository)
    }
}
