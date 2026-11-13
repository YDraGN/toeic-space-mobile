package com.toeicspace.android.feature.profile.data.remote

import com.toeicspace.android.feature.profile.domain.EnglishLevel
import com.toeicspace.android.feature.profile.domain.Gender
import com.toeicspace.android.feature.profile.domain.UserProfile
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * `UserProfileResult`. Identity sends enums as numbers (no string converter there), but they are
 * read as [JsonPrimitive] so a switch to names on the backend does not break the app.
 */
@Serializable
data class UserProfileDto(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val dateOfBirth: String? = null,
    val gender: JsonPrimitive? = null,
    val biography: String? = null,
    val targetScore: Int? = null,
    val currentLevel: JsonPrimitive? = null,
)

/** `UpdateProfileCommand`. Enums go out as their backend numbers (1-based, same order). */
@Serializable
data class UpdateProfileRequestDto(
    val fullName: String,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val dateOfBirth: String? = null,
    val gender: Int? = null,
    val biography: String? = null,
    val targetScore: Int? = null,
    val currentLevel: Int? = null,
)

/** `UploadAvatarCommand`. The content type is part of the URL's signature. */
@Serializable
data class AvatarUploadRequestDto(
    val contentType: String,
)

@Serializable
data class AvatarUploadResponseDto(
    val uploadUrl: String,
)

internal fun UserProfileDto.toDomain() =
    UserProfile(
        fullName = fullName,
        email = email,
        phone = phone?.takeIf(String::isNotBlank),
        dateOfBirth = dateOfBirth?.toLocalDateOrNull(),
        gender = gender.toEnumOrNull<Gender>(),
        biography = biography?.takeIf(String::isNotBlank),
        targetScore = targetScore,
        currentLevel = currentLevel.toEnumOrNull<EnglishLevel>(),
        avatarUrl = avatarUrl?.takeIf(String::isNotBlank),
    )

internal fun UserProfile.toUpdateRequest(normalizePhone: (String) -> String?) =
    UpdateProfileRequestDto(
        fullName = fullName.trim(),
        phone = phone?.let(normalizePhone),
        avatarUrl = avatarUrl,
        // ISO yyyy-MM-dd, the format System.Text.Json uses for DateOnly.
        dateOfBirth = dateOfBirth?.toString(),
        gender = gender?.toBackendNumber(),
        biography = biography?.trim()?.takeIf(String::isNotEmpty),
        targetScore = targetScore,
        currentLevel = currentLevel?.toBackendNumber(),
    )

private fun String.toLocalDateOrNull(): LocalDate? =
    try {
        // A DateTime ("2004-05-12T00:00:00") would also parse after cutting the time off.
        LocalDate.parse(take(ISO_DATE_LENGTH))
    } catch (_: DateTimeParseException) {
        null
    }

/** Backend enums start at 1 and list their values in the same order as ours. */
private fun Enum<*>.toBackendNumber(): Int = ordinal + 1

private inline fun <reified E : Enum<E>> JsonPrimitive?.toEnumOrNull(): E? {
    if (this == null) return null
    val entries = enumValues<E>()
    intOrNull?.let { number -> return entries.getOrNull(number - 1) }
    return entries.firstOrNull { it.name.equals(content, ignoreCase = true) }
}

private const val ISO_DATE_LENGTH = 10
