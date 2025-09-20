package dev.sadakat.screentimetracker.core.domain.usecases

import dev.sadakat.screentimetracker.core.data.local.entities.FocusSession
import dev.sadakat.screentimetracker.core.domain.repository.TrackerRepository
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import dev.sadakat.screentimetracker.utils.ui.AppNotificationManager
import kotlinx.coroutines.flow.Flow

class FocusSessionManagerUseCase(
    private val repository: TrackerRepository,
    private val notificationManager: AppNotificationManager,
    private val appLogger: AppLogger
) {
    companion object {
        private const val TAG = "FocusSessionManager"
    }

    private var currentSessionId: Long? = null
    private var currentSessionStartTime: Long = 0L

    suspend fun startFocusSession(
        durationMinutes: Int,
        appsToBlock: List<String> = emptyList()
    ): Long {
        val startTime = System.currentTimeMillis()
        val targetDurationMillis = durationMinutes * 60 * 1000L

        val focusSession = FocusSession(
            startTime = startTime,
            endTime = startTime + targetDurationMillis, // Initially set to target end time
            targetDurationMillis = targetDurationMillis,
            actualDurationMillis = 0L, // Will be updated when session ends
            appsBlocked = appsToBlock.joinToString(","),
            wasSuccessful = false, // Will be updated when session completes
            interruptionCount = 0
        )

        return try {
            val sessionId = repository.insertFocusSession(focusSession)
            currentSessionId = sessionId
            currentSessionStartTime = startTime

            notificationManager.showFocusSessionStart(durationMinutes)

            appLogger.i(TAG, "Focus session started: ${durationMinutes} minutes, blocking ${appsToBlock.size} apps")
            sessionId

        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to start focus session", e)
            throw e
        }
    }

    suspend fun completeFocusSession(wasSuccessful: Boolean, interruptionCount: Int = 0): Boolean {
        val sessionId = currentSessionId ?: return false

        return try {
            val endTime = System.currentTimeMillis()
            val actualDuration = endTime - currentSessionStartTime

            repository.completeFocusSession(
                id = sessionId,
                endTime = endTime,
                actualDuration = actualDuration,
                wasSuccessful = wasSuccessful,
                interruptionCount = interruptionCount
            )

            // Show completion notification
            val durationMinutes = (actualDuration / (60 * 1000)).toInt()
            notificationManager.showFocusSessionComplete(durationMinutes, wasSuccessful)

            // Update challenge progress if successful
            if (wasSuccessful) {
                val todaysSessions = repository.getFocusSessionsForDate(System.currentTimeMillis())
                val successfulToday = todaysSessions.count { it.wasSuccessful }

                // This could trigger focus marathon challenge
                if (successfulToday >= 3) {
                    // You could integrate with ChallengeManagerUseCase here
                    appLogger.i(TAG, "User completed 3 focus sessions today - marathon achieved!")
                }
            }

            currentSessionId = null
            currentSessionStartTime = 0L

            appLogger.i(TAG, "Focus session completed. Success: $wasSuccessful, Duration: ${actualDuration}ms")
            true

        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to complete focus session", e)
            false
        }
    }

    suspend fun cancelCurrentFocusSession(): Boolean {
        return completeFocusSession(wasSuccessful = false, interruptionCount = 1)
    }

    fun getCurrentSessionId(): Long? = currentSessionId

    fun isSessionActive(): Boolean = currentSessionId != null

    suspend fun getCurrentSessionDuration(): Long {
        return if (isSessionActive()) {
            System.currentTimeMillis() - currentSessionStartTime
        } else {
            0L
        }
    }

    fun getAllFocusSessions(): Flow<List<FocusSession>> {
        return repository.getAllFocusSessions()
    }

    suspend fun getFocusSessionsForDate(date: Long): List<FocusSession> {
        return repository.getFocusSessionsForDate(date)
    }

    suspend fun getFocusStats(sinceTimestamp: Long): FocusStats {
        return try {
            val sessions = repository.getFocusSessionsForDate(sinceTimestamp)
            val totalSessions = sessions.size
            val successfulSessions = sessions.count { it.wasSuccessful }
            val totalFocusTime = sessions.filter { it.wasSuccessful }.sumOf { it.actualDurationMillis }
            val averageSessionLength = if (successfulSessions > 0) {
                sessions.filter { it.wasSuccessful }.map { it.actualDurationMillis }.average().toLong()
            } else {
                0L
            }

            FocusStats(
                totalSessions = totalSessions,
                successfulSessions = successfulSessions,
                totalFocusTime = totalFocusTime,
                averageSessionLength = averageSessionLength,
                successRate = if (totalSessions > 0) (successfulSessions.toFloat() / totalSessions.toFloat()) else 0f
            )
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to get focus stats", e)
            FocusStats()
        }
    }

    /**
     * Check if an app should be blocked during the current focus session
     */
    suspend fun isAppBlocked(packageName: String): Boolean {
        if (!isSessionActive()) return false

        return try {
            val sessionId = currentSessionId ?: return false
            val sessions = repository.getAllFocusSessions()
            // This is a simplified check - in a real implementation,
            // you'd want a more efficient way to get the current session's blocked apps
            true // For now, assume all apps are blocked during focus sessions
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to check if app is blocked", e)
            false
        }
    }

    data class FocusStats(
        val totalSessions: Int = 0,
        val successfulSessions: Int = 0,
        val totalFocusTime: Long = 0L,
        val averageSessionLength: Long = 0L,
        val successRate: Float = 0f
    )
}