package com.example.carenest.feature.social.domain.repository

import com.example.carenest.feature.social.domain.model.Comment
import com.example.carenest.feature.social.domain.model.Group
import com.example.carenest.feature.social.domain.model.PaginatedResponse
import com.example.carenest.feature.social.domain.model.Post
import com.example.carenest.feature.social.domain.model.Reaction
import com.example.carenest.feature.social.domain.model.ReactionType

interface SocialRepository {
    suspend fun getGroups(
        page: Int = 0,
        limit: Int = 20,
        search: String? = null,
    ): Result<PaginatedResponse<Group>>

    suspend fun getGroupPosts(
        groupId: Long,
        page: Int = 0,
        limit: Int = 20,
        cursor: String? = null,
    ): Result<PaginatedResponse<Post>>

    suspend fun getPostComments(
        postId: Long,
        parentCommentId: Long? = null,
        page: Int = 0,
        limit: Int = 20,
        cursor: String? = null,
    ): Result<PaginatedResponse<Comment>>

    suspend fun reactToPost(
        postId: Long,
        reactionType: ReactionType,
    ): Result<Reaction>
}
