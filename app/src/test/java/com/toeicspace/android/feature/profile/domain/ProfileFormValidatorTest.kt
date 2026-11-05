package com.toeicspace.android.feature.profile.domain

import com.toeicspace.android.feature.auth.domain.FieldError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileFormValidatorTest {
    @Test
    fun `full name is required and capped like the backend`() {
        assertEquals(FieldError.Required, ProfileFormValidator.validateFullName("   "))
        assertEquals(FieldError.TooLong, ProfileFormValidator.validateFullName("a".repeat(101)))
        assertNull(ProfileFormValidator.validateFullName("Minh Anh"))
    }

    @Test
    fun `phone may be empty but otherwise must be a vietnamese mobile`() {
        assertNull(ProfileFormValidator.validatePhone(""))
        assertNull(ProfileFormValidator.validatePhone("0905 123 456"))
        assertEquals(FieldError.InvalidPhone, ProfileFormValidator.validatePhone("12345"))
    }

    @Test
    fun `biography is capped at 300 characters`() {
        assertNull(ProfileFormValidator.validateBiography("a".repeat(300)))
        assertEquals(FieldError.TooLong, ProfileFormValidator.validateBiography("a".repeat(301)))
    }

    @Test
    fun `target score options sit inside the backend range`() {
        assert(ProfileFormValidator.TargetScoreOptions.all { it in 10..990 })
    }
}
