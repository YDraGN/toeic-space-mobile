package com.toeicspace.android.testing

import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.model.AuthUser
import com.toeicspace.android.core.model.UserRole
import com.toeicspace.android.feature.auth.domain.AuthRepository
import com.toeicspace.android.feature.auth.domain.RegistrationForm

val TestUser =
    AuthUser(
        id = "user-1",
        fullName = "Oysteic",
        email = "oysteic@toeicspace.vn",
        role = UserRole.User,
    )

/** Returns the configured results and records every call, so tests can check nothing extra was sent. */
class FakeAuthRepository : AuthRepository {
    var signInResult: AppResult<AuthUser> = AppResult.Success(TestUser)
    var registerResult: AppResult<String> = AppResult.Success("challenge-1")
    var verifyEmailResult: AppResult<Unit> = AppResult.Success(Unit)
    var resendVerificationResult: AppResult<String> = AppResult.Success("challenge-2")
    var requestPasswordResetResult: AppResult<Int> = AppResult.Success(60)
    var verifyPasswordResetResult: AppResult<String> = AppResult.Success("reset-token")
    var confirmPasswordResetResult: AppResult<Unit> = AppResult.Success(Unit)

    val calls = mutableListOf<String>()
    var lastRegistration: RegistrationForm? = null
        private set
    var lastResetToken: String? = null
        private set

    override suspend fun signIn(
        email: String,
        password: String,
    ): AppResult<AuthUser> {
        calls += "signIn"
        return signInResult
    }

    override suspend fun register(form: RegistrationForm): AppResult<String> {
        calls += "register"
        lastRegistration = form
        return registerResult
    }

    override suspend fun verifyEmail(
        challengeId: String,
        otp: String,
    ): AppResult<Unit> {
        calls += "verifyEmail:$challengeId:$otp"
        return verifyEmailResult
    }

    override suspend fun resendVerification(email: String): AppResult<String> {
        calls += "resendVerification"
        return resendVerificationResult
    }

    override suspend fun requestPasswordReset(email: String): AppResult<Int> {
        calls += "requestPasswordReset"
        return requestPasswordResetResult
    }

    override suspend fun verifyPasswordReset(
        email: String,
        otp: String,
    ): AppResult<String> {
        calls += "verifyPasswordReset"
        return verifyPasswordResetResult
    }

    override suspend fun confirmPasswordReset(
        resetToken: String,
        newPassword: String,
        confirmPassword: String,
    ): AppResult<Unit> {
        calls += "confirmPasswordReset"
        lastResetToken = resetToken
        return confirmPasswordResetResult
    }
}
