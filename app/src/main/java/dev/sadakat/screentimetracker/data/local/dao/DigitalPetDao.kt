package dev.sadakat.screentimetracker.data.local.dao
import dev.sadakat.screentimetracker.data.local.entities.*

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DigitalPetDao {
    
    @Query("SELECT * FROM digital_pet WHERE id = 1")
    fun getPet(): Flow<DigitalPet?>
    
    @Query("SELECT * FROM digital_pet WHERE id = 1")
    suspend fun getPetSync(): DigitalPet?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePet(pet: DigitalPet)
    
    @Update
    suspend fun updatePet(pet: DigitalPet)
    
    @Query("DELETE FROM digital_pet")
    suspend fun deleteAllPets()
    
    @Query("SELECT COUNT(*) FROM digital_pet")
    suspend fun getPetCount(): Int
    
    @Query("""
        UPDATE digital_pet 
        SET health = :health, 
            happiness = :happiness, 
            energy = :energy, 
            experience_points = :experiencePoints,
            level = :level,
            wellness_streak_days = :wellnessStreakDays,
            last_wellness_check = :lastWellnessCheck,
            updated_at = :updatedAt
        WHERE id = 1
    """)
    suspend fun updatePetStats(
        health: Int,
        happiness: Int,
        energy: Int,
        experiencePoints: Int,
        level: Int,
        wellnessStreakDays: Int,
        lastWellnessCheck: Long,
        updatedAt: Long
    )
    
    @Query("UPDATE digital_pet SET name = :name, updated_at = :updatedAt WHERE id = 1")
    suspend fun updatePetName(name: String, updatedAt: Long)
    
    @Query("UPDATE digital_pet SET pet_type = :petType, updated_at = :updatedAt WHERE id = 1")
    suspend fun updatePetType(petType: PetType, updatedAt: Long)
    
    @Query("SELECT wellness_streak_days FROM digital_pet WHERE id = 1")
    suspend fun getWellnessStreak(): Int?
    
    @Query("SELECT level FROM digital_pet WHERE id = 1")
    suspend fun getPetLevel(): Int?
    
    @Query("SELECT last_wellness_check FROM digital_pet WHERE id = 1")
    suspend fun getLastWellnessCheck(): Long?
}