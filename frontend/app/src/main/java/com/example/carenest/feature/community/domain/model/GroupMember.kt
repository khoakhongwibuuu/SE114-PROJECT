package com.example.carenest.feature.community.domain.model

data class GroupMember(
    val userId: Long,
    val name: String,
    val role: String, // HOST, MODERATOR, MEMBER
    val joinedAt: String
)

data class UpdateGroupRoleRequest(
    val role: String
)
