package com.toeicspace.android.feature.profile.domain

import com.toeicspace.android.feature.auth.domain.AuthValidator
import com.toeicspace.android.feature.auth.domain.FieldError

/**
 * Client-side copy of the Identity `UpdateProfileValidator`, so a bad value never costs a request.
 * The phone rule is shared with sign-up through [AuthValidator].
 */
object ProfileFormValidator {
    const val FULL_NAME_MAX_LENGTH = 100
    const val BIOGRAPHY_MAX_LENGTH = 300

    /** Target scores offered as chips; all inside the backend's 10–990 range. */
    val TargetScoreOptions = listOf(450, 650, 800, 900)

    fun validateFullName(fullName: String): FieldError? =
        when {
            fullName.isBlank() -> FieldError.Required
            fullName.trim().length > FULL_NAME_MAX_LENGTH -> FieldError.TooLong
            else -> null
        }

    /** Optional: an empty phone is fine, anything else must be a Vietnamese mobile number. */
    fun validatePhone(phone: String): FieldError? =
        if (phone.isBlank()) null else AuthValidator.validatePhone(phone)

    fun validateBiography(biography: String): FieldError? =
        if (biography.trim().length > BIOGRAPHY_MAX_LENGTH) FieldError.TooLong else null
}
