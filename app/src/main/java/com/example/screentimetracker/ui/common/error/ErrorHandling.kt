package com.example.screentimetracker.ui.common.error

import androidx.compose.runtime.Stable

/**
 * Represents different types of errors that can occur in the application
 */
@Stable
sealed class AppError(
    open val message: String,
    open val cause: Throwable? = null
) {
    data class NetworkError(
        override val message: String = "Network connection failed",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class DatabaseError(
        override val message: String = "Database operation failed",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class PermissionError(
        override val message: String = "Permission required",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class ServiceError(
        override val message: String = "Service operation failed",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class ValidationError(
        override val message: String = "Invalid input",
        override val cause: Throwable? = null
    ) : AppError(message, cause)

    data class UnknownError(
        override val message: String = "An unexpected error occurred",
        override val cause: Throwable? = null
    ) : AppError(message, cause)
}

/**
 * Result wrapper that encapsulates success and error states
 */
@Stable
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val error: AppError) : Result<Nothing>()
    object Loading : Result<Nothing>()

    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (AppError) -> Unit): Result<T> {
        if (this is Error) action(error)
        return this
    }

    inline fun onLoading(action: () -> Unit): Result<T> {
        if (this is Loading) action()
        return this
    }
}

/**
 * Extension functions to convert exceptions to AppError
 */
fun Throwable.toAppError(): AppError = when (this) {
    is java.net.UnknownHostException -> AppError.NetworkError(cause = this)
    is java.sql.SQLException -> AppError.DatabaseError(cause = this)
    is SecurityException -> AppError.PermissionError(cause = this)
    is IllegalArgumentException -> AppError.ValidationError(message = this.message ?: "Invalid input", cause = this)
    else -> AppError.UnknownError(message = this.message ?: "Unknown error", cause = this)
}
