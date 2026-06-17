package com.example.carenest.feature.social.data.repository

import com.example.carenest.core.data.network.errorMessage
import com.example.carenest.core.data.network.requireData

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.carenest.feature.social.data.paging.CommentPagingSource
import com.example.carenest.feature.social.data.paging.PostPagingSource
import com.example.carenest.feature.social.data.remote.ReactToPostRequest
import com.example.carenest.feature.social.data.remote.CreateCommentRequest
import com.example.carenest.feature.social.data.remote.SocialApi
import com.example.carenest.feature.social.domain.model.Comment
import com.example.carenest.feature.social.domain.model.Group
import com.example.carenest.feature.social.domain.model.PaginatedResponse
import com.example.carenest.feature.social.domain.model.Post
import com.example.carenest.feature.social.domain.model.Reaction
import com.example.carenest.feature.social.domain.model.ReactionType
import com.example.carenest.feature.social.domain.repository.SocialRepository
import kotlinx.coroutines.flow.Flow

class SocialRepositoryImpl(
    private val api: SocialApi
) : SocialRepository {

    override suspend fun getGroups(
        page: Int,
        limit: Int,
        search: String?
    ): Result<PaginatedResponse<Group>> {
        return runCatching {
            val response = api.getGroups(page = page, limit = limit, search = search?.takeIf { it.isNotBlank() })
            if (!response.isSuccessful) {
                throw IllegalStateException(response.errorMessage("Không thể tải danh sách nhóm"))
            }
            response.requireData("Không thể tải danh sách nhóm", "Không nhận được dữ liệu danh sách nhóm")
        }
    }

    override fun getGroupPosts(
        groupId: Long,
        pageSize: Int
    ): Flow<PagingData<Post>> {
        return Pager(
            config = PagingConfig(pageSize = pageSize, prefetchDistance = pageSize / 2, enablePlaceholders = false)
        ) {
            PostPagingSource(api = api, groupId = groupId)
        }.flow
    }

    override fun getPostComments(
        postId: Long,
        parentCommentId: Long?,
        pageSize: Int
    ): Flow<PagingData<Comment>> {
        return Pager(
            config = PagingConfig(pageSize = pageSize, prefetchDistance = pageSize / 2, enablePlaceholders = false)
        ) {
            CommentPagingSource(api = api, postId = postId, parentCommentId = parentCommentId)
        }.flow
    }

    override suspend fun reactToPost(
        postId: Long,
        reactionType: ReactionType
    ): Result<Reaction> {
        return runCatching {
            val response = api.reactToPost(postId = postId, request = ReactToPostRequest(reactionType = reactionType))
            if (!response.isSuccessful) {
                throw IllegalStateException(response.errorMessage("Không thể cập nhật cảm xúc bài viết"))
            }
            response.requireData("Không thể cập nhật cảm xúc bài viết", "Không nhận được dữ liệu cảm xúc bài viết")
        }
    }

    override suspend fun createComment(
        postId: Long,
        content: String,
        parentCommentId: Long?
    ): Result<Comment> {
        return runCatching {
            val response = api.createComment(
                postId = postId,
                request = CreateCommentRequest(content = content, parentCommentId = parentCommentId)
            )
            if (!response.isSuccessful) {
                throw IllegalStateException(response.errorMessage("Không thể gửi bình luận"))
            }
            response.requireData("Không thể gửi bình luận", "Không nhận được dữ liệu bình luận")
        }
    }
}
