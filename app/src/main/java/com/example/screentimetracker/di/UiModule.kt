package com.example.screentimetracker.di

import com.example.screentimetracker.utils.ui.AppNotificationManager
import com.example.screentimetracker.utils.ui.AppNotificationManagerImpl
import com.example.screentimetracker.utils.ui.AppToastManager
import com.example.screentimetracker.utils.ui.AppToastManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UiModule {

    @Binds
    @Singleton
    abstract fun bindAppNotificationManager(appNotificationManagerImpl: AppNotificationManagerImpl): AppNotificationManager

    @Binds
    @Singleton
    abstract fun bindAppToastManager(appToastManagerImpl: AppToastManagerImpl): AppToastManager
}
