package dev.sadakat.screentimetracker.core.data.repository

import dev.sadakat.screentimetracker.core.data.local.entities.DigitalPet
import dev.sadakat.screentimetracker.core.data.local.dao.DigitalPetDao
import dev.sadakat.screentimetracker.core.data.local.entities.PetStats
import dev.sadakat.screentimetracker.core.data.local.entities.PetType
import dev.sadakat.screentimetracker.core.data.local.entities.WellnessFactors
import dev.sadakat.screentimetracker.ui.dashboard.state.DashboardState
import dev.sadakat.screentimetracker.utils.DigitalPetManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DigitalPetRepository @Inject constructor(
    private val digitalPetDao: DigitalPetDao
) {
    
    fun getPet(): Flow<DigitalPet?> = digitalPetDao.getPet()
    
    suspend fun getPetSync(): DigitalPet? = digitalPetDao.getPetSync()
    
    suspend fun createDefaultPet(): DigitalPet {
        val currentTime = System.currentTimeMillis()
        val defaultPet = DigitalPet(
            id = 1,
            name = "Zen",
            petType = PetType.TREE,
            level = 1,
            experiencePoints = 0,
            health = 100,
            happiness = 100,
            energy = 100,
            wellnessStreakDays = 0,
            lastFedTimestamp = currentTime,
            lastWellnessCheck = currentTime,
            createdAt = currentTime,
            updatedAt = currentTime
        )
        
        digitalPetDao.insertOrUpdatePet(defaultPet)
        return defaultPet
    }
    
    suspend fun updatePetWithWellness(
        dashboardState: DashboardState,
        targetScreenTimeHours: Float = 4f,
        wellnessScore: dev.sadakat.screentimetracker.domain.model.WellnessScore? = null
    ): DigitalPet {
        var pet = getPetSync()
        
        // Create default pet if none exists
        if (pet == null) {
            pet = createDefaultPet()
        }
        
        // Calculate wellness factors based on current dashboard state and dynamic wellness score
        val wellnessFactors = if (wellnessScore != null) {
            DigitalPetManager.calculateWellnessFactorsFromScore(
                dashboardState = dashboardState,
                wellnessScore = wellnessScore,
                targetScreenTimeHours = targetScreenTimeHours
            )
        } else {
            DigitalPetManager.calculateWellnessFactors(
                dashboardState = dashboardState,
                targetScreenTimeHours = targetScreenTimeHours
            )
        }
        
        // Calculate days since last update
        val daysSinceLastUpdate = calculateDaysSinceLastUpdate(pet.lastWellnessCheck)
        
        // Update pet state based on wellness
        val updatedPet = DigitalPetManager.updatePetState(
            currentPet = pet,
            wellnessFactors = wellnessFactors,
            daysSinceLastUpdate = daysSinceLastUpdate
        )
        
        // Save updated pet to database
        digitalPetDao.insertOrUpdatePet(updatedPet)
        
        return updatedPet
    }
    
    suspend fun getPetStats(pet: DigitalPet? = null): PetStats {
        val currentPet = pet ?: getPetSync() ?: createDefaultPet()
        return DigitalPetManager.getPetStats(currentPet)
    }
    
    fun getPetStatsFlow(): Flow<PetStats> {
        return getPet().map { pet ->
            if (pet != null) {
                DigitalPetManager.getPetStats(pet)
            } else {
                val defaultPet = DigitalPet()
                DigitalPetManager.getPetStats(defaultPet)
            }
        }
    }
    
    suspend fun updatePetName(newName: String) {
        digitalPetDao.updatePetName(newName, System.currentTimeMillis())
    }
    
    suspend fun updatePetType(newType: PetType) {
        digitalPetDao.updatePetType(newType, System.currentTimeMillis())
    }
    
    suspend fun feedPet(): DigitalPet? {
        val pet = getPetSync() ?: return null
        
        val fedPet = pet.copy(
            happiness = minOf(100, pet.happiness + 10),
            energy = minOf(100, pet.energy + 5),
            lastFedTimestamp = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        digitalPetDao.insertOrUpdatePet(fedPet)
        return fedPet
    }
    
    suspend fun petInteraction(): DigitalPet? {
        val pet = getPetSync() ?: return null
        
        val interactedPet = pet.copy(
            happiness = minOf(100, pet.happiness + 5),
            experiencePoints = pet.experiencePoints + 1,
            updatedAt = System.currentTimeMillis()
        )
        
        digitalPetDao.insertOrUpdatePet(interactedPet)
        return interactedPet
    }
    
    suspend fun resetPet(): DigitalPet {
        digitalPetDao.deleteAllPets()
        return createDefaultPet()
    }
    
    suspend fun calculateWellnessFactors(dashboardState: DashboardState): WellnessFactors {
        return DigitalPetManager.calculateWellnessFactors(dashboardState)
    }
    
    suspend fun getMotivationalMessage(): String {
        val pet = getPetSync() ?: return "Welcome to your digital wellness journey!"
        val stats = getPetStats(pet)
        return DigitalPetManager.getMotivationalMessage(stats, pet.petType)
    }
    
    private fun calculateDaysSinceLastUpdate(lastWellnessCheck: Long): Int {
        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - lastWellnessCheck
        val daysDifference = timeDifference / (24 * 60 * 60 * 1000) // Convert to days
        return daysDifference.toInt()
    }
    
    suspend fun hasBeenUpdatedToday(): Boolean {
        val pet = getPetSync() ?: return false
        val daysSinceLastUpdate = calculateDaysSinceLastUpdate(pet.lastWellnessCheck)
        return daysSinceLastUpdate == 0
    }
    
    suspend fun shouldUpdatePetWellness(): Boolean {
        val pet = getPetSync() ?: return true
        val currentTime = System.currentTimeMillis()
        val timeSinceLastUpdate = currentTime - pet.lastWellnessCheck
        
        // Update pet wellness every 30 minutes to sync with dynamic wellness score
        val updateInterval = 30 * 60 * 1000L // 30 minutes
        
        return timeSinceLastUpdate > updateInterval
    }
    
    suspend fun getPetAge(): Int {
        val pet = getPetSync() ?: return 0
        val currentTime = System.currentTimeMillis()
        val ageInDays = (currentTime - pet.createdAt) / (24 * 60 * 60 * 1000)
        return ageInDays.toInt()
    }
    
    suspend fun isFirstTimeUser(): Boolean {
        return digitalPetDao.getPetCount() == 0
    }
}