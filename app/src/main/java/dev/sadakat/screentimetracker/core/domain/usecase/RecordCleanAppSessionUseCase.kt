package dev.sadakat.screentimetracker.core.domain.usecase

import dev.sadakat.screentimetracker.core.domain.model.AppSession
import dev.sadakat.screentimetracker.core.domain.model.TimeRange
import dev.sadakat.screentimetracker.core.domain.model.AppCategory
import dev.sadakat.screentimetracker.core.domain.model.SessionType
import dev.sadakat.screentimetracker.core.domain.repository.ScreenTimeRepository
import dev.sadakat.screentimetracker.core.domain.service.WellnessCalculationService

/**
 * Clean architecture version of RecordAppSessionUseCase.
 * Records app sessions using pure domain models with business logic validation.
 */
class RecordCleanAppSessionUseCase(
    private val screenTimeRepository: ScreenTimeRepository,
    private val wellnessService: WellnessCalculationService
) {
    /**
     * Records an app session with validation and business logic
     */
    suspend operator fun invoke(
        packageName: String,
        appName: String,
        startTimeMillis: Long,
        endTimeMillis: Long,
        category: AppCategory = AppCategory.UNCATEGORIZED,
        sessionType: SessionType = SessionType.REGULAR
    ): AppSession {
        require(packageName.isNotBlank()) { "Package name cannot be blank" }
        require(appName.isNotBlank()) { "App name cannot be blank" }
        require(startTimeMillis > 0) { "Start time must be positive" }
        require(endTimeMillis > 0) { "End time must be positive" }

        // Business logic: Ensure valid time range
        val validatedEndTime = if (endTimeMillis < startTimeMillis) {
            // Log this issue for debugging but don't crash
            startTimeMillis
        } else {
            endTimeMillis
        }

        val timeRange = TimeRange(startTimeMillis, validatedEndTime)

        val session = AppSession(
            packageName = packageName,
            appName = appName,
            timeRange = timeRange,
            category = category,
            sessionType = sessionType
        )

        // Apply business rules
        val validatedSession = applyBusinessRules(session)

        // Save to repository
        screenTimeRepository.saveAppSession(validatedSession)

        return validatedSession
    }

    /**
     * Records an app session with domain model
     */
    suspend operator fun invoke(session: AppSession): AppSession {
        val validatedSession = applyBusinessRules(session)
        screenTimeRepository.saveAppSession(validatedSession)
        return validatedSession
    }

    /**
     * Records multiple sessions in batch
     */
    suspend fun recordBatch(sessions: List<AppSession>): List<AppSession> {
        val validatedSessions = sessions.map { applyBusinessRules(it) }

        validatedSessions.forEach { session ->
            screenTimeRepository.saveAppSession(session)
        }

        return validatedSessions
    }

    /**
     * Records an ongoing session that will be updated when it ends
     */
    suspend fun startSession(
        packageName: String,
        appName: String,
        startTimeMillis: Long = System.currentTimeMillis(),
        category: AppCategory = AppCategory.UNCATEGORIZED,
        sessionType: SessionType = SessionType.REGULAR
    ): AppSession {
        // Create session with current time as both start and end (ongoing)
        return invoke(packageName, appName, startTimeMillis, startTimeMillis, category, sessionType)
    }

    /**
     * Ends an ongoing session by updating its end time
     */
    suspend fun endSession(
        packageName: String,
        endTimeMillis: Long = System.currentTimeMillis()
    ): AppSession? {
        // Get today's sessions for this app
        val today = TimeRange.today()
        val sessions = screenTimeRepository.getAppSessionsForApps(listOf(packageName), today)

        // Find the most recent session that hasn't ended (same start and end time)
        val ongoingSession = sessions
            .filter { it.timeRange.startMillis == it.timeRange.endMillis }
            .maxByOrNull { it.timeRange.startMillis }

        if (ongoingSession != null) {
            val endedSession = ongoingSession.copy(
                timeRange = TimeRange(ongoingSession.timeRange.startMillis, endTimeMillis)
            )
            screenTimeRepository.saveAppSession(endedSession)
            return endedSession
        }

        return null
    }

    private fun applyBusinessRules(session: AppSession): AppSession {
        var validatedSession = session

        // Rule 1: Minimum session duration (avoid accidental taps)
        val minDurationMillis = 1000L // 1 second
        if (validatedSession.durationMillis < minDurationMillis && validatedSession.durationMillis > 0) {
            // Keep sessions under minimum duration but mark them differently
            validatedSession = validatedSession.copy(sessionType = SessionType.REGULAR)
        }

        // Rule 2: Maximum reasonable session duration (24 hours)
        val maxDurationMillis = 24 * 60 * 60 * 1000L // 24 hours
        if (validatedSession.durationMillis > maxDurationMillis) {
            // Cap at maximum duration
            val cappedEndTime = validatedSession.timeRange.startMillis + maxDurationMillis
            validatedSession = validatedSession.copy(
                timeRange = TimeRange(validatedSession.timeRange.startMillis, cappedEndTime)
            )
        }

        // Rule 3: Classify long sessions as focus sessions for productive apps
        val focusThresholdMillis = 25 * 60 * 1000L // 25 minutes
        if (validatedSession.durationMillis >= focusThresholdMillis &&
            validatedSession.category.isProductive &&
            validatedSession.sessionType == SessionType.REGULAR) {
            validatedSession = validatedSession.copy(sessionType = SessionType.FOCUS_SESSION)
        }

        return validatedSession
    }
}