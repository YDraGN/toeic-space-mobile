package com.toeicspace.android.core.network.model

import com.toeicspace.android.core.model.AuthUser
import com.toeicspace.android.core.model.UserRole
import kotlinx.serialization.Serializable

/** Login and refresh response. The refresh fields are present because the app is a native client. */
@Serializable
data class AuthResponseDto(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Int,
    val expiresAt: String,
    val user: AuthenticatedUserDto,
    val refreshToken: String? = null,
    val refreshTokenExpiresAt: String? = null,
)

@Serializable
data class AuthenticatedUserDto(
    val id: String,
    val fullName: String,
    val email: String,
    val role: String,
)

@Serializable
data class RefreshTokenRequestDto(
    val refreshToken: String,
)

fun AuthenticatedUserDto.toAuthUser() =
    AuthUser(
        id = id,
        fullName = fullName,
        email = email,
        role = UserRole.from(role),
    )
