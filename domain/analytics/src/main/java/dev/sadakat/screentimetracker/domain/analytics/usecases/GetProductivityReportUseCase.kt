package dev.sadakat.screentimetracker.domain.analytics.usecases

import dev.sadakat.screentimetracker.domain.analytics.repository.AnalyticsRepository
import dev.sadakat.screentimetracker.domain.analytics.repository.ProductivityReport
import android.util.Log
import javax.inject.Inject

class GetProductivityReportUseCase @Inject constructor(
    private val repository: AnalyticsRepository
) {
    companion object {
        private const val TAG = "GetProductivityReportUseCase"
    }

    suspend operator fun invoke(days: Int = 7): Result<ProductivityReport> {
        return try {
            val report = repository.getProductivityReport(days)
            Log.i(TAG, "Generated productivity report for $days days - Score: ${report.productivityScore}")
            Result.success(report)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get productivity report", e)
            Result.failure(e)
        }
    }

    suspend fun getDailyProductivityReport(): Result<ProductivityReport> {
        return invoke(1)
    }

    suspend fun getWeeklyProductivityReport(): Result<ProductivityReport> {
        return invoke(7)
    }

    suspend fun getMonthlyProductivityReport(): Result<ProductivityReport> {
        return invoke(30)
    }

    suspend fun analyzeProductivityTrend(
        currentPeriodDays: Int = 7,
        comparisonPeriodDays: Int = 7
    ): Result<ProductivityTrend> {
        return try {
            val currentReport = repository.getProductivityReport(currentPeriodDays)
            val previousReport = repository.getProductivityReport(comparisonPeriodDays)

            val scoreDiff = currentReport.productivityScore - previousReport.productivityScore
            val productiveTimeDiff = currentReport.productiveTime - previousReport.productiveTime
            val distractiveTimeDiff = currentReport.distractiveTime - previousReport.distractiveTime

            val trend = ProductivityTrend(
                currentScore = currentReport.productivityScore,
                previousScore = previousReport.productivityScore,
                scoreChange = scoreDiff,
                scoreChangePercentage = if (previousReport.productivityScore > 0) {
                    (scoreDiff / previousReport.productivityScore) * 100
                } else 0f,
                productiveTimeChange = productiveTimeDiff,
                distractiveTimeChange = distractiveTimeDiff,
                overallTrend = when {
                    scoreDiff > 0.1 -> ProductivityTrendDirection.IMPROVING
                    scoreDiff < -0.1 -> ProductivityTrendDirection.DECLINING
                    else -> ProductivityTrendDirection.STABLE
                }
            )

            Log.i(TAG, "Analyzed productivity trend: ${trend.overallTrend} (${trend.scoreChangePercentage}%)")
            Result.success(trend)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to analyze productivity trend", e)
            Result.failure(e)
        }
    }

    suspend fun getProductivityInsights(report: ProductivityReport): List<ProductivityInsight> {
        return try {
            val insights = mutableListOf<ProductivityInsight>()

            // Score-based insights
            when {
                report.productivityScore >= 0.8f -> {
                    insights.add(ProductivityInsight(
                        type = InsightType.POSITIVE,
                        title = "Excellent Productivity",
                        message = "You're maintaining excellent productivity habits!",
                        suggestion = "Keep up the great work and consider sharing your strategies."
                    ))
                }
                report.productivityScore >= 0.6f -> {
                    insights.add(ProductivityInsight(
                        type = InsightType.NEUTRAL,
                        title = "Good Productivity",
                        message = "Your productivity is above average.",
                        suggestion = "Focus on your most productive hours to maximize efficiency."
                    ))
                }
                else -> {
                    insights.add(ProductivityInsight(
                        type = InsightType.WARNING,
                        title = "Productivity Opportunity",
                        message = "There's room for improvement in your productivity.",
                        suggestion = "Consider reducing time spent on distractive apps."
                    ))
                }
            }

            // Time distribution insights
            val totalTime = report.productiveTime + report.distractiveTime + report.neutralTime
            if (totalTime > 0) {
                val distractivePercentage = (report.distractiveTime.toFloat() / totalTime) * 100

                if (distractivePercentage > 40) {
                    insights.add(ProductivityInsight(
                        type = InsightType.WARNING,
                        title = "High Distractive Usage",
                        message = "Distractive apps account for ${distractivePercentage.toInt()}% of your usage.",
                        suggestion = "Try setting limits on your most distracting apps."
                    ))
                }
            }

            // Peak hours insight
            if (report.mostProductiveHours.isNotEmpty()) {
                val peakHours = report.mostProductiveHours.joinToString(", ") { "${it}:00" }
                insights.add(ProductivityInsight(
                    type = InsightType.INFO,
                    title = "Peak Productivity Hours",
                    message = "You're most productive during: $peakHours",
                    suggestion = "Schedule important tasks during these hours."
                ))
            }

            insights

        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate productivity insights", e)
            listOf(ProductivityInsight(
                type = InsightType.ERROR,
                title = "Analysis Error",
                message = "Unable to generate productivity insights.",
                suggestion = "Please try again later."
            ))
        }
    }
}

data class ProductivityTrend(
    val currentScore: Float,
    val previousScore: Float,
    val scoreChange: Float,
    val scoreChangePercentage: Float,
    val productiveTimeChange: Long,
    val distractiveTimeChange: Long,
    val overallTrend: ProductivityTrendDirection
)

enum class ProductivityTrendDirection {
    IMPROVING, DECLINING, STABLE
}

data class ProductivityInsight(
    val type: InsightType,
    val title: String,
    val message: String,
    val suggestion: String
)

enum class InsightType {
    POSITIVE, WARNING, INFO, NEUTRAL, ERROR
}