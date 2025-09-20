package dev.sadakat.screentimetracker.core.data.mapper

import dev.sadakat.screentimetracker.core.domain.model.WellnessScore as DomainWellnessScore
import dev.sadakat.screentimetracker.core.domain.model.WellnessLevel
import dev.sadakat.screentimetracker.core.data.local.entities.WellnessScore as EntityWellnessScore

/**
 * Mapper for converting between domain WellnessScore and data layer entity
 * This eliminates the need for multiple WellnessScore implementations
 */
object WellnessScoreDataMapper {

    fun mapToDomain(entity: EntityWellnessScore): DomainWellnessScore {
        return DomainWellnessScore(
            date = entity.date,
            overall = entity.totalScore,
            screenTime = entity.timeLimitScore,
            unlocks = entity.breaksScore,
            goals = calculateGoalsScore(entity),
            productivity = entity.focusSessionScore,
            consistency = calculateConsistencyScore(entity),
            timeLimitScore = entity.timeLimitScore,
            focusSessionScore = entity.focusSessionScore,
            breaksScore = entity.breaksScore,
            sleepHygieneScore = entity.sleepHygieneScore,
            calculatedAt = entity.calculatedAt
        )
    }

    fun mapToEntity(domain: DomainWellnessScore): EntityWellnessScore {
        return EntityWellnessScore(
            date = domain.date,
            totalScore = domain.overall,
            timeLimitScore = domain.timeLimitScore,
            focusSessionScore = domain.focusSessionScore,
            breaksScore = domain.breaksScore,
            sleepHygieneScore = domain.sleepHygieneScore,
            level = domain.wellnessLevel.name.lowercase(),
            calculatedAt = domain.calculatedAt
        )
    }

    fun mapToDomainList(entities: List<EntityWellnessScore>): List<DomainWellnessScore> {
        return entities.map { mapToDomain(it) }
    }

    fun mapToEntityList(domainList: List<DomainWellnessScore>): List<EntityWellnessScore> {
        return domainList.map { mapToEntity(it) }
    }

    private fun calculateGoalsScore(entity: EntityWellnessScore): Int {
        // Calculate goals score based on other metrics
        // This is a placeholder - implement actual goal achievement calculation
        return (entity.timeLimitScore + entity.focusSessionScore) / 2
    }

    private fun calculateConsistencyScore(entity: EntityWellnessScore): Int {
        // Calculate consistency based on available metrics
        // This is a placeholder - implement actual consistency calculation
        return entity.sleepHygieneScore
    }
}