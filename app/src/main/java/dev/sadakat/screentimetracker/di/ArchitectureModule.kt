package dev.sadakat.screentimetracker.di

import android.content.Context
import dev.sadakat.screentimetracker.core.domain.permissions.AppPermissionManager
import dev.sadakat.screentimetracker.core.domain.permissions.PermissionManager
import dev.sadakat.screentimetracker.core.domain.service.ScreenTimeServiceManager
import dev.sadakat.screentimetracker.core.domain.service.ServiceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ArchitectureModule {

    @Provides
    @Singleton
    fun providePermissionManager(
        @ApplicationContext context: Context
    ): PermissionManager {
        return AppPermissionManager(context)
    }

    @Provides
    @Singleton
    fun provideServiceManager(
        @ApplicationContext context: Context
    ): ServiceManager {
        return ScreenTimeServiceManager(context)
    }
}
