package dev.sadakat.screentimetracker.di

import android.content.Context
import dev.sadakat.screentimetracker.core.domain.permissions.PermissionManager
import dev.sadakat.screentimetracker.core.domain.service.ServiceManager
import dev.sadakat.screentimetracker.core.domain.categorization.AppCategorizer
import dev.sadakat.screentimetracker.core.presentation.temp.TempPermissionManager
import dev.sadakat.screentimetracker.core.presentation.temp.TempServiceManager
import dev.sadakat.screentimetracker.core.presentation.temp.TempAppCategorizer
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
        return TempPermissionManager(context)
    }

    @Provides
    @Singleton
    fun provideServiceManager(
        @ApplicationContext context: Context
    ): ServiceManager {
        return TempServiceManager(context)
    }

    @Provides
    @Singleton
    fun provideAppCategorizer(): AppCategorizer {
        return TempAppCategorizer()
    }
}
