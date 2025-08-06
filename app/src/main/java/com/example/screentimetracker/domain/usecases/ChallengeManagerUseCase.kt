package com.example.screentimetracker.domain.usecases

import com.example.screentimetracker.data.local.Challenge
import com.example.screentimetracker.domain.repository.TrackerRepository
import com.example.screentimetracker.utils.logger.AppLogger
import com.example.screentimetracker.utils.ui.AppNotificationManager
import kotlinx.coroutines.flow.Flow
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChallengeManagerUseCase @Inject constructor(
    private val repository: TrackerRepository,
    private val notificationManager: AppNotificationManager,
    private val appLogger: AppLogger
) {
    companion object {
        private const val TAG = "ChallengeManagerUseCase"
        
        // Challenge IDs matching requirements.md
        const val PHONE_FREE_MEAL = "phone_free_meal"
        const val DIGITAL_SUNSET = "digital_sunset"
        const val FOCUS_MARATHON = "focus_marathon"
        const val APP_MINIMALIST = "app_minimalist"
        const val STEP_AWAY = "step_away"
    }

    suspend fun createWeeklyChallenges() {
        val currentTime = System.currentTimeMillis()
        val weekStart = getWeekStart(currentTime)
        val weekEnd = weekStart + TimeUnit.DAYS.toMillis(7)
        
        val challenges = listOf(
            Challenge(
                challengeId = PHONE_FREE_MEAL,
                name = "Phone-Free Meals",
                description = "No phone usage during meal times for 5 days",
                emoji = "📱",
                targetValue = 5,
                status = "active",
                startDate = weekStart,
                endDate = weekEnd
            ),
            Challenge(
                challengeId = DIGITAL_SUNSET,
                name = "Digital Sunset",
                description = "No usage 1 hour before bedtime for 5 days",
                emoji = "🌙",
                targetValue = 5,
                status = "active",
                startDate = weekStart,
                endDate = weekEnd
            ),
            Challenge(
                challengeId = FOCUS_MARATHON,
                name = "Focus Marathon",
                description = "Complete 3 focus sessions in a day",
                emoji = "🎯",
                targetValue = 3,
                status = "active",
                startDate = weekStart,
                endDate = weekEnd
            ),
            Challenge(
                challengeId = APP_MINIMALIST,
                name = "App Minimalist",
                description = "Use only 5 essential apps for a day",
                emoji = "📖",
                targetValue = 1,
                status = "active",
                startDate = weekStart,
                endDate = weekEnd
            ),
            Challenge(
                challengeId = STEP_AWAY,
                name = "Step Away",
                description = "Take a 10-minute break every hour",
                emoji = "🚶",
                targetValue = 8, // 8 hours in a typical day
                status = "active",
                startDate = weekStart,
                endDate = weekEnd
            )
        )
        
        challenges.forEach { challenge ->
            try {
                repository.insertChallenge(challenge)
                appLogger.i(TAG, "Created weekly challenge: ${challenge.name}")
            } catch (e: Exception) {
                appLogger.e(TAG, "Failed to create challenge: ${challenge.name}", e)
            }
        }
    }

    fun getActiveChallenges(): Flow<List<Challenge>> {
        return repository.getActiveChallenges(System.currentTimeMillis())
    }

    fun getAllChallenges(): Flow<List<Challenge>> {
        return repository.getAllChallenges()
    }

    suspend fun updateChallengeProgress(challengeId: String, progress: Int) {
        try {
            val challenge = repository.getLatestChallengeByType(challengeId)
            challenge?.let {
                repository.updateChallengeProgress(it.id, progress)
                
                // Check if challenge is completed
                if (progress >= it.targetValue && it.status == "active") {
                    repository.updateChallengeStatus(it.id, "completed")
                    notificationManager.showMotivationBoost(
                        "🎉 Challenge completed: ${it.name}! Great job!"
                    )
                    appLogger.i(TAG, "Challenge completed: ${it.name}")
                }
            }
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to update challenge progress for $challengeId", e)
        }
    }

    suspend fun checkPhoneFreemeal(mealStartTime: Long, mealEndTime: Long, hadPhoneUsage: Boolean) {
        val progress = if (!hadPhoneUsage) 1 else 0
        val currentChallenge = repository.getLatestChallengeByType(PHONE_FREE_MEAL)
        currentChallenge?.let {
            val newProgress = it.currentProgress + progress
            updateChallengeProgress(PHONE_FREE_MEAL, newProgress)
        }
    }

    suspend fun checkDigitalSunset(bedtime: Long, hadUsageBeforeBedtime: Boolean) {
        val progress = if (!hadUsageBeforeBedtime) 1 else 0
        val currentChallenge = repository.getLatestChallengeByType(DIGITAL_SUNSET)
        currentChallenge?.let {
            val newProgress = it.currentProgress + progress
            updateChallengeProgress(DIGITAL_SUNSET, newProgress)
        }
    }

    suspend fun checkFocusMarathon(focusSessionsCompleted: Int) {
        if (focusSessionsCompleted >= 3) {
            updateChallengeProgress(FOCUS_MARATHON, focusSessionsCompleted)
        }
    }

    suspend fun checkAppMinimalist(uniqueAppsUsed: Int) {
        if (uniqueAppsUsed <= 5) {
            updateChallengeProgress(APP_MINIMALIST, 1)
        }
    }

    suspend fun checkStepAway(breaksPerHour: Int) {
        updateChallengeProgress(STEP_AWAY, breaksPerHour)
    }

    suspend fun expireOldChallenges() {
        try {
            val expiredChallenges = repository.getExpiredChallenges(System.currentTimeMillis())
            expiredChallenges.forEach { challenge ->
                if (challenge.currentProgress >= challenge.targetValue) {
                    repository.updateChallengeStatus(challenge.id, "completed")
                } else {
                    repository.updateChallengeStatus(challenge.id, "failed")
                }
                appLogger.i(TAG, "Expired challenge: ${challenge.name}, final status: ${if (challenge.currentProgress >= challenge.targetValue) "completed" else "failed"}")
            }
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to expire old challenges", e)
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
}