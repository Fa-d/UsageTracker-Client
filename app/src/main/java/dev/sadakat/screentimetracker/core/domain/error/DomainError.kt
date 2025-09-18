package dev.sadakat.screentimetracker.core.domain.error

/**
 * Base class for all domain-specific errors.
 * Provides clean error handling without framework dependencies.
 */
sealed class DomainError : Exception() {
    abstract val errorCode: String
    abstract val userMessage: String
    abstract val technicalMessage: String

    // ==================== Validation Errors ====================

    data class ValidationError(
        val field: String,
        val violation: String,
        override val technicalMessage: String = "Validation failed for field '$field': $violation"
    ) : DomainError() {
        override val errorCode: String = "VALIDATION_ERROR"
        override val userMessage: String = "Invalid $field: $violation"
    }

    data class BusinessRuleViolation(
        val rule: String,
        override val technicalMessage: String
    ) : DomainError() {
        override val errorCode: String = "BUSINESS_RULE_VIOLATION"
        override val userMessage: String = "Operation not allowed: $rule"
    }

    // ==================== Data Access Errors ====================

    data class EntityNotFound(
        val entityType: String,
        val entityId: String,
        override val technicalMessage: String = "$entityType with ID '$entityId' not found"
    ) : DomainError() {
        override val errorCode: String = "ENTITY_NOT_FOUND"
        override val userMessage: String = "$entityType not found"
    }

    data class DataAccessError(
        val operation: String,
        override val technicalMessage: String
    ) : DomainError() {
        override val errorCode: String = "DATA_ACCESS_ERROR"
        override val userMessage: String = "Unable to $operation. Please try again."
    }

    data class ConcurrencyError(
        val entityType: String,
        override val technicalMessage: String
    ) : DomainError() {
        override val errorCode: String = "CONCURRENCY_ERROR"
        override val userMessage: String = "$entityType was modified by another user. Please refresh and try again."
    }

    // ==================== Business Logic Errors ====================

    data class WellnessCalculationError(
        val reason: String,
        override val technicalMessage: String
    ) : DomainError() {
        override val errorCode: String = "WELLNESS_CALCULATION_ERROR"
        override val userMessage: String = "Unable to calculate wellness score: $reason"
    }

    data class GoalConstraintViolation(
        val goalType: String,
        val constraint: String,
        override val technicalMessage: String
    ) : DomainError() {
        override val errorCode: String = "GOAL_CONSTRAINT_VIOLATION"
        override val userMessage: String = "Goal cannot be created: $constraint"
    }

    data class AchievementError(
        val achievementId: String,
        val reason: String,
        override val technicalMessage: String
    ) : DomainError() {
        override val errorCode: String = "ACHIEVEMENT_ERROR"
        override val userMessage: String = "Achievement operation failed: $reason"
    }

    data class SessionTrackingError(
        val reason: String,
        override val technicalMessage: String
    ) : DomainError() {
        override val errorCode: String = "SESSION_TRACKING_ERROR"
        override val userMessage: String = "Unable to track session: $reason"
    }

    // ==================== Time-related Errors ====================

    data class TimeRangeError(
        val reason: String,
        override val technicalMessage: String
    ) : DomainError() {
        override val errorCode: String = "TIME_RANGE_ERROR"
        override val userMessage: String = "Invalid time range: $reason"
    }

    data class DateOutOfBounds(
        val date: Long,
        val validRange: String,
        override val technicalMessage: String = "Date $date is outside valid range: $validRange"
    ) : DomainError() {
        override val errorCode: String = "DATE_OUT_OF_BOUNDS"
        override val userMessage: String = "Date is outside valid range"
    }

    // ==================== Limit and Constraint Errors ====================

    data class LimitExceeded(
        val limitType: String,
        val currentValue: Long,
        val limitValue: Long,
        override val technicalMessage: String = "$limitType limit exceeded: $currentValue > $limitValue"
    ) : DomainError() {
        override val errorCode: String = "LIMIT_EXCEEDED"
        override val userMessage: String = "$limitType limit has been exceeded"
    }

    data class QuotaExceeded(
        val quotaType: String,
        val remaining: Long,
        override val technicalMessage: String = "$quotaType quota exceeded, $remaining remaining"
    ) : DomainError() {
        override val errorCode: String = "QUOTA_EXCEEDED"
        override val userMessage: String = "You have exceeded your $quotaType quota"
    }

    // ==================== Configuration Errors ====================

    data class ConfigurationError(
        val setting: String,
        override val technicalMessage: String
    ) : DomainError() {
        override val errorCode: String = "CONFIGURATION_ERROR"
        override val userMessage: String = "Configuration error with $setting"
    }

    data class FeatureNotAvailable(
        val featureName: String,
        override val technicalMessage: String = "Feature '$featureName' is not available"
    ) : DomainError() {
        override val errorCode: String = "FEATURE_NOT_AVAILABLE"
        override val userMessage: String = "$featureName is not available"
    }

    // ==================== System Errors ====================

    data class SystemError(
        val component: String,
        override val technicalMessage: String
    ) : DomainError() {
        override val errorCode: String = "SYSTEM_ERROR"
        override val userMessage: String = "System error occurred. Please try again later."
    }

    data class ServiceUnavailable(
        val serviceName: String,
        override val technicalMessage: String
    ) : DomainError() {
        override val errorCode: String = "SERVICE_UNAVAILABLE"
        override val userMessage: String = "$serviceName is temporarily unavailable"
    }

    // ==================== Helper Methods ====================

    fun isRetryable(): Boolean {
        return when (this) {
            is DataAccessError,
            is SystemError,
            is ServiceUnavailable -> true
            else -> false
        }
    }

    fun getSeverity(): ErrorSeverity {
        return when (this) {
            is ValidationError,
            is TimeRangeError -> ErrorSeverity.LOW

            is BusinessRuleViolation,
            is GoalConstraintViolation,
            is LimitExceeded -> ErrorSeverity.MEDIUM

            is EntityNotFound,
            is ConcurrencyError,
            is ConfigurationError -> ErrorSeverity.HIGH

            is DataAccessError,
            is SystemError,
            is ServiceUnavailable -> ErrorSeverity.CRITICAL

            else -> ErrorSeverity.MEDIUM
        }
    }

    companion object {
        // Factory methods for common errors
        fun invalidTimeRange(startMillis: Long, endMillis: Long): TimeRangeError {
            return TimeRangeError(
                reason = "Start time must be before end time",
                technicalMessage = "Invalid time range: start=$startMillis, end=$endMillis"
            )
        }

        fun goalNotFound(goalId: String): EntityNotFound {
            return EntityNotFound(
                entityType = "Goal",
                entityId = goalId
            )
        }

        fun achievementNotFound(achievementId: String): EntityNotFound {
            return EntityNotFound(
                entityType = "Achievement",
                entityId = achievementId
            )
        }

        fun invalidWellnessScore(score: Int): ValidationError {
            return ValidationError(
                field = "wellness score",
                violation = "must be between 0 and 100, got $score"
            )
        }

        fun screenTimeLimitExceeded(currentTime: Long, limit: Long): LimitExceeded {
            return LimitExceeded(
                limitType = "Screen time",
                currentValue = currentTime,
                limitValue = limit
            )
        }

        fun unlockLimitExceeded(currentUnlocks: Long, limit: Long): LimitExceeded {
            return LimitExceeded(
                limitType = "Phone unlock",
                currentValue = currentUnlocks,
                limitValue = limit
            )
        }

        fun goalAlreadyExists(goalType: String): BusinessRuleViolation {
            return BusinessRuleViolation(
                rule = "Only one active goal of type '$goalType' is allowed",
                technicalMessage = "Duplicate goal type: $goalType"
            )
        }

        fun invalidSessionDuration(duration: Long): ValidationError {
            return ValidationError(
                field = "session duration",
                violation = "must be positive, got $duration"
            )
        }
    }
}

enum class ErrorSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

/**
 * Result wrapper for domain operations that may fail
 */
sealed class DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>()
    data class Failure(val error: DomainError) : DomainResult<Nothing>()

    inline fun <R> map(transform: (T) -> R): DomainResult<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Failure -> this
        }
    }

    inline fun <R> flatMap(transform: (T) -> DomainResult<R>): DomainResult<R> {
        return when (this) {
            is Success -> transform(data)
            is Failure -> this
        }
    }

    inline fun onSuccess(action: (T) -> Unit): DomainResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onFailure(action: (DomainError) -> Unit): DomainResult<T> {
        if (this is Failure) action(error)
        return this
    }

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
    }

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Failure -> throw error
    }

    companion object {
        fun <T> success(data: T): DomainResult<T> = Success(data)
        fun failure(error: DomainError): DomainResult<Nothing> = Failure(error)

        inline fun <T> catch(action: () -> T): DomainResult<T> {
            return try {
                Success(action())
            } catch (e: DomainError) {
                Failure(e)
            } catch (e: Exception) {
                Failure(
                    DomainError.SystemError(
                        component = "Unknown",
                        technicalMessage = e.message ?: "Unknown error"
                    )
                )
            }
        }
    }
}