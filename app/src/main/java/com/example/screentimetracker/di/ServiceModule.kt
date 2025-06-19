package com.example.screentimetracker.di

import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent // Or ServiceComponent if scoped to service
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Provide UsageStatsManager as a Singleton
object ServiceModule { // Renamed to ServiceModule for clarity, or keep as AppModule

    @Provides
    @Singleton
    fun provideUsageStatsManager(@ApplicationContext context: Context): UsageStatsManager {
        return context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }
}
