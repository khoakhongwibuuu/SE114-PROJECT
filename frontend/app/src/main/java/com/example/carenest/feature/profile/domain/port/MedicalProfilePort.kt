package com.example.carenest.feature.profile.domain.port

import com.example.carenest.model.HealthProfile

interface MedicalProfileDataSource {
    suspend fun getFamilyProfile(profileId: Long): Result<HealthProfile>
    suspend fun updateProfile(
        profileId: Long,
        fullName: String,
        birthday: String?,
        gender: String?,
        relationship: String,
        height: Double?,
        weight: Double?,
        bloodType: String?,
        allergy: String?,
        medicalHistory: String?
    ): Result<Unit>
}
