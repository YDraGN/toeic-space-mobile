package com.toeicspace.android.feature.auth.data.repository

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.common.Clock
import com.toeicspace.android.core.common.map
import com.toeicspace.android.core.datastore.SessionTokenStore
import com.toeicspace.android.core.datastore.UserPreferencesDataSource
import com.toeicspace.android.core.model.AuthUser
import com.toeicspace.android.core.network.model.toAuthUser
import com.toeicspace.android.core.network.model.toSessionTokens
import com.toeicspace.android.core.network.safeApiCall
import com.toeicspace.android.feature.auth.data.remote.AuthApiService
import com.toeicspace.android.feature.auth.data.remote.ChangePasswordRequestDto
import com.toeicspace.android.feature.auth.data.remote.ConfirmPasswordResetRequestDto
import com.toeicspace.android.feature.auth.data.remote.EmailRequestDto
import com.toeicspace.android.feature.auth.data.remote.LoginRequestDto
import com.toeicspace.android.feature.auth.data.remote.RegisterRequestDto
import com.toeicspace.android.feature.auth.data.remote.RequestChangePasswordOtpRequestDto
import com.toeicspace.android.feature.auth.data.remote.VerifyEmailRequestDto
import com.toeicspace.android.feature.auth.data.remote.VerifyPasswordResetRequestDto
import com.toeicspace.android.feature.auth.domain.AuthRepository
import com.toeicspace.android.feature.auth.domain.AuthValidator
import com.toeicspace.android.feature.auth.domain.RegistrationForm
import kotlinx.serialization.json.Json
import javax.inject.Inject

internal class AuthRepositoryImpl
    @Inject
    constructor(
        private val authApiService: AuthApiService,
        private val sessionTokenStore: SessionTokenStore,
        private val userPreferencesDataSource: UserPreferencesDataSource,
        private val json: Json,
        private val clock: Clock,
    ) : AuthRepository {
        override suspend fun signIn(
            email: String,
            password: String,
        ): AppResult<AuthUser> {
            val result =
                safeApiCall(json) {
                    authApiService.login(
                        LoginRequestDto(AuthValidator.normalizeEmail(email), password),
                    )
                }
            if (result !is AppResult.Success) return result as AppResult.Failure

            val response = result.data
            // Without a refresh token the session could not survive a restart.
            val tokens =
                response.toSessionTokens(clock.nowMillis())
                    ?: return AppResult.Failure(AppError.Unknown)
            val user = response.user.toAuthUser()
            sessionTokenStore.save(tokens, user)
            userPreferencesDataSource.setOnboardingCompleted(true)
            userPreferencesDataSource.setGuest(false)
            return AppResult.Success(user)
        }

        override suspend fun register(form: RegistrationForm): AppResult<String> =
            safeApiCall(json) {
                authApiService.register(
                    RegisterRequestDto(
                        fullName = form.fullName.trim(),
                        email = AuthValidator.normalizeEmail(form.email),
                        phone = form.phone?.let(AuthValidator::normalizePhone),
                        password = form.password,
                        confirmPassword = form.confirmPassword,
                    ),
                )
            }.map { it.challengeId }

        override suspend fun verifyEmail(
            challengeId: String,
            otp: String,
        ): AppResult<Unit> =
            safeApiCall(json) {
                authApiService.verifyEmail(VerifyEmailRequestDto(challengeId, otp))
            }

        override suspend fun resendVerification(email: String): AppResult<String> =
            safeApiCall(json) {
                authApiService.resendVerification(
                    EmailRequestDto(AuthValidator.normalizeEmail(email)),
                )
            }.map { it.challengeId }

        override suspend fun requestPasswordReset(email: String): AppResult<Int> =
            safeApiCall(json) {
                authApiService.requestPasswordReset(
                    EmailRequestDto(AuthValidator.normalizeEmail(email)),
                )
            }.map { it.cooldownSeconds }

        override suspend fun verifyPasswordReset(
            email: String,
            otp: String,
        ): AppResult<String> {
            val result =
                safeApiCall(json) {
                    authApiService.verifyPasswordReset(
                        VerifyPasswordResetRequestDto(AuthValidator.normalizeEmail(email), otp),
                    )
                }
            return when (result) {
                is AppResult.Failure -> result
                is AppResult.Success ->
                    result.data.resetToken?.let { AppResult.Success(it) }
                        ?: AppResult.Failure(AppError.Unknown)
            }
        }

        override suspend fun confirmPasswordReset(
            resetToken: String,
            newPassword: String,
            confirmPassword: String,
        ): AppResult<Unit> {
            val result =
                safeApiCall(json) {
                    authApiService.confirmPasswordReset(
                        ConfirmPasswordResetRequestDto(newPassword, confirmPassword, resetToken),
                    )
                }
            // A new password revokes every refresh token of the account, including this device's.
            if (result is AppResult.Success) sessionTokenStore.clear()
            return result
        }

        override suspend fun requestChangePasswordOtp(currentPassword: String): AppResult<Int> =
            safeApiCall(json) {
                authApiService.requestChangePasswordOtp(
                    RequestChangePasswordOtpRequestDto(currentPassword),
                )
            }.map { it.cooldownSeconds }

        override suspend fun changePassword(
            currentPassword: String,
            newPassword: String,
            confirmPassword: String,
            otp: String,
        ): AppResult<Unit> {
            val result =
                safeApiCall(json) {
                    authApiService.changePassword(
                        ChangePasswordRequestDto(
                            currentPassword,
                            newPassword,
                            confirmPassword,
                            otp,
                        ),
                    )
                }
            // The backend revokes every refresh token of the account, so this device signs in again.
            if (result is AppResult.Success) sessionTokenStore.clear()
            return result
        }
    }
