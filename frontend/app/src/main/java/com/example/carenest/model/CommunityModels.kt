package com.example.carenest.model

data class CommunityGroup(
    val id: Long,
    val name: String,
    val description: String? = null,
    val category: String? = null,
    val tags: String? = null,
    val private: Boolean = false,
    val leadDoctorId: Long? = null,
    val leadDoctorName: String? = null,
    val memberCount: Long = 0,
    val joined: Boolean = false,
    val latestMessage: String? = null,
    val latestActivityAt: String? = null
)

data class CommunityGroupPreview(
    val id: Long,
    val name: String,
    val description: String? = null,
    val category: String? = null,
    val tags: String? = null,
    val private: Boolean = false,
    val leadDoctorId: Long? = null,
    val leadDoctorName: String? = null,
    val memberCount: Long = 0,
    val joined: Boolean = false,
    val myRole: String? = null,
    val rules: String? = null
)

data class GroupPost(
    val id: Long,
    val communityGroupId: Long,
    val communityGroupName: String? = null,
    val authorId: Long? = null,
    val authorName: String? = null,
    val authorRole: String? = null,
    val content: String,
    val replyToPostId: Long? = null,
    val imageUrl: String? = null,
    val createdAt: String? = null
)

data class CreateGroupPostRequest(
    val content: String,
    val replyToPostId: Long? = null,
    val imageUrl: String? = null
)

data class PageResponse<T>(
    val content: List<T> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val last: Boolean = true
)
