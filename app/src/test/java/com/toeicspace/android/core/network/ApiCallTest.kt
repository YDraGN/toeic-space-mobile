package com.toeicspace.android.core.network

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.ConnectException

class ApiCallTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

    @Test
    fun `validation problem keeps field errors with camel case keys`() {
        val body =
            """
            {"status":400,"title":"Bad Request","code":"VALIDATION_ERROR",
             "errors":{"Email":["Email is invalid."]}}
            """.trimIndent()

        val error = parseProblemDetails(json, body).toAppError(status = 400)

        assertEquals(AppError.Validation(mapOf("email" to listOf("Email is invalid."))), error)
    }

    @Test
    fun `codes are normalised to upper case`() {
        val error =
            parseProblemDetails(
                json,
                """{"status":404,"code":"class_not_found"}""",
            ).toAppError(404)

        assertEquals(AppError.Server(code = "CLASS_NOT_FOUND", status = 404), error)
    }

    @Test
    fun `offline device maps to network error`() =
        runTest {
            val result = safeApiCall(json) { throw NoConnectivityException() }

            assertEquals(AppResult.Failure(AppError.Network), result)
        }

    @Test
    fun `server that does not answer maps to server unreachable`() =
        runTest {
            val result = safeApiCall(json) { throw ConnectException("Connection refused") }

            assertEquals(AppResult.Failure(AppError.ServerUnreachable), result)
        }

    @Test
    fun `unreadable body still maps to a server error`() {
        val error = parseProblemDetails(json, "<html>Bad gateway</html>").toAppError(502)

        assertEquals(AppError.Server(code = null, status = 502), error)
    }
}
