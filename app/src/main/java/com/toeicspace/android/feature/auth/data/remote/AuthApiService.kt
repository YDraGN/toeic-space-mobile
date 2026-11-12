package com.toeicspace.android.feature.auth.data.remote

import com.toeicspace.android.core.network.PublicEndpoint
import com.toeicspace.android.core.network.model.AuthResponseDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * Identity auth endpoints. Paths include the gateway prefix. Most are called before the user has a
 * session ([PublicEndpoint]); change password needs the signed-in user's access token.
 */
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

    /** Checks the current password and emails an OTP to the signed-in account. */
    @POST("identity/api/v1/auth/change-password/otp")
    suspend fun requestChangePasswordOtp(
        @Body body: RequestChangePasswordOtpRequestDto,
    ): PasswordResetOtpResponseDto

    @PUT("identity/api/v1/auth/change-password")
    suspend fun changePassword(
        @Body body: ChangePasswordRequestDto,
    )
}
