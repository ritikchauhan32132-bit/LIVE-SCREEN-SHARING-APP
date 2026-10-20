package com.ritiksc.remoteaccess.common

/**
 * Sealed class representing the result of an operation.
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Throwable? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()

    val isSuccess get() = this is Success
    val isError get() = this is Error
    val isLoading get() = this is Loading

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun errorMessageOrNull(): String? = when (this) {
        is Error -> message
        else -> null
    }

    companion object {
        fun <T> success(data: T) = Success(data)
        fun error(message: String, exception: Throwable? = null) = Error(message, exception)
        fun loading() = Loading
    }
}

/**
 * UI state wrapper for screens.
 */
data class UiState<T>(
    val data: T? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEmpty: Boolean = false
) {
    companion object {
        fun <T> loading() = UiState<T>(isLoading = true)
        fun <T> success(data: T) = UiState(data = data)
        fun <T> error(message: String) = UiState<T>(error = message)
        fun <T> empty() = UiState<T>(isEmpty = true)
    }
}
