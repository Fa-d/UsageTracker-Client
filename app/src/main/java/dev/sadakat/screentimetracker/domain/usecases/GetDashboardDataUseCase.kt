package dev.sadakat.screentimetracker.domain.usecases

import dev.sadakat.screentimetracker.data.local.dto.AppLastOpenedData
import dev.sadakat.screentimetracker.data.local.dto.AppSessionDataAggregate // Needed
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfTodayMillis = calendar.timeInMillis
        
        // Calculate end of today (23:59:59.999) to capture all sessions for today
        val endCalendar = Calendar.getInstance()
        endCalendar.set(Calendar.HOUR_OF_DAY, 23)
        endCalendar.set(Calendar.MINUTE, 59)
        endCalendar.set(Calendar.SECOND, 59)
        endCalendar.set(Calendar.MILLISECOND, 999)
        val endOfTodayMillis = endCalendar.timeInMillis

        return combine(
            repository.getUnlockCountForDayFlow(startOfTodayMillis, endOfTodayMillis),
            repository.getAggregatedSessionDataForDayFlow(startOfTodayMillis, endOfTodayMillis),
            repository.getLastOpenedTimestampsForAppsInRangeFlow(startOfTodayMillis, endOfTodayMillis)
        ) { screenUnlocks, aggregatedSessionsToday, lastOpenedTimestamps ->
            val lastOpenedMap = lastOpenedTimestamps.associateBy { it.packageName }
            val todaysAppDetails = aggregatedSessionsToday.map { aggregate ->
                TodaysAppData(
                    packageName = aggregate.packageName,
                    totalDurationMillis = aggregate.totalDuration,
                    sessionCount = aggregate.sessionCount,
                    lastOpenedTimestamp = lastOpenedMap[aggregate.packageName]?.lastOpenedTimestamp ?: 0L
                )
            }
            val totalScreenTime = aggregatedSessionsToday.sumOf { it.totalDuration }

            DashboardData(
                totalScreenUnlocksToday = screenUnlocks,
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
