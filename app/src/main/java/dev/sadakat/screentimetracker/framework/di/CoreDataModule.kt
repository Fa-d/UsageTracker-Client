package dev.sadakat.screentimetracker.framework.di

import dev.sadakat.screentimetracker.core.domain.repository.ScreenTimeRepository
import dev.sadakat.screentimetracker.core.domain.repository.UserGoalRepository
import dev.sadakat.screentimetracker.core.domain.repository.AchievementRepository
import dev.sadakat.screentimetracker.core.data.repository.ScreenTimeRepositoryImpl
import dev.sadakat.screentimetracker.core.data.repository.UserGoalRepositoryImpl
import dev.sadakat.screentimetracker.core.data.repository.AchievementRepositoryImpl
import dev.sadakat.screentimetracker.core.data.mapper.ScreenTimeDataMapper
import dev.sadakat.screentimetracker.core.data.mapper.UserGoalDataMapper
import dev.sadakat.screentimetracker.core.data.mapper.AchievementDataMapper
import dev.sadakat.screentimetracker.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency injection module for core data layer.
 * Provides repository implementations and data mappers.
 */
@Module
@InstallIn(SingletonComponent::class)
object CoreDataModule {

    // ==================== Data Mappers ====================

    @Provides
    @Singleton
    fun provideScreenTimeDataMapper(): ScreenTimeDataMapper {
        return ScreenTimeDataMapper()
    }

    @Provides
    @Singleton
    fun provideUserGoalDataMapper(): UserGoalDataMapper {
        return UserGoalDataMapper()
    }

    @Provides
    @Singleton
    fun provideAchievementDataMapper(): AchievementDataMapper {
        return AchievementDataMapper()
    }

    // ==================== Repository Implementations ====================

    @Provides
    @Singleton
    fun provideScreenTimeRepository(
        appSessionDao: AppSessionDao,
        appUsageDao: AppUsageDao,
        screenUnlockDao: ScreenUnlockDao,
        wellnessScoreDao: WellnessScoreDao,
        dataMapper: ScreenTimeDataMapper
    ): ScreenTimeRepository {
        return ScreenTimeRepositoryImpl(
            appSessionDao,
            appUsageDao,
            screenUnlockDao,
            wellnessScoreDao,
            dataMapper
        )
    }

    @Provides
    @Singleton
    fun provideUserGoalRepository(
        userGoalDao: UserGoalDao,
        dataMapper: UserGoalDataMapper
    ): UserGoalRepository {
        return UserGoalRepositoryImpl(userGoalDao, dataMapper)
    }

    @Provides
    @Singleton
    fun provideAchievementRepository(
        achievementDao: AchievementDao,
        dataMapper: AchievementDataMapper
    ): AchievementRepository {
        return AchievementRepositoryImpl(achievementDao, dataMapper)
    }
}