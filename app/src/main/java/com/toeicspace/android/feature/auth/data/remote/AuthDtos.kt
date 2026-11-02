package com.toeicspace.android.feature.auth.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class RegisterRequestDto(
    val fullName: String,
    val email: String,
    val phone: String?,
    val password: String,
    val confirmPassword: String,
)

@Serializable
data class ChallengeResponseDto(
    val userId: String,
    val challengeId: String,
)

@Serializable
data class VerifyEmailRequestDto(
    val challengeId: String,
    val otp: String,
)

@Serializable
data class EmailRequestDto(
    val email: String,
)

@Serializable
data class PasswordResetOtpResponseDto(
    val cooldownSeconds: Int = 60,
)

@Serializable
data class VerifyPasswordResetRequestDto(
    val email: String,
    val otp: String,
)

@Serializable
data class PasswordResetVerifiedResponseDto(
    val expiresAt: String? = null,
    val resetToken: String? = null,
)

@Serializable
data class ConfirmPasswordResetRequestDto(
    val newPassword: String,
    val confirmPassword: String,
    val resetToken: String,
)
