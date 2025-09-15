package dev.sadakat.screentimetracker.di

import android.app.Application
import androidx.room.Room
import dev.sadakat.screentimetracker.core.database.ScreenTimeDatabase
import dev.sadakat.screentimetracker.core.database.dao.*
import dev.sadakat.screentimetracker.core.database.repository.UserPreferencesRepository
import dev.sadakat.screentimetracker.domain.tracking.repository.TrackerRepositoryImpl
import dev.sadakat.screentimetracker.domain.tracking.repository.AppCategoryRepositoryImpl
import dev.sadakat.screentimetracker.domain.tracking.repository.TrackerRepository
import dev.sadakat.screentimetracker.domain.tracking.repository.AppCategoryRepository
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
    fun provideScreenTimeDatabase(app: Application): ScreenTimeDatabase {
        return Room.databaseBuilder(
            app,
            ScreenTimeDatabase::class.java,
            "screen_time_database"
        )
            .fallbackToDestructiveMigration() // For now, use destructive migration
        .build()
    }

    @Provides
    @Singleton
    fun provideScreenUnlockDao(db: ScreenTimeDatabase): ScreenUnlockDao {
        return db.screenUnlockDao()
    }

    @Provides
    @Singleton
    fun provideAppUsageDao(db: ScreenTimeDatabase): AppUsageDao {
        return db.appUsageDao()
    }

    @Provides
    @Singleton
    fun provideAppSessionDao(db: ScreenTimeDatabase): AppSessionDao { // Provide AppSessionDao
        return db.appSessionDao()
    }

    @Provides
    @Singleton
    fun provideTrackerRepository(db: ScreenTimeDatabase): TrackerRepository {
         // TrackerRepositoryImpl now gets ScreenTimeDatabase and internally accesses db.appSessionDao()
        return TrackerRepositoryImpl(db)
    }

    @Provides
    @Singleton
    fun provideDailyAppSummaryDao(db: ScreenTimeDatabase): DailyAppSummaryDao {
        return db.dailyAppSummaryDao()
    }

    @Provides
    @Singleton
    fun provideDailyScreenUnlockSummaryDao(db: ScreenTimeDatabase): DailyScreenUnlockSummaryDao {
        return db.dailyScreenUnlockSummaryDao()
    }

    @Provides
    @Singleton
    fun provideLimitedAppDao(db: ScreenTimeDatabase): LimitedAppDao { // Provide LimitedAppDao
        return db.limitedAppDao()
    }

    @Provides
    @Singleton
    fun provideAchievementDao(db: ScreenTimeDatabase): AchievementDao {
        return db.achievementDao()
    }

    @Provides
    @Singleton
    fun provideWellnessScoreDao(db: ScreenTimeDatabase): WellnessScoreDao {
        return db.wellnessScoreDao()
    }

    @Provides
    @Singleton
    fun provideUserGoalDao(db: ScreenTimeDatabase): UserGoalDao {
        return db.userGoalDao()
    }

    @Provides
    @Singleton
    fun provideChallengeDao(db: ScreenTimeDatabase): ChallengeDao {
        return db.challengeDao()
    }

    @Provides
    @Singleton
    fun provideFocusSessionDao(db: ScreenTimeDatabase): FocusSessionDao {
        return db.focusSessionDao()
    }

    @Provides
    @Singleton
    fun provideHabitTrackerDao(db: ScreenTimeDatabase): HabitTrackerDao {
        return db.habitTrackerDao()
    }

    @Provides
    @Singleton
    fun provideTimeRestrictionDao(db: ScreenTimeDatabase): TimeRestrictionDao {
        return db.timeRestrictionDao()
    }

    @Provides
    @Singleton
    fun provideProgressiveLimitDao(db: ScreenTimeDatabase): ProgressiveLimitDao {
        return db.progressiveLimitDao()
    }

    @Provides
    @Singleton
    fun provideProgressiveMilestoneDao(db: ScreenTimeDatabase): ProgressiveMilestoneDao {
        return db.progressiveMilestoneDao()
    }

    @Provides
    @Singleton
    fun provideUserPreferencesDao(db: ScreenTimeDatabase): UserPreferencesDao {
        return db.userPreferencesDao()
    }

    @Provides
    @Singleton
    fun provideMindfulnessSessionDao(db: ScreenTimeDatabase): MindfulnessSessionDao {
        return db.mindfulnessSessionDao()
    }

    @Provides
    @Singleton
    fun providePrivacySettingsDao(db: ScreenTimeDatabase): PrivacySettingsDao {
        return db.privacySettingsDao()
    }

    @Provides
    @Singleton
    fun provideReplacementActivityDao(db: ScreenTimeDatabase): ReplacementActivityDao {
        return db.replacementActivityDao()
    }
    
    @Provides
    @Singleton
    fun provideAppCategoryDao(db: ScreenTimeDatabase): AppCategoryDao {
        return db.appCategoryDao()
    }
    
    @Provides
    @Singleton
    fun provideDigitalPetDao(db: ScreenTimeDatabase): DigitalPetDao {
        return db.digitalPetDao()
    }
    
    @Provides
    @Singleton
    fun provideUserPreferencesRepository(
        userPreferencesDao: UserPreferencesDao
    ): UserPreferencesRepository {
        return UserPreferencesRepository(userPreferencesDao)
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
