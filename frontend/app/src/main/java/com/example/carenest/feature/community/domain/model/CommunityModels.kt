package com.example.carenest.feature.community.domain.model

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

data class Article(
    val id: Long,
    val title: String,
    val content: String,
    val tags: String? = null,
    val imageUrl: String? = null,
    val authorId: Long? = null,
    val authorName: String? = null,
    val authorRole: String? = null,
    val authorAvatarUrl: String? = null,
    val authorSpecialty: String? = null,
    val authorHospitalName: String? = null,
    val authorPrivateGroupId: Long? = null,
    val authorSpecialtyGroupId: Long? = null,
    val createdAt: String? = null,
    val likeCount: Long = 0,
    val commentCount: Long = 0,
    val likedByMe: Boolean = false
)

data class ArticleLikeResponse(
    val articleId: Long,
    val likedByMe: Boolean,
    val likeCount: Long
)

data class ArticleComment(
    val id: Long,
    val articleId: Long,
    val authorId: Long? = null,
    val authorName: String? = null,
    val authorAvatarUrl: String? = null,
    val content: String,
    val createdAt: String? = null
)

data class CreateArticleCommentRequest(
    val content: String
)
