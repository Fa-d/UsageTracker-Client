package dev.sadakat.screentimetracker.di

import dev.sadakat.screentimetracker.utils.ui.AppNotificationManager
import dev.sadakat.screentimetracker.utils.ui.AppNotificationManagerImpl
import dev.sadakat.screentimetracker.utils.ui.AppToastManager
import dev.sadakat.screentimetracker.utils.ui.AppToastManagerImpl
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
