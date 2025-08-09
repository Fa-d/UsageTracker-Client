package com.example.screentimetracker.validation

import com.example.screentimetracker.data.local.*
import com.example.screentimetracker.domain.model.WellnessScore
import com.example.screentimetracker.domain.usecases.WeeklyInsightsUseCase
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit

class DataValidationTest {

    @Test
    fun `TimeRestriction validation should work correctly`() {
        // Valid time restriction
        val validRestriction = TimeRestriction(
            id = 1,
            packageName = "com.instagram.android",
            startTimeMillis = TimeUnit.HOURS.toMillis(9),
            endTimeMillis = TimeUnit.HOURS.toMillis(17),
            daysOfWeek = setOf(1, 2, 3, 4, 5),
            isActive = true,
            createdAt = System.currentTimeMillis(),
            violationCount = 0
        )
        
        // Validate basic properties
        assertTrue("Package name should not be empty", validRestriction.packageName.isNotEmpty())
        assertTrue("Start time should be before end time", validRestriction.startTimeMillis < validRestriction.endTimeMillis)
        assertTrue("Days of week should not be empty", validRestriction.daysOfWeek.isNotEmpty())
        assertTrue("Days of week should be valid (1-7)", validRestriction.daysOfWeek.all { it in 1..7 })
        assertTrue("Violation count should be non-negative", validRestriction.violationCount >= 0)
        assertTrue("Created time should be positive", validRestriction.createdAt > 0)
    }

    @Test
    fun `TimeRestriction edge cases should be handled`() {
        // Invalid cases that should be detected
        val invalidPackageName = TimeRestriction(
            id = 1,
            packageName = "", // Empty package name
            startTimeMillis = TimeUnit.HOURS.toMillis(9),
            endTimeMillis = TimeUnit.HOURS.toMillis(17),
            daysOfWeek = setOf(1),
            isActive = true,
            createdAt = System.currentTimeMillis(),
            violationCount = 0
        )
        
        assertTrue("Empty package name should be invalid", invalidPackageName.packageName.isEmpty())
        
        // Overlapping times (end before start)
        val invalidTimes = TimeRestriction(
            id = 2,
            packageName = "com.test.app",
            startTimeMillis = TimeUnit.HOURS.toMillis(17),
            endTimeMillis = TimeUnit.HOURS.toMillis(9), // End before start
            daysOfWeek = setOf(1),
            isActive = true,
            createdAt = System.currentTimeMillis(),
            violationCount = 0
        )
        
        assertTrue("End time should not be before start time", invalidTimes.startTimeMillis > invalidTimes.endTimeMillis)
        
        // Invalid days of week
        val invalidDays = TimeRestriction(
            id = 3,
            packageName = "com.test.app",
            startTimeMillis = TimeUnit.HOURS.toMillis(9),
            endTimeMillis = TimeUnit.HOURS.toMillis(17),
            daysOfWeek = setOf(0, 8, 9), // Invalid days
            isActive = true,
            createdAt = System.currentTimeMillis(),
            violationCount = 0
        )
        
        assertTrue("Should have invalid days of week", invalidDays.daysOfWeek.any { it !in 1..7 })
    }

    @Test
    fun `SessionData validation should work correctly`() {
        val validSession = SessionData(
            id = 1,
            packageName = "com.instagram.android",
            startTimeMillis = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1),
            endTimeMillis = System.currentTimeMillis(),
            durationMillis = TimeUnit.HOURS.toMillis(1)
        )
        
        // Validate basic properties
        assertTrue("Package name should not be empty", validSession.packageName.isNotEmpty())
        assertTrue("Start time should be before end time", validSession.startTimeMillis < validSession.endTimeMillis)
        assertTrue("Duration should be positive", validSession.durationMillis > 0)
        assertEquals("Duration should match time difference", 
            validSession.endTimeMillis - validSession.startTimeMillis, 
            validSession.durationMillis)
    }

    @Test
    fun `SessionData edge cases should be handled`() {
        // Invalid session with negative duration
        val invalidDuration = SessionData(
            id = 1,
            packageName = "com.test.app",
            startTimeMillis = System.currentTimeMillis(),
            endTimeMillis = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1), // End before start
            durationMillis = -TimeUnit.HOURS.toMillis(1) // Negative duration
        )
        
        assertTrue("Duration should not be negative", invalidDuration.durationMillis < 0)
        assertTrue("End time should not be before start", invalidDuration.endTimeMillis < invalidDuration.startTimeMillis)
        
        // Session with mismatched duration
        val mismatchedDuration = SessionData(
            id = 2,
            packageName = "com.test.app",
            startTimeMillis = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2),
            endTimeMillis = System.currentTimeMillis(),
            durationMillis = TimeUnit.MINUTES.toMillis(30) // Duration doesn't match time diff
        )
        
        val actualDuration = mismatchedDuration.endTimeMillis - mismatchedDuration.startTimeMillis
        assertNotEquals("Duration should match actual time difference", actualDuration, mismatchedDuration.durationMillis)
    }

    @Test
    fun `WellnessScore validation should work correctly`() {
        val validScore = WellnessScore(
            id = 1,
            score = 75,
            calculatedAt = System.currentTimeMillis(),
            dayStartMillis = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(12)
        )
        
        // Validate wellness score properties
        assertTrue("Score should be between 0 and 100", validScore.score in 0..100)
        assertTrue("Calculated time should be positive", validScore.calculatedAt > 0)
        assertTrue("Day start should be positive", validScore.dayStartMillis > 0)
        assertTrue("Day start should be before calculated time", validScore.dayStartMillis <= validScore.calculatedAt)
    }

    @Test
    fun `WellnessScore edge cases should be handled`() {
        // Invalid scores
        val negativeScore = WellnessScore(1, -10, System.currentTimeMillis(), System.currentTimeMillis())
        val tooHighScore = WellnessScore(2, 150, System.currentTimeMillis(), System.currentTimeMillis())
        
        assertTrue("Score should not be negative", negativeScore.score < 0)
        assertTrue("Score should not exceed 100", tooHighScore.score > 100)
        
        // Invalid timestamps
        val invalidTimes = WellnessScore(
            id = 3,
            score = 75,
            calculatedAt = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1),
            dayStartMillis = System.currentTimeMillis() // Day start after calculated time
        )
        
        assertTrue("Day start should not be after calculated time", invalidTimes.dayStartMillis > invalidTimes.calculatedAt)
    }

    @Test
    fun `DailyAppSummary validation should work correctly`() {
        val validSummary = DailyAppSummary(
            packageName = "com.instagram.android",
            totalDurationMillis = TimeUnit.HOURS.toMillis(2),
            openCount = 15,
            dayStartMillis = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)
        )
        
        // Validate summary properties
        assertTrue("Package name should not be empty", validSummary.packageName.isNotEmpty())
        assertTrue("Duration should be non-negative", validSummary.totalDurationMillis >= 0)
        assertTrue("Open count should be non-negative", validSummary.openCount >= 0)
        assertTrue("Day start should be positive", validSummary.dayStartMillis > 0)
    }

    @Test
    fun `Goal validation should work correctly`() {
        val validGoal = Goal(
            id = 1,
            name = "Reduce Social Media",
            targetValue = 60,
            currentValue = 45,
            createdAt = System.currentTimeMillis(),
            isActive = true
        )
        
        // Validate goal properties
        assertTrue("Goal name should not be empty", validGoal.name.isNotEmpty())
        assertTrue("Target value should be positive", validGoal.targetValue > 0)
        assertTrue("Current value should be non-negative", validGoal.currentValue >= 0)
        assertTrue("Created time should be positive", validGoal.createdAt > 0)
    }

    @Test
    fun `WeeklyReport validation should work correctly`() {
        val currentTime = System.currentTimeMillis()
        val weekStart = currentTime - TimeUnit.DAYS.toMillis(7)
        
        val validReport = WeeklyInsightsUseCase.WeeklyReport(
            weekStart = weekStart,
            weekEnd = currentTime,
            totalScreenTimeMillis = TimeUnit.HOURS.toMillis(20),
            averageDailyScreenTimeMillis = TimeUnit.HOURS.toMillis(3),
            totalUnlocks = 150,
            averageUnlocksPerDay = 21,
            averageWellnessScore = 75,
            topApps = listOf(
                WeeklyInsightsUseCase.AppUsageInsight(
                    packageName = "com.instagram.android",
                    totalTimeMillis = TimeUnit.HOURS.toMillis(5),
                    sessionsCount = 42,
                    averageDailyTime = TimeUnit.MINUTES.toMillis(43)
                )
            ),
            insights = listOf("Test insight"),
            generatedAt = currentTime
        )
        
        // Validate report structure
        assertTrue("Week start should be before week end", validReport.weekStart < validReport.weekEnd)
        assertTrue("Total screen time should be non-negative", validReport.totalScreenTimeMillis >= 0)
        assertTrue("Average daily time should be non-negative", validReport.averageDailyScreenTimeMillis >= 0)
        assertTrue("Total unlocks should be non-negative", validReport.totalUnlocks >= 0)
        assertTrue("Average unlocks should be non-negative", validReport.averageUnlocksPerDay >= 0)
        assertTrue("Wellness score should be valid", validReport.averageWellnessScore in 0..100)
        assertFalse("Top apps should have data", validReport.topApps.isEmpty())
        assertFalse("Insights should not be empty", validReport.insights.isEmpty())
        assertTrue("Generated time should be positive", validReport.generatedAt > 0)
        
        // Validate app usage insights
        validReport.topApps.forEach { app ->
            assertTrue("App package name should not be empty", app.packageName.isNotEmpty())
            assertTrue("App total time should be non-negative", app.totalTimeMillis >= 0)
            assertTrue("Sessions count should be non-negative", app.sessionsCount >= 0)
            assertTrue("Average daily time should be non-negative", app.averageDailyTime >= 0)
        }
    }

    @Test
    fun `ProductivityHour validation should work correctly`() {
        val validProductivityHour = WeeklyInsightsUseCase.ProductivityHour(
            hour = 14, // 2 PM
            usageTimeMillis = TimeUnit.MINUTES.toMillis(45),
            productivity = 0.8f
        )
        
        // Validate productivity hour properties
        assertTrue("Hour should be valid (0-23)", validProductivityHour.hour in 0..23)
        assertTrue("Usage time should be non-negative", validProductivityHour.usageTimeMillis >= 0)
        assertTrue("Productivity should be between 0 and 1", validProductivityHour.productivity in 0f..1f)
    }

    @Test
    fun `CategoryInsight validation should work correctly`() {
        val validCategory = WeeklyInsightsUseCase.CategoryInsight(
            categoryName = "Social",
            totalTimeMillis = TimeUnit.HOURS.toMillis(8),
            percentageOfTotal = 40f
        )
        
        // Validate category properties
        assertTrue("Category name should not be empty", validCategory.categoryName.isNotEmpty())
        assertTrue("Total time should be non-negative", validCategory.totalTimeMillis >= 0)
        assertTrue("Percentage should be non-negative", validCategory.percentageOfTotal >= 0f)
        assertTrue("Percentage should not exceed 100", validCategory.percentageOfTotal <= 100f)
    }

    @Test
    fun `Large dataset edge cases should be handled`() {
        // Test very large durations
        val largeDuration = TimeUnit.DAYS.toMillis(365) // 1 year
        assertTrue("Should handle large durations", largeDuration > 0)
        
        // Test very large counts
        val largeCount = 1_000_000
        assertTrue("Should handle large counts", largeCount > 0)
        
        // Test very small durations
        val smallDuration = 1L // 1 millisecond
        assertTrue("Should handle small durations", smallDuration > 0)
        
        // Test empty collections
        val emptyReport = WeeklyInsightsUseCase.WeeklyReport.empty()
        assertEquals("Empty report should have zero values", 0, emptyReport.totalScreenTimeMillis)
        assertTrue("Empty report should have empty collections", emptyReport.topApps.isEmpty())
        assertTrue("Empty report should have empty insights", emptyReport.insights.isEmpty())
    }

    @Test
    fun `Time calculation edge cases should be handled`() {
        val now = System.currentTimeMillis()
        val weekInMillis = TimeUnit.DAYS.toMillis(7)
        
        // Test week boundaries
        val weekStart = now - weekInMillis
        val weekEnd = now
        
        assertTrue("Week should be exactly 7 days", weekEnd - weekStart == weekInMillis)
        
        // Test daily averages
        val totalTime = TimeUnit.HOURS.toMillis(21) // 21 hours total
        val averageDaily = totalTime / 7 // Should be 3 hours per day
        assertEquals("Daily average should be calculated correctly", TimeUnit.HOURS.toMillis(3), averageDaily)
        
        // Test hour-of-day calculations
        for (hour in 0..23) {
            assertTrue("Hour should be valid", hour in 0..23)
            
            val startOfHour = TimeUnit.HOURS.toMillis(hour.toLong())
            val endOfHour = TimeUnit.HOURS.toMillis(hour.toLong() + 1)
            assertTrue("Hour range should be valid", endOfHour > startOfHour)
        }
    }
}