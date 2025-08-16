package com.example.screentimetracker.di

import android.app.Application
import androidx.room.Room
import com.example.screentimetracker.data.local.AppDatabase
import com.example.screentimetracker.data.local.AppSessionDao
import com.example.screentimetracker.data.local.AppUsageDao
import com.example.screentimetracker.data.local.DailyAppSummaryDao
import com.example.screentimetracker.data.local.DailyScreenUnlockSummaryDao
import com.example.screentimetracker.data.local.LimitedAppDao // Import LimitedAppDao
import com.example.screentimetracker.data.local.ScreenUnlockDao
import com.example.screentimetracker.data.local.AchievementDao
import com.example.screentimetracker.data.local.WellnessScoreDao
import com.example.screentimetracker.data.local.UserGoalDao
import com.example.screentimetracker.data.local.ChallengeDao
import com.example.screentimetracker.data.local.FocusSessionDao
import com.example.screentimetracker.data.local.HabitTrackerDao
import com.example.screentimetracker.data.local.TimeRestrictionDao
import com.example.screentimetracker.data.local.ProgressiveLimitDao
import com.example.screentimetracker.data.local.ProgressiveMilestoneDao
import com.example.screentimetracker.data.local.UserPreferencesDao
import com.example.screentimetracker.data.local.MindfulnessSessionDao
import com.example.screentimetracker.data.local.PrivacySettingsDao
import com.example.screentimetracker.data.local.ReplacementActivityDao
import com.example.screentimetracker.data.local.AppCategoryDao
import com.example.screentimetracker.data.repository.TrackerRepositoryImpl
import com.example.screentimetracker.data.repository.AppCategoryRepositoryImpl
import com.example.screentimetracker.domain.repository.TrackerRepository
import com.example.screentimetracker.domain.repository.AppCategoryRepository
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
            .addMigrations(AppDatabase.MIGRATION_9_10, AppDatabase.MIGRATION_10_11, AppDatabase.MIGRATION_11_12, AppDatabase.MIGRATION_12_13) // Add the proper migrations
          //  .fallbackToDestructiveMigrationOnDowngrade() // Only for downgrades
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

    @Provides
    @Singleton
    fun provideLimitedAppDao(db: AppDatabase): LimitedAppDao { // Provide LimitedAppDao
        return db.limitedAppDao()
    }

    @Provides
    @Singleton
    fun provideAchievementDao(db: AppDatabase): AchievementDao {
        return db.achievementDao()
    }

    @Provides
    @Singleton
    fun provideWellnessScoreDao(db: AppDatabase): WellnessScoreDao {
        return db.wellnessScoreDao()
    }

    @Provides
    @Singleton
    fun provideUserGoalDao(db: AppDatabase): UserGoalDao {
        return db.userGoalDao()
    }

    @Provides
    @Singleton
    fun provideChallengeDao(db: AppDatabase): ChallengeDao {
        return db.challengeDao()
    }

    @Provides
    @Singleton
    fun provideFocusSessionDao(db: AppDatabase): FocusSessionDao {
        return db.focusSessionDao()
    }

    @Provides
    @Singleton
    fun provideHabitTrackerDao(db: AppDatabase): HabitTrackerDao {
        return db.habitTrackerDao()
    }

    @Provides
    @Singleton
    fun provideTimeRestrictionDao(db: AppDatabase): TimeRestrictionDao {
        return db.timeRestrictionDao()
    }

    @Provides
    @Singleton
    fun provideProgressiveLimitDao(db: AppDatabase): ProgressiveLimitDao {
        return db.progressiveLimitDao()
    }

    @Provides
    @Singleton
    fun provideProgressiveMilestoneDao(db: AppDatabase): ProgressiveMilestoneDao {
        return db.progressiveMilestoneDao()
    }

    @Provides
    @Singleton
    fun provideUserPreferencesDao(db: AppDatabase): UserPreferencesDao {
        return db.userPreferencesDao()
    }

    @Provides
    @Singleton
    fun provideMindfulnessSessionDao(db: AppDatabase): MindfulnessSessionDao {
        return db.mindfulnessSessionDao()
    }

    @Provides
    @Singleton
    fun providePrivacySettingsDao(db: AppDatabase): PrivacySettingsDao {
        return db.privacySettingsDao()
    }

    @Provides
    @Singleton
    fun provideReplacementActivityDao(db: AppDatabase): ReplacementActivityDao {
        return db.replacementActivityDao()
    }
    
    @Provides
    @Singleton
    fun provideAppCategoryDao(db: AppDatabase): AppCategoryDao {
        return db.appCategoryDao()
    }
    
    @Provides
    @Singleton
    fun provideAppCategoryRepository(
        appCategoryDao: AppCategoryDao,
        appLogger: com.example.screentimetracker.utils.logger.AppLogger
    ): AppCategoryRepository {
        return AppCategoryRepositoryImpl(appCategoryDao, appLogger)
    }
}
