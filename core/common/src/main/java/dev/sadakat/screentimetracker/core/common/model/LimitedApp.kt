package dev.sadakat.screentimetracker.core.common.model

data class LimitedApp(
    val packageName: String,
    val timeLimitMillis: Long
)
