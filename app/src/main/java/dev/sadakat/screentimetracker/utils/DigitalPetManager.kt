package dev.sadakat.screentimetracker.utils

import dev.sadakat.screentimetracker.core.domain.service.DigitalPetService
import dev.sadakat.screentimetracker.core.domain.service.impl.DigitalPetServiceImpl
import dev.sadakat.screentimetracker.data.local.entities.DigitalPet
import dev.sadakat.screentimetracker.data.local.entities.PetStats
import dev.sadakat.screentimetracker.data.local.entities.PetType
import dev.sadakat.screentimetracker.data.local.entities.WellnessFactors
import dev.sadakat.screentimetracker.ui.dashboard.state.DashboardState
import dev.sadakat.screentimetracker.core.domain.model.WellnessFactors as DomainWellnessFactors
import dev.sadakat.screentimetracker.core.domain.model.WellnessScore as DomainWellnessScore

/**
 * Legacy adapter for DigitalPetManager that delegates to domain service.
 * This maintains backwards compatibility while following clean architecture.
 *
 * REFACTORING NOTE: This will be deprecated once all callers are updated
 * to use DigitalPetService directly through dependency injection.
 */
object DigitalPetManager {

    // Domain service instance
    private val digitalPetService: DigitalPetService = DigitalPetServiceImpl()

    fun calculateWellnessFactors(
        dashboardState: DashboardState,
        targetScreenTimeHours: Float = 4f
    ): WellnessFactors {
        // Delegate to domain service
        val domainFactors = digitalPetService.calculateWellnessFactors(
            screenTimeMillis = dashboardState.totalScreenTimeTodayMillis,
            unlockCount = dashboardState.totalScreenUnlocksToday,
            appOpenCount = dashboardState.appUsagesToday.sumOf { it.openCount },
            targetScreenTimeHours = targetScreenTimeHours
        )

        // Convert domain model to data entity
        return mapToDataEntity(domainFactors)
    }

    fun calculateWellnessFactorsFromScore(
        dashboardState: DashboardState, wellnessScore: DomainWellnessScore,
        targetScreenTimeHours: Float = 4f
    ): WellnessFactors {
        // Score is already in domain model format
        val domainWellnessScore = wellnessScore

        // Delegate to domain service
        val domainFactors = digitalPetService.calculateWellnessFactorsFromScore(
            wellnessScore = domainWellnessScore, targetScreenTimeHours = targetScreenTimeHours
        )

        // Convert domain model to data entity
        return mapToDataEntity(domainFactors)
    }

    fun updatePetState(
        currentPet: DigitalPet,
        wellnessFactors: WellnessFactors,
        daysSinceLastUpdate: Int = 0
    ): DigitalPet {
        // Convert to domain models
        val domainPet = mapToDomainPet(currentPet)
        val domainFactors = mapToDomainFactors(wellnessFactors)

        // Delegate to domain service
        val updatedDomainPet = digitalPetService.updatePetState(
            currentPet = domainPet,
            wellnessFactors = domainFactors,
            daysSinceLastUpdate = daysSinceLastUpdate
        )

        // Convert back to data entity
        return mapToDataPet(updatedDomainPet)
    }

    fun getPetStats(pet: DigitalPet): PetStats {
        // Convert to domain model
        val domainPet = mapToDomainPet(pet)

        // Delegate to domain service
        val domainStats = digitalPetService.getPetStats(domainPet)

        // Convert back to data entity
        return PetStats(
            level = domainStats.level,
            experiencePoints = domainStats.experiencePoints,
            experienceToNextLevel = domainStats.experienceToNextLevel,
            health = domainStats.health,
            happiness = domainStats.happiness,
            energy = domainStats.energy,
            mood = dev.sadakat.screentimetracker.data.local.entities.PetMood.valueOf(domainStats.mood.name),
            wellnessScore = domainStats.wellnessScore,
            evolutionStage = domainStats.evolutionStage,
            daysSinceCreated = domainStats.daysSinceCreated
        )
    }

    fun getMotivationalMessage(petStats: PetStats, petType: PetType): String {
        // Convert data entities to domain models
        val domainPetStats = mapToDomainPetStats(petStats)
        val domainPetType = mapToDomainPetType(petType)
        return digitalPetService.getMotivationalMessage(domainPetStats, domainPetType)
    }

    // Private mapper methods

    private fun mapToDataEntity(domainFactors: DomainWellnessFactors): WellnessFactors {
        return WellnessFactors(
            screenTimeScore = domainFactors.screenTimeScore,
            unlockFrequencyScore = domainFactors.unlockFrequencyScore,
            appUsageScore = domainFactors.appUsageScore,
            breakScore = domainFactors.breakScore,
            sleepScore = domainFactors.sleepScore,
            consistencyScore = domainFactors.consistencyScore
        )
    }

    private fun mapToDomainFactors(dataFactors: WellnessFactors): DomainWellnessFactors {
        return DomainWellnessFactors(
            screenTimeScore = dataFactors.screenTimeScore,
            unlockFrequencyScore = dataFactors.unlockFrequencyScore,
            appUsageScore = dataFactors.appUsageScore,
            breakScore = dataFactors.breakScore,
            sleepScore = dataFactors.sleepScore,
            consistencyScore = dataFactors.consistencyScore
        )
    }

    private fun mapToDomainPet(dataPet: DigitalPet): dev.sadakat.screentimetracker.core.domain.model.DigitalPet {
        return dev.sadakat.screentimetracker.core.domain.model.DigitalPet(
            id = dataPet.id.toString(),
            name = dataPet.name,
            petType = dev.sadakat.screentimetracker.core.domain.model.PetType.valueOf(dataPet.petType.name),
            level = dataPet.level,
            experiencePoints = dataPet.experiencePoints,
            health = dataPet.health,
            happiness = dataPet.happiness,
            energy = dataPet.energy,
            wellnessStreakDays = dataPet.wellnessStreakDays,
            lastWellnessCheck = dataPet.lastWellnessCheck,
            createdAt = dataPet.createdAt
        )
    }

    private fun mapToDataPet(domainPet: dev.sadakat.screentimetracker.core.domain.model.DigitalPet): DigitalPet {
        return DigitalPet(
            id = domainPet.id.toInt(),
            name = domainPet.name,
            petType = PetType.valueOf(domainPet.petType.name),
            level = domainPet.level,
            experiencePoints = domainPet.experiencePoints,
            health = domainPet.health,
            happiness = domainPet.happiness,
            energy = domainPet.energy,
            wellnessStreakDays = domainPet.wellnessStreakDays,
            lastWellnessCheck = domainPet.lastWellnessCheck,
            createdAt = domainPet.createdAt,
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun mapToDomainPetStats(dataStats: PetStats): dev.sadakat.screentimetracker.core.domain.model.PetStats {
        return dev.sadakat.screentimetracker.core.domain.model.PetStats(
            level = dataStats.level,
            experiencePoints = dataStats.experiencePoints,
            experienceToNextLevel = dataStats.experienceToNextLevel,
            health = dataStats.health,
            happiness = dataStats.happiness,
            energy = dataStats.energy,
            mood = dev.sadakat.screentimetracker.core.domain.model.PetMood.valueOf(dataStats.mood.name),
            wellnessScore = dataStats.wellnessScore,
            evolutionStage = dataStats.evolutionStage,
            daysSinceCreated = dataStats.daysSinceCreated
        )
    }

    private fun mapToDomainPetType(dataType: PetType): dev.sadakat.screentimetracker.core.domain.model.PetType {
        return dev.sadakat.screentimetracker.core.domain.model.PetType.valueOf(dataType.name)
    }
}