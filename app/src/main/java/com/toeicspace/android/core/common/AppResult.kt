package com.toeicspace.android.core.common

/** Outcome of a repository call. Loading is UI state, so it is not modelled here. */
sealed interface AppResult<out T> {
    data class Success<T>(
        val data: T,
    ) : AppResult<T>

    data class Failure(
        val error: AppError,
    ) : AppResult<Nothing>
}

sealed interface AppError {
    /** The device has no network connection. */
    data object Network : AppError

    /** The device is online but the server did not answer: refused, timed out or DNS failed. */
    data object ServerUnreachable : AppError

    /** The session could not be refreshed; the user has to sign in again. */
    data object Unauthorized : AppError

    /** Field errors from the backend, keyed by field name in lower camel case. */
    data class Validation(
        val fields: Map<String, List<String>>,
    ) : AppError

    data class Server(
        val code: String?,
        val status: Int,
    ) : AppError

    data object Unknown : AppError
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> =
    when (this) {
        is AppResult.Success -> AppResult.Success(transform(data))
        is AppResult.Failure -> this
    }
