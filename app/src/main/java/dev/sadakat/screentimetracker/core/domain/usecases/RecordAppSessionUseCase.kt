package dev.sadakat.screentimetracker.core.domain.usecases

import dev.sadakat.screentimetracker.core.data.local.entities.AppSessionEvent
import dev.sadakat.screentimetracker.core.domain.repository.TrackerRepository

class RecordAppSessionUseCase(
    private val repository: TrackerRepository
) {
    /**
     * Inserts a completed app session event into the repository.
     *
     * @param packageName The package name of the app.
     * @param startTimeMillis The start time of the session in milliseconds.
     * @param endTimeMillis The end time of the session in milliseconds.
     */
    suspend operator fun invoke(packageName: String, startTimeMillis: Long, endTimeMillis: Long) {
        // Basic validation or calculation
        if (endTimeMillis < startTimeMillis) {
            // Potentially log an error or handle this case appropriately
            // For now, let's assume valid inputs or that service layer handles this check.
            // Or, we could throw an IllegalArgumentException here.
            // Alternatively, cap duration at 0 if endTime < startTime.
            // For robustness, let's ensure duration isn't negative.
            val actualEndTimeMillis = if (endTimeMillis < startTimeMillis) startTimeMillis else endTimeMillis
            val durationMillis = actualEndTimeMillis - startTimeMillis

            val event = AppSessionEvent(
                packageName = packageName,
                startTimeMillis = startTimeMillis,
                endTimeMillis = actualEndTimeMillis,
                durationMillis = durationMillis
            )
            repository.insertAppSession(event)
            return
        }

        val durationMillis = endTimeMillis - startTimeMillis
        val event = AppSessionEvent(
            packageName = packageName,
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
            durationMillis = durationMillis
        )
        repository.insertAppSession(event)
    }

    /**
     * Overloaded operator to directly take an AppSessionEvent object.
     * This can be useful if the event object is already constructed elsewhere.
     */
    suspend operator fun invoke(sessionEvent: AppSessionEvent) {
        // Optional: Add validation for the sessionEvent object if needed
        if (sessionEvent.endTimeMillis < sessionEvent.startTimeMillis) {
             // Handle invalid event, e.g. by correcting times or logging
            val correctedEvent = sessionEvent.copy(
                endTimeMillis = sessionEvent.startTimeMillis, // Cap end time to start time
                durationMillis = 0 // Duration becomes 0
            )
            repository.insertAppSession(correctedEvent)
            return
        }
         if (sessionEvent.durationMillis != (sessionEvent.endTimeMillis - sessionEvent.startTimeMillis)) {
            // Log inconsistency or recalculate duration
             val consistentEvent = sessionEvent.copy(
                 durationMillis = sessionEvent.endTimeMillis - sessionEvent.startTimeMillis
             )
            repository.insertAppSession(consistentEvent)
            return
         }
        repository.insertAppSession(sessionEvent)
    }
}
