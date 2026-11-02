package com.toeicspace.android.core.model

import kotlinx.serialization.Serializable

/** Signed-in account, cached locally so the app can open without a network round trip. */
@Serializable
data class AuthUser(
    val id: String,
    val fullName: String,
    val email: String,
    val role: UserRole,
)

/** Mirrors the Identity service roles. Unknown values fall back to [User]. */
enum class UserRole {
    User,
    Teacher,
    Admin,
    ;

    companion object {
        fun from(value: String): UserRole =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: User
    }
}
