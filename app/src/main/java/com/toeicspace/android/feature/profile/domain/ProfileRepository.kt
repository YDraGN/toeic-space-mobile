package com.toeicspace.android.feature.profile.domain

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** Mirrors the Identity service's `UserGender`. */
enum class Gender {
    MALE,
    FEMALE,
    OTHER,
}

/** Mirrors the Identity service's `EnglishLevel`. */
enum class EnglishLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
}

/**
 * The learner's profile, shaped like `UserProfileResult` from `GET /users/me`. Everything past the
 * email is optional and stays null until the learner fills it in.
 */
data class UserProfile(
    val fullName: String,
    val email: String,
    val phone: String? = null,
    val dateOfBirth: LocalDate? = null,
    val gender: Gender? = null,
    val biography: String? = null,
    val targetScore: Int? = null,
    val currentLevel: EnglishLevel? = null,
    val avatarUrl: String? = null,
)

interface ProfileRepository {
    /** The signed-in learner's profile, or null for guests. */
    val profile: Flow<UserProfile?>
}
