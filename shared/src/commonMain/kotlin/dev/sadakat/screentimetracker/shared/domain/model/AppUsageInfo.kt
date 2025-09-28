package dev.sadakat.screentimetracker.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val usageTimeMs: Long,
    val usageTimeFormatted: String,
    val lastUsed: Long,
    val iconData: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AppUsageInfo) return false

        if (packageName != other.packageName) return false
        if (appName != other.appName) return false
        if (usageTimeMs != other.usageTimeMs) return false
        if (usageTimeFormatted != other.usageTimeFormatted) return false
        if (lastUsed != other.lastUsed) return false
        if (iconData != null) {
            if (other.iconData == null) return false
            if (!iconData.contentEquals(other.iconData)) return false
        } else if (other.iconData != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + appName.hashCode()
        result = 31 * result + usageTimeMs.hashCode()
        result = 31 * result + usageTimeFormatted.hashCode()
        result = 31 * result + lastUsed.hashCode()
        result = 31 * result + (iconData?.contentHashCode() ?: 0)
        return result
    }
}