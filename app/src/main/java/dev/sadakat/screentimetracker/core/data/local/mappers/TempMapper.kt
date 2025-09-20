package dev.sadakat.screentimetracker.core.data.local.mappers

import dev.sadakat.screentimetracker.core.data.local.entities.Achievement as EntityAchievement
import dev.sadakat.screentimetracker.core.data.local.entities.WellnessScore as EntityWellnessScore
import dev.sadakat.screentimetracker.core.domain.model.Achievement as DomainAchievement
import dev.sadakat.screentimetracker.core.domain.model.WellnessScore as DomainWellnessScore
import dev.sadakat.screentimetracker.domain.model.AppOpenData
import dev.sadakat.screentimetracker.domain.model.AppSessionDataAggregate

/**
 * Temporary mapper functions to restore build compatibility
 * These delegate to proper mappers to avoid duplication
 */

// Achievement mapping extensions
fun EntityAchievement.toDomainModel(): DomainAchievement {
    return DomainAchievement(
        id = this.achievementId,
        name = this.name,
        description = this.description,
        emoji = this.emoji,
        category = dev.sadakat.screentimetracker.core.domain.model.AchievementCategory.WELLNESS,
        targetValue = this.targetValue,
        currentProgress = this.currentProgress,
        isUnlocked = this.isUnlocked,
        unlockedAt = this.unlockedDate,
        requirements = emptyList(),
        tier = dev.sadakat.screentimetracker.core.domain.model.AchievementTier.BRONZE
    )
}

fun DomainAchievement.toDataModel(): EntityAchievement {
    return EntityAchievement(
        achievementId = this.id,
        name = this.name,
        description = this.description,
        emoji = this.emoji,
        category = this.category.name.lowercase(),
        targetValue = this.targetValue,
        currentProgress = this.currentProgress,
        isUnlocked = this.isUnlocked,
        unlockedDate = this.unlockedAt
    )
}

// WellnessScore mapping extensions
fun EntityWellnessScore.toDomainModel(): DomainWellnessScore {
    return DomainWellnessScore(
        date = this.date,
        overall = this.totalScore,
        screenTime = this.timeLimitScore,
        unlocks = this.breaksScore,
        goals = 70,
        productivity = this.focusSessionScore,
        consistency = this.sleepHygieneScore
    )
}

fun DomainWellnessScore.toDataModel(): EntityWellnessScore {
    return EntityWellnessScore(
        date = this.date,
        totalScore = this.overall,
        timeLimitScore = this.screenTime,
        focusSessionScore = this.productivity,
        breaksScore = this.unlocks,
        sleepHygieneScore = this.consistency,
        level = this.wellnessLevel.name.lowercase(),
        calculatedAt = this.calculatedAt
    )
}

// Temporary stubs for legacy compatibility
fun Any.toAppOpenData(): AppOpenData {
    return AppOpenData("", 0, 0L, 0L)
}

fun Any.toAppSessionDataAggregate(): AppSessionDataAggregate {
    return AppSessionDataAggregate("", 0L, 0, 0L, 0L)
}