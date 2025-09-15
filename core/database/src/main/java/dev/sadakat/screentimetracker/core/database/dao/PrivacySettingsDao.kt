package dev.sadakat.screentimetracker.core.database.dao

import androidx.room.*
import dev.sadakat.screentimetracker.core.database.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PrivacySettingsDao {
    
    @Query("SELECT * FROM privacy_settings WHERE id = 1")
    fun getPrivacySettings(): Flow<PrivacySettings?>
    
    @Query("SELECT * FROM privacy_settings WHERE id = 1")
    suspend fun getPrivacySettingsSync(): PrivacySettings?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrivacySettings(privacySettings: PrivacySettings)
    
    @Update
    suspend fun updatePrivacySettings(privacySettings: PrivacySettings)
    
    @Query("UPDATE privacy_settings SET isStealthModeEnabled = :enabled WHERE id = 1")
    suspend fun setStealthModeEnabled(enabled: Boolean)
    
    @Query("UPDATE privacy_settings SET isGuestModeEnabled = :enabled, guestModeStartTime = :startTime, guestModeEndTime = :endTime WHERE id = 1")
    suspend fun setGuestMode(enabled: Boolean, startTime: Long, endTime: Long)
    
    @Query("UPDATE privacy_settings SET hiddenAppsPackages = :packages WHERE id = 1")
    suspend fun updateHiddenApps(packages: List<String>)
    
    @Query("UPDATE privacy_settings SET excludedAppsFromTracking = :packages WHERE id = 1")
    suspend fun updateExcludedApps(packages: List<String>)
    
    @Query("UPDATE privacy_settings SET lastDataExportTime = :time WHERE id = 1")
    suspend fun updateLastExportTime(time: Long)
}