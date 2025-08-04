package com.example.screentimetracker.di

import com.example.screentimetracker.utils.logger.AppLogger
import com.example.screentimetracker.utils.logger.AppLoggerImpl
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
