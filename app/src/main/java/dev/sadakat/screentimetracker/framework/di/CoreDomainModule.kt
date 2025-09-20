package dev.sadakat.screentimetracker.framework.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.sadakat.screentimetracker.core.domain.service.AchievementService
import dev.sadakat.screentimetracker.core.domain.service.GoalProgressService
import dev.sadakat.screentimetracker.core.domain.service.WellnessCalculationService
import dev.sadakat.screentimetracker.core.domain.service.impl.AchievementServiceImpl
import dev.sadakat.screentimetracker.core.domain.service.impl.GoalProgressServiceImpl
import dev.sadakat.screentimetracker.core.domain.service.impl.WellnessCalculationServiceImpl
import javax.inject.Singleton

/**
 * Dependency injection module for core domain layer services.
 * These are pure business logic services with no external dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object CoreDomainModule {

    @Provides
    @Singleton
    fun provideWellnessCalculationService(): WellnessCalculationService {
        return WellnessCalculationServiceImpl()
    }

    @Provides
    @Singleton
    fun provideGoalProgressService(): GoalProgressService {
        return GoalProgressServiceImpl()
    }

    @Provides
    @Singleton
    fun provideAchievementService(): AchievementService {
        return AchievementServiceImpl()
    }
}