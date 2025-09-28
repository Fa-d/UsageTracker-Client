package dev.sadakat.screentimetracker.shared.domain.repository

import dev.sadakat.screentimetracker.shared.domain.model.AppUsageInfo
import dev.sadakat.screentimetracker.shared.domain.model.DashboardData
import dev.sadakat.screentimetracker.shared.domain.model.UsageSession
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface UsageRepository {
    suspend fun getDashboardData(): Flow<DashboardData>
    suspend fun getAppUsageForDate(date: LocalDate): Flow<List<AppUsageInfo>>
    suspend fun getUsageSessionsForDate(date: LocalDate): Flow<List<UsageSession>>
    suspend fun getTotalScreenTimeForPeriod(startDate: LocalDate, endDate: LocalDate): Long
    suspend fun getPickupsForDate(date: LocalDate): Int
    suspend fun recordUsageSession(session: UsageSession)
    suspend fun calculateWellnessScore(): Float
}