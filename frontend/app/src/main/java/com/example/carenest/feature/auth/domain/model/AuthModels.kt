package com.example.carenest.feature.auth.domain.model

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
    val avatarUrl: String? = null,
    val role: String = "USER"
)
