package com.example.screentimetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "privacy_settings")
data class PrivacySettings(
    @PrimaryKey
    val id: Int = 1, // Single row for privacy settings
    val isStealthModeEnabled: Boolean = false,
    val stealthModePassword: String = "",
    val isGuestModeEnabled: Boolean = false,
    val guestModeStartTime: Long = 0L,
    val guestModeEndTime: Long = 0L,
    val hiddenAppsPackages: List<String> = emptyList(),
    val excludedAppsFromTracking: List<String> = emptyList(),
    val dataExportEnabled: Boolean = true,
    val lastDataExportTime: Long = 0L
)