package com.toeicspace.android.feature.auth.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidatorTest {
    @Test
    fun `email must be present and well formed`() {
        assertEquals(FieldError.Required, AuthValidator.validateEmail("  "))
        assertEquals(FieldError.InvalidEmail, AuthValidator.validateEmail("oysteic@"))
        assertNull(AuthValidator.validateEmail(" oysteic@toeicspace.vn "))
    }

    @Test
    fun `new account password follows the register rule`() {
        assertEquals(FieldError.PasswordTooShort, AuthValidator.validateNewAccountPassword("Ab1!"))
        assertEquals(
            FieldError.PasswordMissingUppercase,
            AuthValidator.validateNewAccountPassword("abcdefg1!"),
        )
        assertEquals(
            FieldError.PasswordMissingSpecial,
            AuthValidator.validateNewAccountPassword("Abcdefg12"),
        )
        assertEquals(
            FieldError.PasswordMissingDigit,
            AuthValidator.validateNewAccountPassword("Abcdefgh!"),
        )
        assertNull(AuthValidator.validateNewAccountPassword("Abcdefg1!"))
    }

    @Test
    fun `reset password does not require a digit`() {
        assertNull(AuthValidator.validateResetPassword("Abcdefgh!"))
    }

    @Test
    fun `sign in password only needs to be present`() {
        assertEquals(FieldError.Required, AuthValidator.validateSignInPassword(""))
        assertNull(AuthValidator.validateSignInPassword("x"))
    }

    @Test
    fun `phone is optional and accepts backend formats`() {
        assertNull(AuthValidator.validatePhone(""))
        assertNull(AuthValidator.validatePhone("+84 912.345.678"))
        assertNull(AuthValidator.validatePhone("0084-912-345-678"))
        assertEquals(FieldError.InvalidPhone, AuthValidator.validatePhone("0212345678"))
        assertEquals("0912345678", AuthValidator.normalizePhone("+84 912 345 678"))
    }

    @Test
    fun `otp must be six digits`() {
        assertEquals(FieldError.InvalidOtp, AuthValidator.validateOtp("12345"))
        assertEquals(FieldError.InvalidOtp, AuthValidator.validateOtp("12345a"))
        assertNull(AuthValidator.validateOtp("123456"))
    }

    @Test
    fun `confirm password must match`() {
        assertEquals(
            FieldError.PasswordsDoNotMatch,
            AuthValidator.validateConfirmPassword("Abc", "abc"),
        )
        assertNull(AuthValidator.validateConfirmPassword("Abc", "Abc"))
    }
}
