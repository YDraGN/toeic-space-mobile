package com.toeicspace.android.feature.auth.domain

import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.model.AuthUser

data class RegistrationForm(
    val fullName: String,
    val email: String,
    val phone: String?,
    val password: String,
    val confirmPassword: String,
)

interface AuthRepository {
    /** Signs in and stores the session; also ends guest mode. */
    suspend fun signIn(
        email: String,
        password: String,
    ): AppResult<AuthUser>

    /** Returns the `challengeId` of the email verification OTP. */
    suspend fun register(form: RegistrationForm): AppResult<String>

    suspend fun verifyEmail(
        challengeId: String,
        otp: String,
    ): AppResult<Unit>

    /** Sends a new verification OTP and returns its `challengeId`. */
    suspend fun resendVerification(email: String): AppResult<String>

    /** Returns how many seconds to wait before another OTP can be requested. */
    suspend fun requestPasswordReset(email: String): AppResult<Int>

    /** Returns the short-lived reset token that authorises the new password. */
    suspend fun verifyPasswordReset(
        email: String,
        otp: String,
    ): AppResult<String>

    suspend fun confirmPasswordReset(
        resetToken: String,
        newPassword: String,
        confirmPassword: String,
    ): AppResult<Unit>

    /**
     * Signed-in only: checks [currentPassword] and emails an OTP. Returns how many seconds to wait
     * before another OTP can be requested.
     */
    suspend fun requestChangePasswordOtp(currentPassword: String): AppResult<Int>

    /** Signed-in only. On success every session of the account ends, this device's included. */
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
        otp: String,
    ): AppResult<Unit>
}
