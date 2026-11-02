package com.toeicspace.android.core.network

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.network.model.ProblemDetailsDto
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

private const val VALIDATION_ERROR_CODE = "VALIDATION_ERROR"

/** Runs a Retrofit call and turns transport and HTTP failures into [AppError]. */
suspend fun <T> safeApiCall(
    json: Json,
    call: suspend () -> T,
): AppResult<T> =
    try {
        AppResult.Success(call())
    } catch (exception: HttpException) {
        AppResult.Failure(exception.toAppError(json))
    } catch (_: NoConnectivityException) {
        AppResult.Failure(AppError.Network)
    } catch (_: IOException) {
        AppResult.Failure(AppError.ServerUnreachable)
    } catch (_: SerializationException) {
        AppResult.Failure(AppError.Unknown)
    }

internal fun HttpException.toAppError(json: Json): AppError {
    val body =
        try {
            response()?.errorBody()?.use { it.string() }
        } catch (_: IOException) {
            null
        }
    return parseProblemDetails(json, body).toAppError(status = code())
}

internal fun parseProblemDetails(
    json: Json,
    body: String?,
): ProblemDetailsDto? =
    body?.let {
        try {
            json.decodeFromString(ProblemDetailsDto.serializer(), it)
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

/** Error codes differ in case between services, so they are normalised to upper case. */
internal fun ProblemDetailsDto?.toAppError(status: Int): AppError {
    val fields =
        this
            ?.errors
            .orEmpty()
            .mapKeys { (key, _) -> key.replaceFirstChar(Char::lowercaseChar) }
    val code = this?.code?.uppercase()
    return if (fields.isNotEmpty() && (code == null || code == VALIDATION_ERROR_CODE)) {
        AppError.Validation(fields)
    } else {
        AppError.Server(code = code, status = status)
    }
}
