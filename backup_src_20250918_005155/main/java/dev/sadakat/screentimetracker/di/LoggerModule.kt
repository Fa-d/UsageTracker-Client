package dev.sadakat.screentimetracker.di

import dev.sadakat.screentimetracker.utils.logger.AppLogger
import dev.sadakat.screentimetracker.utils.logger.AppLoggerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LoggerModule {

    @Binds
    @Singleton
    abstract fun bindAppLogger(appLoggerImpl: AppLoggerImpl): AppLogger
}
