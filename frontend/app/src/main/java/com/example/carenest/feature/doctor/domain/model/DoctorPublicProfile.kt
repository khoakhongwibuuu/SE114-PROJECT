package com.example.carenest.feature.doctor.domain.model

data class DoctorPublicProfile(
    val id: Long,
    val fullName: String,
    val avatarUrl: String? = null,
    val specialty: String? = null,
    val hospitalName: String? = null,
    val certificationNumber: String? = null,
    val isVerified: Boolean = false,
    val verifiedAt: String? = null
)
