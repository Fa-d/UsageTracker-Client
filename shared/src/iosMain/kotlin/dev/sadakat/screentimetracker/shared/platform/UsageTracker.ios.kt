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

class IOSUsageTracker : UsageTracker {

    override suspend fun getAppUsageForDate(date: LocalDate): Flow<List<AppUsageInfo>> = flow {
        // iOS implementation using ScreenTime framework would go here
        // This requires user consent and is limited compared to Android
        emit(emptyList())
    }

    override suspend fun getCurrentUsageSessions(): Flow<List<UsageSession>> = flow {
        // iOS doesn't provide real-time usage sessions
        emit(emptyList())
    }

    override suspend fun getPickupsForDate(date: LocalDate): Int {
        // iOS ScreenTime API doesn't provide pickup count
        return 0
    }

    override suspend fun isUsageAccessPermissionGranted(): Boolean {
        // On iOS, this would check ScreenTime authorization status
        return false
    }

    override suspend fun requestUsageAccessPermission() {
        // iOS would request ScreenTime authorization here
        // This requires native iOS implementation
    }
}