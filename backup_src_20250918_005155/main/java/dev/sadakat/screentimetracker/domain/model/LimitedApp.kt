package dev.sadakat.screentimetracker.domain.model

data class LimitedApp(
    val packageName: String,
    val timeLimitMillis: Long
)
