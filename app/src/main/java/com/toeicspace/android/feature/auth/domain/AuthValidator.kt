package com.toeicspace.android.feature.auth.domain

/**
 * Client-side copies of the Identity validators. Invalid input is caught here, so it never costs
 * a request; the backend still has the final say.
 */
enum class FieldError {
    Required,
    InvalidEmail,
    TooLong,
    PasswordTooShort,
    PasswordMissingUppercase,
    PasswordMissingDigit,
    PasswordMissingSpecial,
    PasswordsDoNotMatch,
    PasswordSameAsOld,
    InvalidPhone,
    InvalidOtp,
}

object AuthValidator {
    const val OTP_LENGTH = 6
    private const val EMAIL_MAX_LENGTH = 254
    private const val FULL_NAME_MAX_LENGTH = 200
    private const val PASSWORD_MIN_LENGTH = 8
    private const val PASSWORD_MAX_LENGTH = 128

    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    private val phoneSeparatorRegex = Regex("[\\s().-]+")
    private val vietnameseMobileRegex = Regex("^0[35789]\\d{8}$")
    private val otpRegex = Regex("^\\d{$OTP_LENGTH}$")

    fun validateEmail(email: String): FieldError? {
        val value = email.trim()
        return when {
            value.isEmpty() -> FieldError.Required
            value.length > EMAIL_MAX_LENGTH -> FieldError.TooLong
            !emailRegex.matches(value) -> FieldError.InvalidEmail
            else -> null
        }
    }

    fun validateFullName(fullName: String): FieldError? =
        when {
            fullName.isBlank() -> FieldError.Required
            fullName.trim().length > FULL_NAME_MAX_LENGTH -> FieldError.TooLong
            else -> null
        }

    /** Sign in only checks presence: complexity rules would hint at the password policy. */
    fun validateSignInPassword(password: String): FieldError? =
        when {
            password.isEmpty() -> FieldError.Required
            password.length > PASSWORD_MAX_LENGTH -> FieldError.TooLong
            else -> null
        }

    /** Registration rule: 8–128 characters with an uppercase letter, a digit and a special character. */
    fun validateNewAccountPassword(password: String): FieldError? =
        validateResetPassword(password)
            ?: if (password.none(Char::isDigit)) FieldError.PasswordMissingDigit else null

    /** Password reset rule: 8–128 characters with an uppercase letter and a special character. */
    fun validateResetPassword(password: String): FieldError? =
        when {
            password.isEmpty() -> FieldError.Required
            password.length < PASSWORD_MIN_LENGTH -> FieldError.PasswordTooShort
            password.length > PASSWORD_MAX_LENGTH -> FieldError.TooLong
            password.none { it in 'A'..'Z' } -> FieldError.PasswordMissingUppercase
            password.none {
                !it.isLetterOrDigit() && !it.isWhitespace()
            } -> FieldError.PasswordMissingSpecial
            else -> null
        }

    fun validateConfirmPassword(
        password: String,
        confirmPassword: String,
    ): FieldError? =
        when {
            confirmPassword.isEmpty() -> FieldError.Required
            confirmPassword != password -> FieldError.PasswordsDoNotMatch
            else -> null
        }

    /** Optional; accepts the same formats as the backend (`+84`, `0084`, spaces, dots, dashes). */
    fun validatePhone(phone: String): FieldError? {
        val normalized = normalizePhone(phone) ?: return null
        return if (vietnameseMobileRegex.matches(normalized)) null else FieldError.InvalidPhone
    }

    fun validateOtp(otp: String): FieldError? =
        when {
            otp.isEmpty() -> FieldError.Required
            !otpRegex.matches(otp) -> FieldError.InvalidOtp
            else -> null
        }

    fun normalizeEmail(email: String): String = email.trim()

    fun normalizePhone(phone: String): String? {
        if (phone.isBlank()) return null
        val compact = phoneSeparatorRegex.replace(phone.trim(), "")
        return when {
            compact.startsWith("+84") -> "0" + compact.substring(3)
            compact.startsWith("0084") -> "0" + compact.substring(4)
            else -> compact
        }
    }
}
