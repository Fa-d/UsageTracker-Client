package dev.sadakat.screentimetracker.shared.platform

import dev.sadakat.screentimetracker.shared.domain.model.AppUsageInfo
import dev.sadakat.screentimetracker.shared.domain.model.UsageSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate

actual interface UsageTracker {
    actual suspend fun getAppUsageForDate(date: LocalDate): Flow<List<AppUsageInfo>>
    actual suspend fun getCurrentUsageSessions(): Flow<List<UsageSession>>
    actual suspend fun getPickupsForDate(date: LocalDate): Int
    actual suspend fun isUsageAccessPermissionGranted(): Boolean
    actual suspend fun requestUsageAccessPermission()
}

class DesktopUsageTracker : UsageTracker {

    override suspend fun getAppUsageForDate(date: LocalDate): Flow<List<AppUsageInfo>> = flow {
        // Desktop implementation would use platform-specific APIs
        // Windows: WMI queries, macOS: Activity Monitor APIs, Linux: /proc filesystem
        emit(emptyList())
    }

    override suspend fun getCurrentUsageSessions(): Flow<List<UsageSession>> = flow {
        // Desktop implementation for active window tracking
        emit(emptyList())
    }

    override suspend fun getPickupsForDate(date: LocalDate): Int {
        // Desktop concept of "pickups" doesn't directly apply
        return 0
    }

    override suspend fun isUsageAccessPermissionGranted(): Boolean {
        // Desktop platforms have different permission models
        return true
    }

    override suspend fun requestUsageAccessPermission() {
        // Desktop implementation would show permission dialog or instructions
    }
}