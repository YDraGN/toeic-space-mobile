package com.toeicspace.android.core.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError

/** What an [ErrorState] shows for an [AppError]: mascot, title, body and which action fits. */
data class ErrorStateContent(
    @param:DrawableRes val mascotResId: Int,
    @param:StringRes val titleRes: Int,
    @param:StringRes val bodyRes: Int,
    val action: ErrorAction,
)

enum class ErrorAction {
    /** Trying again may help: offline, server down or busy, unknown. */
    RETRY,

    /** The session ended; only signing in again helps. */
    SIGN_IN,

    /** Retrying gives the same answer (not found, no access); the screen offers its own way out. */
    NONE,
}

private const val HTTP_FORBIDDEN = 403
private const val HTTP_NOT_FOUND = 404
private const val HTTP_TOO_MANY_REQUESTS = 429
private const val HTTP_SERVER_ERROR = 500

/**
 * Network (no connection on the device) and server errors read differently, with their own
 * mascot, so the learner knows whether to check their Wi-Fi or just wait. Backend `title`/`detail`
 * texts are never shown.
 */
fun AppError.toErrorStateContent(): ErrorStateContent =
    when (this) {
        AppError.Network ->
            ErrorStateContent(
                mascotResId = R.drawable.oy2_offline,
                titleRes = R.string.state_error_network_title,
                bodyRes = R.string.common_error_network,
                action = ErrorAction.RETRY,
            )
        AppError.ServerUnreachable ->
            ErrorStateContent(
                mascotResId = R.drawable.oy2_error,
                titleRes = R.string.state_error_server_unreachable_title,
                bodyRes = R.string.common_error_server_unreachable,
                action = ErrorAction.RETRY,
            )
        AppError.Unauthorized ->
            ErrorStateContent(
                mascotResId = R.drawable.oy2_locked,
                titleRes = R.string.state_error_session_expired_title,
                bodyRes = R.string.state_error_session_expired_body,
                action = ErrorAction.SIGN_IN,
            )
        is AppError.Server -> serverContent()
        is AppError.Validation,
        AppError.Unknown,
        -> unknownContent()
    }

private fun AppError.Server.serverContent(): ErrorStateContent =
    when {
        status == HTTP_NOT_FOUND ->
            ErrorStateContent(
                mascotResId = R.drawable.oy2_404,
                titleRes = R.string.state_error_not_found_title,
                bodyRes = R.string.state_error_not_found_body,
                action = ErrorAction.NONE,
            )
        status == HTTP_FORBIDDEN ->
            ErrorStateContent(
                mascotResId = R.drawable.oy2_denied,
                titleRes = R.string.state_error_forbidden_title,
                bodyRes = R.string.state_error_forbidden_body,
                action = ErrorAction.NONE,
            )
        status == HTTP_TOO_MANY_REQUESTS ->
            ErrorStateContent(
                mascotResId = R.drawable.oy2_tired,
                titleRes = R.string.state_error_too_many_requests_title,
                bodyRes = R.string.auth_error_too_many_requests,
                action = ErrorAction.RETRY,
            )
        status >= HTTP_SERVER_ERROR ->
            ErrorStateContent(
                mascotResId = R.drawable.oy2_error,
                titleRes = R.string.state_error_server_title,
                bodyRes = R.string.common_error_server,
                action = ErrorAction.RETRY,
            )
        else -> unknownContent()
    }

private fun unknownContent(): ErrorStateContent =
    ErrorStateContent(
        mascotResId = R.drawable.oy2_confused,
        titleRes = R.string.state_error_unknown_title,
        bodyRes = R.string.common_error_unknown,
        action = ErrorAction.RETRY,
    )
