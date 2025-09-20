package dev.sadakat.screentimetracker.core.data.repository

import dev.sadakat.screentimetracker.core.domain.repository.*
import dev.sadakat.screentimetracker.core.domain.service.*
import dev.sadakat.screentimetracker.core.data.mapper.UserPreferencesDataMapper
import dev.sadakat.screentimetracker.data.local.dao.UserPreferencesDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of UserPreferencesRepository using Room database.
 * Maps between domain preference models and single UserPreferences entity.
 */
@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val userPreferencesDao: UserPreferencesDao,
    private val dataMapper: UserPreferencesDataMapper
) : UserPreferencesRepository {

    override suspend fun getInsightPreferences(): InsightPreferences {
        val entity = userPreferencesDao.getUserPreferencesSync()
            ?: dataMapper.createDefaultEntity()
        return dataMapper.mapToInsightPreferences(entity)
    }

    override suspend fun updateInsightPreferences(preferences: InsightPreferences) {
        val currentEntity = userPreferencesDao.getUserPreferencesSync()
            ?: dataMapper.createDefaultEntity()

        val updatedEntity = dataMapper.updateEntityWithInsightPreferences(currentEntity, preferences)

        if (dataMapper.validateEntity(updatedEntity)) {
            userPreferencesDao.insertOrUpdateUserPreferences(updatedEntity)
        }
    }

    override fun observeInsightPreferences(): Flow<InsightPreferences> {
        return userPreferencesDao.getUserPreferences().map { entity ->
            val validEntity = entity ?: dataMapper.createDefaultEntity()
            dataMapper.mapToInsightPreferences(validEntity)
        }
    }

    override suspend fun getUserBehaviorProfile(): UserBehaviorProfile {
        val entity = userPreferencesDao.getUserPreferencesSync()
            ?: dataMapper.createDefaultEntity()
        return dataMapper.mapToUserBehaviorProfile(entity)
    }

    override suspend fun updateUserBehaviorProfile(profile: UserBehaviorProfile) {
        // UserBehaviorProfile updates would require tracking additional data
        // For now, we update related preferences based on the profile
        val currentEntity = userPreferencesDao.getUserPreferencesSync()
            ?: dataMapper.createDefaultEntity()

        val updatedEntity = currentEntity.copy(
            motivationalMessagesEnabled = profile.motivationLevel != MotivationLevel.LOW,
            wellnessCoachingEnabled = profile.preferredEnforcementStyle == EnforcementStyle.GENTLE_NUDGES,
            updatedAt = System.currentTimeMillis()
        )

        if (dataMapper.validateEntity(updatedEntity)) {
            userPreferencesDao.insertOrUpdateUserPreferences(updatedEntity)
        }
    }

    override suspend fun getNotificationSettings(): NotificationSettings {
        val entity = userPreferencesDao.getUserPreferencesSync()
            ?: dataMapper.createDefaultEntity()
        return dataMapper.mapToNotificationSettings(entity)
    }

    override suspend fun updateNotificationSettings(settings: NotificationSettings) {
        val currentEntity = userPreferencesDao.getUserPreferencesSync()
            ?: dataMapper.createDefaultEntity()

        val updatedEntity = dataMapper.updateEntityWithNotificationSettings(currentEntity, settings)

        if (dataMapper.validateEntity(updatedEntity)) {
            userPreferencesDao.insertOrUpdateUserPreferences(updatedEntity)
        }
    }

    override fun observeNotificationSettings(): Flow<NotificationSettings> {
        return userPreferencesDao.getUserPreferences().map { entity ->
            val validEntity = entity ?: dataMapper.createDefaultEntity()
            dataMapper.mapToNotificationSettings(validEntity)
        }
    }

    override suspend fun getAppearanceSettings(): AppearanceSettings {
        val entity = userPreferencesDao.getUserPreferencesSync()
            ?: dataMapper.createDefaultEntity()
        return dataMapper.mapToAppearanceSettings(entity)
    }

    override suspend fun updateAppearanceSettings(settings: AppearanceSettings) {
        val currentEntity = userPreferencesDao.getUserPreferencesSync()
            ?: dataMapper.createDefaultEntity()

        val updatedEntity = dataMapper.updateEntityWithAppearanceSettings(currentEntity, settings)

        if (dataMapper.validateEntity(updatedEntity)) {
            userPreferencesDao.insertOrUpdateUserPreferences(updatedEntity)
        }
    }

    override suspend fun getPrivacySettings(): PrivacySettings {
        val entity = userPreferencesDao.getUserPreferencesSync()
            ?: dataMapper.createDefaultEntity()
        return dataMapper.mapToPrivacySettings(entity)
    }

    override suspend fun updatePrivacySettings(settings: PrivacySettings) {
        val currentEntity = userPreferencesDao.getUserPreferencesSync()
            ?: dataMapper.createDefaultEntity()

        // Update related fields based on privacy settings
        val updatedEntity = currentEntity.copy(
            aiFeaturesEnabled = settings.analyticsEnabled,
            aiInsightsEnabled = settings.personalizedRecommendationsEnabled,
            aiGoalRecommendationsEnabled = settings.personalizedRecommendationsEnabled,
            updatedAt = System.currentTimeMillis()
        )

        if (dataMapper.validateEntity(updatedEntity)) {
            userPreferencesDao.insertOrUpdateUserPreferences(updatedEntity)
        }
    }

    override suspend fun getFocusSessionSettings(): FocusSessionSettings {
        val entity = userPreferencesDao.getUserPreferencesSync()
            ?: dataMapper.createDefaultEntity()
        return dataMapper.mapToFocusSessionSettings(entity)
    }

    override suspend fun updateFocusSessionSettings(settings: FocusSessionSettings) {
        val currentEntity = userPreferencesDao.getUserPreferencesSync()
            ?: dataMapper.createDefaultEntity()

        val updatedEntity = dataMapper.updateEntityWithFocusSessionSettings(currentEntity, settings)

        if (dataMapper.validateEntity(updatedEntity)) {
            userPreferencesDao.insertOrUpdateUserPreferences(updatedEntity)
        }
    }

    override suspend fun resetToDefaults() {
        val defaultEntity = dataMapper.createDefaultEntity()
        userPreferencesDao.insertOrUpdateUserPreferences(defaultEntity)
    }

    override suspend fun exportPreferences(): PreferencesBackup {
        val entity = userPreferencesDao.getUserPreferencesSync()
            ?: dataMapper.createDefaultEntity()

        return dataMapper.mapToPreferencesBackup(entity, "1.0.0") // Would get actual app version
    }

    override suspend fun importPreferences(backup: PreferencesBackup) {
        val currentEntity = userPreferencesDao.getUserPreferencesSync()
            ?: dataMapper.createDefaultEntity()

        // Apply backup settings to entity
        var updatedEntity = dataMapper.updateEntityWithInsightPreferences(
            currentEntity, backup.insightPreferences
        )
        updatedEntity = dataMapper.updateEntityWithNotificationSettings(
            updatedEntity, backup.notificationSettings
        )
        updatedEntity = dataMapper.updateEntityWithAppearanceSettings(
            updatedEntity, backup.appearanceSettings
        )
        updatedEntity = dataMapper.updateEntityWithFocusSessionSettings(
            updatedEntity, backup.focusSessionSettings
        )

        if (dataMapper.validateEntity(updatedEntity)) {
            userPreferencesDao.insertOrUpdateUserPreferences(updatedEntity)
        }
    }
}