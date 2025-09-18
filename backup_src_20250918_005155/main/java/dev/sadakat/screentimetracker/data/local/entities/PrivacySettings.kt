<<<<<<<< HEAD:core/database/src/main/java/dev/sadakat/screentimetracker/core/database/entities/PrivacySettings.kt
package dev.sadakat.screentimetracker.core.database.entities
========
package dev.sadakat.screentimetracker.data.local.entities
>>>>>>>> origin/detached3:backup_src_20250918_005155/main/java/dev/sadakat/screentimetracker/data/local/entities/PrivacySettings.kt

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