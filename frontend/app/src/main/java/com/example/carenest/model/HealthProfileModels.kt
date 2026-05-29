package com.example.carenest.model

data class EmergencyContact(
    val name: String,
    val relation: String,
    val phone: String
)

data class MedicalCondition(
    val name: String,
    val description: String
)

data class HealthProfile(
    val id: Int,
    val name: String,
    val role: String,
    val age: Int?,
    val location: String?,
    val avatarUrl: String?,
    val isVerified: Boolean = true, // for the tick badge
    
    val bloodType: String?,
    val allergies: List<String>,
    
    val height: Float?,
    val weight: Float?,
    val bmi: Float?,
    
    val medicalHistory: List<MedicalCondition>,
    
    val emergencyContact: EmergencyContact?
)

data class RawHealthProfileResponse(
    val id: Int,
    val userId: Int,
    val familyId: Int,
    val fullName: String,
    val dateOfBirth: String?,
    val gender: String,
    val relationship: String,
    val bloodType: String?,
    val allergies: String?,
    val chronicDiseases: String?,
    val notes: String?,
    val avatarUrl: String?,
    val isChild: Boolean,
    val height: Float?,
    val weight: Float?,
    val emergencyContactPhone: String?,
    val createdAt: String,
    val updatedAt: String
)
