package com.example.screentimetracker.di

import android.app.Application
import androidx.room.Room
import com.example.screentimetracker.data.local.AppDatabase
import com.example.screentimetracker.data.local.AppSessionDao
import com.example.screentimetracker.data.local.AppUsageDao
import com.example.screentimetracker.data.local.DailyAppSummaryDao
import com.example.screentimetracker.data.local.DailyScreenUnlockSummaryDao
import com.example.screentimetracker.data.local.ScreenUnlockDao
import com.example.screentimetracker.data.repository.TrackerRepositoryImpl
import com.example.screentimetracker.domain.repository.TrackerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        // .addMigrations(AppDatabase.MIGRATION_1_2) // Add migrations here if implemented
        .fallbackToDestructiveMigration() // For development, if migrations are not yet handled
        .build()
    }

    @Provides
    @Singleton
    fun provideScreenUnlockDao(db: AppDatabase): ScreenUnlockDao {
        return db.screenUnlockDao()
    }

    @Provides
    @Singleton
    fun provideAppUsageDao(db: AppDatabase): AppUsageDao {
        return db.appUsageDao()
    }

    @Provides
    @Singleton
    fun provideAppSessionDao(db: AppDatabase): AppSessionDao { // Provide AppSessionDao
        return db.appSessionDao()
    }

    @Provides
    @Singleton
    fun provideTrackerRepository(db: AppDatabase): TrackerRepository {
         // TrackerRepositoryImpl now gets AppDatabase and internally accesses db.appSessionDao()
        return TrackerRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun provideDailyAppSummaryDao(db: AppDatabase): DailyAppSummaryDao {
        return db.dailyAppSummaryDao()
    }

    @Provides
    @Singleton
    fun provideDailyScreenUnlockSummaryDao(db: AppDatabase): DailyScreenUnlockSummaryDao {
        return db.dailyScreenUnlockSummaryDao()
    }
}
