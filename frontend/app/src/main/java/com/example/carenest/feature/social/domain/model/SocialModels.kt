package com.example.carenest.feature.social.domain.model

enum class GroupPrivacyType {
    PUBLIC,
    PRIVATE,
    SECRET
}

enum class AuthorRole {
    USER,
    DOCTOR,
    ADMIN,
    UNKNOWN
}

enum class ReactionType {
    LIKE,
    LOVE,
    CARE,
    HAHA,
    WOW,
    SAD,
    ANGRY
}

data class Group(
    val id: Long,
    val name: String,
    val description: String? = null,
    val coverImageUrl: String? = null,
    val memberCount: Long = 0,
    val privacyType: GroupPrivacyType = GroupPrivacyType.PUBLIC
)

data class Post(
    val id: Long,
    val groupId: Long,
    val authorId: Long,
    val authorName: String,
    val authorAvatar: String? = null,
    val authorRole: AuthorRole = AuthorRole.UNKNOWN,
    val content: String,
    val imageUrls: List<String> = emptyList(),
    val likeCount: Long = 0,
    val commentCount: Long = 0,
    val createdAt: String? = null
)

data class Comment(
    val id: Long,
    val postId: Long,
    val authorId: Long,
    val authorName: String,
    val content: String,
    val parentCommentId: Long? = null,
    val createdAt: String? = null
)

data class Reaction(
    val postId: Long,
    val userId: Long,
    val reactionType: ReactionType
)

data class PaginatedResponse<T>(
    val items: List<T> = emptyList(),
    val page: Int = 0,
    val limit: Int = 20,
    val totalItems: Long = 0,
    val totalPages: Int = 0,
    val hasNextPage: Boolean = false,
    val nextCursor: String? = null
)
