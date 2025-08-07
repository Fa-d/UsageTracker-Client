package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.PrivacySettings
import com.example.screentimetracker.data.local.PrivacySettingsDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import javax.inject.Inject

class PrivacyManagerUseCase @Inject constructor(
    private val privacySettingsDao: PrivacySettingsDao
) {
    
    fun getPrivacySettings(): Flow<PrivacySettings> {
        return privacySettingsDao.getPrivacySettings().map { 
            it ?: PrivacySettings()
        }
    }
    
    suspend fun enableStealthMode(password: String) {
        val hashedPassword = hashPassword(password)
        val currentSettings = privacySettingsDao.getPrivacySettingsSync() ?: PrivacySettings()
        privacySettingsDao.insertPrivacySettings(
            currentSettings.copy(
                isStealthModeEnabled = true,
                stealthModePassword = hashedPassword
            )
        )
    }
    
    suspend fun disableStealthMode() {
        privacySettingsDao.setStealthModeEnabled(false)
    }
    
    suspend fun verifyStealthModePassword(inputPassword: String): Boolean {
        val settings = privacySettingsDao.getPrivacySettingsSync()
        return if (settings?.isStealthModeEnabled == true) {
            val hashedInput = hashPassword(inputPassword)
            hashedInput == settings.stealthModePassword
        } else {
            true // No stealth mode enabled
        }
    }
    
    suspend fun enableGuestMode(durationMinutes: Int) {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + (durationMinutes * 60 * 1000L)
        privacySettingsDao.setGuestMode(true, startTime, endTime)
    }
    
    suspend fun disableGuestMode() {
        privacySettingsDao.setGuestMode(false, 0L, 0L)
    }
    
    suspend fun isGuestModeActive(): Boolean {
        val settings = privacySettingsDao.getPrivacySettingsSync()
        return if (settings?.isGuestModeEnabled == true) {
            System.currentTimeMillis() < settings.guestModeEndTime
        } else {
            false
        }
    }
    
    suspend fun addHiddenApp(packageName: String) {
        val settings = privacySettingsDao.getPrivacySettingsSync() ?: PrivacySettings()
        val updatedList = settings.hiddenAppsPackages.toMutableList()
        if (!updatedList.contains(packageName)) {
            updatedList.add(packageName)
            privacySettingsDao.updateHiddenApps(updatedList)
        }
    }
    
    suspend fun removeHiddenApp(packageName: String) {
        val settings = privacySettingsDao.getPrivacySettingsSync() ?: PrivacySettings()
        val updatedList = settings.hiddenAppsPackages.toMutableList()
        updatedList.remove(packageName)
        privacySettingsDao.updateHiddenApps(updatedList)
    }
    
    suspend fun addExcludedApp(packageName: String) {
        val settings = privacySettingsDao.getPrivacySettingsSync() ?: PrivacySettings()
        val updatedList = settings.excludedAppsFromTracking.toMutableList()
        if (!updatedList.contains(packageName)) {
            updatedList.add(packageName)
            privacySettingsDao.updateExcludedApps(updatedList)
        }
    }
    
    suspend fun removeExcludedApp(packageName: String) {
        val settings = privacySettingsDao.getPrivacySettingsSync() ?: PrivacySettings()
        val updatedList = settings.excludedAppsFromTracking.toMutableList()
        updatedList.remove(packageName)
        privacySettingsDao.updateExcludedApps(updatedList)
    }
    
    suspend fun isAppHidden(packageName: String): Boolean {
        val settings = privacySettingsDao.getPrivacySettingsSync()
        return settings?.hiddenAppsPackages?.contains(packageName) ?: false
    }
    
    suspend fun isAppExcludedFromTracking(packageName: String): Boolean {
        val settings = privacySettingsDao.getPrivacySettingsSync()
        return settings?.excludedAppsFromTracking?.contains(packageName) ?: false
    }
    
    suspend fun updateLastExportTime() {
        privacySettingsDao.updateLastExportTime(System.currentTimeMillis())
    }
    
    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(bytes)
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }
}