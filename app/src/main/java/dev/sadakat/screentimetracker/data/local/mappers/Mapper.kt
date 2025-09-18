package dev.sadakat.screentimetracker.data.local.mappers

import dev.sadakat.screentimetracker.data.local.entities.*
import dev.sadakat.screentimetracker.domain.model.LimitedApp as DomainLimitedApp

fun LimitedApp.toDomain() = DomainLimitedApp(
    packageName = packageName,
    timeLimitMillis = timeLimitMillis
)

fun DomainLimitedApp.toEntity() = LimitedApp(
    packageName = packageName,
    timeLimitMillis = timeLimitMillis
)

// Achievement Mappers
fun Achievement.toDomainModel(): dev.sadakat.screentimetracker.domain.model.Achievement {
    return dev.sadakat.screentimetracker.domain.model.Achievement(
        achievementId = achievementId,
        name = name,
        description = description,
        emoji = emoji,
        category = dev.sadakat.screentimetracker.domain.model.AchievementCategory.valueOf(category.uppercase()),
        targetValue = targetValue,
        isUnlocked = isUnlocked,
        unlockedDate = unlockedDate,
        currentProgress = currentProgress
    )
}

fun dev.sadakat.screentimetracker.domain.model.Achievement.toDataModel(): Achievement {
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
fun WellnessScore.toDomainModel(): dev.sadakat.screentimetracker.domain.model.WellnessScore {
    return dev.sadakat.screentimetracker.domain.model.WellnessScore(
        date = date,
        totalScore = totalScore,
        timeLimitScore = timeLimitScore,
        focusSessionScore = focusSessionScore,
        breaksScore = breaksScore,
        sleepHygieneScore = sleepHygieneScore,
        level = dev.sadakat.screentimetracker.domain.model.WellnessLevel.valueOf(level.uppercase()),
        calculatedAt = calculatedAt
    )
}

fun dev.sadakat.screentimetracker.domain.model.WellnessScore.toDataModel(): WellnessScore {
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
