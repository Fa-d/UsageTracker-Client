package dev.sadakat.screentimetracker.core.common.utils

import kotlinx.datetime.*

/**
 * Converts milliseconds to a human-readable time format
 * @param millis Time in milliseconds
 * @return Formatted time string (e.g., "2h 30m", "45m 12s", "30s")
 */
fun millisToReadableTime(millis: Long): String {
    val hours = millis / (1000 * 60 * 60)
    val minutes = (millis / (1000 * 60)) % 60
    val seconds = (millis / 1000) % 60

    return if (hours > 0) {
        "%dh %02dm".format(hours, minutes)
    } else if (minutes > 0) {
        "%dm %02ds".format(minutes, seconds)
    } else {
        "%ds".format(seconds)
    }
}

/**
 * Converts duration to milliseconds
 * @param duration Kotlin Duration
 * @return Milliseconds as Long
 */
fun kotlin.time.Duration.toMilliseconds(): Long = this.inWholeMilliseconds

/**
 * Get start of day for a given LocalDateTime
 */
fun LocalDateTime.startOfDay(): LocalDateTime =
    LocalDateTime(date, LocalTime(0, 0, 0))

/**
 * Get end of day for a given LocalDateTime
 */
fun LocalDateTime.endOfDay(): LocalDateTime =
    LocalDateTime(date, LocalTime(23, 59, 59, 999_000_000))

/**
 * Check if a LocalDateTime is today
 */
fun LocalDateTime.isToday(timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
    val now = Clock.System.now().toLocalDateTime(timeZone)
    return this.date == now.date
}

/**
 * Format time duration for display
 * @param minutes Duration in minutes
 * @return Formatted string like "2h 30m" or "45m"
 */
fun formatDurationFromMinutes(minutes: Int): String {
    return if (minutes >= 60) {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        if (remainingMinutes > 0) {
            "${hours}h ${remainingMinutes}m"
        } else {
            "${hours}h"
        }
    } else {
        "${minutes}m"
    }
}