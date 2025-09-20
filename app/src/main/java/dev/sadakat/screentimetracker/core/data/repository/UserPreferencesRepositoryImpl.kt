package dev.sadakat.screentimetracker.core.data.repository

import dev.sadakat.screentimetracker.core.data.local.dao.UserPreferencesDao
import dev.sadakat.screentimetracker.core.data.mapper.UserPreferencesDataMapper
import dev.sadakat.screentimetracker.core.domain.repository.AppearanceSettings
import dev.sadakat.screentimetracker.core.domain.repository.FocusSessionSettings
import dev.sadakat.screentimetracker.core.domain.repository.NotificationSettings
import dev.sadakat.screentimetracker.core.domain.repository.PreferencesBackup
import dev.sadakat.screentimetracker.core.domain.repository.PrivacySettings
import dev.sadakat.screentimetracker.core.domain.repository.UserPreferencesRepository
import dev.sadakat.screentimetracker.core.domain.service.InsightPreferences
import dev.sadakat.screentimetracker.core.domain.service.UserBehaviorProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

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
        val currentEntity = userPreferencesDao.getUserPreferencesSync()
            ?: dataMapper.createDefaultEntity()

        val updatedEntity = dataMapper.updateEntityWithUserBehaviorProfile(currentEntity, profile)

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

        val updatedEntity = dataMapper.updateEntityWithPrivacySettings(currentEntity, settings)

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
        return dataMapper.mapToPreferencesBackup(entity, "1.0.0") // Placeholder version
    }

    override suspend fun importPreferences(backup: PreferencesBackup) {
        val currentEntity = userPreferencesDao.getUserPreferencesSync()
            ?: dataMapper.createDefaultEntity()

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
        updatedEntity = dataMapper.updateEntityWithPrivacySettings(
            updatedEntity, backup.privacySettings
        )
        updatedEntity = dataMapper.updateEntityWithUserBehaviorProfile(
            updatedEntity, backup.userBehaviorProfile
        )

        if (dataMapper.validateEntity(updatedEntity)) {
            userPreferencesDao.insertOrUpdateUserPreferences(updatedEntity)
        }
    }

    override fun getUserPreferences(): Flow<dev.sadakat.screentimetracker.core.data.local.entities.UserPreferences?> {
        return userPreferencesDao.getUserPreferences()
    }

    override suspend fun updateAIFeaturesEnabled(enabled: Boolean) {
        val currentEntity = userPreferencesDao.getUserPreferencesSync()
            ?: dataMapper.createDefaultEntity()
        val updatedEntity = currentEntity.copy(aiFeaturesEnabled = enabled)
        userPreferencesDao.insertOrUpdateUserPreferences(updatedEntity)
    }

    override suspend fun updateAIModuleDownloaded(downloaded: Boolean) {
        val currentEntity = userPreferencesDao.getUserPreferencesSync()
            ?: dataMapper.createDefaultEntity()
        val updatedEntity = currentEntity.copy(aiModuleDownloaded = downloaded)
        userPreferencesDao.insertOrUpdateUserPreferences(updatedEntity)
    }

    override suspend fun updateAIInsightsEnabled(enabled: Boolean) {
        val currentEntity = userPreferencesDao.getUserPreferencesSync()
            ?: dataMapper.createDefaultEntity()
        val updatedEntity = currentEntity.copy(aiInsightsEnabled = enabled)
        userPreferencesDao.insertOrUpdateUserPreferences(updatedEntity)
    }

    override suspend fun updateAIGoalRecommendationsEnabled(enabled: Boolean) {
        val currentEntity = userPreferencesDao.getUserPreferencesSync()
            ?: dataMapper.createDefaultEntity()
        val updatedEntity = currentEntity.copy(aiGoalRecommendationsEnabled = enabled)
        userPreferencesDao.insertOrUpdateUserPreferences(updatedEntity)
    }

    override suspend fun updateAIPredictiveCoachingEnabled(enabled: Boolean) {
        val currentEntity = userPreferencesDao.getUserPreferencesSync()
            ?: dataMapper.createDefaultEntity()
        val updatedEntity = currentEntity.copy(aiPredictiveCoachingEnabled = enabled)
        userPreferencesDao.insertOrUpdateUserPreferences(updatedEntity)
    }

    override suspend fun updateAIUsagePredictionsEnabled(enabled: Boolean) {
        val currentEntity = userPreferencesDao.getUserPreferencesSync()
            ?: dataMapper.createDefaultEntity()
        val updatedEntity = currentEntity.copy(aiUsagePredictionsEnabled = enabled)
        userPreferencesDao.insertOrUpdateUserPreferences(updatedEntity)
    }
}
