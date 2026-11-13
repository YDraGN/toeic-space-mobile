package com.toeicspace.android.feature.profile.domain

import com.toeicspace.android.core.common.AppResult
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
    /**
     * The signed-in learner's profile, or null for guests. Until [refresh] succeeds it holds only the
     * name and email cached with the session, so the screens work offline.
     */
    val profile: Flow<UserProfile?>

    /** Loads the full profile from `GET /users/me`. */
    suspend fun refresh(): AppResult<Unit>

    /** Sends every field to `PUT /users/me`, which replaces the whole profile. */
    suspend fun updateProfile(profile: UserProfile): AppResult<UserProfile>

    /** True while [updateAvatar] runs, from any screen, so every avatar can show progress. */
    val isUploadingAvatar: Flow<Boolean>

    /**
     * Shrinks the picked image ([imageUri], a content URI), uploads it to storage and saves the new
     * avatar URL on the profile.
     */
    suspend fun updateAvatar(imageUri: String): AppResult<UserProfile>
}
