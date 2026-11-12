package com.toeicspace.android.feature.auth.presentation.common

import androidx.annotation.StringRes
import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.feature.auth.domain.FieldError

@StringRes
fun FieldError.messageRes(): Int =
    when (this) {
        FieldError.Required -> R.string.auth_error_required
        FieldError.InvalidEmail -> R.string.auth_error_email_invalid
        FieldError.TooLong -> R.string.auth_error_too_long
        FieldError.PasswordTooShort -> R.string.auth_error_password_too_short
        FieldError.PasswordMissingUppercase -> R.string.auth_error_password_uppercase
        FieldError.PasswordMissingDigit -> R.string.auth_error_password_digit
        FieldError.PasswordMissingSpecial -> R.string.auth_error_password_special
        FieldError.PasswordsDoNotMatch -> R.string.auth_error_password_mismatch
        FieldError.PasswordSameAsOld -> R.string.auth_error_password_same_as_old
        FieldError.InvalidPhone -> R.string.auth_error_phone_invalid
        FieldError.InvalidOtp -> R.string.auth_error_otp_format
    }

/** Maps an Identity error to a message. `title` and `detail` from the backend are never shown. */
@StringRes
fun AppError.messageRes(): Int =
    when (this) {
        AppError.Network -> R.string.common_error_network
        AppError.ServerUnreachable -> R.string.common_error_server_unreachable
        AppError.Unauthorized -> R.string.auth_error_invalid_credentials
        is AppError.Validation -> R.string.common_error_unknown
        AppError.Unknown -> R.string.common_error_unknown
        is AppError.Server -> serverMessageRes()
    }

@StringRes
private fun AppError.Server.serverMessageRes(): Int =
    when (code) {
        "AUTH_INVALID_CREDENTIALS" -> R.string.auth_error_invalid_credentials
        "AUTH_ACCOUNT_LOCKED" -> R.string.auth_error_account_locked
        "AUTH_LOGIN_TEMPORARILY_LOCKED" -> R.string.auth_error_login_temporarily_locked
        "AUTH_EMAIL_ALREADY_VERIFIED" -> R.string.auth_error_email_already_verified
        "AUTH_PASSWORD_SAME_AS_OLD" -> R.string.auth_error_password_same_as_old
        "AUTH_PASSWORDS_DO_NOT_MATCH" -> R.string.auth_error_password_mismatch
        "OTP_INVALID_OR_EXPIRED" -> R.string.auth_error_otp_invalid
        "OTP_RATE_LIMIT_EXCEEDED" -> R.string.auth_error_otp_rate_limited
        "REGISTRATION_COULD_NOT_BE_COMPLETED",
        "USER_EMAIL_ALREADY_EXISTS",
        "USER_PHONE_ALREADY_EXISTS",
        -> R.string.auth_error_registration_failed
        "TOKEN_INVALID",
        "TOKEN_EXPIRED",
        "TOKEN_REVOKED",
        -> R.string.auth_error_reset_session_expired
        else ->
            when {
                status == 429 -> R.string.auth_error_too_many_requests
                status >= 500 -> R.string.common_error_server
                else -> R.string.common_error_unknown
            }
    }

const val EMAIL_NOT_VERIFIED_CODE = "AUTH_EMAIL_NOT_VERIFIED"

val AppError.isEmailNotVerified: Boolean
    get() = this is AppError.Server && code == EMAIL_NOT_VERIFIED_CODE

/** Field errors from the backend, so they can be shown under the matching field. */
fun AppError.fieldMessage(field: String): String? =
    (this as? AppError.Validation)?.fields?.get(field)?.firstOrNull()
