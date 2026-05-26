package com.example.carenest.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val familyId: String? = null,
    val message: String? = null
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val phoneNumber: String
)

data class RegisterResponse(
    val message: String
)
