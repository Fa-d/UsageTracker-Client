package dev.sadakat.screentimetracker.di

import android.app.Application // Needed for GetInstalledAppsUseCase
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import dev.sadakat.screentimetracker.domain.usecases.AddLimitedAppUseCase
import dev.sadakat.screentimetracker.domain.usecases.GetAllLimitedAppsUseCase
import dev.sadakat.screentimetracker.domain.usecases.GetDashboardDataUseCase
import dev.sadakat.screentimetracker.domain.usecases.GetHistoricalDataUseCase
import dev.sadakat.screentimetracker.domain.usecases.GetInstalledAppsUseCase
import dev.sadakat.screentimetracker.domain.usecases.GetLimitedAppUseCase
import dev.sadakat.screentimetracker.domain.usecases.RecordAppSessionUseCase
import dev.sadakat.screentimetracker.domain.usecases.RecordAppUsageEventUseCase
import dev.sadakat.screentimetracker.domain.usecases.RecordScreenUnlockUseCase
import dev.sadakat.screentimetracker.domain.usecases.RemoveLimitedAppUseCase
import dev.sadakat.screentimetracker.domain.usecases.GetAppSessionEventsUseCase
import dev.sadakat.screentimetracker.domain.usecases.UpdateLimitedAppUseCase
import dev.sadakat.screentimetracker.domain.usecases.GetAchievementsUseCase
import dev.sadakat.screentimetracker.domain.usecases.CalculateWellnessScoreUseCase
import dev.sadakat.screentimetracker.domain.usecases.InitializeAchievementsUseCase
import dev.sadakat.screentimetracker.domain.usecases.UpdateAchievementProgressUseCase
import dev.sadakat.screentimetracker.domain.usecases.ProgressiveLimitsUseCase
import dev.sadakat.screentimetracker.domain.usecases.UserPreferencesUseCase
import dev.sadakat.screentimetracker.data.local.dao.ProgressiveLimitDao
import dev.sadakat.screentimetracker.data.local.dao.ProgressiveMilestoneDao
import dev.sadakat.screentimetracker.data.local.dao.UserPreferencesDao
import dev.sadakat.screentimetracker.utils.ui.AppNotificationManager
import dev.sadakat.screentimetracker.utils.logger.AppLogger
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

    @Provides
    @Singleton
    fun provideGetAchievementsUseCase(repository: TrackerRepository): GetAchievementsUseCase {
        return GetAchievementsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideCalculateWellnessScoreUseCase(repository: TrackerRepository): CalculateWellnessScoreUseCase {
        return CalculateWellnessScoreUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideInitializeAchievementsUseCase(repository: TrackerRepository): InitializeAchievementsUseCase {
        return InitializeAchievementsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateAchievementProgressUseCase(
        repository: TrackerRepository,
        notificationManager: AppNotificationManager,
        appLogger: AppLogger
    ): UpdateAchievementProgressUseCase {
        return UpdateAchievementProgressUseCase(repository, notificationManager, appLogger)
    }

    @Provides
    @Singleton
    fun provideProgressiveLimitsUseCase(
        progressiveLimitDao: ProgressiveLimitDao,
        progressiveMilestoneDao: ProgressiveMilestoneDao,
        trackerRepository: TrackerRepository
    ): ProgressiveLimitsUseCase {
        return ProgressiveLimitsUseCase(progressiveLimitDao, progressiveMilestoneDao, trackerRepository)
    }

    @Provides
    @Singleton
    fun provideUserPreferencesUseCase(
        userPreferencesDao: UserPreferencesDao
    ): UserPreferencesUseCase {
        return UserPreferencesUseCase(userPreferencesDao)
    }
}