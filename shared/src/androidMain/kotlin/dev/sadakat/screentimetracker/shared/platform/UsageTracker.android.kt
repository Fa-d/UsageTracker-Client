package dev.sadakat.screentimetracker.shared.platform

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import dev.sadakat.screentimetracker.shared.domain.model.AppUsageInfo
import dev.sadakat.screentimetracker.shared.domain.model.UsageSession
import dev.sadakat.screentimetracker.shared.utils.TimeFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.DatePeriod

actual interface UsageTracker {
    actual suspend fun getAppUsageForDate(date: LocalDate): Flow<List<AppUsageInfo>>
    actual suspend fun getCurrentUsageSessions(): Flow<List<UsageSession>>
    actual suspend fun getPickupsForDate(date: LocalDate): Int
    actual suspend fun isUsageAccessPermissionGranted(): Boolean
    actual suspend fun requestUsageAccessPermission()
}

class AndroidUsageTracker(
    private val context: Context
) : UsageTracker {

    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val packageManager = context.packageManager

    override suspend fun getAppUsageForDate(date: LocalDate): Flow<List<AppUsageInfo>> = flow {
        if (!isUsageAccessPermissionGranted()) {
            emit(emptyList())
            return@flow
        }

        val startTime = date.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val endTime = startTime + (24 * 60 * 60 * 1000) // Add 24 hours in milliseconds

        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        val appUsageList = usageStats
            .filter { it.totalTimeInForeground > 0 }
            .map { usageStats ->
                val appName = try {
                    packageManager.getApplicationLabel(
                        packageManager.getApplicationInfo(usageStats.packageName, 0)
                    ).toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    usageStats.packageName
                }

                AppUsageInfo(
                    packageName = usageStats.packageName,
                    appName = appName,
                    usageTimeMs = usageStats.totalTimeInForeground,
                    usageTimeFormatted = TimeFormatter.formatDuration(usageStats.totalTimeInForeground),
                    lastUsed = usageStats.lastTimeUsed
                )
            }
            .sortedByDescending { it.usageTimeMs }

        emit(appUsageList)
    }

    override suspend fun getCurrentUsageSessions(): Flow<List<UsageSession>> = flow {
        // Implementation for getting current usage sessions
        emit(emptyList())
    }

    override suspend fun getPickupsForDate(date: LocalDate): Int {
        // Android doesn't directly provide pickup count, so we estimate based on app launch events
        return 0 // Placeholder implementation
    }

    override suspend fun isUsageAccessPermissionGranted(): Boolean {
        val startTime = System.currentTimeMillis() - 1000 * 60
        val endTime = System.currentTimeMillis()

        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        return usageStats.isNotEmpty()
    }

    override suspend fun requestUsageAccessPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}