package com.entrymyslot.app.screens.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidationTest {
    @Test
    fun loginDoesNotApplyNewAccountPasswordRulesToExistingCredentials() {
        assertEquals(AuthFormErrors(), validateLogin("member@example.com", "existing-password"))
        assertEquals(AuthFormErrors(), validateLogin("member@example.com", "short"))
    }

    @Test
    fun pastedEmailIsValidatedAfterTrimming() {
        assertEquals(AuthFormErrors(), validateLogin("  member@example.com  ", "ExistingPassword1!"))
        assertNull(validateRegistration("Member Name", " member@example.com ", "Password1!", "Password1!").email)
    }

    @Test
    fun loginStillRequiresEmailAndPassword() {
        val errors = validateLogin("", " ")
        assertNotNull(errors.email)
        assertNotNull(errors.password)
        assertNotNull(validateLogin("invalid-email", "Password1!").email)
    }

    @Test
    fun registrationKeepsItsExistingPasswordAndConfirmationRules() {
        assertNotNull(validateRegistration("Member", "member@example.com", "existing-password", "existing-password").password)
        assertNotNull(validateRegistration("Member", "member@example.com", "Password1!", "DifferentPassword1!").confirmPassword)
        assertEquals(AuthFormErrors(), validateRegistration("Member", "member@example.com", "Password1!", "Password1!"))
    }
}
