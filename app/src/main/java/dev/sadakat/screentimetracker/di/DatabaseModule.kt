package dev.sadakat.screentimetracker.di

import android.app.Application
import androidx.room.Room
import dev.sadakat.screentimetracker.core.data.local.database.AppDatabase
import dev.sadakat.screentimetracker.core.data.local.dao.*
import dev.sadakat.screentimetracker.core.data.repository.TrackerRepositoryImpl
import dev.sadakat.screentimetracker.core.data.repository.AppCategoryRepositoryImpl
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import dev.sadakat.screentimetracker.domain.repository.AppCategoryRepository
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
            .addMigrations(AppDatabase.MIGRATION_9_10, AppDatabase.MIGRATION_10_11, AppDatabase.MIGRATION_11_12, AppDatabase.MIGRATION_12_13, AppDatabase.MIGRATION_13_14, AppDatabase.MIGRATION_14_15) // Add the proper migrations
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
    fun provideDigitalPetDao(db: AppDatabase): DigitalPetDao {
        return db.digitalPetDao()
    }
    
    
    @Provides
    @Singleton
    fun provideAppCategoryRepository(
        appCategoryDao: AppCategoryDao,
        appLogger: dev.sadakat.screentimetracker.utils.logger.AppLogger
    ): AppCategoryRepository {
        return AppCategoryRepositoryImpl(appCategoryDao, appLogger)
    }
}
