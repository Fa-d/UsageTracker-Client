package dev.sadakat.screentimetracker.framework.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.sadakat.screentimetracker.core.data.local.dao.AchievementDao
import dev.sadakat.screentimetracker.core.data.local.dao.AppSessionDao
import dev.sadakat.screentimetracker.core.data.local.dao.AppUsageDao
import dev.sadakat.screentimetracker.core.data.local.dao.ProgressiveLimitDao
import dev.sadakat.screentimetracker.core.data.local.dao.ScreenUnlockDao
import dev.sadakat.screentimetracker.core.data.local.dao.UserGoalDao
import dev.sadakat.screentimetracker.core.data.local.dao.WellnessScoreDao
import dev.sadakat.screentimetracker.core.data.mapper.AchievementDataMapper
import dev.sadakat.screentimetracker.core.data.mapper.ScreenTimeDataMapper
import dev.sadakat.screentimetracker.core.data.mapper.UserGoalDataMapper
import dev.sadakat.screentimetracker.core.data.mapper.UserPreferencesDataMapper
import dev.sadakat.screentimetracker.core.data.repository.AchievementRepositoryImpl
import dev.sadakat.screentimetracker.core.data.repository.InsightRepositoryImpl
import dev.sadakat.screentimetracker.core.data.repository.ScreenTimeRepositoryImpl
import dev.sadakat.screentimetracker.core.data.repository.UsageLimitRepositoryImpl
import dev.sadakat.screentimetracker.core.data.repository.UserGoalRepositoryImpl
import dev.sadakat.screentimetracker.core.data.repository.UserPreferencesRepositoryImpl
import dev.sadakat.screentimetracker.core.domain.repository.AchievementRepository
import dev.sadakat.screentimetracker.core.domain.repository.InsightRepository
import dev.sadakat.screentimetracker.core.domain.repository.ScreenTimeRepository
import dev.sadakat.screentimetracker.core.domain.repository.UsageLimitRepository
import dev.sadakat.screentimetracker.core.domain.repository.UserGoalRepository
import dev.sadakat.screentimetracker.core.domain.repository.UserPreferencesRepository
import javax.inject.Singleton

/**
 * Dependency injection module for core data layer.
 * Provides repository implementations and data mappers.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CoreDataModule {

    // ==================== Repository Implementations ====================
    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        userPreferencesRepositoryImpl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository

    companion object {
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

        @Provides
        @Singleton
        fun provideUserPreferencesDataMapper(): UserPreferencesDataMapper {
            return UserPreferencesDataMapper()
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

        @Provides
        @Singleton
        fun provideInsightRepository(): InsightRepository {
            return InsightRepositoryImpl()
        }

        @Provides
        @Singleton
        fun provideUsageLimitRepository(
            progressiveLimitDao: ProgressiveLimitDao
        ): UsageLimitRepository {
            return UsageLimitRepositoryImpl(progressiveLimitDao)
        }
    }
}