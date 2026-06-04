package com.example.carenest.feature.social.domain.repository

import androidx.paging.PagingData
import com.example.carenest.feature.social.domain.model.Comment
import com.example.carenest.feature.social.domain.model.Group
import com.example.carenest.feature.social.domain.model.PaginatedResponse
import com.example.carenest.feature.social.domain.model.Post
import com.example.carenest.feature.social.domain.model.Reaction
import com.example.carenest.feature.social.domain.model.ReactionType
import kotlinx.coroutines.flow.Flow

interface SocialRepository {
    suspend fun getGroups(
        page: Int = 0,
        limit: Int = 20,
        search: String? = null
    ): Result<PaginatedResponse<Group>>

    fun getGroupPosts(
        groupId: Long,
        pageSize: Int = 20
    ): Flow<PagingData<Post>>

    fun getPostComments(
        postId: Long,
        parentCommentId: Long? = null,
        pageSize: Int = 20
    ): Flow<PagingData<Comment>>

    suspend fun reactToPost(
        postId: Long,
        reactionType: ReactionType
    ): Result<Reaction>
}
