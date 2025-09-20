package dev.sadakat.screentimetracker.utils

import dev.sadakat.screentimetracker.core.data.local.DigitalPet
import dev.sadakat.screentimetracker.core.data.local.entities.PetMood
import dev.sadakat.screentimetracker.core.data.local.PetStats
import dev.sadakat.screentimetracker.core.data.local.PetType
import dev.sadakat.screentimetracker.core.data.local.WellnessFactors
import dev.sadakat.screentimetracker.ui.dashboard.state.DashboardState
import kotlin.math.max
import kotlin.math.min

object DigitalPetManager {
    
    private const val BASE_EXPERIENCE_PER_LEVEL = 100
    private const val HOURS_TO_MILLISECONDS = 3600000L
    private const val DAILY_DECAY_RATE = 5 // Points lost per day without good wellness
    private const val WELLNESS_REWARD_MULTIPLIER = 2
    
    fun calculateWellnessFactors(
        dashboardState: DashboardState,
        targetScreenTimeHours: Float = 4f
    ): WellnessFactors {
        val screenTimeHours = dashboardState.totalScreenTimeTodayMillis / HOURS_TO_MILLISECONDS.toFloat()
        val unlocks = dashboardState.totalScreenUnlocksToday
        val appOpens = dashboardState.appUsagesToday.sumOf { it.openCount }
        
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
            unlocks <= 20 -> 100
            unlocks <= 40 -> 80
            unlocks <= 80 -> 60
            unlocks <= 120 -> 40
            else -> 20
        }
        
        // App usage score - fewer app opens = better focus
        val appUsageScore = when {
            appOpens <= 30 -> 100
            appOpens <= 60 -> 80
            appOpens <= 100 -> 60
            appOpens <= 150 -> 40
            else -> 20
        }
        
        // Break score - calculated based on usage patterns (simplified for now)
        val breakScore = calculateBreakScore(dashboardState)
        
        // Sleep score - based on late night usage (simplified)
        val sleepScore = calculateSleepScore(dashboardState)
        
        // Consistency score - based on historical data (simplified)
        val consistencyScore = calculateConsistencyScore(dashboardState)
        
        return WellnessFactors(
            screenTimeScore = screenTimeScore,
            unlockFrequencyScore = unlockFrequencyScore,
            appUsageScore = appUsageScore,
            breakScore = breakScore,
            sleepScore = sleepScore,
            consistencyScore = consistencyScore
        )
    }
    
    fun calculateWellnessFactorsFromScore(
        dashboardState: DashboardState,
        wellnessScore: dev.sadakat.screentimetracker.domain.model.WellnessScore,
        targetScreenTimeHours: Float = 4f
    ): WellnessFactors {
        // Use the dynamic wellness score components directly
        val screenTimeScore = wellnessScore.timeLimitScore
        val unlockFrequencyScore = wellnessScore.breaksScore // Break score correlates with unlock patterns
        val appUsageScore = calculateAppUsageScore(dashboardState) // Keep app usage calculation
        val breakScore = wellnessScore.breaksScore
        val sleepScore = wellnessScore.sleepHygieneScore
        val consistencyScore = calculateConsistencyScore(dashboardState) // Keep consistency calculation
        
        return WellnessFactors(
            screenTimeScore = screenTimeScore,
            unlockFrequencyScore = unlockFrequencyScore,
            appUsageScore = appUsageScore,
            breakScore = breakScore,
            sleepScore = sleepScore,
            consistencyScore = consistencyScore
        )
    }
    
    private fun calculateAppUsageScore(dashboardState: DashboardState): Int {
        val appOpens = dashboardState.appUsagesToday.sumOf { it.openCount }
        return when {
            appOpens <= 30 -> 100
            appOpens <= 60 -> 80
            appOpens <= 100 -> 60
            appOpens <= 150 -> 40
            else -> 20
        }
    }
    
    private fun calculateBreakScore(state: DashboardState): Int {
        // Simplified: assume good breaks if unlocks are spaced reasonably
        val unlocks = state.totalScreenUnlocksToday
        return when {
            unlocks == 0 -> 100 // No usage at all
            unlocks <= 30 -> 90 // Good spacing
            unlocks <= 60 -> 70 // Moderate
            unlocks <= 100 -> 50 // Frequent but not terrible
            else -> 30 // Poor break patterns
        }
    }
    
    private fun calculateSleepScore(state: DashboardState): Int {
        // Simplified: assume good sleep hygiene for now
        // In real implementation, check for late night usage
        return 80
    }
    
    private fun calculateConsistencyScore(state: DashboardState): Int {
        // Simplified: use current data as proxy
        // In real implementation, compare with historical averages
        val screenTimeHours = state.totalScreenTimeTodayMillis / HOURS_TO_MILLISECONDS.toFloat()
        val avgLastWeek = state.averageDailyScreenTimeMillisLastWeek / HOURS_TO_MILLISECONDS.toFloat()
        
        return if (avgLastWeek > 0) {
            val variation = kotlin.math.abs(screenTimeHours - avgLastWeek) / avgLastWeek
            when {
                variation <= 0.2f -> 100 // Very consistent
                variation <= 0.4f -> 80  // Good consistency
                variation <= 0.6f -> 60  // Moderate
                variation <= 0.8f -> 40  // Poor
                else -> 20 // Very inconsistent
            }
        } else {
            75 // Default score for new users
        }
    }
    
    fun updatePetState(
        currentPet: DigitalPet,
        wellnessFactors: WellnessFactors,
        daysSinceLastUpdate: Int = 0
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
        val newLevel = calculateLevel(newExperience)
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
            lastWellnessCheck = currentTime,
            updatedAt = currentTime
        )
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
    
    private fun calculateLevel(experience: Int): Int {
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
    
    fun getPetStats(pet: DigitalPet): PetStats {
        val currentLevel = pet.level
        val currentExp = pet.experiencePoints
        val expForCurrentLevel = getExperienceRequiredForLevel(currentLevel)
        val expForNextLevel = getExperienceRequiredForLevel(currentLevel + 1)
        val expToNextLevel = expForNextLevel - currentExp
        
        val mood = calculateMood(pet)
        val wellnessScore = calculateOverallWellnessScore(pet)
        val evolutionStage = calculateEvolutionStage(pet)
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
    
    private fun getExperienceRequiredForLevel(level: Int): Int {
        var totalExp = 0
        for (i in 1 until level) {
            totalExp += (BASE_EXPERIENCE_PER_LEVEL * (1.2 * i)).toInt()
        }
        return totalExp
    }
    
    private fun calculateMood(pet: DigitalPet): PetMood {
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
    
    private fun calculateOverallWellnessScore(pet: DigitalPet): Int {
        // Base score from pet stats
        val baseScore = (pet.health + pet.happiness + pet.energy) / 3
        
        // Bonus for streak and level
        val streakBonus = min(10, pet.wellnessStreakDays)
        val levelBonus = min(15, pet.level * 2)
        
        return min(100, baseScore + streakBonus + levelBonus)
    }
    
    private fun calculateEvolutionStage(pet: DigitalPet): Int {
        return when {
            pet.level >= 25 -> 4 // Legendary
            pet.level >= 20 -> 3 // Master
            pet.level >= 15 -> 2 // Advanced
            pet.level >= 10 -> 1 // Intermediate
            else -> 0 // Beginner
        }
    }
    
    fun getMotivationalMessage(petStats: PetStats, petType: PetType): String {
        return when (petStats.mood) {
            PetMood.THRIVING -> "Your ${petType.displayName} is absolutely glowing! Keep up the excellent digital wellness! ✨"
            PetMood.HAPPY -> "Your ${petType.displayName} is delighted with your balanced approach to technology! 😊"
            PetMood.CONTENT -> "Your ${petType.displayName} appreciates your mindful usage. Small improvements can make a big difference!"
            PetMood.CONCERNED -> "Your ${petType.displayName} is a bit worried. Try reducing screen time or taking more breaks. 😟"
            PetMood.SICK -> "Your ${petType.displayName} needs your attention! Focus on digital wellness to help them recover. 💚"
            PetMood.SLEEPING -> "Your ${petType.displayName} is resting. Perfect time for a digital break! 😴"
        }
    }
}