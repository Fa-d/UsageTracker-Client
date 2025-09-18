package dev.sadakat.screentimetracker.core.domain.model

data class UserGoal(
    val id: String,
    val title: String,
    val description: String,
    val type: GoalType,
    val targetValue: Long,
    val currentProgress: Long = 0,
    val unit: GoalUnit,
    val deadline: Long? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val priority: GoalPriority = GoalPriority.MEDIUM
) {
    init {
        require(targetValue > 0) { "Target value must be positive" }
        require(currentProgress >= 0) { "Current progress cannot be negative" }
    }

    val progressPercentage: Float
        get() = if (targetValue > 0) (currentProgress.toFloat() / targetValue * 100).coerceAtMost(100f) else 0f

    val isCompleted: Boolean
        get() = currentProgress >= targetValue

    val isOverdue: Boolean
        get() = deadline != null && deadline < System.currentTimeMillis() && !isCompleted

    val daysUntilDeadline: Int?
        get() = deadline?.let {
            val days = (it - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
            days.toInt()
        }

    fun addProgress(amount: Long): UserGoal {
        require(amount >= 0) { "Progress amount must be positive" }
        val newProgress = (currentProgress + amount).coerceAtMost(targetValue)
        val completed = if (newProgress >= targetValue && completedAt == null) {
            System.currentTimeMillis()
        } else completedAt

        return copy(
            currentProgress = newProgress,
            completedAt = completed
        )
    }

    fun reset(): UserGoal {
        return copy(
            currentProgress = 0,
            completedAt = null
        )
    }

    fun getRemainingProgress(): Long {
        return (targetValue - currentProgress).coerceAtLeast(0)
    }

    companion object {
        fun screenTimeLimit(limitMillis: Long): UserGoal {
            return UserGoal(
                id = "screen_time_limit_${System.currentTimeMillis()}",
                title = "Daily Screen Time Limit",
                description = "Keep daily screen time under ${limitMillis / (60 * 60 * 1000)} hours",
                type = GoalType.SCREEN_TIME_LIMIT,
                targetValue = limitMillis,
                unit = GoalUnit.MILLISECONDS,
                priority = GoalPriority.HIGH
            )
        }

        fun unlockLimit(maxUnlocks: Int): UserGoal {
            return UserGoal(
                id = "unlock_limit_${System.currentTimeMillis()}",
                title = "Daily Unlock Limit",
                description = "Keep phone unlocks under $maxUnlocks per day",
                type = GoalType.UNLOCK_LIMIT,
                targetValue = maxUnlocks.toLong(),
                unit = GoalUnit.COUNT,
                priority = GoalPriority.MEDIUM
            )
        }

        fun focusSessionGoal(sessionCount: Int, durationMinutes: Int): UserGoal {
            return UserGoal(
                id = "focus_sessions_${System.currentTimeMillis()}",
                title = "Daily Focus Sessions",
                description = "Complete $sessionCount focus sessions of $durationMinutes minutes each",
                type = GoalType.FOCUS_SESSIONS,
                targetValue = sessionCount.toLong(),
                unit = GoalUnit.COUNT,
                priority = GoalPriority.HIGH
            )
        }

        fun productivityGoal(productiveTimeHours: Int): UserGoal {
            return UserGoal(
                id = "productivity_${System.currentTimeMillis()}",
                title = "Daily Productive Time",
                description = "Spend at least $productiveTimeHours hours on productive apps",
                type = GoalType.PRODUCTIVE_TIME,
                targetValue = (productiveTimeHours * 60 * 60 * 1000).toLong(),
                unit = GoalUnit.MILLISECONDS,
                priority = GoalPriority.MEDIUM
            )
        }
    }
}

enum class GoalType {
    SCREEN_TIME_LIMIT,
    UNLOCK_LIMIT,
    FOCUS_SESSIONS,
    PRODUCTIVE_TIME,
    APP_LIMIT,
    WELLNESS_STREAK,
    DIGITAL_DETOX,
    SLEEP_HYGIENE
}

enum class GoalUnit {
    MILLISECONDS,
    MINUTES,
    HOURS,
    COUNT,
    PERCENTAGE
}

enum class GoalPriority {
    LOW, MEDIUM, HIGH, CRITICAL
}