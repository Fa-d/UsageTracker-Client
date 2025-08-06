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

// Achievement Mappers
fun Achievement.toDomainModel(): com.example.screentimetracker.domain.model.Achievement {
    return com.example.screentimetracker.domain.model.Achievement(
        achievementId = achievementId,
        name = name,
        description = description,
        emoji = emoji,
        category = com.example.screentimetracker.domain.model.AchievementCategory.valueOf(category.uppercase()),
        targetValue = targetValue,
        isUnlocked = isUnlocked,
        unlockedDate = unlockedDate,
        currentProgress = currentProgress
    )
}

fun com.example.screentimetracker.domain.model.Achievement.toDataModel(): Achievement {
    return Achievement(
        achievementId = achievementId,
        name = name,
        description = description,
        emoji = emoji,
        category = category.name.lowercase(),
        targetValue = targetValue,
        isUnlocked = isUnlocked,
        unlockedDate = unlockedDate,
        currentProgress = currentProgress
    )
}

// Wellness Score Mappers
fun WellnessScore.toDomainModel(): com.example.screentimetracker.domain.model.WellnessScore {
    return com.example.screentimetracker.domain.model.WellnessScore(
        date = date,
        totalScore = totalScore,
        timeLimitScore = timeLimitScore,
        focusSessionScore = focusSessionScore,
        breaksScore = breaksScore,
        sleepHygieneScore = sleepHygieneScore,
        level = com.example.screentimetracker.domain.model.WellnessLevel.valueOf(level.uppercase()),
        calculatedAt = calculatedAt
    )
}

fun com.example.screentimetracker.domain.model.WellnessScore.toDataModel(): WellnessScore {
    return WellnessScore(
        date = date,
        totalScore = totalScore,
        timeLimitScore = timeLimitScore,
        focusSessionScore = focusSessionScore,
        breaksScore = breaksScore,
        sleepHygieneScore = sleepHygieneScore,
        level = level.name.lowercase(),
        calculatedAt = calculatedAt
    )
}
