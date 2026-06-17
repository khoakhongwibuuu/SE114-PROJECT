package com.example.carenest.feature.community.domain.model

data class CreateGroupCreationRequest(
    val name: String,
    val shortDescription: String,
    val detailedPurpose: String,
    val category: String,
    val coverImageUrl: String?,
    val groupType: String,
    val moderationIntent: String?,
    val communityRules: String?
)

data class GroupCreationRequest(
    val id: Long,
    val requesterId: Long,
    val groupType: String,
    val name: String,
    val shortDescription: String,
    val detailedPurpose: String,
    val category: String,
    val coverImageUrl: String?,
    val status: String,
    val rejectionReason: String?,
    val reviewerId: Long?,
    val reviewedAt: String?,
    val createdAt: String?
)
