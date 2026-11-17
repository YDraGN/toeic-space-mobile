package com.toeicspace.android.core.ui.components

import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorStateContentTest {
    @Test
    fun network_showsOfflineMascotAndNetworkCopy() {
        val content = AppError.Network.toErrorStateContent()

        assertEquals(R.drawable.oy2_offline, content.mascotResId)
        assertEquals(R.string.state_error_network_title, content.titleRes)
        assertEquals(R.string.common_error_network, content.bodyRes)
        assertEquals(ErrorAction.RETRY, content.action)
    }

    @Test
    fun serverUnreachable_showsErrorMascotAndServerCopy() {
        val content = AppError.ServerUnreachable.toErrorStateContent()

        assertEquals(R.drawable.oy2_error, content.mascotResId)
        assertEquals(R.string.state_error_server_unreachable_title, content.titleRes)
        assertEquals(R.string.common_error_server_unreachable, content.bodyRes)
        assertEquals(ErrorAction.RETRY, content.action)
    }

    @Test
    fun networkAndServerErrors_readDifferently() {
        val network = AppError.Network.toErrorStateContent()
        val server = AppError.ServerUnreachable.toErrorStateContent()

        assertNotEquals(network.mascotResId, server.mascotResId)
        assertNotEquals(network.titleRes, server.titleRes)
        assertNotEquals(network.bodyRes, server.bodyRes)
    }

    @Test
    fun server5xx_isRetryableServerError() {
        val content = AppError.Server(code = null, status = 503).toErrorStateContent()

        assertEquals(R.drawable.oy2_error, content.mascotResId)
        assertEquals(R.string.state_error_server_title, content.titleRes)
        assertEquals(R.string.common_error_server, content.bodyRes)
        assertEquals(ErrorAction.RETRY, content.action)
    }

    @Test
    fun notFound_hasNoRetry() {
        val content = AppError.Server(code = "COURSE_NOT_FOUND", status = 404).toErrorStateContent()

        assertEquals(R.drawable.oy2_404, content.mascotResId)
        assertEquals(R.string.state_error_not_found_title, content.titleRes)
        assertEquals(ErrorAction.NONE, content.action)
    }

    @Test
    fun forbidden_hasNoRetry() {
        val content = AppError.Server(code = null, status = 403).toErrorStateContent()

        assertEquals(R.drawable.oy2_denied, content.mascotResId)
        assertEquals(ErrorAction.NONE, content.action)
    }

    @Test
    fun tooManyRequests_reusesExistingCopy() {
        val content = AppError.Server(code = null, status = 429).toErrorStateContent()

        assertEquals(R.string.auth_error_too_many_requests, content.bodyRes)
        assertEquals(ErrorAction.RETRY, content.action)
    }

    @Test
    fun unauthorized_asksToSignInAgain() {
        val content = AppError.Unauthorized.toErrorStateContent()

        assertEquals(R.drawable.oy2_locked, content.mascotResId)
        assertEquals(R.string.state_error_session_expired_title, content.titleRes)
        assertEquals(ErrorAction.SIGN_IN, content.action)
    }

    @Test
    fun unknownValidationAndOther4xx_fallBackToGenericCopy() {
        val errors =
            listOf(
                AppError.Unknown,
                AppError.Validation(fields = mapOf("email" to listOf("bad"))),
                AppError.Server(code = null, status = 400),
            )

        errors.forEach { error ->
            val content = error.toErrorStateContent()
            assertEquals(error.toString(), R.drawable.oy2_confused, content.mascotResId)
            assertEquals(error.toString(), R.string.state_error_unknown_title, content.titleRes)
            assertEquals(error.toString(), R.string.common_error_unknown, content.bodyRes)
            assertEquals(error.toString(), ErrorAction.RETRY, content.action)
        }
    }

    @Test
    fun isConnectivityError_onlyForNetworkAndServerUnreachable() {
        assertTrue(AppError.Network.isConnectivityError)
        assertTrue(AppError.ServerUnreachable.isConnectivityError)
        assertFalse(AppError.Unknown.isConnectivityError)
        assertFalse(AppError.Unauthorized.isConnectivityError)
        assertFalse(AppError.Server(code = null, status = 500).isConnectivityError)
    }
}
