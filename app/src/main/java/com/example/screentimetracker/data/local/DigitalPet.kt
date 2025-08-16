package com.example.screentimetracker.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "digital_pet")
data class DigitalPet(
    @PrimaryKey
    val id: Int = 1, // Single pet instance
    
    @ColumnInfo(name = "name")
    val name: String = "Zen",
    
    @ColumnInfo(name = "pet_type")
    val petType: PetType = PetType.TREE,
    
    @ColumnInfo(name = "level")
    val level: Int = 1,
    
    @ColumnInfo(name = "experience_points")
    val experiencePoints: Int = 0,
    
    @ColumnInfo(name = "health")
    val health: Int = 100,
    
    @ColumnInfo(name = "happiness")
    val happiness: Int = 100,
    
    @ColumnInfo(name = "energy")
    val energy: Int = 100,
    
    @ColumnInfo(name = "wellness_streak_days")
    val wellnessStreakDays: Int = 0,
    
    @ColumnInfo(name = "last_fed_timestamp")
    val lastFedTimestamp: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "last_wellness_check")
    val lastWellnessCheck: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

enum class PetType(
    val displayName: String,
    val emoji: String,
    val description: String
) {
    TREE("Digital Tree", "🌱", "Grows stronger with your digital wellness"),
    CAT("Zen Cat", "🐱", "Purrs when you maintain healthy habits"),
    BIRD("Focus Bird", "🐦", "Soars higher with your concentration"),
    ROBOT("Wellness Bot", "🤖", "Evolves with your mindful usage"),
    DRAGON("Wisdom Dragon", "🐉", "Gains power from your balance")
}

enum class PetMood(
    val displayName: String,
    val emoji: String,
    val description: String
) {
    THRIVING("Thriving", "✨", "Your pet is flourishing with excellent digital wellness!"),
    HAPPY("Happy", "😊", "Your pet is content with good digital habits"),
    CONTENT("Content", "😌", "Your pet is doing well, maintain the balance"),
    CONCERNED("Concerned", "😟", "Your pet is worried about your screen time"),
    SICK("Sick", "😵", "Your pet needs attention - improve your digital wellness"),
    SLEEPING("Sleeping", "😴", "Your pet is resting during your break time")
}

data class PetStats(
    val level: Int,
    val experiencePoints: Int,
    val experienceToNextLevel: Int,
    val health: Int,
    val happiness: Int,
    val energy: Int,
    val mood: PetMood,
    val wellnessScore: Int,
    val evolutionStage: Int,
    val daysSinceCreated: Int
) {
    fun getEvolutionEmoji(petType: PetType): String {
        return when (petType) {
            PetType.TREE -> when (evolutionStage) {
                0 -> "🌱" // Seedling
                1 -> "🌿" // Sprout
                2 -> "🌳" // Tree
                3 -> "🌲" // Mature tree
                else -> "🌸" // Flowering tree
            }
            PetType.CAT -> when (evolutionStage) {
                0 -> "🐱" // Kitten
                1 -> "😸" // Happy cat
                2 -> "😻" // Heart-eyes cat
                3 -> "🦁" // Lion
                else -> "👑" // Royal cat
            }
            PetType.BIRD -> when (evolutionStage) {
                0 -> "🐣" // Chick
                1 -> "🐦" // Bird
                2 -> "🕊️" // Dove
                3 -> "🦅" // Eagle
                else -> "🪶" // Phoenix
            }
            PetType.ROBOT -> when (evolutionStage) {
                0 -> "🤖" // Basic robot
                1 -> "🛸" // UFO
                2 -> "⚡" // Energy
                3 -> "🔮" // Crystal ball
                else -> "✨" // Sparkles
            }
            PetType.DRAGON -> when (evolutionStage) {
                0 -> "🐉" // Dragon
                1 -> "🔥" // Fire
                2 -> "⚡" // Lightning
                3 -> "🌟" // Star
                else -> "🏆" // Trophy
            }
        }
    }
}

data class WellnessFactors(
    val screenTimeScore: Int, // 0-100 based on daily screen time goals
    val unlockFrequencyScore: Int, // 0-100 based on unlock patterns
    val appUsageScore: Int, // 0-100 based on productive vs unproductive app usage
    val breakScore: Int, // 0-100 based on taking regular breaks
    val sleepScore: Int, // 0-100 based on night mode usage
    val consistencyScore: Int, // 0-100 based on maintaining routines
    val overallScore: Int = (screenTimeScore + unlockFrequencyScore + appUsageScore + breakScore + sleepScore + consistencyScore) / 6
)