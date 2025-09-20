package dev.sadakat.screentimetracker.core.domain.service

import dev.sadakat.screentimetracker.core.domain.model.DigitalPet
import dev.sadakat.screentimetracker.core.domain.model.WellnessFactors
import dev.sadakat.screentimetracker.core.domain.model.WellnessScore
import dev.sadakat.screentimetracker.core.domain.model.PetMood
import dev.sadakat.screentimetracker.core.domain.model.PetType
import dev.sadakat.screentimetracker.core.domain.model.PetStats

/**
 * Domain service for digital pet business logic.
 * Extracted from DigitalPetManager to follow clean architecture principles.
 */
interface DigitalPetService {

    /**
     * Calculates wellness factors from daily usage data
     */
    fun calculateWellnessFactors(
        screenTimeMillis: Long,
        unlockCount: Int,
        appOpenCount: Int,
        targetScreenTimeHours: Float = 4f
    ): WellnessFactors

    /**
     * Calculates wellness factors using an existing wellness score
     */
    fun calculateWellnessFactorsFromScore(
        wellnessScore: WellnessScore,
        targetScreenTimeHours: Float = 4f
    ): WellnessFactors

    /**
     * Updates pet state based on wellness factors and elapsed time
     */
    fun updatePetState(
        currentPet: DigitalPet,
        wellnessFactors: WellnessFactors,
        daysSinceLastUpdate: Int = 0
    ): DigitalPet

    /**
     * Gets comprehensive pet statistics
     */
    fun getPetStats(pet: DigitalPet): PetStats

    /**
     * Calculates pet mood based on current stats
     */
    fun calculatePetMood(pet: DigitalPet): PetMood

    /**
     * Calculates overall wellness score from pet stats
     */
    fun calculateOverallWellnessScore(pet: DigitalPet): Int

    /**
     * Gets motivational message based on pet state
     */
    fun getMotivationalMessage(petStats: PetStats, petType: PetType): String

    /**
     * Calculates experience needed for next level
     */
    fun getExperienceForNextLevel(currentLevel: Int): Int

    /**
     * Calculates pet level from experience points
     */
    fun calculateLevelFromExperience(experience: Int): Int

    /**
     * Calculates evolution stage based on pet level
     */
    fun calculateEvolutionStage(level: Int): Int
}