<<<<<<<< HEAD:core/database/src/main/java/dev/sadakat/screentimetracker/core/database/Mapper.kt
package dev.sadakat.screentimetracker.core.database

import dev.sadakat.screentimetracker.core.common.model.LimitedApp as DomainLimitedApp
import dev.sadakat.screentimetracker.core.common.model.Achievement as DomainAchievement
import dev.sadakat.screentimetracker.core.common.model.AchievementCategory
import dev.sadakat.screentimetracker.core.common.model.WellnessScore as DomainWellnessScore
import dev.sadakat.screentimetracker.core.common.model.WellnessLevel
import dev.sadakat.screentimetracker.core.database.entities.*
========
package dev.sadakat.screentimetracker.data.local.mappers

import dev.sadakat.screentimetracker.data.local.entities.*
import dev.sadakat.screentimetracker.domain.model.LimitedApp as DomainLimitedApp
>>>>>>>> origin/detached3:backup_src_20250918_005155/main/java/dev/sadakat/screentimetracker/data/local/mappers/Mapper.kt

fun LimitedApp.toDomain() = DomainLimitedApp(
    packageName = packageName,
    timeLimitMillis = timeLimitMillis
)

fun DomainLimitedApp.toEntity() = LimitedApp(
    packageName = packageName,
    timeLimitMillis = timeLimitMillis
)

// Achievement Mappers
fun Achievement.toDomainModel(): DomainAchievement {
    return DomainAchievement(
        achievementId = achievementId,
        name = name,
        description = description,
        emoji = emoji,
        category = AchievementCategory.valueOf(category.uppercase()),
        targetValue = targetValue,
        isUnlocked = isUnlocked,
        unlockedDate = unlockedDate,
        currentProgress = currentProgress
    )
}

fun DomainAchievement.toDataModel(): Achievement {
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
fun WellnessScore.toDomainModel(): DomainWellnessScore {
    return DomainWellnessScore(
        date = date,
        totalScore = totalScore,
        timeLimitScore = timeLimitScore,
        focusSessionScore = focusSessionScore,
        breaksScore = breaksScore,
        sleepHygieneScore = sleepHygieneScore,
        level = WellnessLevel.valueOf(level.uppercase()),
        calculatedAt = calculatedAt
    )
}

fun DomainWellnessScore.toDataModel(): WellnessScore {
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
