package dev.sadakat.screentimetracker.domain.analytics.repository

import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    suspend fun getAppUsageInsights(days: Int = 7): AppUsageInsights
    suspend fun getCategoryDistribution(days: Int = 7): List<CategoryUsage>
    suspend fun getUsagePatterns(days: Int = 30): UsagePatterns
    suspend fun getProductivityReport(days: Int = 7): ProductivityReport
    suspend fun getSessionAnalytics(days: Int = 7): List<SessionAnalytic>
    suspend fun getWeeklyComparison(): WeeklyComparison
    suspend fun getMonthlyTrends(): MonthlyTrends
    suspend fun getUserBehaviorInsights(days: Int = 30): UserBehaviorInsights

    fun getAppUsageFlow(appPackage: String, days: Int = 30): Flow<List<DailyAppUsage>>
    fun getTotalUsageFlow(days: Int = 30): Flow<List<DailyUsage>>
    fun getPickupAnalyticsFlow(days: Int = 7): Flow<PickupAnalytics>
}

data class AppUsageInsights(
    val totalScreenTime: Long,
    val averageSessionDuration: Long,
    val totalSessions: Int,
    val totalPickups: Int,
    val mostUsedApp: AppUsageDetail,
    val longestSession: AppSession,
    val topApps: List<AppUsageDetail>
)

data class AppUsageDetail(
    val packageName: String,
    val appName: String,
    val totalUsageTime: Long,
    val sessionCount: Int,
    val averageSessionDuration: Long,
    val usagePercentage: Float
)

data class CategoryUsage(
    val categoryId: String,
    val categoryName: String,
    val totalUsageTime: Long,
    val appCount: Int,
    val usagePercentage: Float,
    val topAppsInCategory: List<AppUsageDetail>
)

data class UsagePatterns(
    val peakUsageHours: List<HourlyUsage>,
    val weekdayVsWeekendUsage: WeekdayWeekendComparison,
    val averageFirstPickupTime: String,
    val averageLastUsageTime: String,
    val typicalSessionPattern: List<SessionPattern>
)

data class HourlyUsage(
    val hour: Int,
    val totalMinutes: Long,
    val sessionCount: Int,
    val averageIntensity: Float
)

data class WeekdayWeekendComparison(
    val weekdayAverage: Long,
    val weekendAverage: Long,
    val difference: Long,
    val differencePercentage: Float
)

data class SessionPattern(
    val timeRange: String,
    val averageDuration: Long,
    val commonApps: List<String>,
    val intensity: SessionIntensity
)

enum class SessionIntensity {
    LOW, MODERATE, HIGH, INTENSIVE
}

data class ProductivityReport(
    val productiveTime: Long,
    val distractiveTime: Long,
    val neutralTime: Long,
    val productivityScore: Float,
    val mostProductiveHours: List<Int>,
    val leastProductiveHours: List<Int>,
    val productiveApps: List<AppUsageDetail>,
    val distractiveApps: List<AppUsageDetail>
)

data class SessionAnalytic(
    val date: Long,
    val sessionId: String,
    val appPackage: String,
    val startTime: Long,
    val endTime: Long,
    val duration: Long,
    val isProductive: Boolean,
    val contextType: SessionContextType
)

enum class SessionContextType {
    WORK, LEISURE, COMMUNICATION, ENTERTAINMENT, LEARNING, OTHER
}

data class AppSession(
    val packageName: String,
    val appName: String,
    val duration: Long,
    val startTime: Long,
    val endTime: Long
)

data class WeeklyComparison(
    val currentWeekTotal: Long,
    val previousWeekTotal: Long,
    val changeAmount: Long,
    val changePercentage: Float,
    val trend: TrendDirection,
    val dailyComparison: List<DailyComparison>
)

data class DailyComparison(
    val dayOfWeek: String,
    val currentWeek: Long,
    val previousWeek: Long,
    val change: Long
)

enum class TrendDirection {
    INCREASING, DECREASING, STABLE
}

data class MonthlyTrends(
    val monthlyTotals: List<MonthlyTotal>,
    val averageGrowth: Float,
    val peakUsageMonth: String,
    val lowestUsageMonth: String,
    val trends: List<TrendAnalysis>
)

data class MonthlyTotal(
    val month: String,
    val year: Int,
    val totalUsage: Long,
    val averageDaily: Long,
    val topApp: String
)

data class TrendAnalysis(
    val metric: String,
    val direction: TrendDirection,
    val significance: Float,
    val description: String
)

data class UserBehaviorInsights(
    val habitualApps: List<String>,
    val impulsiveUsagePattern: List<ImpulsiveUsage>,
    val focusScore: Float,
    val multitaskingTendency: Float,
    val digitalWellbeingScore: Float,
    val behaviorRecommendations: List<String>
)

data class ImpulsiveUsage(
    val appPackage: String,
    val frequency: Int,
    val averageDuration: Long,
    val typicalTriggerTime: String
)

data class DailyAppUsage(
    val date: Long,
    val packageName: String,
    val totalUsage: Long,
    val sessionCount: Int
)

data class DailyUsage(
    val date: Long,
    val totalScreenTime: Long,
    val totalSessions: Int,
    val totalPickups: Int
)

data class PickupAnalytics(
    val averagePickupsPerDay: Float,
    val peakPickupHours: List<Int>,
    val averageTimeBetweenPickups: Long,
    val longestBreakBetweenPickups: Long,
    val pickupTrend: TrendDirection
)