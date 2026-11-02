package com.toeicspace.android.feature.auth.data.remote

import com.toeicspace.android.core.network.PublicEndpoint
import com.toeicspace.android.core.network.model.AuthResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

/** Identity endpoints used before the user has a session. Paths include the gateway prefix. */
interface AuthApiService {
    @PublicEndpoint
    @POST("identity/api/auth/login")
    suspend fun login(
        @Body body: LoginRequestDto,
    ): AuthResponseDto

    @PublicEndpoint
    @POST("identity/api/auth/register")
    suspend fun register(
        @Body body: RegisterRequestDto,
    ): ChallengeResponseDto

    @PublicEndpoint
    @POST("identity/api/auth/verify-email")
    suspend fun verifyEmail(
        @Body body: VerifyEmailRequestDto,
    )

    @PublicEndpoint
    @POST("identity/api/auth/resend-verification")
    suspend fun resendVerification(
        @Body body: EmailRequestDto,
    ): ChallengeResponseDto

    @PublicEndpoint
    @POST("identity/api/v1/auth/password-reset/otp")
    suspend fun requestPasswordReset(
        @Body body: EmailRequestDto,
    ): PasswordResetOtpResponseDto

    @PublicEndpoint
    @POST("identity/api/v1/auth/password-reset/verify")
    suspend fun verifyPasswordReset(
        @Body body: VerifyPasswordResetRequestDto,
    ): PasswordResetVerifiedResponseDto

    @PublicEndpoint
    @POST("identity/api/v1/auth/password-reset/confirm")
    suspend fun confirmPasswordReset(
        @Body body: ConfirmPasswordResetRequestDto,
    )
}
