package dev.sadakat.screentimetracker.core.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

data class TimeRange(
    val startMillis: Long,
    val endMillis: Long
) {
    init {
        require(startMillis <= endMillis) { "Start time must be before or equal to end time" }
    }

    fun durationMillis(): Long = endMillis - startMillis

    fun durationMinutes(): Long = durationMillis() / (60 * 1000)

    fun durationHours(): Long = durationMillis() / (60 * 60 * 1000)

    fun durationDays(): Long = durationMillis() / (24 * 60 * 60 * 1000)

    fun contains(timestamp: Long): Boolean = timestamp in startMillis..endMillis

    fun overlaps(other: TimeRange): Boolean {
        return startMillis <= other.endMillis && endMillis >= other.startMillis
    }

    fun intersect(other: TimeRange): TimeRange? {
        if (!overlaps(other)) return null

        val intersectStart = maxOf(startMillis, other.startMillis)
        val intersectEnd = minOf(endMillis, other.endMillis)

        return TimeRange(intersectStart, intersectEnd)
    }

    fun union(other: TimeRange): TimeRange? {
        if (!overlaps(other) && !isAdjacent(other)) return null

        return TimeRange(
            minOf(startMillis, other.startMillis),
            maxOf(endMillis, other.endMillis)
        )
    }

    fun isAdjacent(other: TimeRange): Boolean {
        return endMillis == other.startMillis || startMillis == other.endMillis
    }

    fun split(splitPoint: Long): Pair<TimeRange?, TimeRange?> {
        if (!contains(splitPoint)) {
            return null to null
        }

        val before = if (splitPoint > startMillis) TimeRange(startMillis, splitPoint) else null
        val after = if (splitPoint < endMillis) TimeRange(splitPoint, endMillis) else null

        return before to after
    }

    fun expandBy(millis: Long): TimeRange {
        require(millis >= 0) { "Expansion duration must be non-negative" }
        return TimeRange(startMillis - millis, endMillis + millis)
    }

    fun shrinkBy(millis: Long): TimeRange {
        require(millis >= 0) { "Shrink duration must be non-negative" }
        val newStart = startMillis + millis
        val newEnd = endMillis - millis
        require(newStart <= newEnd) { "Cannot shrink range below zero duration" }
        return TimeRange(newStart, newEnd)
    }

    fun toLocalDateTimeRange(zoneId: ZoneId = ZoneId.systemDefault()): Pair<LocalDateTime, LocalDateTime> {
        val startDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startMillis), zoneId)
        val endDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endMillis), zoneId)
        return startDateTime to endDateTime
    }

    fun isInPast(): Boolean = endMillis < System.currentTimeMillis()

    fun isInFuture(): Boolean = startMillis > System.currentTimeMillis()

    fun isActive(): Boolean = contains(System.currentTimeMillis())

    companion object {
        fun fromLocalDateTime(
            start: LocalDateTime,
            end: LocalDateTime,
            zoneId: ZoneId = ZoneId.systemDefault()
        ): TimeRange {
            return TimeRange(
                start.atZone(zoneId).toInstant().toEpochMilli(),
                end.atZone(zoneId).toInstant().toEpochMilli()
            )
        }

        fun today(zoneId: ZoneId = ZoneId.systemDefault()): TimeRange {
            val today = LocalDate.now(zoneId)
            val startOfDay = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endOfDay = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
            return TimeRange(startOfDay, endOfDay)
        }

        fun yesterday(zoneId: ZoneId = ZoneId.systemDefault()): TimeRange {
            val yesterday = LocalDate.now(zoneId).minusDays(1)
            val startOfDay = yesterday.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endOfDay = yesterday.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
            return TimeRange(startOfDay, endOfDay)
        }

        fun thisWeek(zoneId: ZoneId = ZoneId.systemDefault()): TimeRange {
            val today = LocalDate.now(zoneId)
            val startOfWeek = today.minusDays(today.dayOfWeek.value - 1L)
            val endOfWeek = startOfWeek.plusDays(6)

            val startMillis = startOfWeek.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endMillis = endOfWeek.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

            return TimeRange(startMillis, endMillis)
        }

        fun thisMonth(zoneId: ZoneId = ZoneId.systemDefault()): TimeRange {
            val today = LocalDate.now(zoneId)
            val startOfMonth = today.withDayOfMonth(1)
            val endOfMonth = startOfMonth.plusMonths(1).minusDays(1)

            val startMillis = startOfMonth.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endMillis = endOfMonth.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

            return TimeRange(startMillis, endMillis)
        }

        fun lastNDays(days: Int, zoneId: ZoneId = ZoneId.systemDefault()): TimeRange {
            require(days > 0) { "Number of days must be positive" }

            val today = LocalDate.now(zoneId)
            val startDate = today.minusDays(days - 1L)

            val startMillis = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endMillis = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

            return TimeRange(startMillis, endMillis)
        }

        fun fromDuration(startMillis: Long, durationMillis: Long): TimeRange {
            require(durationMillis >= 0) { "Duration must be non-negative" }
            return TimeRange(startMillis, startMillis + durationMillis)
        }

        fun now(): TimeRange {
            val currentTime = System.currentTimeMillis()
            return TimeRange(currentTime, currentTime)
        }

        fun between(millis1: Long, millis2: Long): TimeRange {
            return TimeRange(minOf(millis1, millis2), maxOf(millis1, millis2))
        }
    }
}