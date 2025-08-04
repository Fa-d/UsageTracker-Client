package com.example.screentimetracker.data.local

import com.example.screentimetracker.domain.model.LimitedApp as DomainLimitedApp

fun LimitedApp.toDomain() = DomainLimitedApp(
    packageName = packageName,
    timeLimitMillis = timeLimitMillis
)

fun DomainLimitedApp.toEntity() = LimitedApp(
    packageName = packageName,
    timeLimitMillis = timeLimitMillis
)
