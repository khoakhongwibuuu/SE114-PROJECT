package com.example.carenest.core.domain.model

enum class UserRole {
    ROLE_ADMIN,
    ROLE_DOCTOR,
    ROLE_USER;

    companion object {
        fun from(value: String?): UserRole = when (value?.removePrefix("ROLE_")?.uppercase()) {
            "ADMIN" -> ROLE_ADMIN
            "DOCTOR" -> ROLE_DOCTOR
            else -> ROLE_USER
        }
    }
}
