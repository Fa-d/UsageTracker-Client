package com.example.screentimetracker.di

import android.content.Context
import com.example.screentimetracker.domain.permissions.AppPermissionManager
import com.example.screentimetracker.domain.permissions.PermissionManager
import com.example.screentimetracker.domain.service.ScreenTimeServiceManager
import com.example.screentimetracker.domain.service.ServiceManager
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
