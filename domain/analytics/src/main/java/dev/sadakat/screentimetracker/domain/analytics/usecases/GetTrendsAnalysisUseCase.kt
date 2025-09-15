package dev.sadakat.screentimetracker.domain.analytics.usecases

import dev.sadakat.screentimetracker.domain.analytics.repository.AnalyticsRepository
import dev.sadakat.screentimetracker.domain.analytics.repository.WeeklyComparison
import dev.sadakat.screentimetracker.domain.analytics.repository.MonthlyTrends
import android.util.Log
import javax.inject.Inject

class GetTrendsAnalysisUseCase @Inject constructor(
    private val repository: AnalyticsRepository
) {
    companion object {
        private const val TAG = "GetTrendsAnalysisUseCase"
    }

    suspend fun getWeeklyComparison(): Result<WeeklyComparison> {
        return try {
            val comparison = repository.getWeeklyComparison()
            Log.i(TAG, "Retrieved weekly comparison - Change: ${comparison.changePercentage}%")
            Result.success(comparison)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get weekly comparison", e)
            Result.failure(e)
        }
    }

    suspend fun getMonthlyTrends(): Result<MonthlyTrends> {
        return try {
            val trends = repository.getMonthlyTrends()
            Log.i(TAG, "Retrieved monthly trends - Average growth: ${trends.averageGrowth}%")
            Result.success(trends)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get monthly trends", e)
            Result.failure(e)
        }
    }

    suspend fun generateTrendSummary(): Result<TrendSummary> {
        return try {
            val weeklyComparison = repository.getWeeklyComparison()
            val monthlyTrends = repository.getMonthlyTrends()

            val summary = TrendSummary(
                weeklyTrend = weeklyComparison.trend,
                weeklyChangePercentage = weeklyComparison.changePercentage,
                monthlyGrowthRate = monthlyTrends.averageGrowth,
                overallDirection = determineOverallDirection(weeklyComparison, monthlyTrends),
                keyInsights = generateKeyInsights(weeklyComparison, monthlyTrends)
            )

            Log.i(TAG, "Generated trend summary - Overall: ${summary.overallDirection}")
            Result.success(summary)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate trend summary", e)
            Result.failure(e)
        }
    }

    private fun determineOverallDirection(
        weekly: WeeklyComparison,
        monthly: MonthlyTrends
    ): OverallTrendDirection {
        val weeklyWeight = 0.7f
        val monthlyWeight = 0.3f

        val weeklyScore = when (weekly.trend) {
            dev.sadakat.screentimetracker.domain.analytics.repository.TrendDirection.INCREASING -> 1f
            dev.sadakat.screentimetracker.domain.analytics.repository.TrendDirection.DECREASING -> -1f
            dev.sadakat.screentimetracker.domain.analytics.repository.TrendDirection.STABLE -> 0f
        }

        val monthlyScore = when {
            monthly.averageGrowth > 5f -> 1f
            monthly.averageGrowth < -5f -> -1f
            else -> 0f
        }

        val combinedScore = (weeklyScore * weeklyWeight) + (monthlyScore * monthlyWeight)

        return when {
            combinedScore > 0.3f -> OverallTrendDirection.INCREASING
            combinedScore < -0.3f -> OverallTrendDirection.DECREASING
            else -> OverallTrendDirection.STABLE
        }
    }

    private fun generateKeyInsights(
        weekly: WeeklyComparison,
        monthly: MonthlyTrends
    ): List<TrendInsight> {
        val insights = mutableListOf<TrendInsight>()

        // Weekly insights
        when {
            kotlin.math.abs(weekly.changePercentage) > 20f -> {
                val direction = if (weekly.changePercentage > 0) "increased" else "decreased"
                insights.add(TrendInsight(
                    category = "Weekly Pattern",
                    message = "Screen time has $direction significantly by ${kotlin.math.abs(weekly.changePercentage).toInt()}% this week",
                    severity = if (kotlin.math.abs(weekly.changePercentage) > 30f) InsightSeverity.HIGH else InsightSeverity.MEDIUM
                ))
            }
            kotlin.math.abs(weekly.changePercentage) > 10f -> {
                insights.add(TrendInsight(
                    category = "Weekly Pattern",
                    message = "Moderate change in screen time this week (${weekly.changePercentage.toInt()}%)",
                    severity = InsightSeverity.LOW
                ))
            }
            else -> {
                insights.add(TrendInsight(
                    category = "Weekly Pattern",
                    message = "Screen time remains consistent with last week",
                    severity = InsightSeverity.INFO
                ))
            }
        }

        // Monthly insights
        when {
            monthly.averageGrowth > 15f -> {
                insights.add(TrendInsight(
                    category = "Monthly Trend",
                    message = "Strong upward trend in usage over recent months (+${monthly.averageGrowth.toInt()}%)",
                    severity = InsightSeverity.HIGH
                ))
            }
            monthly.averageGrowth < -15f -> {
                insights.add(TrendInsight(
                    category = "Monthly Trend",
                    message = "Significant reduction in usage over recent months (${monthly.averageGrowth.toInt()}%)",
                    severity = InsightSeverity.HIGH
                ))
            }
            kotlin.math.abs(monthly.averageGrowth) < 5f -> {
                insights.add(TrendInsight(
                    category = "Monthly Trend",
                    message = "Stable usage pattern maintained over recent months",
                    severity = InsightSeverity.INFO
                ))
            }
        }

        // Peak/low month insights
        if (monthly.peakUsageMonth.isNotEmpty() && monthly.lowestUsageMonth.isNotEmpty()) {
            insights.add(TrendInsight(
                category = "Historical Pattern",
                message = "Peak usage was in ${monthly.peakUsageMonth}, lowest in ${monthly.lowestUsageMonth}",
                severity = InsightSeverity.INFO
            ))
        }

        return insights
    }
}

data class TrendSummary(
    val weeklyTrend: dev.sadakat.screentimetracker.domain.analytics.repository.TrendDirection,
    val weeklyChangePercentage: Float,
    val monthlyGrowthRate: Float,
    val overallDirection: OverallTrendDirection,
    val keyInsights: List<TrendInsight>
)

enum class OverallTrendDirection {
    INCREASING, DECREASING, STABLE
}

data class TrendInsight(
    val category: String,
    val message: String,
    val severity: InsightSeverity
)

enum class InsightSeverity {
    INFO, LOW, MEDIUM, HIGH
}