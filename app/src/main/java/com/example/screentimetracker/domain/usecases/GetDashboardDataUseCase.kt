package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.AppLastOpenedData
import com.example.screentimetracker.data.local.AppSessionDataAggregate // Needed
import com.example.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import javax.inject.Inject

// New DTO for use case output, representing today's app-specific aggregated data
data class TodaysAppData(
    val packageName: String,
    val totalDurationMillis: Long,
    val sessionCount: Int, // This is effectively open count from sessions
    val lastOpenedTimestamp: Long // Added lastOpenedTimestamp
)

// DTO from GetDashboardDataUseCase, representing all data needed for "today's" view
data class DashboardData(
    val totalScreenUnlocksToday: Int,
    val appDetailsToday: List<TodaysAppData>, // Changed from List<AppOpenData>
    val totalScreenTimeFromSessionsToday: Long
)

class GetDashboardDataUseCase @Inject constructor(
    private val repository: TrackerRepository
) {
    operator fun invoke(): Flow<DashboardData> {
        val calendar = Calendar.getInstance()
        // For "today", we want data from the start of today up to the current moment.
        val endOfTodayMillis = calendar.timeInMillis // Current time as end for "today"

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfTodayMillis = calendar.timeInMillis

        val screenUnlocksFlow: Flow<Int> = repository.getUnlockCountForDay(startOfTodayMillis, endOfTodayMillis)
        // Use getAggregatedSessionDataForDay for today's app details
        val aggregatedSessionsTodayFlow: Flow<List<AppSessionDataAggregate>> = repository.getAggregatedSessionDataForDay(startOfTodayMillis, endOfTodayMillis)
        val lastOpenedTimestampsFlow: Flow<List<AppLastOpenedData>> = repository.getLastOpenedTimestampsForAppsInRange(startOfTodayMillis, endOfTodayMillis)

        return combine(screenUnlocksFlow, aggregatedSessionsTodayFlow, lastOpenedTimestampsFlow) { unlocks, sessionAggregates, lastOpenedTimestamps ->
            val lastOpenedMap = lastOpenedTimestamps.associateBy { it.packageName }
            val todaysAppDetails = sessionAggregates.map { aggregate ->
                TodaysAppData(
                    packageName = aggregate.packageName,
                    totalDurationMillis = aggregate.totalDuration,
                    sessionCount = aggregate.sessionCount,
                    lastOpenedTimestamp = lastOpenedMap[aggregate.packageName]?.lastOpenedTimestamp ?: 0L
                )
            }
            val totalScreenTime = sessionAggregates.sumOf { it.totalDuration }

            DashboardData(
                totalScreenUnlocksToday = unlocks,
                appDetailsToday = todaysAppDetails,
                totalScreenTimeFromSessionsToday = totalScreenTime
            )
        }
    }

    // This helper might not be strictly needed by external classes anymore if invoke is self-contained for "today"
    internal fun getStartOfTodayMillis(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
