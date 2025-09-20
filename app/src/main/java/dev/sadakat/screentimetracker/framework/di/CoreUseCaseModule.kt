package dev.sadakat.screentimetracker.framework.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.sadakat.screentimetracker.core.domain.repository.AchievementRepository
import dev.sadakat.screentimetracker.core.domain.repository.ScreenTimeRepository
import dev.sadakat.screentimetracker.core.domain.repository.UserGoalRepository
import dev.sadakat.screentimetracker.core.domain.service.AchievementService
import dev.sadakat.screentimetracker.core.domain.service.GoalProgressService
import dev.sadakat.screentimetracker.core.domain.service.WellnessCalculationService
import dev.sadakat.screentimetracker.core.domain.usecase.CalculateWellnessScoreUseCase
import dev.sadakat.screentimetracker.core.domain.usecase.GetDashboardDataUseCase
import dev.sadakat.screentimetracker.core.domain.usecase.ManageUsageLimitsUseCase
import dev.sadakat.screentimetracker.core.domain.usecase.ProcessAchievementsUseCase
import dev.sadakat.screentimetracker.core.domain.usecase.TrackAppUsageUseCase
import javax.inject.Singleton

/**
 * Dependency injection module for core domain use cases.
 * These use cases orchestrate business logic using domain services and repositories.
 */
@Module
@InstallIn(SingletonComponent::class)
object CoreUseCaseModule {

    @Provides
    @Singleton
    fun provideGetDashboardDataUseCase(
        screenTimeRepository: ScreenTimeRepository,
        userGoalRepository: UserGoalRepository,
        achievementRepository: AchievementRepository,
        wellnessService: WellnessCalculationService,
        achievementService: AchievementService
    ): GetDashboardDataUseCase {
        return GetDashboardDataUseCase(
            screenTimeRepository,
            userGoalRepository,
            achievementRepository,
            wellnessService,
            achievementService
        )
    }

    @Provides
    @Singleton
    fun provideTrackAppUsageUseCase(
        screenTimeRepository: ScreenTimeRepository,
        wellnessService: WellnessCalculationService
    ): TrackAppUsageUseCase {
        return TrackAppUsageUseCase(
            screenTimeRepository,
            wellnessService
        )
    }

    @Provides
    @Singleton
    fun provideCalculateWellnessScoreUseCase(
        screenTimeRepository: ScreenTimeRepository,
        userGoalRepository: UserGoalRepository,
        wellnessService: WellnessCalculationService
    ): CalculateWellnessScoreUseCase {
        return CalculateWellnessScoreUseCase(
            screenTimeRepository,
            userGoalRepository,
            wellnessService
        )
    }

    @Provides
    @Singleton
    fun provideManageUsageLimitsUseCase(
        userGoalRepository: UserGoalRepository,
        goalProgressService: GoalProgressService
    ): ManageUsageLimitsUseCase {
        return ManageUsageLimitsUseCase(
            userGoalRepository,
            goalProgressService
        )
    }

    @Provides
    @Singleton
    fun provideProcessAchievementsUseCase(
        achievementRepository: AchievementRepository,
        screenTimeRepository: ScreenTimeRepository,
        userGoalRepository: UserGoalRepository,
        achievementService: AchievementService,
        wellnessService: WellnessCalculationService
    ): ProcessAchievementsUseCase {
        return ProcessAchievementsUseCase(
            achievementRepository,
            screenTimeRepository,
            userGoalRepository,
            achievementService,
            wellnessService
        )
    }
}