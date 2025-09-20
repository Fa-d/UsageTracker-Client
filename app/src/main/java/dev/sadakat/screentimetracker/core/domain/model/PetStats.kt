package dev.sadakat.screentimetracker.core.domain.model

/**
 * Domain model for pet statistics
 */
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
)