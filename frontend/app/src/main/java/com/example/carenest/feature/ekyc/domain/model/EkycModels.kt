package com.example.carenest.feature.ekyc.domain.model

import com.google.gson.annotations.SerializedName

enum class VerificationStatus {
    @SerializedName("PENDING")
    PENDING,

    @SerializedName("APPROVED")
    APPROVED,

    @SerializedName("REJECTED")
    REJECTED
}

data class SubmitDoctorVerificationRequest(
    val certificationNumber: String,
    val specialty: String,
    val hospitalName: String,
    val documentUrl: String
)

data class DoctorVerificationResponse(
    val id: Long,
    val userId: Long?,
    val userEmail: String?,
    val userFullName: String?,
    val certificationNumber: String,
    val specialty: String,
    val hospitalName: String,
    val documentUrl: String,
    val status: VerificationStatus,
    val rejectionReason: String?,
    val createdAt: String?,
    val updatedAt: String?
)
