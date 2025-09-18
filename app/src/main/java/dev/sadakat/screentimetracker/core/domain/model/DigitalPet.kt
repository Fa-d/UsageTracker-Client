package dev.sadakat.screentimetracker.core.domain.model

data class DigitalPet(
    val id: String,
    val name: String,
    val petType: PetType,
    val level: Int,
    val experiencePoints: Int,
    val health: Int,
    val happiness: Int,
    val energy: Int,
    val wellnessStreakDays: Int,
    val createdAt: Long,
    val lastWellnessCheck: Long
) {
    init {
        require(level >= 1) { "Pet level must be at least 1" }
        require(experiencePoints >= 0) { "Experience points cannot be negative" }
        require(health in 0..100) { "Health must be between 0 and 100" }
        require(happiness in 0..100) { "Happiness must be between 0 and 100" }
        require(energy in 0..100) { "Energy must be between 0 and 100" }
        require(wellnessStreakDays >= 0) { "Wellness streak cannot be negative" }
    }

    val mood: PetMood
        get() = calculateMood()

    val evolutionStage: Int
        get() = calculateEvolutionStage()

    val experienceToNextLevel: Int
        get() = calculateExperienceToNextLevel()

    val daysSinceCreated: Int
        get() = ((System.currentTimeMillis() - createdAt) / (24 * 60 * 60 * 1000)).toInt()

    fun needsAttention(): Boolean {
        return health < 30 || happiness < 30 || energy < 20
    }

    fun isFlourishing(): Boolean {
        return health >= 80 && happiness >= 80 && energy >= 70
    }

    fun canEvolve(): Boolean {
        val requiredExp = calculateExperienceToNextLevel()
        return experiencePoints >= requiredExp && level < 50
    }

    fun evolve(): DigitalPet {
        require(canEvolve()) { "Pet cannot evolve yet" }
        return copy(
            level = level + 1,
            experiencePoints = experiencePoints - calculateExperienceToNextLevel()
        )
    }

    fun gainExperience(points: Int): DigitalPet {
        require(points >= 0) { "Experience points must be positive" }
        return copy(experiencePoints = experiencePoints + points)
    }

    fun updateStats(
        healthChange: Int = 0,
        happinessChange: Int = 0,
        energyChange: Int = 0
    ): DigitalPet {
        return copy(
            health = (health + healthChange).coerceIn(0, 100),
            happiness = (happiness + happinessChange).coerceIn(0, 100),
            energy = (energy + energyChange).coerceIn(0, 100),
            lastWellnessCheck = System.currentTimeMillis()
        )
    }

    private fun calculateMood(): PetMood {
        val averageStats = (health + happiness + energy) / 3
        return when {
            averageStats >= 90 -> PetMood.THRIVING
            averageStats >= 70 -> PetMood.HAPPY
            averageStats >= 50 -> PetMood.CONTENT
            averageStats >= 30 -> PetMood.CONCERNED
            averageStats >= 10 -> PetMood.SICK
            else -> PetMood.SLEEPING
        }
    }

    private fun calculateEvolutionStage(): Int {
        return when {
            level >= 40 -> 4
            level >= 30 -> 3
            level >= 20 -> 2
            level >= 10 -> 1
            else -> 0
        }
    }

    private fun calculateExperienceToNextLevel(): Int {
        return level * 100 + (level * level * 10)
    }

    companion object {
        fun create(
            name: String,
            petType: PetType,
            id: String = java.util.UUID.randomUUID().toString()
        ): DigitalPet {
            return DigitalPet(
                id = id,
                name = name,
                petType = petType,
                level = 1,
                experiencePoints = 0,
                health = 100,
                happiness = 100,
                energy = 100,
                wellnessStreakDays = 0,
                createdAt = System.currentTimeMillis(),
                lastWellnessCheck = System.currentTimeMillis()
            )
        }
    }
}

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