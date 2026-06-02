package com.example.carenest.feature.auth.domain.model

enum class AppRole {
    USER,
    DOCTOR,
    ADMIN,
}

data class LoginRequest(
    val email: String,
    val password: String
)


data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val phoneNumber: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserInfo? = null
)

data class UserInfo(
    val id: Long,
    val email: String,
    val fullName: String? = null,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val role: String = "USER",
    val isVerified: Boolean? = null
)

data class UpdateCurrentUserRequest(
    val fullName: String,
    val phone: String? = null,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val avatarUrl: String? = null
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val newPassword: String,
    val confirmPassword: String
)

