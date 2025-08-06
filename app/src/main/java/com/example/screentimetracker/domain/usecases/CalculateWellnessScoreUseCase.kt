package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.domain.model.WellnessScore
import com.example.screentimetracker.domain.model.WellnessLevel
import com.example.screentimetracker.domain.repository.TrackerRepository
import javax.inject.Inject

class CalculateWellnessScoreUseCase @Inject constructor(
    private val repository: TrackerRepository
) {
    suspend operator fun invoke(date: Long): WellnessScore {
        val startOfDay = getStartOfDay(date)
        
        // For now, return a sample wellness score
        // Real implementation would calculate based on actual usage data
        val sampleScore = 75
        val level = WellnessLevel.fromScore(sampleScore)
        
        return WellnessScore(
            date = startOfDay,
            totalScore = sampleScore,
            timeLimitScore = 80,
            focusSessionScore = 70,
            breaksScore = 75,
            sleepHygieneScore = 75,
            level = level,
            calculatedAt = System.currentTimeMillis()
        )
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}