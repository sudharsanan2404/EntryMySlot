package com.entrymyslot.app.screens.auth

internal data class AuthFormErrors(
    val fullName: String? = null,
    val email: String? = null,
    val password: String? = null,
    val confirmPassword: String? = null,
    val otp: String? = null
)

internal fun validateLogin(email: String, password: String) = AuthFormErrors(
    email = email.validateEmail(),
    password = if (password.isBlank()) "Password is required" else null
)

internal fun validateRegistration(
    fullName: String,
    email: String,
    password: String,
    confirmPassword: String
) = AuthFormErrors(
    fullName = when {
        fullName.isBlank() -> "Full name is required"
        fullName.trim().length < 2 -> "Enter at least 2 characters"
        else -> null
    },
    email = email.validateEmail(),
    password = password.validatePassword(),
    confirmPassword = when {
        confirmPassword.isBlank() -> "Please confirm your password"
        password != confirmPassword -> "Passwords do not match"
        else -> null
    }
)

private fun String.validateEmail(): String? = when {
    isBlank() -> "Email address is required"
    !trim().matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) -> "Enter a valid email address"
    else -> null
}

private fun String.validatePassword(): String? = when {
    isBlank() -> "Password is required"
    length < 8 -> "Password must be at least 8 characters"
    !contains(Regex("[A-Z]")) -> "Must contain at least one uppercase letter"
    !contains(Regex("[a-z]")) -> "Must contain at least one lowercase letter"
    !contains(Regex("[0-9]")) -> "Must contain at least one number"
    !contains(Regex("[!@#\$%^&*(),.?\":{}|<>]")) -> "Must contain at least one special character"
    else -> null
}

