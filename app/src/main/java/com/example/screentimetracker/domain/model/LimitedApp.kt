package com.example.screentimetracker.domain.model

data class LimitedApp(
    val packageName: String,
    val timeLimitMillis: Long
)
