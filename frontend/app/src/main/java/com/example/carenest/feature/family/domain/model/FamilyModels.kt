package com.example.carenest.feature.family.domain.model

data class FamilySummary(
    val id: Int,
    val name: String,
    val memberCount: Int,
    val myRole: String,
    val ownerName: String
)

data class FamilyMemberSummary(
    val id: Int,
    val familyMemberId: Int?,
    val profileId: Int?,
    val userId: Int?,
    val fullName: String,
    val role: String,
    val avatarUrl: String?,
    val age: Int?,
    val healthStatus: String?
)

data class FamilyDetailResponse(
    val id: Int,
    val name: String,
    val ownerId: Int?,
    val ownerUserId: Int?,
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
