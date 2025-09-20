package dev.sadakat.screentimetracker.di

import android.app.Application // Needed for GetInstalledAppsUseCase
import dev.sadakat.screentimetracker.core.domain.repository.TrackerRepository
import dev.sadakat.screentimetracker.core.domain.usecases.AddLimitedAppUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.GetAllLimitedAppsUseCase
// GetDashboardDataUseCase provided by CoreUseCaseModule
import dev.sadakat.screentimetracker.core.domain.usecases.GetHistoricalDataUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.GetInstalledAppsUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.GetLimitedAppUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.RecordAppSessionUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.RecordAppUsageEventUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.RecordScreenUnlockUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.RemoveLimitedAppUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.GetAppSessionEventsUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.UpdateLimitedAppUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.GetAchievementsUseCase
// CalculateWellnessScoreUseCase provided by CoreUseCaseModule
import dev.sadakat.screentimetracker.core.domain.usecases.InitializeAchievementsUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.UpdateAchievementProgressUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.ProgressiveLimitsUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.UserPreferencesUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.AggregateDailyUsageUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.TimeRestrictionManagerUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.FocusSessionManagerUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.WeeklyInsightsUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.ChallengeManagerUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.SmartGoalSettingUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.HabitTrackerUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.DataExportUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.GoalProgressTrackingUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.PrivacyManagerUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.ReplacementActivitiesUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.MindfulnessUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.AIIntegrationUseCase
import dev.sadakat.screentimetracker.core.domain.usecases.AppCategoryManagementUseCase
import dev.sadakat.screentimetracker.core.domain.categorization.AppCategorizer
import dev.sadakat.screentimetracker.core.domain.repository.AppCategoryRepository
import dev.sadakat.screentimetracker.core.data.local.dao.ProgressiveLimitDao
import dev.sadakat.screentimetracker.core.data.local.dao.ProgressiveMilestoneDao
import dev.sadakat.screentimetracker.core.data.local.dao.UserPreferencesDao
import dev.sadakat.screentimetracker.utils.ui.AppNotificationManager
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // All these are stateless or app-scoped
object DomainModule {

    // GetDashboardDataUseCase is now provided by CoreUseCaseModule for clean architecture

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

    // CalculateWellnessScoreUseCase is now provided by CoreUseCaseModule for clean architecture

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

    @Provides
    @Singleton
    fun provideAggregateDailyUsageUseCase(
        repository: TrackerRepository,
        appLogger: AppLogger
    ): AggregateDailyUsageUseCase {
        return AggregateDailyUsageUseCase(repository, appLogger)
    }

    @Provides
    @Singleton
    fun provideTimeRestrictionManagerUseCase(
        repository: TrackerRepository,
        notificationManager: AppNotificationManager,
        appLogger: AppLogger
    ): TimeRestrictionManagerUseCase {
        return TimeRestrictionManagerUseCase(repository, notificationManager, appLogger)
    }

    @Provides
    @Singleton
    fun provideFocusSessionManagerUseCase(
        repository: TrackerRepository,
        notificationManager: AppNotificationManager,
        appLogger: AppLogger
    ): FocusSessionManagerUseCase {
        return FocusSessionManagerUseCase(repository, notificationManager, appLogger)
    }

    @Provides
    @Singleton
    fun provideChallengeManagerUseCase(
        repository: TrackerRepository,
        notificationManager: AppNotificationManager,
        appLogger: AppLogger
    ): ChallengeManagerUseCase {
        return ChallengeManagerUseCase(repository, notificationManager, appLogger)
    }

    @Provides
    @Singleton
    fun provideSmartGoalSettingUseCase(
        repository: TrackerRepository,
        notificationManager: AppNotificationManager,
        appLogger: AppLogger
    ): SmartGoalSettingUseCase {
        return SmartGoalSettingUseCase(repository, notificationManager, appLogger)
    }

    @Provides
    @Singleton
    fun provideGoalProgressTrackingUseCase(
        repository: TrackerRepository,
        notificationManager: AppNotificationManager,
        appLogger: AppLogger
    ): GoalProgressTrackingUseCase {
        return GoalProgressTrackingUseCase(repository, notificationManager, appLogger)
    }

    @Provides
    @Singleton
    fun provideWeeklyInsightsUseCase(
        repository: TrackerRepository,
        notificationManager: AppNotificationManager,
        appLogger: AppLogger,
        appCategorizer: dev.sadakat.screentimetracker.core.domain.categorization.AppCategorizer
    ): WeeklyInsightsUseCase {
        return WeeklyInsightsUseCase(repository, notificationManager, appLogger, appCategorizer)
    }

    @Provides
    @Singleton
    fun provideHabitTrackerUseCase(
        repository: TrackerRepository,
        notificationManager: AppNotificationManager,
        appLogger: AppLogger,
        focusSessionManagerUseCase: FocusSessionManagerUseCase,
        getDashboardDataUseCase: dev.sadakat.screentimetracker.core.domain.usecase.GetDashboardDataUseCase
    ): HabitTrackerUseCase {
        return HabitTrackerUseCase(repository, notificationManager, appLogger, focusSessionManagerUseCase, getDashboardDataUseCase)
    }

    @Provides
    @Singleton
    fun provideAppCategoryManagementUseCase(
        appCategorizer: AppCategorizer,
        appCategoryRepository: AppCategoryRepository,
        appLogger: AppLogger
    ): AppCategoryManagementUseCase {
        return AppCategoryManagementUseCase(appCategorizer, appCategoryRepository, appLogger)
    }

    @Provides
    @Singleton
    fun provideMindfulnessUseCase(
        mindfulnessSessionDao: dev.sadakat.screentimetracker.core.data.local.dao.MindfulnessSessionDao
    ): MindfulnessUseCase {
        return MindfulnessUseCase(mindfulnessSessionDao)
    }

    @Provides
    @Singleton
    fun provideAIIntegrationUseCase(): AIIntegrationUseCase {
        return AIIntegrationUseCase()
    }

    @Provides
    @Singleton
    fun providePrivacyManagerUseCase(
        privacySettingsDao: dev.sadakat.screentimetracker.core.data.local.dao.PrivacySettingsDao
    ): PrivacyManagerUseCase {
        return PrivacyManagerUseCase(privacySettingsDao)
    }

    @Provides
    @Singleton
    fun provideReplacementActivitiesUseCase(
        replacementActivityDao: dev.sadakat.screentimetracker.core.data.local.dao.ReplacementActivityDao
    ): ReplacementActivitiesUseCase {
        return ReplacementActivitiesUseCase(replacementActivityDao)
    }

    @Provides
    @Singleton
    fun provideDataExportUseCase(
        @ApplicationContext context: android.content.Context,
        repository: TrackerRepository,
        appUsageDao: dev.sadakat.screentimetracker.core.data.local.dao.AppUsageDao,
        appSessionDao: dev.sadakat.screentimetracker.core.data.local.dao.AppSessionDao,
        screenUnlockDao: dev.sadakat.screentimetracker.core.data.local.dao.ScreenUnlockDao,
        dailyAppSummaryDao: dev.sadakat.screentimetracker.core.data.local.dao.DailyAppSummaryDao,
        dailyScreenUnlockSummaryDao: dev.sadakat.screentimetracker.core.data.local.dao.DailyScreenUnlockSummaryDao,
        achievementDao: dev.sadakat.screentimetracker.core.data.local.dao.AchievementDao,
        wellnessScoreDao: dev.sadakat.screentimetracker.core.data.local.dao.WellnessScoreDao,
        userGoalDao: dev.sadakat.screentimetracker.core.data.local.dao.UserGoalDao,
        challengeDao: dev.sadakat.screentimetracker.core.data.local.dao.ChallengeDao,
        focusSessionDao: dev.sadakat.screentimetracker.core.data.local.dao.FocusSessionDao,
        habitTrackerDao: dev.sadakat.screentimetracker.core.data.local.dao.HabitTrackerDao,
        timeRestrictionDao: dev.sadakat.screentimetracker.core.data.local.dao.TimeRestrictionDao,
        progressiveLimitDao: dev.sadakat.screentimetracker.core.data.local.dao.ProgressiveLimitDao,
        progressiveMilestoneDao: dev.sadakat.screentimetracker.core.data.local.dao.ProgressiveMilestoneDao,
        limitedAppDao: dev.sadakat.screentimetracker.core.data.local.dao.LimitedAppDao,
        privacyManagerUseCase: PrivacyManagerUseCase
    ): DataExportUseCase {
        return DataExportUseCase(
            context, repository, appUsageDao, appSessionDao, screenUnlockDao,
            dailyAppSummaryDao, dailyScreenUnlockSummaryDao, achievementDao,
            wellnessScoreDao, userGoalDao, challengeDao, focusSessionDao,
            habitTrackerDao, timeRestrictionDao, progressiveLimitDao,
            progressiveMilestoneDao, limitedAppDao, privacyManagerUseCase
        )
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: android.content.Context): androidx.work.WorkManager {
        return androidx.work.WorkManager.getInstance(context)
    }
}