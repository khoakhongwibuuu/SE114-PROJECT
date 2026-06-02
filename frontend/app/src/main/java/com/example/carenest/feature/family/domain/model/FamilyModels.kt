package com.example.carenest.feature.family.domain.model

data class FamilySummary(
    val id: Long,
    val name: String,
    val memberCount: Int,
    val myRole: String,
    val ownerName: String
)

data class FamilyMemberSummary(
    val id: Long,
    val familyMemberId: Long?,
    val profileId: Long?,
    val userId: Long?,
    val fullName: String,
    val role: String,
    val avatarUrl: String?,
    val age: Int?,
    val healthStatus: String?
)

data class FamilyDetailResponse(
    val id: Long,
    val name: String,
    val ownerId: Long?,
    val ownerUserId: Long?,
    val memberCount: Int,
    val members: List<FamilyMemberSummary>
)

data class FamilyInvitationItem(
    val inviteId: Int,
    val id: Int?,
    val name: String?,
    val senderEmail: String?,
    val receiverEmail: String?,
    val status: String?,
    val createdAt: String?
)

data class FamilyJoinCodeResponse(
    val id: Int,
    val name: String,
    val joinCode: String,
    val joinLink: String,
    val qrCodeBase64: String?,
    val expiresAt: String
)

data class CreateFamilyRequest(
    val name: String
)

data class JoinFamilyByCodeRequest(
    val joinCode: String,
    val role: String?
)

data class InviteMemberRequest(
    val email: String,
    val role: String
)

data class UpdateInvitationRequest(
    val status: String
)

data class UpdateProfileDetailsRequest(
    val fullName: String,
    val dateOfBirth: String?,
    val gender: String?,
    val relationship: String,
    val isChild: Boolean,
    val height: Double?,
    val weight: Double?
)

data class UpdateMedicalInfoRequest(
    val bloodType: String?,
    val allergies: String?,
    val chronicDiseases: String?
)

