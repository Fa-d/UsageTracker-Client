package dev.sadakat.screentimetracker.shared.platform

import dev.sadakat.screentimetracker.shared.domain.model.AppUsageInfo
import dev.sadakat.screentimetracker.shared.domain.model.UsageSession
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

expect interface UsageTracker {
    suspend fun getAppUsageForDate(date: LocalDate): Flow<List<AppUsageInfo>>
    suspend fun getCurrentUsageSessions(): Flow<List<UsageSession>>
    suspend fun getPickupsForDate(date: LocalDate): Int
    suspend fun isUsageAccessPermissionGranted(): Boolean
    suspend fun requestUsageAccessPermission()
}