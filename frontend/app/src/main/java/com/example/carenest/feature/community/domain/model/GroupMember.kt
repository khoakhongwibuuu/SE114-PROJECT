package com.example.carenest.feature.community.domain.model

data class GroupMember(
    val userId: Long,
    val name: String,
    val role: String, // HOST, MODERATOR, MEMBER
    val joinedAt: String
)

data class UpdateGroupRoleRequest(
    val role: String,
    val reason: String
)

data class GroupGovernanceAuditEntry(
    val id: Long,
    val action: String,
    val actorName: String? = null,
    val actorId: Long? = null,
    val targetUserName: String? = null,
    val targetUserId: Long? = null,
    val previousRole: String? = null,
    val newRole: String? = null,
    val note: String? = null,
    val createdAt: String? = null
)
