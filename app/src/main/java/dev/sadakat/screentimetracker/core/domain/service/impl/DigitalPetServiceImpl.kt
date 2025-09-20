package dev.sadakat.screentimetracker.core.domain.service.impl

import dev.sadakat.screentimetracker.core.domain.model.DigitalPet
import dev.sadakat.screentimetracker.core.domain.model.WellnessFactors
import dev.sadakat.screentimetracker.core.domain.model.WellnessScore
import dev.sadakat.screentimetracker.core.domain.model.PetMood
import dev.sadakat.screentimetracker.core.domain.model.PetStats
import dev.sadakat.screentimetracker.core.domain.model.PetType
import dev.sadakat.screentimetracker.core.domain.service.DigitalPetService
import kotlin.math.max
import kotlin.math.min

/**
 * Implementation of DigitalPetService containing business logic extracted from DigitalPetManager.
 * This follows clean architecture by keeping business logic in the domain layer.
 */
class DigitalPetServiceImpl : DigitalPetService {

    companion object {
        private const val BASE_EXPERIENCE_PER_LEVEL = 100
        private const val HOURS_TO_MILLISECONDS = 3600000L
        private const val DAILY_DECAY_RATE = 5 // Points lost per day without good wellness
        private const val WELLNESS_REWARD_MULTIPLIER = 2
    }

    override fun calculateWellnessFactors(
        screenTimeMillis: Long,
        unlockCount: Int,
        appOpenCount: Int,
        targetScreenTimeHours: Float
    ): WellnessFactors {
        val screenTimeHours = screenTimeMillis / HOURS_TO_MILLISECONDS.toFloat()

        // Screen time score (0-100) - lower screen time = higher score
        val screenTimeScore = when {
            screenTimeHours <= targetScreenTimeHours * 0.5f -> 100
            screenTimeHours <= targetScreenTimeHours -> 80
            screenTimeHours <= targetScreenTimeHours * 1.5f -> 60
            screenTimeHours <= targetScreenTimeHours * 2f -> 40
            else -> 20
        }

        // Unlock frequency score - fewer unlocks = better score
        val unlockFrequencyScore = when {
            unlockCount <= 20 -> 100
            unlockCount <= 40 -> 80
            unlockCount <= 80 -> 60
            unlockCount <= 120 -> 40
            else -> 20
        }

        // App usage score - fewer app opens = better focus
        val appUsageScore = when {
            appOpenCount <= 30 -> 100
            appOpenCount <= 60 -> 80
            appOpenCount <= 100 -> 60
            appOpenCount <= 150 -> 40
            else -> 20
        }

        // Break score - calculated based on usage patterns (simplified for now)
        val breakScore = calculateBreakScore(unlockCount)

        // Sleep score - based on late night usage (simplified)
        val sleepScore = calculateSleepScore()

        // Consistency score - based on historical data (simplified)
        val consistencyScore = calculateConsistencyScore(screenTimeHours)

        return WellnessFactors(
            screenTimeScore = screenTimeScore,
            unlockFrequencyScore = unlockFrequencyScore,
            appUsageScore = appUsageScore,
            breakScore = breakScore,
            sleepScore = sleepScore,
            consistencyScore = consistencyScore
        )
    }

    override fun calculateWellnessFactorsFromScore(
        wellnessScore: WellnessScore,
        targetScreenTimeHours: Float
    ): WellnessFactors {
        // Map wellness score components to factors
        return WellnessFactors(
            screenTimeScore = wellnessScore.screenTime,
            unlockFrequencyScore = wellnessScore.unlocks,
            appUsageScore = wellnessScore.productivity,
            breakScore = wellnessScore.unlocks, // Break score correlates with unlock patterns
            sleepScore = wellnessScore.consistency,
            consistencyScore = wellnessScore.consistency
        )
    }

    override fun updatePetState(
        currentPet: DigitalPet,
        wellnessFactors: WellnessFactors,
        daysSinceLastUpdate: Int
    ): DigitalPet {
        val currentTime = System.currentTimeMillis()

        // Calculate decay for missed days
        val decayAmount = daysSinceLastUpdate * DAILY_DECAY_RATE

        // Calculate new stats based on wellness
        val healthChange = calculateHealthChange(wellnessFactors) - decayAmount
        val happinessChange = calculateHappinessChange(wellnessFactors) - decayAmount
        val energyChange = calculateEnergyChange(wellnessFactors) - decayAmount
        val experienceGain = calculateExperienceGain(wellnessFactors)

        val newHealth = min(100, max(0, currentPet.health + healthChange))
        val newHappiness = min(100, max(0, currentPet.happiness + happinessChange))
        val newEnergy = min(100, max(0, currentPet.energy + energyChange))
        val newExperience = currentPet.experiencePoints + experienceGain

        // Check for level up
        val newLevel = calculateLevelFromExperience(newExperience)
        val leveledUp = newLevel > currentPet.level

        // Update wellness streak
        val newStreakDays = if (wellnessFactors.overallScore >= 70) {
            currentPet.wellnessStreakDays + 1
        } else {
            0
        }

        return currentPet.copy(
            health = newHealth,
            happiness = newHappiness,
            energy = newEnergy,
            experiencePoints = newExperience,
            level = newLevel,
            wellnessStreakDays = newStreakDays,
            lastWellnessCheck = currentTime
        )
    }

    override fun getPetStats(pet: DigitalPet): PetStats {
        val currentLevel = pet.level
        val currentExp = pet.experiencePoints
        val expForCurrentLevel = getExperienceRequiredForLevel(currentLevel)
        val expForNextLevel = getExperienceRequiredForLevel(currentLevel + 1)
        val expToNextLevel = expForNextLevel - currentExp

        val mood = calculatePetMood(pet)
        val wellnessScore = calculateOverallWellnessScore(pet)
        val evolutionStage = calculateEvolutionStage(pet.level)
        val daysSinceCreated = ((System.currentTimeMillis() - pet.createdAt) / (24 * 60 * 60 * 1000)).toInt()

        return PetStats(
            level = currentLevel,
            experiencePoints = currentExp,
            experienceToNextLevel = expToNextLevel,
            health = pet.health,
            happiness = pet.happiness,
            energy = pet.energy,
            mood = mood,
            wellnessScore = wellnessScore,
            evolutionStage = evolutionStage,
            daysSinceCreated = daysSinceCreated
        )
    }

    override fun calculatePetMood(pet: DigitalPet): PetMood {
        val avgStats = (pet.health + pet.happiness + pet.energy) / 3

        return when {
            avgStats >= 90 -> PetMood.THRIVING
            avgStats >= 75 -> PetMood.HAPPY
            avgStats >= 60 -> PetMood.CONTENT
            avgStats >= 40 -> PetMood.CONCERNED
            avgStats >= 20 -> PetMood.SICK
            else -> PetMood.SLEEPING
        }
    }

    override fun calculateOverallWellnessScore(pet: DigitalPet): Int {
        // Base score from pet stats
        val baseScore = (pet.health + pet.happiness + pet.energy) / 3

        // Bonus for streak and level
        val streakBonus = min(10, pet.wellnessStreakDays)
        val levelBonus = min(15, pet.level * 2)

        return min(100, baseScore + streakBonus + levelBonus)
    }

    override fun getMotivationalMessage(petStats: PetStats, petType: PetType): String {
        return when (petStats.mood) {
            PetMood.THRIVING -> "Your ${petType.displayName} is absolutely glowing! Keep up the excellent digital wellness! ✨"
            PetMood.HAPPY -> "Your ${petType.displayName} is delighted with your balanced approach to technology! 😊"
            PetMood.CONTENT -> "Your ${petType.displayName} appreciates your mindful usage. Small improvements can make a big difference!"
            PetMood.CONCERNED -> "Your ${petType.displayName} is a bit worried. Try reducing screen time or taking more breaks. 😟"
            PetMood.SICK -> "Your ${petType.displayName} needs your attention! Focus on digital wellness to help them recover. 💚"
            PetMood.SLEEPING -> "Your ${petType.displayName} is resting. Perfect time for a digital break! 😴"
        }
    }

    override fun getExperienceForNextLevel(currentLevel: Int): Int {
        return getExperienceRequiredForLevel(currentLevel + 1)
    }

    override fun calculateLevelFromExperience(experience: Int): Int {
        var level = 1
        var expRequired = BASE_EXPERIENCE_PER_LEVEL
        var totalExpRequired = expRequired

        while (experience >= totalExpRequired) {
            level++
            expRequired = (BASE_EXPERIENCE_PER_LEVEL * (1.2 * level)).toInt()
            totalExpRequired += expRequired
        }

        return level
    }

    override fun calculateEvolutionStage(level: Int): Int {
        return when {
            level >= 25 -> 4 // Legendary
            level >= 20 -> 3 // Master
            level >= 15 -> 2 // Advanced
            level >= 10 -> 1 // Intermediate
            else -> 0 // Beginner
        }
    }

    // Private helper methods

    private fun calculateBreakScore(unlockCount: Int): Int {
        // Simplified: assume good breaks if unlocks are spaced reasonably
        return when {
            unlockCount == 0 -> 100 // No usage at all
            unlockCount <= 30 -> 90 // Good spacing
            unlockCount <= 60 -> 70 // Moderate
            unlockCount <= 100 -> 50 // Frequent but not terrible
            else -> 30 // Poor break patterns
        }
    }

    private fun calculateSleepScore(): Int {
        // Simplified: assume good sleep hygiene for now
        // In real implementation, check for late night usage
        return 80
    }

    private fun calculateConsistencyScore(screenTimeHours: Float): Int {
        // Simplified: use current data as proxy
        // In real implementation, compare with historical averages
        return 75 // Default score for consistent behavior
    }

    private fun calculateHealthChange(factors: WellnessFactors): Int {
        return when {
            factors.overallScore >= 90 -> 10
            factors.overallScore >= 80 -> 5
            factors.overallScore >= 70 -> 2
            factors.overallScore >= 60 -> 0
            factors.overallScore >= 50 -> -2
            factors.overallScore >= 40 -> -5
            else -> -10
        }
    }

    private fun calculateHappinessChange(factors: WellnessFactors): Int {
        return when {
            factors.overallScore >= 85 -> 8
            factors.overallScore >= 75 -> 4
            factors.overallScore >= 65 -> 1
            factors.overallScore >= 55 -> -1
            factors.overallScore >= 45 -> -4
            else -> -8
        }
    }

    private fun calculateEnergyChange(factors: WellnessFactors): Int {
        return when {
            factors.overallScore >= 80 -> 6
            factors.overallScore >= 70 -> 3
            factors.overallScore >= 60 -> 0
            factors.overallScore >= 50 -> -3
            else -> -6
        }
    }

    private fun calculateExperienceGain(factors: WellnessFactors): Int {
        val baseGain = factors.overallScore / 10 // 0-10 base points
        return baseGain * WELLNESS_REWARD_MULTIPLIER
    }

    private fun getExperienceRequiredForLevel(level: Int): Int {
        var totalExp = 0
        for (i in 1 until level) {
            totalExp += (BASE_EXPERIENCE_PER_LEVEL * (1.2 * i)).toInt()
        }
        return totalExp
    }
}