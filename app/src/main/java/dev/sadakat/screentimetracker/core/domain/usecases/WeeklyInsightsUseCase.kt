package dev.sadakat.screentimetracker.core.domain.usecases

// REMOVED: import dev.sadakat.screentimetracker.core.data.local.dto.AppSessionDataAggregate
import dev.sadakat.screentimetracker.core.data.local.entities.DailyAppSummary
import dev.sadakat.screentimetracker.core.domain.categorization.AppCategorizer
import dev.sadakat.screentimetracker.core.domain.model.WellnessScore as DomainWellnessScore
import dev.sadakat.screentimetracker.core.domain.repository.TrackerRepository
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import dev.sadakat.screentimetracker.utils.ui.AppNotificationManager
import kotlinx.coroutines.flow.first
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeeklyInsightsUseCase @Inject constructor(
    private val repository: TrackerRepository,
    private val notificationManager: AppNotificationManager,
    private val appLogger: AppLogger,
    private val appCategorizer: AppCategorizer
) {
    companion object {
        private const val TAG = "WeeklyInsightsUseCase"
    }

    suspend fun generateWeeklyReport(): WeeklyReport {
        return try {
            val weekStart = getWeekStart(System.currentTimeMillis())
            val weekEnd = weekStart + TimeUnit.DAYS.toMillis(7)
            
            // Get basic usage data
            val sessionData = repository.getAggregatedSessionDataForDayFlow(weekStart, weekEnd).first()
            val appSummaries = repository.getDailyAppSummaries(weekStart, weekEnd).first()
            val unlockCounts = repository.getUnlockCountForDayFlow(weekStart, weekEnd).first()
            val wellnessScores = repository.getAllWellnessScores().first()
                .filter { it.date >= weekStart && it.date < weekEnd }
            
            // Calculate insights
            val totalScreenTime = sessionData.sumOf { it.totalDuration }
            val averageDailyScreenTime = totalScreenTime / 7
            val mostUsedApps = appSummaries
                .groupBy { it.packageName }
                .map { (packageName, summaries) ->
                    AppUsageInsight(
                        packageName = packageName,
                        totalTimeMillis = summaries.sumOf { it.totalDurationMillis },
                        sessionsCount = summaries.sumOf { it.openCount },
                        averageDailyTime = summaries.sumOf { it.totalDurationMillis } / 7
                    )
                }
                .sortedByDescending { it.totalTimeMillis }
                .take(5)
            
            val averageWellnessScore = if (wellnessScores.isNotEmpty()) {
                wellnessScores.map { it.overall }.average().toInt()
            } else 0
            
            val totalUnlocks = unlockCounts
            val averageUnlocksPerDay = totalUnlocks / 7
            
            // Detect patterns and trends
            val insights = generateInsights(
                totalScreenTime, averageDailyScreenTime, mostUsedApps, 
                averageWellnessScore, averageUnlocksPerDay
            )
            
            WeeklyReport(
                weekStart = weekStart,
                weekEnd = weekEnd,
                totalScreenTimeMillis = totalScreenTime,
                averageDailyScreenTimeMillis = averageDailyScreenTime,
                totalUnlocks = totalUnlocks,
                averageUnlocksPerDay = averageUnlocksPerDay,
                averageWellnessScore = averageWellnessScore,
                topApps = mostUsedApps,
                insights = insights,
                generatedAt = System.currentTimeMillis()
            )
            
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to generate weekly report", e)
            WeeklyReport.empty()
        }
    }

    suspend fun sendWeeklyReportNotification() {
        try {
            val report = generateWeeklyReport()
            val screenTimeHours = TimeUnit.MILLISECONDS.toHours(report.totalScreenTimeMillis)
            val goalsAchieved = calculateGoalsAchieved()
            val totalGoals = calculateTotalGoals()
            
            notificationManager.showWeeklyReport(
                report.totalScreenTimeMillis,
                goalsAchieved,
                totalGoals
            )
            
            appLogger.i(TAG, "Weekly report notification sent")
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to send weekly report notification", e)
        }
    }

    suspend fun getProductivityHours(): List<ProductivityHour> {
        return try {
            val weekStart = getWeekStart(System.currentTimeMillis())
            val weekEnd = weekStart + TimeUnit.DAYS.toMillis(7)
            
            val sessions = repository.getAllSessionsInRange(weekStart, weekEnd).first()
            val hourlyUsage = mutableMapOf<Int, Long>()
            
            sessions.forEach { session ->
                val calendar = Calendar.getInstance().apply { timeInMillis = session.startTimeMillis }
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                hourlyUsage[hour] = (hourlyUsage[hour] ?: 0) + session.durationMillis
            }
            
            (0..23).map { hour ->
                val usageTime = hourlyUsage[hour] ?: 0
                ProductivityHour(
                    hour = hour,
                    usageTimeMillis = usageTime,
                    productivity = calculateProductivityScore(hour, usageTime)
                )
            }
            
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to get productivity hours", e)
            emptyList()
        }
    }

    suspend fun getAppCategoryInsights(): List<CategoryInsight> {
        return try {
            val weekStart = getWeekStart(System.currentTimeMillis())
            val weekEnd = weekStart + TimeUnit.DAYS.toMillis(7)
            val appSummaries = repository.getDailyAppSummaries(weekStart, weekEnd).first()
            
            // Group apps by their dynamically determined categories
            val categoryUsageMap = mutableMapOf<String, Long>()
            
            // Process each app and categorize it dynamically
            for (appSummary in appSummaries) {
                val category = appCategorizer.categorizeApp(appSummary.packageName)
                categoryUsageMap[category] = categoryUsageMap.getOrDefault(category, 0L) + appSummary.totalDurationMillis
            }
            
            val totalUsageMillis = appSummaries.sumOf { it.totalDurationMillis }.toFloat()
            
            // Convert to CategoryInsight objects
            categoryUsageMap.map { (categoryName, totalTimeMillis) ->
                CategoryInsight(
                    categoryName = categoryName,
                    totalTimeMillis = totalTimeMillis,
                    percentageOfTotal = if (totalUsageMillis > 0) {
                        (totalTimeMillis.toFloat() / totalUsageMillis) * 100
                    } else 0f
                )
            }.sortedByDescending { it.totalTimeMillis }
            
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to get category insights", e)
            emptyList()
        }
    }

    private suspend fun calculateGoalsAchieved(): Int {
        return try {
            val activeGoals = repository.getActiveGoals().first()
            activeGoals.count { goal ->
                goal.currentProgress >= goal.targetValue
            }
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun calculateTotalGoals(): Int {
        return try {
            repository.getActiveGoals().first().size
        } catch (e: Exception) {
            0
        }
    }

    private fun generateInsights(
        totalScreenTime: Long,
        avgDailyScreenTime: Long,
        topApps: List<AppUsageInsight>,
        avgWellnessScore: Int,
        avgUnlocksPerDay: Int
    ): List<String> {
        val insights = mutableListOf<String>()
        
        // Screen time insights
        val avgDailyHours = TimeUnit.MILLISECONDS.toHours(avgDailyScreenTime)
        when {
            avgDailyHours < 2 -> insights.add("🌟 Great job! Your daily screen time is under 2 hours.")
            avgDailyHours < 4 -> insights.add("👍 Your screen time is moderate. Consider reducing further for better wellness.")
            avgDailyHours < 6 -> insights.add("⚠️ Your screen time is above recommended levels. Try setting daily limits.")
            else -> insights.add("🚨 High screen time detected. Consider implementing stricter limits.")
        }
        
        // Most used app insight
        if (topApps.isNotEmpty()) {
            val topApp = topApps.first()
            val topAppHours = TimeUnit.MILLISECONDS.toHours(topApp.totalTimeMillis)
            insights.add("📱 Your most used app consumed ${topAppHours}h this week.")
        }
        
        // Wellness score insight
        when {
            avgWellnessScore >= 75 -> insights.add("🏆 Excellent wellness score! Keep up the healthy digital habits.")
            avgWellnessScore >= 50 -> insights.add("💪 Good wellness score. Small improvements can make it even better.")
            else -> insights.add("🎯 Your wellness score has room for improvement. Try the weekly challenges!")
        }
        
        // Unlock frequency insight
        when {
            avgUnlocksPerDay < 30 -> insights.add("🔒 Low unlock frequency shows good phone discipline.")
            avgUnlocksPerDay < 60 -> insights.add("📲 Moderate unlock frequency. Consider longer focused sessions.")
            else -> insights.add("⚡ High unlock frequency suggests frequent interruptions. Try focus mode.")
        }
        
        return insights
    }

    private fun calculateProductivityScore(hour: Int, usageTime: Long): Float {
        // Simple productivity scoring based on typical work hours
        return when (hour) {
            in 9..17 -> { // Work hours
                val maxProductiveUsage = TimeUnit.HOURS.toMillis(1) // 1 hour per work hour is productive
                (usageTime.toFloat() / maxProductiveUsage).coerceAtMost(1.0f)
            }
            in 22..23, in 0..6 -> { // Late night/early morning
                0.1f // Low productivity score for late night usage
            }
            else -> 0.5f // Neutral score for other hours
        }
    }

    private fun getWeekStart(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    data class WeeklyReport(
        val weekStart: Long,
        val weekEnd: Long,
        val totalScreenTimeMillis: Long,
        val averageDailyScreenTimeMillis: Long,
        val totalUnlocks: Int,
        val averageUnlocksPerDay: Int,
        val averageWellnessScore: Int,
        val topApps: List<AppUsageInsight>,
        val insights: List<String>,
        val generatedAt: Long
    ) {
        companion object {
            fun empty() = WeeklyReport(
                weekStart = 0,
                weekEnd = 0,
                totalScreenTimeMillis = 0,
                averageDailyScreenTimeMillis = 0,
                totalUnlocks = 0,
                averageUnlocksPerDay = 0,
                averageWellnessScore = 0,
                topApps = emptyList(),
                insights = listOf("Unable to generate insights. Please check back later."),
                generatedAt = System.currentTimeMillis()
            )
        }
    }

    data class AppUsageInsight(
        val packageName: String,
        val totalTimeMillis: Long,
        val sessionsCount: Int,
        val averageDailyTime: Long
    )

    data class ProductivityHour(
        val hour: Int,
        val usageTimeMillis: Long,
        val productivity: Float
    )

    data class CategoryInsight(
        val categoryName: String,
        val totalTimeMillis: Long,
        val percentageOfTotal: Float
    )
}